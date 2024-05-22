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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import io.dolby.app.common.ui.ButtonType
import io.dolby.app.common.ui.DolbyButtonsContainer
import io.dolby.app.common.ui.PermissionDependentButton
import io.dolby.app.common.ui.StyledButton
import io.dolby.app.common.ui.toPermissionModel
import io.dolby.app.features.publish.PublishAction
import io.dolby.app.features.publish.PublishSideEffect
import io.dolby.app.features.publish.PublishViewModel
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
            if (it) {
                viewModel.onUiAction(PublishAction.GrantedAudio)
            } else {
                viewModel.onUiAction(PublishAction.Stop)
            }
        }
    val cameraPermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA) {
            if (it) {
                viewModel.onUiAction(PublishAction.GrantedVideo)
            } else {
                viewModel.onUiAction(PublishAction.Stop)
            }
        }
    val combinedPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    ) {
        if (it.values.all { granted -> granted }) {
            viewModel.onUiAction(PublishAction.GrantedAudioAndVideo)
        } else {
            viewModel.onUiAction(PublishAction.Stop)
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest {
            when (it) {
                is PublishSideEffect.RequiresMicrophoneAccess -> microphonePermissionState.launchPermissionRequest()
                is PublishSideEffect.RequiresCameraAccess -> cameraPermissionState.launchPermissionRequest()
                is PublishSideEffect.RequiresCombinedAccess -> combinedPermissionsState.launchMultiplePermissionRequest()
            }
        }
    }
    DolbyButtonsContainer(screenName = screenName) {
        PermissionDependentButton(
            permissionModel = microphonePermissionState.toPermissionModel(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Publish Audio"
                    testTag = "Publish Audio"
                },
            allowText = "Allow Microphone Access to Publish Audio",
            actionText = "Publish Audio",
            requestClick = { viewModel.onUiAction(PublishAction.RequestMicrophone) },
            actionClick = { viewModel.onUiAction(PublishAction.StartAudio) },
            buttonType = uiState.publishingAudioButtonType,
            isEnabled = uiState.isStartEnabled
        )

        Spacer(modifier = Modifier.height(5.dp))
        PermissionDependentButton(
            permissionModel = cameraPermissionState.toPermissionModel(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Publish Video"
                    testTag = "Publish Video"
                },
            allowText = "Allow Camera Access to Publish Video",
            actionText = "Publish Video",
            requestClick = { viewModel.onUiAction(PublishAction.RequestCamera) },
            actionClick = { viewModel.onUiAction(PublishAction.StartVideo) },
            buttonType = uiState.publishingVideoButtonType,
            isEnabled = uiState.isStartEnabled
        )

        Spacer(modifier = Modifier.height(5.dp))
        PermissionDependentButton(
            permissionModel = combinedPermissionsState.toPermissionModel(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Publish Audio and Video"
                    testTag = "Publish Audio and Video"
                },
            allowText = "Allow Microphone and Camera Access to Publish Audio and Video",
            actionText = "Publish Audio and Video",
            requestClick = { viewModel.onUiAction(PublishAction.RequestMicrophoneAndCamera) },
            actionClick = { viewModel.onUiAction(PublishAction.StartAudioVideo) },
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
                viewModel.onUiAction(PublishAction.Stop)
            },
            buttonType = ButtonType.SECONDARY,
            isEnabled = uiState.isStopEnabled
        )
    }
}
