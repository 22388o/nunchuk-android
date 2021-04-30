package com.nunchuk.android.signer.ss.name

sealed class AddSoftwareSignerNameEvent {
    data class SignerNameInputCompletedEvent(val signerName: String) : AddSoftwareSignerNameEvent()
    object SignerNameRequiredEvent : AddSoftwareSignerNameEvent()
}

data class AddSoftwareSignerNameState(val signerName: String = "")