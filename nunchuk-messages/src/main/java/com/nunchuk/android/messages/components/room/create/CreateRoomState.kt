package com.nunchuk.android.messages.components.room.create

import com.nunchuk.android.model.Contact

data class CreateRoomState(val receipts: List<Contact>, val suggestions: List<Contact>) {

    companion object {
        fun empty() = CreateRoomState(ArrayList(), ArrayList())
    }

}

sealed class CreateRoomEvent {
    object NoContactsEvent : CreateRoomEvent()
    data class CreateRoomSuccessEvent(val roomId: String) : CreateRoomEvent()
    data class CreateRoomErrorEvent(val message: String) : CreateRoomEvent()
}