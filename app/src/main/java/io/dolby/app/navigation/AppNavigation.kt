package io.dolby.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.dolby.app.features.home.ui.HomeScreen
import io.dolby.app.features.subscribe.ui.SubscribeScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavigation() {
    val navigator: Navigator = koinInject()
    val navController: NavHostController = rememberNavController()
    val navigationViewModel: NavigationViewModel = koinViewModel()
    LaunchedEffect(Unit) {
        navigator.subscribe(navController)
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
