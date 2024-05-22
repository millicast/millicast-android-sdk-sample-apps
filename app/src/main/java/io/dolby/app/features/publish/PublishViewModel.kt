package io.dolby.app.features.publish

import androidx.lifecycle.viewModelScope
import io.dolby.app.common.MultipleStatesViewModel
import io.dolby.app.common.ui.ButtonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishViewModel(stateModel: PublishModelState = PublishModelState()) : MultipleStatesViewModel<PublishAction, PublishViewUiState, PublishModelState, PublishSideEffect>() {

    init {
        updateModelStateAndReduceToUi { stateModel }
    }

    override fun initializeState() = PublishModelState()

    override fun initializeUiState() = PublishViewUiState()

    override fun onUiAction(uiAction: PublishAction) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                val publishingChoice = when (uiAction) {
                    is PublishAction.RequestMicrophone -> {
                        sendEffect(PublishSideEffect.RequiresMicrophoneAccess)
                        CurrentPublishingChoice.REQUEST_AUDIO
                    }
                    is PublishAction.RequestCamera -> {
                        sendEffect(PublishSideEffect.RequiresCameraAccess)
                        CurrentPublishingChoice.REQUEST_VIDEO
                    }
                    is PublishAction.RequestMicrophoneAndCamera -> {
                        sendEffect(PublishSideEffect.RequiresCombinedAccess)
                        CurrentPublishingChoice.REQUEST_AUDIO_VIDEO
                    }
                    PublishAction.GrantedAudio -> {
                        if (state.value.publishingChoice == CurrentPublishingChoice.REQUEST_AUDIO) {
                            CurrentPublishingChoice.START_AUDIO
                        } else {
                            null
                        }
                    }
                    PublishAction.GrantedVideo -> {
                        if (state.value.publishingChoice == CurrentPublishingChoice.REQUEST_VIDEO) {
                            CurrentPublishingChoice.START_VIDEO
                        } else {
                            null
                        }
                    }
                    PublishAction.GrantedAudioAndVideo -> {
                        if (state.value.publishingChoice == CurrentPublishingChoice.REQUEST_AUDIO_VIDEO) {
                            CurrentPublishingChoice.START_AUDIO_VIDEO
                        } else {
                            null
                        }
                    }
                    PublishAction.StartAudio -> CurrentPublishingChoice.START_AUDIO
                    PublishAction.StartVideo -> CurrentPublishingChoice.START_VIDEO
                    PublishAction.StartAudioVideo -> CurrentPublishingChoice.START_AUDIO_VIDEO
                    is PublishAction.Stop -> {
                        CurrentPublishingChoice.NONE
                    }
                }
                publishingChoice?.let {
                    updateModelStateAndReduceToUi { copy(publishingChoice = it) }
                }
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
            publishingAudioButtonType = getPublishingButtonType(state.publishingChoice == CurrentPublishingChoice.START_AUDIO),
            publishingVideoButtonType = getPublishingButtonType(state.publishingChoice == CurrentPublishingChoice.START_VIDEO),
            publishingAudioVideoButtonType = getPublishingButtonType(state.publishingChoice == CurrentPublishingChoice.START_AUDIO_VIDEO)
        )
    }

    private fun getPublishingButtonType(isPublishing: Boolean) = if (isPublishing) {
        ButtonType.PRIMARY
    } else {
        ButtonType.SECONDARY
    }
}
