package io.dolby.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.dolby.app.ui.common.CollectSideEffect
import io.dolby.app.ui.home.HomeScreen
import io.dolby.app.ui.subscribe.SubscribeScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun HandleNavigation(
    navController: NavHostController
) {
    val navigationViewModel: NavigationViewModel = koinViewModel()
    // Having a centralized place to handle all navigation
    CollectSideEffect(navigationViewModel.effect) {
        when (it) {
            is NavigationContract.NavigationEffect.NavigateTo -> {
                navController.navigate(it.route, it.navOptions)
            }

            is NavigationContract.NavigationEffect.NavigateBack -> {
                navController.popBackStack()
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
        popExitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        }
    ) {
        composable(
            route = Screen.HomeScreen.route
        ) {
            HomeScreen(navigationViewModel)
        }

        composable(
            route = Screen.SubscribeScreen.route
        ) {
            SubscribeScreen(navigationViewModel)
        }
    }
}
