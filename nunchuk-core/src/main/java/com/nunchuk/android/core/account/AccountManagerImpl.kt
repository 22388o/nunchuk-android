package com.nunchuk.android.core.account

import com.nunchuk.android.core.matrix.SessionHolder
import com.nunchuk.android.utils.onException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface AccountManager {
    fun isAccountExisted(): Boolean

    fun isAccountActivated(): Boolean

    fun isStaySignedIn(): Boolean

    fun isLinkedWithMatrix(): Boolean

    fun getAccount(): AccountInfo

    fun storeAccount(accountInfo: AccountInfo)

    fun signOut()
}

@Singleton
internal class AccountManagerImpl @Inject constructor(
    private val accountSharedPref: AccountSharedPref
) : AccountManager {

    override fun isAccountExisted() = accountSharedPref.getAccountInfo().token.isNotBlank()

    override fun isAccountActivated() = accountSharedPref.getAccountInfo().activated

    override fun isStaySignedIn() = accountSharedPref.getAccountInfo().staySignedIn

    override fun isLinkedWithMatrix() = SessionHolder.hasActiveSession() && accountSharedPref.getAccountInfo().chatId.isNotEmpty()

    override fun getAccount() = accountSharedPref.getAccountInfo()

    override fun storeAccount(accountInfo: AccountInfo) {
        accountSharedPref.storeAccountInfo(accountInfo)
    }

    override fun signOut() {
        // TODO call Nunchuk SignOut Api
        GlobalScope.launch {
            signOutMatrix().flowOn(Dispatchers.IO)
                .onException {
                    Timber.e("signOut error ", it)
                }
                .flowOn(Dispatchers.Main)
                .collect {
                    accountSharedPref.clearAccountInfo()
                    SessionHolder.activeSession = null
                    SessionHolder.currentRoom = null
                    Timber.e("signOut success ")
                }
        }
    }

    private fun signOutMatrix() = flow {
        emit(SessionHolder.clearActiveSession())
    }

}