package io.dolby.app.features.home.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import io.dolby.app.common.ui.ButtonType
import io.dolby.app.common.ui.DolbyButtonsContainer
import io.dolby.app.common.ui.StyledButton
import io.dolby.app.navigation.NavAction
import io.dolby.app.navigation.NavigationViewModel
import io.dolby.millicast.androidsdk.sampleapps.R

@Composable
fun HomeScreen(navigationViewModel: NavigationViewModel) {
    val screenName = stringResource(id = R.string.app_name)
    DolbyButtonsContainer(screenName = screenName) {
        StyledButton(
            Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Subscribe"
                    testTag = "Subscribe"
                },
            buttonText = "Subscribe",
            onClickAction = {
                navigationViewModel.onUiAction(NavAction.ToSubscribeOptions)
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
                navigationViewModel.onUiAction(NavAction.ToPublish)
            },
            buttonType = ButtonType.SECONDARY
        )
    }
}
