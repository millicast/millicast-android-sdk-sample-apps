package io.dolby.app.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription

class Navigator {
    private val navigationEventFlow =
        MutableSharedFlow<NavigationEvent>(extraBufferCapacity = Int.MAX_VALUE)
    private val navControllerStateFlow = MutableStateFlow<NavController?>(null)
    private fun NavController.handleNavigationEvent(navigationEvent: NavigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.NavigateTo -> {
                navigate(navigationEvent.route, navigationEvent.options)
            }

            NavigationEvent.NavigateUp -> navigateUp()
        }
    }

    suspend fun subscribe(navController: NavController) {
        navigationEventFlow
            .onSubscription { this@Navigator.navControllerStateFlow.value = navController }
            .onCompletion { this@Navigator.navControllerStateFlow.value = null }
            .collect { navController.handleNavigationEvent(it) }
    }

    fun navigate(route: String, navOptions: (NavOptionsBuilder.() -> Unit)? = null) {
        val options = navOptions?.let { navOptions(it) }
        navigationEventFlow.tryEmit(NavigationEvent.NavigateTo(route, options))
    }

    fun goBack() {
        navigationEventFlow.tryEmit(NavigationEvent.NavigateUp)
    }
}
