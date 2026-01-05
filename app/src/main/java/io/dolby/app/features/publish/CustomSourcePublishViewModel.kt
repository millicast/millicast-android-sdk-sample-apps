package io.dolby.app.features.publish

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.millicast.Core
import com.millicast.Media
import com.millicast.Media.audioSources
import com.millicast.Publisher
import com.millicast.devices.source.audio.MicrophoneAudioSource
import com.millicast.devices.source.custom.CustomVideoSource
import com.millicast.devices.source.custom.VideoFrame
import com.millicast.publishers.Credential
import com.millicast.publishers.Option
import com.millicast.publishers.state.PublisherConnectionState
import com.millicast.utils.Logger
import com.millicast.utils.VideoType
import io.dolby.app.common.MultipleStatesViewModel
import io.dolby.app.common.ui.ButtonType
import io.dolby.millicast.androidsdk.sampleapps.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

class CustomSourcePublishViewModel(
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
            publishingAudioVideoButtonType = getPublishingButtonType(state.isAudioVideoSelected()),
            shouldShowPreview = state.activeTracks?.videoTrack != null,
            activeVideoTrack = state.activeTracks?.videoTrack
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
        updateModelStateAndReduceToUi { copy(activeTracks = tracks) }
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
                val customVideoSource = CustomVideoSource()
                updateModelState { copy(customVideoSource = customVideoSource) }
                viewModelScope.launch {
                    val fps = 60
                    fun generateFrame(): ByteArray {
                        val width = 1920
                        val height = 1080
                        val ySize = width * height
                        val uvSize = ((width + 1) / 2) * ((height + 1) / 2)
                        val frameData = ByteArray(ySize + uvSize * 2)
                        val pi = 3.14159265
                        val yval =
                            (128 + 127 * sin(2.0 * pi * System.currentTimeMillis() / 3000.0)).toInt().toByte()
                        val uval =
                            (128 + 127 * sin(2.0 * pi * System.currentTimeMillis() / 5000.0)).toInt().toByte()
                        val vval =
                            (128 + 127 * sin(2.0 * pi * System.currentTimeMillis() / 7000.0)).toInt().toByte()
                        frameData.fill(yval, 0, ySize) // Y plane
                        frameData.fill(uval, ySize, ySize + uvSize) // U plane
                        frameData.fill(vval, ySize + uvSize, ySize + uvSize * 2) // V plane
                        return frameData
                    }

                    val videoFrame = MVideoFrameImpl()
                    while (true) {
                        videoFrame.setBufferData(generateFrame())
                        customVideoSource.onFrame(videoFrame)
                        delay(1000L / fps)
                    }
                }
                customVideoSource.startCapture()
            } catch (e: Throwable) {
                handleError(e)
                null
            }
        } else {
            null
        }
        return ActiveTracks(audioTrack, videoTrack)
    }

    private class MVideoFrameImpl(
        private val width: Int = 1920,
        private val height: Int = 1080,
        private val type: VideoType = VideoType.I420
    ) : VideoFrame {
        private val nativeBuffer =
            ByteBuffer.allocateDirect(size(type)).order(ByteOrder.nativeOrder())

        fun setBufferData(data: ByteArray) {
            nativeBuffer.clear()
            nativeBuffer.put(data)
            nativeBuffer.flip()
        }

        override fun getBuffer(type: VideoType, buffer: ByteArray) {
            nativeBuffer.get(buffer, 0, nativeBuffer.limit())
            nativeBuffer.rewind()
        }

        override fun getType() = type

        override fun height() = height

        override fun size(type: VideoType): Int {
            return when(type) {
                VideoType.I420 -> width * height * 3 / 2
                VideoType.RGB24 -> width * height * 3
                else -> width * height // TODO: handle other types
            }
        }

        override fun width() = width
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
                    videoSource = null,
                    activeTracks = null
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
