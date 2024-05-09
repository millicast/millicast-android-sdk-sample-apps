package io.dolby.app.features.publish.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.dolby.app.common.ui.ButtonType
import io.dolby.app.common.ui.DolbyBackgroundBox
import io.dolby.app.common.ui.DolbyCopyrightFooterView
import io.dolby.app.common.ui.StyledButton
import io.dolby.app.common.ui.TopActionBar
import io.dolby.app.common.ui.fontColor
import io.dolby.app.features.publish.PublishAction
import io.dolby.app.features.publish.PublishViewModel
import io.dolby.millicast.androidsdk.sampleapps.R
import org.koin.compose.koinInject

@Composable
fun PublishScreen(viewModel: PublishViewModel = koinInject()) {
    val screenName = stringResource(id = R.string.publish_screen_name)
    val background = MaterialTheme.colors.background
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopActionBar()
        },
        bottomBar = {
            DolbyCopyrightFooterView()
        }
    ) { paddingValues ->
        DolbyBackgroundBox(
            modifier = Modifier
                .padding(paddingValues)
                .semantics { contentDescription = screenName }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.Center)
                    .background(background, shape = RoundedCornerShape(4.dp))
                    .clip(MaterialTheme.shapes.large)
                    .padding(horizontal = 30.dp)
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    stringResource(id = R.string.publish_options),
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium,
                    color = fontColor(MaterialTheme.colors.background),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
                val startAudioStr = stringResource(R.string.publish_start_audio)
                StyledButton(
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = startAudioStr
                            testTag = startAudioStr
                        },
                    buttonText = startAudioStr,
                    onClickAction = {
                        viewModel.onUiAction(PublishAction.StartAudio)
                    },
                    buttonType = uiState.publishingAudioButtonType,
                    isEnabled = uiState.isStartEnabled
                )

                Spacer(modifier = Modifier.height(5.dp))
                val startVideoStr = stringResource(R.string.publish_start_video)
                StyledButton(
                    Modifier
                        .semantics {
                            contentDescription = startVideoStr
                            testTag = startVideoStr
                        }
                        .fillMaxWidth(),
                    buttonText = startVideoStr,
                    onClickAction = {
                        viewModel.onUiAction(PublishAction.StartVideo)
                    },
                    buttonType = uiState.publishingVideoButtonType,
                    isEnabled = uiState.isStartEnabled
                )

                Spacer(modifier = Modifier.height(5.dp))
                val startAudioVideoStr = stringResource(R.string.publish_start_audio_video)
                StyledButton(
                    Modifier
                        .semantics {
                            contentDescription = startAudioVideoStr
                            testTag = startAudioVideoStr
                        }
                        .fillMaxWidth(),
                    buttonText = startAudioVideoStr,
                    onClickAction = {
                        viewModel.onUiAction(PublishAction.StartAudioVideo)
                    },
                    buttonType = uiState.publishingAudioVideoButtonType,
                    isEnabled = uiState.isStartEnabled
                )

                Spacer(modifier = Modifier.height(5.dp))
                val stopStr = stringResource(R.string.publish_stop)
                StyledButton(
                    Modifier
                        .semantics {
                            contentDescription = stopStr
                            testTag = stopStr
                        }
                        .fillMaxWidth(),
                    buttonText = stopStr,
                    onClickAction = {
                        viewModel.onUiAction(PublishAction.Stop)
                    },
                    buttonType = ButtonType.SECONDARY,
                    isEnabled = uiState.isStopEnabled
                )
            }
        }
    }
}
