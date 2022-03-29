package com.nunchuk.android.messages.components.list

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.ext.defaultSchedulers
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.matrix.SessionHolder
import com.nunchuk.android.core.matrix.roomSummariesFlow
import com.nunchuk.android.core.network.UnauthorizedEventBus
import com.nunchuk.android.messages.usecase.message.LeaveRoomUseCase
import com.nunchuk.android.messages.util.sortByLastMessage
import com.nunchuk.android.model.RoomWallet
import com.nunchuk.android.usecase.GetAllRoomWalletsUseCase
import com.nunchuk.android.utils.CrashlyticsReporter
import com.nunchuk.android.utils.onException
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.failure.GlobalError
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RoomsViewModel @Inject constructor(
    private val getAllRoomWalletsUseCase: GetAllRoomWalletsUseCase,
    private val leaveRoomUseCase: LeaveRoomUseCase
) : NunchukViewModel<RoomsState, RoomsEvent>() {

    override val initialState = RoomsState.empty()

    fun init() {
        SessionHolder.activeSession?.let(::subscribeEvent)
    }

    fun handleMatrixSignedIn(session: Session) {
        listenRoomSummaries(session)
    }

    private fun subscribeEvent(session: Session) {
        addListener(session)
        listenRoomSummaries(session)
    }

    private fun listenRoomSummaries(session: Session) {
        session.roomSummariesFlow()
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()
            .onStart { event(RoomsEvent.LoadingEvent(true)) }
            .onEach {
                Timber.tag(TAG).d("listenRoomSummaries($it)")
                retrieveMessages()
            }
            .onCompletion { event(RoomsEvent.LoadingEvent(false)) }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }

    private fun addListener(session: Session) {
        session.addListener(object : Session.Listener {
            override fun onNewInvitedRoom(session: Session, roomId: String) {
                session.getRoom(roomId)?.let(::joinRoom)
            }

            override fun onGlobalError(session: Session, globalError: GlobalError) {
                if (globalError is GlobalError.InvalidToken || globalError === GlobalError.ExpiredAccount) {
                    UnauthorizedEventBus.instance().publish()
                }
            }

            override fun onSessionStarted(session: Session) {
                Timber.d("onSessionStarted($session)")
            }

            override fun onSessionStopped(session: Session) {
                Timber.d("onSessionStopped($session)")
            }
        })
    }

    private fun joinRoom(room: Room) {
        viewModelScope.launch {
            try {
                room.join()
            } catch (e: Throwable) {
                CrashlyticsReporter.recordException(e)
            }
        }
    }

    fun retrieveMessages() {
        if (SessionHolder.hasActiveSession()) {
            SessionHolder.activeSession!!.roomSummariesFlow()
                .zip(getAllRoomWalletsUseCase.execute()) { rooms, wallets -> rooms to wallets }
                .flowOn(Dispatchers.IO)
                .onException { onRetrieveMessageError(it) }
                .flowOn(Dispatchers.Main)
                .onEach { onRetrieveMessageSuccess(it) }
                .distinctUntilChanged()
                .launchIn(viewModelScope)
        }
    }

    private fun onRetrieveMessageError(t: Throwable) {
        event(RoomsEvent.LoadingEvent(false))
        updateState { copy(rooms = emptyList()) }
        CrashlyticsReporter.recordException(t)
    }

    private fun onRetrieveMessageSuccess(p: Pair<List<RoomSummary>, List<RoomWallet>>) {
        event(RoomsEvent.LoadingEvent(false))
        updateState {
            copy(
                rooms = p.first.sortByLastMessage(),
                roomWallets = p.second
            )
        }
    }

    fun removeRoom(roomSummary: RoomSummary) {
        viewModelScope.launch {
            event(RoomsEvent.LoadingEvent(true))
            val room = getRoom(roomSummary)
            if (room != null) {
                handleRemoveRoom(room)
            } else {
                event(RoomsEvent.LoadingEvent(false))
            }
        }
    }

    private fun handleRemoveRoom(room: Room) {
        viewModelScope.launch {
            leaveRoomUseCase.execute(room)
                .flowOn(Dispatchers.IO)
                .onException { RoomsEvent.LoadingEvent(false) }
                .collect { awaitAndRetrieveMessages() }
        }
    }

    private fun awaitAndRetrieveMessages() {
        Completable.fromCallable {}
            .delay(DELAY_IN_SECONDS, TimeUnit.SECONDS)
            .defaultSchedulers()
            .doAfterTerminate { event(RoomsEvent.LoadingEvent(false)) }
            .subscribe(::retrieveMessages, CrashlyticsReporter::recordException)
            .addToDisposables()
    }

    private fun getRoom(roomSummary: RoomSummary) = SessionHolder.activeSession?.getRoom(roomSummary.roomId)

    companion object {
        private const val TAG = "MainActivityViewModel"
        private const val DELAY_IN_SECONDS = 2L
    }

}