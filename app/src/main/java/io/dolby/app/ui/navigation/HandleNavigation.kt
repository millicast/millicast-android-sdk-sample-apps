package io.dolby.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import io.dolby.app.ui.common.CollectSideEffect
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
}
