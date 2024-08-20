package io.dolby.app.features.publish

import com.millicast.devices.source.audio.AudioSource
import com.millicast.devices.source.video.VideoSource
import com.millicast.devices.track.AudioTrack
import com.millicast.devices.track.VideoTrack
import com.millicast.publishers.state.PublisherConnectionState
import io.dolby.app.common.ModelState
import io.dolby.app.common.ViewAction
import io.dolby.app.common.ViewSideEffect
import io.dolby.app.common.ViewUIState
import io.dolby.app.common.ui.ButtonType

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

data class ActiveTracks(val audioTrack: AudioTrack?, val videoTrack: VideoTrack?) {
    fun isEmpty() = audioTrack == null && videoTrack == null
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
    val selectedMode: PublishingOptionMode = PublishingOptionMode.StopMode,
    val publishingState: PublisherConnectionState? = null,
    val audioSource: AudioSource? = null,
    val videoSource: VideoSource? = null,
    val activeTracks: ActiveTracks? = null
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
    val publishingConnectionStateText: String = "Unknown publishing state.",
    val publishingAudioButtonText: String = "Publish Audio",
    val publishingVideoButtonText: String = "Publish Video",
    val publishingAudioVideoButtonText: String = "Publish Audio and Video",
    val publishingAudioButtonType: ButtonType = ButtonType.SECONDARY,
    val publishingVideoButtonType: ButtonType = ButtonType.SECONDARY,
    val publishingAudioVideoButtonType: ButtonType = ButtonType.SECONDARY,
    val shouldShowPreview: Boolean = false,
    val activeVideoTrack: VideoTrack? = null
) : ViewUIState

sealed class PublishSideEffect : ViewSideEffect {
    data class RequiresPermission(val publishingType: PublishingType) : PublishSideEffect()
    data class DeniedPermission(val publishingType: PublishingType) : PublishSideEffect()
    data class PublishingError(val msg: String = "An error occurred while trying to publish.") : PublishSideEffect()
}
