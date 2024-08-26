package io.dolby.app.features.publish.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.millicast.Media
import com.millicast.devices.track.VideoTrack
import com.millicast.video.TextureViewRenderer
import org.webrtc.RendererCommon

@Composable
fun PreviewVideoTrack(sourceTrack: VideoTrack, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val videoRenderer = remember(context) {
        TextureViewRenderer(context).apply {
            init(Media.eglBaseContext, null)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        }
    }
    Box(
        modifier = modifier
            .aspectRatio(16 / 9f),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.testTag("Preview"),
            factory = { videoRenderer },
            update = { view ->
                view.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                sourceTrack.setVideoSink(videoRenderer)
            }
        )
    }
}
