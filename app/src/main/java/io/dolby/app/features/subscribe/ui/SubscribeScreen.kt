package io.dolby.app.features.subscribe.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.millicast.Media
import com.millicast.subscribers.state.TrackHolder
import com.millicast.video.TextureViewRenderer
import io.dolby.millicast.androidsdk.sampleapps.R
import org.koin.compose.koinInject
import org.webrtc.RendererCommon

@Composable
fun SubscribeScreen(
    subscribeViewModel: SubscribeViewModel = koinInject()
) {
    // TODO To be splitted into multiview.
    val context = LocalContext.current
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    val uiState by subscribeViewModel.uiState.collectAsStateWithLifecycle()
    val tag = stringResource(id = R.string.subscribe_screen_tag)
    val videoRenderer = remember(context) {
        TextureViewRenderer(context).apply {
            init(Media.eglBaseContext, null)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }
    }

    LaunchedEffect(Unit) {
        Log.i(tag, "Screen subscribe")
        subscribeViewModel.onUiAction(SubscribeAction.Subscribe)
    }

    DisposableEffect(videoRenderer) {
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
        if (uiState.isSubscribed) {
            Box(
                modifier = Modifier
                    .aspectRatio(16 / 9f)
                    .align(Alignment.Center)
            ) {
                AndroidView(
                    modifier = Modifier.align(Alignment.Center),
                    factory = { videoRenderer },
                    update = { view ->
                        view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                        val videoTrack: TrackHolder.VideoTrackHolder? =
                            uiState.tracks.values.firstOrNull()?.filterIsInstance<TrackHolder.VideoTrackHolder>()
                                ?.firstOrNull()
                        videoTrack?.videoTrack?.setVideoSink(videoRenderer)
                    }
                )
            }
        }
    }
}
