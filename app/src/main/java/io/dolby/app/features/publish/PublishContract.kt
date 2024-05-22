package io.dolby.app.features.publish

import io.dolby.app.common.ModelState
import io.dolby.app.common.ViewAction
import io.dolby.app.common.ViewSideEffect
import io.dolby.app.common.ViewUIState
import io.dolby.app.common.ui.ButtonType

// class PublishStateModel : ModelState {
//
//    private val _state = MutableStateFlow(PublishState())
//    private val state = _state.asStateFlow()
//
//    fun asFlow() = state
//
//    suspend fun updateToChooseAudio() {
//        _state.emit(_state.value.copy(publishingChoice = PublishState.CurrentPublishingChoice.AUDIO))
//    }
//
//    suspend fun updateToChooseVideo() {
//        _state.emit(_state.value.copy(publishingChoice = PublishState.CurrentPublishingChoice.VIDEO))
//    }
//
//    suspend fun updateToChooseAudioVideo() {
//        _state.emit(_state.value.copy(publishingChoice = PublishState.CurrentPublishingChoice.AUDIO_VIDEO))
//    }
//
//    suspend fun updateToStop() {
//        _state.emit(_state.value.copy(publishingChoice = PublishState.CurrentPublishingChoice.NONE))
//    }
//
//    data class PublishState(internal val publishingChoice: CurrentPublishingChoice = CurrentPublishingChoice.NONE): StateModel<PublishViewUiState> {
//
//        override fun reduceToUi(): PublishViewUiState {
//            return PublishViewUiState(
//                isStartEnabled = publishingChoice == CurrentPublishingChoice.NONE,
//                isStopEnabled = publishingChoice != CurrentPublishingChoice.NONE,
//                publishingAudioButtonType = getPublishingButtonType(publishingChoice == CurrentPublishingChoice.AUDIO),
//                publishingVideoButtonType = getPublishingButtonType(publishingChoice == CurrentPublishingChoice.VIDEO),
//                publishingAudioVideoButtonType = getPublishingButtonType(publishingChoice == CurrentPublishingChoice.AUDIO_VIDEO)
//            )
//        }
//

//
//
//    }
// }

enum class CurrentPublishingChoice { REQUEST_AUDIO, REQUEST_VIDEO, REQUEST_AUDIO_VIDEO, START_AUDIO, START_VIDEO, START_AUDIO_VIDEO, NONE }

data class PublishModelState(internal val publishingChoice: CurrentPublishingChoice = CurrentPublishingChoice.NONE) : ModelState

sealed class PublishAction : ViewAction {
    data object RequestMicrophone : PublishAction()
    data object RequestCamera : PublishAction()
    data object RequestMicrophoneAndCamera : PublishAction()
    data object GrantedAudio : PublishAction()
    data object GrantedVideo : PublishAction()
    data object GrantedAudioAndVideo : PublishAction()
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

sealed class PublishSideEffect : ViewSideEffect {
    data object RequiresMicrophoneAccess : PublishSideEffect()
    data object RequiresCameraAccess : PublishSideEffect()
    data object RequiresCombinedAccess : PublishSideEffect()
}
