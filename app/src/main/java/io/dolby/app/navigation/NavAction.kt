package io.dolby.app.navigation

import io.dolby.app.common.ViewAction

sealed class NavAction : ViewAction {
    data object ToSubscribe : NavAction()
    data object ToPublish : NavAction()
}
