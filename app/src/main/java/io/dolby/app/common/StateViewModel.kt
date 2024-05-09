package io.dolby.app.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class StateViewModel<Action : ViewAction, UiState : ViewUIState, SideEffect : ViewSideEffect> :
    ViewModel() {
    abstract fun initializeState(): UiState
    abstract fun onUiAction(uiAction: Action)

    private val initialState: UiState by lazy { initializeState() }

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _event: MutableSharedFlow<Action> = MutableSharedFlow()

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()

    protected fun updateUiState(block: UiState.() -> UiState) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _uiState.update(block)
            }
        }
    }
    protected fun sendEffect(effect: SideEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}

interface ViewAction

interface ViewUIState

interface ViewSideEffect
