package io.dolby.app.features.publish.ui

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.dolby.app.common.CollectSideEffect
import io.dolby.app.common.ui.ButtonType
import io.dolby.app.common.ui.DolbyButtonsContainer
import io.dolby.app.common.ui.StyledButton
import io.dolby.app.common.ui.fontColor
import io.dolby.app.features.publish.PermissionStatus
import io.dolby.app.features.publish.PublishAction
import io.dolby.app.features.publish.PublishSideEffect
import io.dolby.app.features.publish.PublishViewModel
import io.dolby.app.features.publish.PublishingType
import io.dolby.millicast.androidsdk.sampleapps.R
import org.koin.compose.koinInject

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PublishScreen(viewModel: PublishViewModel = koinInject()) {
    val context = LocalContext.current
    val screenName = stringResource(id = R.string.publish_options)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
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

    DisposableEffect(viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onUiAction(PublishAction.SelectedButton.Stop)
            }
        }
        val lifecycle = lifecycleOwner.value.lifecycle
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            viewModel.onUiAction(PublishAction.SelectedButton.Stop)
        }
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
    CollectSideEffect(effect = viewModel.effect) {
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
                            viewModel.onUiAction(
                                PublishAction.PermissionUpdate(
                                    PublishingType.AUDIO,
                                    PermissionStatus.SHOW_RATIONALE
                                )
                            )
                        }
                    }
                    PublishingType.VIDEO -> {
                        if (cameraPermissionState.status.shouldShowRationale) {
                            viewModel.onUiAction(
                                PublishAction.PermissionUpdate(
                                    PublishingType.VIDEO,
                                    PermissionStatus.SHOW_RATIONALE
                                )
                            )
                        }
                    }

                    PublishingType.AUDIO_VIDEO -> {
                        if (combinedPermissionsState.shouldShowRationale) {
                            viewModel.onUiAction(
                                PublishAction.PermissionUpdate(
                                    PublishingType.AUDIO_VIDEO,
                                    PermissionStatus.SHOW_RATIONALE
                                )
                            )
                        }
                    }
                }
                Toast.makeText(
                    context,
                    "Permissions are required for publishing.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is PublishSideEffect.PublishingError -> {
                Toast.makeText(context, "Publishing Error! ${it.msg}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DolbyButtonsContainer(screenName = screenName) {
        Text(
            text = uiState.publishingConnectionStateText,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium,
            color = fontColor(MaterialTheme.colors.background),
            textAlign = TextAlign.Center

        )
        Spacer(modifier = Modifier.height(12.dp))

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
