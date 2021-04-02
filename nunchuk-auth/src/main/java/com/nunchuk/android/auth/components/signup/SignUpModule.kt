package com.nunchuk.android.auth.components.signup

import androidx.lifecycle.ViewModel
import com.nunchuk.android.arch.vm.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SignUpModule {

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    fun bindSignUpViewModel(viewModel: SignUpViewModel): ViewModel

}