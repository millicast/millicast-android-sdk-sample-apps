package io.dolby.app.features.subscribe.ui

import com.millicast.Subscriber
import com.millicast.subscribers.remote.RemoteAudioTrack
import com.millicast.subscribers.remote.RemoteVideoTrack
import com.millicast.subscribers.state.SubscriberConnectionState
import io.dolby.app.common.ModelState
import io.dolby.app.common.ViewAction
import io.dolby.app.common.ViewSideEffect
import io.dolby.app.common.ViewUIState

data class SubscribeModelState(
    val connectionState: SubscriberConnectionState = SubscriberConnectionState.Disconnected,
    val subscriber: Subscriber? = null,
    val tracks: ArrayList<RemoteVideoTrack> = arrayListOf(),
    var audioTrack: RemoteAudioTrack? = null
) : ModelState

data class SubscribeUiState(
    val shouldShowTracks: Boolean = false,
    val isMultiView: Boolean = false,
    val sourceVideoTracks: ArrayList<RemoteVideoTrack> = arrayListOf(),
    var audioTrack: RemoteAudioTrack? = null
) : ViewUIState

sealed class SubscribeAction : ViewAction {
    data object Subscribe : SubscribeAction()
    data object Disconnect : SubscribeAction()
    data class Pause(val sourceId: String? = null) : SubscribeAction()
    data class Resume(val sourceId: String? = null) : SubscribeAction()
}

sealed class SubscribeEffect : ViewSideEffect {
    data class ShowError(val reason: String) : SubscribeEffect()
}
