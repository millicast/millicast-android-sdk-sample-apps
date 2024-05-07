package io.dolby.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.dolby.app.ui.home.HomeScreen
import io.dolby.app.ui.subscribe.SubscribeScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()
    val navigationViewModel: NavigationViewModel = koinViewModel()
    // Having a centralized place to handle all navigation
    HandleNavigation(navController = navController)
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
