package com.nunchuk.android.core.domain

import com.nunchuk.android.domain.di.IoDispatcher
import com.nunchuk.android.model.SatsCardSlot
import com.nunchuk.android.nativelib.NunchukNativeSdk
import com.nunchuk.android.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetSatsCardSlotBalanceUseCase @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val nunchukNativeSdk: NunchukNativeSdk,
) : UseCase<List<SatsCardSlot>, List<SatsCardSlot>>(dispatcher) {
    override suspend fun execute(parameters: List<SatsCardSlot>): List<SatsCardSlot> {
        return nunchukNativeSdk.loadSatsCardSlotsBalance(parameters)
    }
}