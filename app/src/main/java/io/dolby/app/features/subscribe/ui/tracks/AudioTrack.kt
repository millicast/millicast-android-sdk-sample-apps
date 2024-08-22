package io.dolby.app.features.subscribe.ui.tracks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.millicast.subscribers.remote.RemoteAudioTrack

@Composable
fun AudioTrack(sourceTrack: RemoteAudioTrack) {
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    DisposableEffect(sourceTrack) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    sourceTrack.disableAsync()
                }

                Lifecycle.Event.ON_RESUME -> {
                    sourceTrack.enableAsync()
                }

                else -> {
                }
            }
        }
        val lifecycle = lifecycleOwner.value.lifecycle
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            sourceTrack.disableAsync()
        }
    }
}
