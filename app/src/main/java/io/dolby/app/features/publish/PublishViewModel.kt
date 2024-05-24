package io.dolby.app.features.publish

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.millicast.Core
import com.millicast.Media
import com.millicast.Publisher
import com.millicast.publishers.Option
import com.millicast.utils.Logger
import io.dolby.app.common.MultipleStatesViewModel
import io.dolby.app.common.ui.ButtonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.startKoin

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
                startPublishing(PublishingType.AUDIO)
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
                startPublishing(PublishingType.VIDEO)
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
                startPublishing(PublishingType.AUDIO_VIDEO)
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
            startPublishing(selectAction.type)
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
            showPublishingConnectionState = true, // always show for now
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

    fun startPublishing(type: PublishingType) {
        startPublishingSources(type)
        val currentState = state.value
        if (currentState.hasPublishingTrack()) {
            viewModelScope.launch {
                // TODO: credentials
                currentState.audioTrack?.let {
                    publisher.addTrack(it)
                }
                currentState.videoTrack?.let {
                    publisher.addTrack(it)
                }
                publisher.connect()
                // TODO: do we have to wait for publishing
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
    }

    private fun startPublishingSources(type: PublishingType) {
        if (type == PublishingType.AUDIO || type == PublishingType.AUDIO_VIDEO) {
            try {
                val audioSource = Media.audioSources.firstOrNull()
                val audioTrack = audioSource?.startCapture()
                updateModelState { copy(audioSource = audioSource, audioTrack = audioTrack) }
            } catch (e: Throwable) {
                handleError(e)
                return
            }
        }
        if (type == PublishingType.VIDEO || type == PublishingType.AUDIO_VIDEO) {
            try {
                val videoSource = Media.videoSources.firstOrNull()
                val videoTrack = videoSource?.startCapture()
                updateModelState { copy(videoSource = videoSource, videoTrack = videoTrack) }
            } catch (e: Throwable) {
                handleError(e)
                return
            }
        }
        if (!state.value.hasPublishingTrack()) {
            handleError(Throwable("Unable to get media source to publish. Check permissions."))
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
                    audioTrack = null,
                    videoTrack = null
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
