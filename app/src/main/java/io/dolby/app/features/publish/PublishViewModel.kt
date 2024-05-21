package io.dolby.app.features.publish

import androidx.lifecycle.viewModelScope
import io.dolby.app.common.MultipleStatesViewModel
import io.dolby.app.common.ViewSideEffect
import io.dolby.app.common.ui.ButtonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishViewModel(private val stateModel: PublishModelState = PublishModelState()) : MultipleStatesViewModel<PublishAction, PublishViewUiState, PublishModelState, ViewSideEffect>() {

    override fun initializeState() = PublishModelState()

    override fun initializeUiState() = PublishViewUiState()

    override fun onUiAction(uiAction: PublishAction) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                val publishingChoice = when (uiAction) {
                    is PublishAction.StartAudio -> CurrentPublishingChoice.AUDIO
                    is PublishAction.StartVideo -> CurrentPublishingChoice.VIDEO
                    is PublishAction.StartAudioVideo -> CurrentPublishingChoice.AUDIO_VIDEO
                    is PublishAction.Stop -> CurrentPublishingChoice.NONE
                }
                updateModelStateAndReduceToUi { copy(publishingChoice = publishingChoice) }
            }
        }
    }

    override fun reduceToUi(
        state: PublishModelState,
        uiState: PublishViewUiState
    ): PublishViewUiState {
        return PublishViewUiState(
            isStartEnabled = state.publishingChoice == CurrentPublishingChoice.NONE,
            isStopEnabled = state.publishingChoice != CurrentPublishingChoice.NONE,
            publishingAudioButtonType = getPublishingButtonType(state.publishingChoice == CurrentPublishingChoice.AUDIO),
            publishingVideoButtonType = getPublishingButtonType(state.publishingChoice == CurrentPublishingChoice.VIDEO),
            publishingAudioVideoButtonType = getPublishingButtonType(state.publishingChoice == CurrentPublishingChoice.AUDIO_VIDEO)
        )
    }

    private fun getPublishingButtonType(isPublishing: Boolean) = if (isPublishing) {
        ButtonType.PRIMARY
    } else {
        ButtonType.SECONDARY
    }
}
