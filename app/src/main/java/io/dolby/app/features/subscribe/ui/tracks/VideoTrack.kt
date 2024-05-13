package io.dolby.app.features.subscribe.ui.tracks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import com.millicast.Media
import com.millicast.subscribers.state.TrackHolder
import com.millicast.video.TextureViewRenderer
import io.dolby.app.common.ui.fontColor
import org.webrtc.RendererCommon

@Composable
fun VideoTrack(
    sourceTrack: Map.Entry<String, TrackHolder.VideoTrackHolder>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoRenderer = remember(context) {
        TextureViewRenderer(context).apply {
            init(Media.eglBaseContext, null)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }
    }
    Box(
        modifier = modifier
            .aspectRatio(16 / 9f)
    ) {
        AndroidView(
            factory = { videoRenderer },
            update = { view ->
                view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                sourceTrack.value.videoTrack.setVideoSink(videoRenderer)
            }
        )
        SourceText(sourceId = sourceTrack.key, modifier = Modifier.align(Alignment.TopStart))
    }
}

@Composable
fun SourceText(sourceId: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(color = MaterialTheme.colors.background)
    ) {
        Text(
            text = sourceId,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium,
            color = fontColor(MaterialTheme.colors.background),
            textAlign = TextAlign.Center
        )
    }
}
