package io.dolby.app.features.publish

import androidx.lifecycle.viewModelScope
import io.dolby.app.common.MultipleStatesViewModel
import io.dolby.app.common.ui.ButtonType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishViewModel(stateModel: PublishModelState = PublishModelState()) :
    MultipleStatesViewModel<PublishAction, PublishViewUiState, PublishModelState, PublishSideEffect>() {

    init {
        updateModelStateAndReduceToUi { stateModel }
    }

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
                    is PublishAction.SelectedButton.PublishButton -> handlePublishingSelection(uiAction)
                    PublishAction.SelectedButton.Stop -> {
                        updateModelStateAndReduceToUi {
                            copy(selectedMode = PublishingOptionMode.StopMode)
                        }
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
                // start publishing flow
            } else {
                // stop publishing flow
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
                // start publishing flow
            } else {
                // stop publishing flow
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
                // start publishing flow
            } else {
                // stop publishing flow
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
            // start publishing
        } else {
            sendEffect(PublishSideEffect.RequiresPermission(selectAction.type))
        }
    }

    override fun reduceToUi(
        state: PublishModelState,
        uiState: PublishViewUiState
    ): PublishViewUiState {
        val audioText = if (state.audioPublishingMode.permissionStatus == PermissionStatus.SHOW_RATIONALE) {
            "Publish Audio - Requires Microphone Access"
        } else {
            "Publish Audio"
        }
        val videoText = if (state.videoPublishingMode.permissionStatus == PermissionStatus.SHOW_RATIONALE) {
            "Publish Video - Requires Camera Access"
        } else {
            "Publish Video"
        }
        val audioVideoText = if (state.audioVideoPublishingMode.permissionStatus == PermissionStatus.SHOW_RATIONALE) {
            "Publish Audio and Video - Requires Microphone and Camera Access"
        } else {
            "Publish Audio and Video"
        }
        return PublishViewUiState(
            isStartEnabled = state.isStopSelected(),
            isStopEnabled = !state.isStopSelected(),
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
}
