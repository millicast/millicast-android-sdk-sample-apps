package io.dolby.app.features.publish

import androidx.lifecycle.viewModelScope
import io.dolby.app.common.StateViewModel
import io.dolby.app.common.ViewAction
import io.dolby.app.common.ViewSideEffect
import io.dolby.app.common.ViewUIState
import io.dolby.app.common.ui.ButtonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishViewModel(private val stateModel: PublishStateModel = PublishStateModel()) : StateViewModel<PublishAction, PublishViewUiState, ViewSideEffect>() {

    init {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                stateModel.asFlow().collect {
                    updateUiState {
                        it.reduceToUi()
                    }
                }
            }
        }
    }

    override fun initializeState() = PublishViewUiState()

    override fun onUiAction(uiAction: PublishAction) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                when (uiAction) {
                    is PublishAction.StartAudio -> stateModel.updateToChooseAudio()
                    is PublishAction.StartVideo -> stateModel.updateToChooseVideo()
                    is PublishAction.StartAudioVideo -> stateModel.updateToChooseAudioVideo()
                    is PublishAction.Stop -> stateModel.updateToStop()
                }
            }
        }
    }
}
