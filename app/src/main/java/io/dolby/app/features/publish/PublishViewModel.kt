package io.dolby.app.features.publish

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.millicast.Core
import com.millicast.Media
import com.millicast.Media.audioSources
import com.millicast.Media.videoSources
import com.millicast.Publisher
import com.millicast.devices.source.audio.MicrophoneAudioSource
import com.millicast.devices.source.video.CameraVideoSource
import com.millicast.devices.track.AudioTrack
import com.millicast.devices.track.VideoTrack
import com.millicast.publishers.Credential
import com.millicast.publishers.Option
import com.millicast.publishers.state.PublisherConnectionState
import com.millicast.utils.Logger
import io.dolby.app.common.MultipleStatesViewModel
import io.dolby.app.common.ui.ButtonType
import io.dolby.millicast.androidsdk.sampleapps.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishViewModel(
    stateModel: PublishModelState = PublishModelState(),
    mockablePublisher: Publisher? = null
) :
    MultipleStatesViewModel<PublishAction, PublishViewUiState, PublishModelState, PublishSideEffect>() {

    private val publisher: Publisher = mockablePublisher ?: Core.createPublisher()

    init {
        updateModelStateAndReduceToUi { stateModel.copy(publishingState = publisher.currentState.connectionState) }
        Logger.setLoggerListener { msg, _ -> Log.d(TAG, msg) }
        viewModelScope.launch {
            publisher.state.map { it.connectionState }.distinctUntilChanged().collect {
                updateModelStateAndReduceToUi { copy(publishingState = it) }
                if (it == PublisherConnectionState.Connected) {
                    startPublishing()
                }
            }
        }
    }

    // region UI handling

    override fun initializeState() = PublishModelState()

    override fun initializeUiState() = PublishViewUiState()

    override fun onUiAction(uiAction: PublishAction) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                when (uiAction) {
                    is PublishAction.PermissionUpdate -> {
                        when (uiAction.type) {
                            PublishingType.AUDIO -> handleAudioPermissionChange(uiAction.permissionStatus)
                            PublishingType.VIDEO -> handleVideoPermissionChange(uiAction.permissionStatus)
                            PublishingType.AUDIO_VIDEO -> handleAudioVideoPermissionChange(uiAction.permissionStatus)
                        }
                    }
                    is PublishAction.SelectedButton.PublishButton -> handlePublishingSelection(
                        uiAction
                    )
                    PublishAction.SelectedButton.Stop -> {
                        updateModelStateAndReduceToUi {
                            copy(selectedMode = PublishingOptionMode.StopMode)
                        }
                        stopPublishing()
                    }
                }
            }
        }
    }

    private fun handleAudioPermissionChange(permissionStatus: PermissionStatus) {
        updateModelStateAndReduceToUi {
            val mode =
                audioPublishingMode.copy(permissionStatus = permissionStatus)
            if (isAudioSelected() && permissionStatus != PermissionStatus.GRANTED) {
                copy(audioPublishingMode = mode, selectedMode = PublishingOptionMode.StopMode)
            } else {
                copy(audioPublishingMode = mode)
            }
        }
        if (state.value.isAudioSelected()) {
            if (permissionStatus == PermissionStatus.GRANTED) {
                connectPublisher(PublishingType.AUDIO)
            } else {
                stopPublishing()
                if (permissionStatus == PermissionStatus.DENIED) {
                    sendEffect(
                        PublishSideEffect.DeniedPermission(
                            PublishingType.AUDIO
                        )
                    )
                }
            }
        }
    }

    private fun handleVideoPermissionChange(permissionStatus: PermissionStatus) {
        updateModelStateAndReduceToUi {
            val mode =
                videoPublishingMode.copy(permissionStatus = permissionStatus)
            if (isVideoSelected() && permissionStatus != PermissionStatus.GRANTED) {
                copy(videoPublishingMode = mode, selectedMode = PublishingOptionMode.StopMode)
            } else {
                copy(videoPublishingMode = mode)
            }
        }
        if (state.value.isVideoSelected()) {
            if (permissionStatus == PermissionStatus.GRANTED) {
                connectPublisher(PublishingType.VIDEO)
            } else {
                stopPublishing()
                if (permissionStatus == PermissionStatus.DENIED) {
                    sendEffect(
                        PublishSideEffect.DeniedPermission(
                            PublishingType.VIDEO
                        )
                    )
                }
            }
        }
    }

    private fun handleAudioVideoPermissionChange(permissionStatus: PermissionStatus) {
        updateModelStateAndReduceToUi {
            val mode =
                audioVideoPublishingMode.copy(permissionStatus = permissionStatus)
            if (isVideoSelected() && permissionStatus != PermissionStatus.GRANTED) {
                copy(audioVideoPublishingMode = mode, selectedMode = PublishingOptionMode.StopMode)
            } else {
                copy(audioVideoPublishingMode = mode)
            }
        }
        if (state.value.isAudioVideoSelected()) {
            if (permissionStatus == PermissionStatus.GRANTED) {
                connectPublisher(PublishingType.AUDIO_VIDEO)
            } else {
                stopPublishing()
                if (permissionStatus == PermissionStatus.DENIED) {
                    sendEffect(
                        PublishSideEffect.DeniedPermission(
                            PublishingType.AUDIO_VIDEO
                        )
                    )
                }
            }
        }
    }

    private fun handlePublishingSelection(selectAction: PublishAction.SelectedButton.PublishButton) {
        updateModelStateAndReduceToUi {
            copy(
                selectedMode = PublishingOptionMode.PublishingMode(
                    type = selectAction.type,
                    permissionStatus = selectAction.permissionStatus
                )
            )
        }
        if (selectAction.permissionStatus == PermissionStatus.GRANTED) {
            connectPublisher(selectAction.type)
        } else {
            sendEffect(PublishSideEffect.RequiresPermission(selectAction.type))
        }
    }

    override fun reduceToUi(
        state: PublishModelState,
        uiState: PublishViewUiState
    ): PublishViewUiState {
        val audioText =
            if (state.audioPublishingMode.permissionStatus == PermissionStatus.SHOW_RATIONALE) {
                "Publish Audio - Requires Microphone Access"
            } else {
                "Publish Audio"
            }
        val videoText =
            if (state.videoPublishingMode.permissionStatus == PermissionStatus.SHOW_RATIONALE) {
                "Publish Video - Requires Camera Access"
            } else {
                "Publish Video"
            }
        val audioVideoText =
            if (state.audioVideoPublishingMode.permissionStatus == PermissionStatus.SHOW_RATIONALE) {
                "Publish Audio and Video - Requires Microphone and Camera Access"
            } else {
                "Publish Audio and Video"
            }

        return PublishViewUiState(
            isStartEnabled = state.isStopSelected(),
            isStopEnabled = !state.isStopSelected(),
            publishingConnectionStateText = "Publishing State: ${state.publishingState?.toString() ?: "null"}",
            publishingAudioButtonText = audioText,
            publishingVideoButtonText = videoText,
            publishingAudioVideoButtonText = audioVideoText,
            publishingAudioButtonType = getPublishingButtonType(state.isAudioSelected()),
            publishingVideoButtonType = getPublishingButtonType(state.isVideoSelected()),
            publishingAudioVideoButtonType = getPublishingButtonType(state.isAudioVideoSelected())
        )
    }

    private fun getPublishingButtonType(isPublishing: Boolean) = if (isPublishing) {
        ButtonType.PRIMARY
    } else {
        ButtonType.SECONDARY
    }

    // endregion

    // region publishing

    private fun connectPublisher(type: PublishingType) {
        val tracks = readyPublishingSources(type)
        if (tracks.isEmpty()) {
            handleError(Throwable("Unable to get media source to publish. Check permissions."))
        } else {
            viewModelScope.launch {
                tracks.audioTrack?.let {
                    publisher.addTrack(it)
                }
                tracks.videoTrack?.let {
                    publisher.addTrack(it)
                }
                val credentials = Credential(
                    streamName = BuildConfig.STREAM_NAME,
                    token = BuildConfig.PUBLISH_TOKEN,
                    apiUrl = "https://director.millicast.com/api/director/publish"
                )
                publisher.setCredentials(credentials)
                publisher.connect()
            }
        }
    }

    private fun readyPublishingSources(type: PublishingType): ActiveTracks {
        val audioTrack = if (type == PublishingType.AUDIO || type == PublishingType.AUDIO_VIDEO) {
            try {
                val audioSource = audioSources<MicrophoneAudioSource>().firstOrNull()
                updateModelState { copy(audioSource = audioSource) }
                audioSource?.startCapture()
            } catch (e: Throwable) {
                handleError(e)
                null
            }
        } else {
            null
        }
        val videoTrack = if (type == PublishingType.VIDEO || type == PublishingType.AUDIO_VIDEO) {
            try {
                val videoSource = videoSources<CameraVideoSource>().firstOrNull()
                updateModelState { copy(videoSource = videoSource) }
                videoSource?.startCapture()
            } catch (e: Throwable) {
                handleError(e)
                null
            }
        } else {
            null
        }
        return ActiveTracks(audioTrack, videoTrack)
    }

    private fun startPublishing() {
        viewModelScope.launch {
            publisher.publish(
                Option(
                    audioCodec = Media.supportedAudioCodecs.first(),
                    videoCodec = Media.supportedVideoCodecs.first(),
                    sourceId = "androidSourceId",
                    dtx = true,
                    stereo = true
                )
            )
        }
    }

    fun stopPublishing() {
        viewModelScope.launch {
            publisher.unpublish()
            publisher.disconnect()
            state.value.audioSource?.stopCapture()
            state.value.videoSource?.stopCapture()
            state.value.audioSource?.release()
            state.value.videoSource?.release()
            updateModelStateAndReduceToUi {
                copy(
                    selectedMode = PublishingOptionMode.StopMode,
                    audioSource = null,
                    videoSource = null
                )
            }
        }
    }

    private fun handleError(e: Throwable) {
        Log.e(TAG, "Error publishing!", e)
        val errorEffect = e.message?.takeIf { it.isNotBlank() }?.let {
            PublishSideEffect.PublishingError(it)
        } ?: run {
            PublishSideEffect.PublishingError()
        }
        sendEffect(errorEffect)
        stopPublishing()
    }

    // endregion

    companion object {
        private const val TAG = "PublishViewModel"
    }
}

data class ActiveTracks(val audioTrack: AudioTrack?, val videoTrack: VideoTrack?) {
    fun isEmpty() = audioTrack == null && videoTrack == null
}
