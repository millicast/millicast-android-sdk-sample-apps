package io.dolby.app.ui.home

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
import io.dolby.app.ui.navigation.NavigationContract
import io.dolby.app.ui.navigation.NavigationViewModel
import io.dolby.millicast.androidsdk.sampleapps.R
import io.dolby.uikit.DolbyBackgroundBox
import io.dolby.uikit.DolbyCopyrightFooterView
import io.dolby.uikit.actionbar.TopActionBar
import io.dolby.uikit.button.ButtonType
import io.dolby.uikit.button.StyledButton
import io.dolby.uikit.theme.fontColor

@Composable
fun HomeScreen(navigationViewModel: NavigationViewModel) {
    val screenName = stringResource(id = R.string.stream_detail_screen_name)
    val background = MaterialTheme.colors.background
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
                    stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium,
                    color = fontColor(MaterialTheme.colors.background),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
                StyledButton(
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Subscribe"
                            testTag = "Subscribe"
                        },
                    buttonText = "Subscribe",
                    onClickAction = {
                        navigationViewModel.onUiAction(NavigationContract.NavigationAction.NavigateToSubscribe)
                    },
                    buttonType = ButtonType.PRIMARY
                )
                Spacer(modifier = Modifier.height(5.dp))
                StyledButton(
                    Modifier
                        .semantics {
                            contentDescription = "Publish"
                            testTag = "Publish"
                        }
                        .fillMaxWidth(),
                    buttonText = "Publish",
                    onClickAction = {
                        // TODO navigate to Publish screen
                    },
                    buttonType = ButtonType.SECONDARY
                )
            }
        }
    }
}
