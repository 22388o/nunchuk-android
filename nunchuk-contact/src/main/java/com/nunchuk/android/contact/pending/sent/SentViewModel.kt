package com.nunchuk.android.contact.pending.sent

import com.nunchuk.android.arch.ext.defaultSchedulers
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.contact.pending.sent.SentEvent.LoadingEvent
import com.nunchuk.android.contact.repository.ContactsRepository
import com.nunchuk.android.contact.usecase.GetSentContactsUseCase
import com.nunchuk.android.model.SentContact
import javax.inject.Inject

// FIXME
class SentViewModel @Inject constructor(
    private val getSentContactsUseCase: GetSentContactsUseCase,
    private val contactsRepository: ContactsRepository
) : NunchukViewModel<SentState, SentEvent>() {

    override val initialState: SentState = SentState()

    fun retrieveData() {
        getSentContactsUseCase.execute()
            .defaultSchedulers()
            .subscribe(
                { updateState { copy(contacts = it) } },
                { updateState { copy(contacts = emptyList()) } }
            )
            .addToDisposables()
    }

    fun handleWithDraw(contact: SentContact) {
        event(LoadingEvent(true))
        contactsRepository.cancelContact(contact.contact.id)
            .defaultSchedulers()
            .doAfterTerminate { event(LoadingEvent(false)) }
            .subscribe(::retrieveData) {}
            .addToDisposables()
    }

}