package io.dolby.app.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<Action : ViewAction, SideEffect : ViewSideEffect> :
    ViewModel() {
    abstract fun onUiAction(uiAction: Action)

    private val _event: MutableSharedFlow<Action> = MutableSharedFlow()

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    protected fun sendEffect(effect: SideEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    protected fun launchIOScope(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            block.invoke()
        }
    }

    protected fun launchDefaultScope(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            block.invoke(this)
        }
    }
}

interface ViewAction

interface ViewUIState

interface ModelState

interface ViewSideEffect
