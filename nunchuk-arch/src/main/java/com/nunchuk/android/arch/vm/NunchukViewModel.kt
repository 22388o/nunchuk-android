package com.nunchuk.android.arch.vm

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class NunchukViewModel<State, Event> : ViewModel() {

    private val _state = MutableLiveData<State>()

    private val _event = SingleLiveEvent<Event>()

    protected abstract val initialState: State

    val state: LiveData<State> get() = _state

    val event: LiveData<Event> get() = _event

    @MainThread
    protected fun updateState(updater: State.() -> State) {
        _state.value = updater(_state.value ?: initialState)
    }

    @MainThread
    protected fun setState(state: State) {
        _state.value = state
    }

    protected fun event(event: Event) {
        _event.value = event
    }

    protected fun getState() = _state.value ?: initialState

}