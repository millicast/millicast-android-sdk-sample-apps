package io.dolby.app.features.publish.ui

import android.Manifest
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.dolby.app.common.ui.ButtonType
import io.dolby.app.common.ui.DolbyButtonsContainer
import io.dolby.app.common.ui.StyledButton
import io.dolby.app.features.publish.PermissionStatus
import io.dolby.app.features.publish.PublishAction
import io.dolby.app.features.publish.PublishSideEffect
import io.dolby.app.features.publish.PublishViewModel
import io.dolby.app.features.publish.PublishingType
import io.dolby.millicast.androidsdk.sampleapps.R
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PublishScreen(viewModel: PublishViewModel = koinInject()) {
    val screenName = stringResource(id = R.string.publish_options)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val microphonePermissionState =
        rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO) {
            viewModel.onUiAction(
                PublishAction.PermissionUpdate(
                    type = PublishingType.AUDIO,
                    permissionStatus = PermissionStatus.fromHasPermission(it)
                )
            )
        }
    val cameraPermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA) {
            viewModel.onUiAction(
                PublishAction.PermissionUpdate(
                    type = PublishingType.VIDEO,
                    permissionStatus = PermissionStatus.fromHasPermission(it)
                )
            )
        }
    val combinedPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    ) {
        val accepted = it.values.all { granted -> granted }
        viewModel.onUiAction(
            PublishAction.PermissionUpdate(
                type = PublishingType.AUDIO_VIDEO,
                permissionStatus = PermissionStatus.fromHasPermission(accepted)
            )
        )
    }

    LaunchedEffect(key1 = Unit) {
        // initial update of permissions only if we have permissions, otherwise they remain in default state of UNKNOWN
        if (microphonePermissionState.status.isGranted) {
            viewModel.onUiAction(
                PublishAction.PermissionUpdate(
                    type = PublishingType.AUDIO,
                    permissionStatus = PermissionStatus.GRANTED
                )
            )
        }
        if (cameraPermissionState.status.isGranted) {
            viewModel.onUiAction(
                PublishAction.PermissionUpdate(
                    type = PublishingType.VIDEO,
                    permissionStatus = PermissionStatus.GRANTED
                )
            )
        }
        if (combinedPermissionsState.allPermissionsGranted) {
            viewModel.onUiAction(
                PublishAction.PermissionUpdate(
                    type = PublishingType.AUDIO_VIDEO,
                    permissionStatus = PermissionStatus.GRANTED
                )
            )
        }
    }
    LaunchedEffect(key1 = viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                is PublishSideEffect.RequiresPermission -> {
                    when (it.publishingType) {
                        PublishingType.AUDIO -> microphonePermissionState.launchPermissionRequest()
                        PublishingType.VIDEO -> cameraPermissionState.launchPermissionRequest()
                        PublishingType.AUDIO_VIDEO -> combinedPermissionsState.launchMultiplePermissionRequest()
                    }
                }
                is PublishSideEffect.DeniedPermission -> {
                    // check if we can show rationale
                    when (it.publishingType) {
                        PublishingType.AUDIO -> {
                            if (microphonePermissionState.status.shouldShowRationale) {
                                viewModel.onUiAction(PublishAction.PermissionUpdate(PublishingType.AUDIO, PermissionStatus.SHOW_RATIONALE))
                            }
                        }
                        PublishingType.VIDEO -> {
                            if (cameraPermissionState.status.shouldShowRationale) {
                                viewModel.onUiAction(PublishAction.PermissionUpdate(PublishingType.VIDEO, PermissionStatus.SHOW_RATIONALE))
                            }
                        }
                        PublishingType.AUDIO_VIDEO -> {
                            if (combinedPermissionsState.shouldShowRationale) {
                                viewModel.onUiAction(PublishAction.PermissionUpdate(PublishingType.AUDIO_VIDEO, PermissionStatus.SHOW_RATIONALE))
                            }
                        }
                    }
                    // show toast
                }
            }
        }
    }
    DolbyButtonsContainer(screenName = screenName) {
        StyledButton(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Publish Audio"
                    testTag = "Publish Audio"
                },
            buttonText = uiState.publishingAudioButtonText,
            onClickAction = {
                viewModel.onUiAction(
                    PublishAction.SelectedButton.PublishButton(
                        type = PublishingType.AUDIO,
                        permissionStatus = PermissionStatus.fromHasPermissionAndShowRationale(
                            hasPermission = microphonePermissionState.status.isGranted,
                            shouldShowRationale = microphonePermissionState.status.shouldShowRationale
                        )
                    )
                )
            },
            buttonType = uiState.publishingAudioButtonType,
            isEnabled = uiState.isStartEnabled
        )

        Spacer(modifier = Modifier.height(5.dp))
        StyledButton(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Publish Video"
                    testTag = "Publish Video"
                },
            buttonText = uiState.publishingVideoButtonText,
            onClickAction = {
                viewModel.onUiAction(
                    PublishAction.SelectedButton.PublishButton(
                        type = PublishingType.VIDEO,
                        permissionStatus = PermissionStatus.fromHasPermissionAndShowRationale(
                            hasPermission = cameraPermissionState.status.isGranted,
                            shouldShowRationale = cameraPermissionState.status.shouldShowRationale
                        )
                    )
                )
            },
            buttonType = uiState.publishingVideoButtonType,
            isEnabled = uiState.isStartEnabled
        )

        Spacer(modifier = Modifier.height(5.dp))
        StyledButton(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Publish Audio and Video"
                    testTag = "Publish Audio and Video"
                },
            buttonText = uiState.publishingAudioVideoButtonText,
            onClickAction = {
                viewModel.onUiAction(
                    PublishAction.SelectedButton.PublishButton(
                        type = PublishingType.AUDIO_VIDEO,
                        permissionStatus = PermissionStatus.fromHasPermissionAndShowRationale(
                            hasPermission = combinedPermissionsState.allPermissionsGranted,
                            shouldShowRationale = combinedPermissionsState.shouldShowRationale
                        )
                    )
                )
            },
            buttonType = uiState.publishingAudioVideoButtonType,
            isEnabled = uiState.isStartEnabled
        )

        Spacer(modifier = Modifier.height(5.dp))
        StyledButton(
            Modifier
                .semantics {
                    contentDescription = "Stop Publishing"
                    testTag = "Stop Publishing"
                }
                .fillMaxWidth(),
            buttonText = "Stop Publishing",
            onClickAction = {
                viewModel.onUiAction(PublishAction.SelectedButton.Stop)
            },
            buttonType = ButtonType.SECONDARY,
            isEnabled = uiState.isStopEnabled
        )
    }
}
