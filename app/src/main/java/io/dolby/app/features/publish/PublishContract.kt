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

enum class PublishingType { AUDIO, VIDEO, AUDIO_VIDEO }
enum class PermissionStatus {
    UNKNOWN,
    GRANTED,
    SHOW_RATIONALE,
    DENIED;

    companion object {
        fun fromHasPermission(hasPermission: Boolean): PermissionStatus {
            return if (hasPermission) GRANTED else DENIED
        }
        fun fromHasPermissionAndShowRationale(hasPermission: Boolean, shouldShowRationale: Boolean): PermissionStatus {
            return if (hasPermission) {
                GRANTED
            } else if (shouldShowRationale) {
                SHOW_RATIONALE
            } else {
                DENIED
            }
        }
    }
}

sealed class PublishingOptionMode {
    data class PublishingMode(
        val type: PublishingType,
        val permissionStatus: PermissionStatus = PermissionStatus.UNKNOWN
    ) : PublishingOptionMode()

    data object StopMode : PublishingOptionMode()
}

data class PublishModelState(
    val audioPublishingMode: PublishingOptionMode.PublishingMode = PublishingOptionMode.PublishingMode(
        type = PublishingType.AUDIO
    ),
    val videoPublishingMode: PublishingOptionMode.PublishingMode = PublishingOptionMode.PublishingMode(
        type = PublishingType.VIDEO
    ),
    val audioVideoPublishingMode: PublishingOptionMode.PublishingMode = PublishingOptionMode.PublishingMode(
        type = PublishingType.AUDIO_VIDEO
    ),
    val selectedMode: PublishingOptionMode = PublishingOptionMode.StopMode
) : ModelState {
    fun isAudioSelected(): Boolean =
        (selectedMode as? PublishingOptionMode.PublishingMode)?.type == PublishingType.AUDIO

    fun isVideoSelected(): Boolean =
        (selectedMode as? PublishingOptionMode.PublishingMode)?.type == PublishingType.VIDEO

    fun isAudioVideoSelected(): Boolean =
        (selectedMode as? PublishingOptionMode.PublishingMode)?.type == PublishingType.AUDIO_VIDEO

    fun isStopSelected(): Boolean = selectedMode is PublishingOptionMode.StopMode
}

sealed class PublishAction : ViewAction {
    sealed class SelectedButton : PublishAction() {
        data class PublishButton(val type: PublishingType, val permissionStatus: PermissionStatus) : PublishAction()
        data object Stop : PublishAction()
    }

    data class PermissionUpdate(val type: PublishingType, val permissionStatus: PermissionStatus) :
        PublishAction()
}

data class PublishViewUiState(
    val isStartEnabled: Boolean = true,
    val isStopEnabled: Boolean = false,
    val publishingAudioButtonText: String = "Publish Audio",
    val publishingVideoButtonText: String = "Publish Video",
    val publishingAudioVideoButtonText: String = "Publish Audio and Video",
    val publishingAudioButtonType: ButtonType = ButtonType.SECONDARY,
    val publishingVideoButtonType: ButtonType = ButtonType.SECONDARY,
    val publishingAudioVideoButtonType: ButtonType = ButtonType.SECONDARY
) : ViewUIState

sealed class PublishSideEffect : ViewSideEffect {
    data class RequiresPermission(val publishingType: PublishingType) : PublishSideEffect()
    data class DeniedPermission(val publishingType: PublishingType) : PublishSideEffect()
}
