package io.dolby.app.features.publish

import io.dolby.app.common.ViewAction
import io.dolby.app.common.ViewUIState
import io.dolby.app.common.ui.ButtonType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PublishStateModel {

    private val _state = MutableStateFlow(PublishState())
    private val state = _state.asStateFlow()

    fun asFlow() = state

    suspend fun updateToChooseAudio() {
        _state.emit(_state.value.copy(publishingChoice = PublishState.CurrentPublishingChoice.AUDIO))
    }

    suspend fun updateToChooseVideo() {
        _state.emit(_state.value.copy(publishingChoice = PublishState.CurrentPublishingChoice.VIDEO))
    }

    suspend fun updateToChooseAudioVideo() {
        _state.emit(_state.value.copy(publishingChoice = PublishState.CurrentPublishingChoice.AUDIO_VIDEO))
    }

    suspend fun updateToStop() {
        _state.emit(_state.value.copy(publishingChoice = PublishState.CurrentPublishingChoice.NONE))
    }

    data class PublishState(internal val publishingChoice: CurrentPublishingChoice = CurrentPublishingChoice.NONE) {

        fun reduceToUi(): PublishViewUiState {
            return PublishViewUiState(
                isStartEnabled = publishingChoice == CurrentPublishingChoice.NONE,
                isStopEnabled = publishingChoice != CurrentPublishingChoice.NONE,
                publishingAudioButtonType = getPublishingButtonType(publishingChoice == CurrentPublishingChoice.AUDIO),
                publishingVideoButtonType = getPublishingButtonType(publishingChoice == CurrentPublishingChoice.VIDEO),
                publishingAudioVideoButtonType = getPublishingButtonType(publishingChoice == CurrentPublishingChoice.AUDIO_VIDEO)
            )
        }

        private fun getPublishingButtonType(isPublishing: Boolean) = if (isPublishing) {
            ButtonType.PRIMARY
        } else {
            ButtonType.SECONDARY
        }

        enum class CurrentPublishingChoice { AUDIO, VIDEO, AUDIO_VIDEO, NONE }
    }
}

sealed class PublishAction : ViewAction {
    data object StartAudio : PublishAction()
    data object StartVideo : PublishAction()
    data object StartAudioVideo : PublishAction()
    data object Stop : PublishAction()
}

data class PublishViewUiState(
    val isStartEnabled: Boolean = true,
    val isStopEnabled: Boolean = false,
    val publishingAudioButtonType: ButtonType = ButtonType.SECONDARY,
    val publishingVideoButtonType: ButtonType = ButtonType.SECONDARY,
    val publishingAudioVideoButtonType: ButtonType = ButtonType.SECONDARY
) : ViewUIState

