package io.dolby.app.features.subscribe.ui.options

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
fun SubscribeOptions(navigationViewModel: NavigationViewModel) {
    val screenName = stringResource(id = R.string.subscribe_options_screen_name)

    DolbyButtonsContainer(screenName = screenName) {
        StyledButton(
            Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Single View"
                    testTag = "Single View"
                },
            buttonText = "Subscribe Single View",
            onClickAction = {
                navigationViewModel.onUiAction(NavAction.ToSubscribe(isMultiView = false))
            },
            buttonType = ButtonType.SECONDARY
        )
        Spacer(modifier = Modifier.height(5.dp))
        StyledButton(
            Modifier
                .semantics {
                    contentDescription = "Multi View"
                    testTag = "Multi View"
                }
                .fillMaxWidth(),
            buttonText = "Subscribe Multi View",
            onClickAction = {
                navigationViewModel.onUiAction(NavAction.ToSubscribe(isMultiView = true))
            },
            buttonType = ButtonType.SECONDARY
        )
    }
}
