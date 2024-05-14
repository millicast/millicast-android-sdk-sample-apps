package io.dolby.app.features.subscribe.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.dolby.app.features.subscribe.ui.tracks.Tracks
import io.dolby.millicast.androidsdk.sampleapps.R
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun SubscribeScreen(
    isMultiView: Boolean,
    subscribeViewModel: SubscribeViewModel = koinInject(parameters = { parametersOf(isMultiView) })
) {
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    val uiState by subscribeViewModel.uiState.collectAsStateWithLifecycle()
    val tag = stringResource(id = R.string.subscribe_screen_tag)
    LaunchedEffect(Unit) {
        Log.i(tag, "Screen subscribe")
        subscribeViewModel.onUiAction(SubscribeAction.Subscribe)
    }

    DisposableEffect(subscribeViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(tag, " Lifecycle onPause")
                    subscribeViewModel.onUiAction(SubscribeAction.Pause())
                }

                Lifecycle.Event.ON_RESUME -> {
                    Log.d(tag, "Lifecycle OnResume")
                    subscribeViewModel.onUiAction(SubscribeAction.Resume())
                }

                Lifecycle.Event.ON_DESTROY -> {
                    Log.d(tag, "Lifecycle onDestroy")
                    subscribeViewModel.onUiAction(SubscribeAction.Disconnect)
                }

                else -> {
                }
            }
        }
        val lifecycle = lifecycleOwner.value.lifecycle
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            subscribeViewModel.onUiAction(SubscribeAction.Disconnect)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        if (uiState.shouldShowTracks) {
            Tracks(subscribeViewModel, Modifier.fillMaxSize())
        }
    }
}
