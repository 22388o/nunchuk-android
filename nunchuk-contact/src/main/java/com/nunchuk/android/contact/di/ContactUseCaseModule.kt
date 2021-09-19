package com.nunchuk.android.contact.di

import com.nunchuk.android.contact.usecase.*
import com.nunchuk.android.share.GetContactsUseCase
import dagger.Binds
import dagger.Module

@Module
internal interface ContactUseCaseModule {

    @Binds
    fun bindSearchContactUseCase(useCase: SearchContactUseCaseImpl): SearchContactUseCase

    @Binds
    fun bindAddContactUseCase(useCase: AddContactUseCaseImpl): AddContactUseCase

    @Binds
    fun bindAutoCompleteSearchUseCase(useCase: AutoCompleteSearchUseCaseImpl): AutoCompleteSearchUseCase

    @Binds
    fun bindGetReceivedContactsUseCase(useCase: GetReceivedContactsUseCaseImpl): GetReceivedContactsUseCase

    @Binds
    fun bindGetSentContactsUseCase(useCase: GetSentContactsUseCaseImpl): GetSentContactsUseCase

    @Binds
    fun bindGetContactsUseCase(useCase: GetContactsUseCaseImpl): GetContactsUseCase

    @Binds
    fun bindUpdateAvatarUseCase(useCase: UpdateAvatarUseCaseImpl): UpdateAvatarUseCase

}