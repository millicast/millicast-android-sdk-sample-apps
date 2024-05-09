package io.dolby.app.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@Composable
fun <SideEffect> CollectSideEffect(
    effect: Flow<SideEffect>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    context: CoroutineContext = Dispatchers.Main.immediate,
    onSideEffect: suspend CoroutineScope.(effect: SideEffect) -> Unit
) {
    LaunchedEffect(effect, LocalLifecycleOwner.current) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(context) {
                effect.collect { onSideEffect(it) }
            }
        }
    }
}
