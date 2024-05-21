package io.dolby.app.common

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class MultipleStatesViewModel<Action : ViewAction, UiState : ViewUIState, State : ModelState, SideEffect : ViewSideEffect> :
    SingleStateViewModel<Action, UiState, SideEffect>() {
    abstract fun initializeState(): State
    open fun reduceToUi(state: State, uiState: UiState): UiState = initializeUiState()

    private val initialState: State by lazy { initializeState() }

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    protected fun updateModelState(block: State.() -> State) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                _state.update(block)
            }
        }
    }

    protected fun updateModelStateAndReduceToUi(block: State.() -> State) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                updateUiState {
                    reduceToUi(_state.updateAndGet(block), uiState.value)
                }
            }
        }
    }
}
