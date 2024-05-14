package io.dolby.app.navigation

import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription

class Navigator {
    private val navigationEventFlow =
        MutableSharedFlow<NavigationEvent>(extraBufferCapacity = Int.MAX_VALUE)
    private val navControllerStateFlow = MutableStateFlow<NavController?>(null)
    private fun NavController.handleNavigationEvent(navEvent: NavigationEvent) {
        when (navEvent) {
            is NavigationEvent.NavigateTo -> {
                navigate(navEvent.route, navEvent.navOptions)
            }
        }
    }

    suspend fun subscribe(navController: NavController) {
        navigationEventFlow
            .onSubscription { this@Navigator.navControllerStateFlow.value = navController }
            .onCompletion { this@Navigator.navControllerStateFlow.value = null }
            .collect { navController.handleNavigationEvent(it) }
    }

    fun navigate(navigationEvent: NavigationEvent) {
        navigationEventFlow.tryEmit(navigationEvent)
    }
}
