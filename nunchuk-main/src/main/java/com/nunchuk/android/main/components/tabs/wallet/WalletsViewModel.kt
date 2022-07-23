package com.nunchuk.android.main.components.tabs.wallet

import android.nfc.tech.IsoDep
import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.domain.BaseNfcUseCase
import com.nunchuk.android.core.domain.GetAppSettingUseCase
import com.nunchuk.android.core.domain.GetSatsCardStatusUseCase
import com.nunchuk.android.core.signer.SignerModel
import com.nunchuk.android.main.components.tabs.wallet.WalletsEvent.*
import com.nunchuk.android.usecase.GetCompoundSignersUseCase
import com.nunchuk.android.usecase.GetWalletsUseCase
import com.nunchuk.android.utils.onException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
internal class WalletsViewModel @Inject constructor(
    private val getCompoundSignersUseCase: GetCompoundSignersUseCase,
    private val getWalletsUseCase: GetWalletsUseCase,
    private val getAppSettingUseCase: GetAppSettingUseCase,
    private val getSatsCardStatusUseCase: GetSatsCardStatusUseCase
) : NunchukViewModel<WalletsState, WalletsEvent>() {

    private var isRetrievingData = AtomicBoolean(false)

    override val initialState = WalletsState()

    fun getAppSettings() {
        viewModelScope.launch {
            getAppSettingUseCase.execute()
                .flowOn(Dispatchers.IO)
                .onException { }
                .flowOn(Dispatchers.Main)
                .collect {
                    updateState {
                        copy(chain = it.chain)
                    }
                }
        }
    }

    fun retrieveData() {
        if (isRetrievingData.get()) return
        isRetrievingData.set(true)
        viewModelScope.launch {
            getCompoundSignersUseCase.execute()
                .zip(getWalletsUseCase.execute()) { p, wallets ->
                    Triple(p.first, p.second, wallets)
                }
                .flowOn(Dispatchers.IO)
                .onException {
                    updateState { copy(signers = emptyList(), masterSigners = emptyList()) }
                }
                .flowOn(Dispatchers.Main)
                .onCompletion {
                    event(Loading(false))
                    isRetrievingData.set(false)
                }
                .collect { updateState { copy(masterSigners = it.first, signers = it.second, wallets = it.third) } }
        }
    }

    fun handleAddSignerOrWallet() {
        if (hasSigner()) {
            handleAddWallet()
        } else {
            handleAddSigner()
        }
    }

    fun handleAddSigner() {
        event(ShowSignerIntroEvent)
    }

    fun handleAddWallet() {
        event(AddWalletEvent)
    }

    fun isInWallet(signer: SignerModel): Boolean {
        return getState().wallets
            .any {
                it.wallet.signers.any { singleSigner -> singleSigner.masterSignerId == signer.id }
            }
    }

    fun hasSigner() = getState().signers.isNotEmpty() || getState().masterSigners.isNotEmpty()

    fun hasWallet() = getState().wallets.isNotEmpty()

    fun getSatsCardStatus(isoDep: IsoDep?) {
        isoDep ?: return
        viewModelScope.launch {
            setEvent(NfcLoading(true))
            val result = getSatsCardStatusUseCase(BaseNfcUseCase.Data(isoDep))
            setEvent(NfcLoading(false))
            if (result.isSuccess) {
                val status = result.getOrThrow()
                if (status.isNeedSetup) {
                    setEvent(NeedSetupSatsCard)
                } else if (status.isUsedUp) {
                    setEvent(SatsCardUsedUp(status.numberOfSlot))
                } else {
                    setEvent(GoToSatsCardScreen(status))
                }
            } else {
                val message = result.exceptionOrNull()?.message.orEmpty()
                if (message.isNotEmpty()) {
                    setEvent(ShowErrorEvent(message))
                }
            }
        }
    }
}