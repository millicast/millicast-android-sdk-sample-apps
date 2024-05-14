package io.dolby.app.features.subscribe.ui

import com.millicast.Subscriber
import com.millicast.subscribers.state.SubscriberConnectionState
import com.millicast.subscribers.state.TrackHolder
import io.dolby.app.common.ModelState
import io.dolby.app.common.ViewAction
import io.dolby.app.common.ViewSideEffect
import io.dolby.app.common.ViewUIState

data class SubscribeModelState(
    val connectionState: SubscriberConnectionState = SubscriberConnectionState.Disconnected,
    val subscriber: Subscriber? = null,
    val tracks: LinkedHashMap<String, List<TrackHolder>> = linkedMapOf()
) : ModelState

data class SubscribeUiState(
    val shouldShowTracks: Boolean = false,
    val sourceVideoTracks: Map<String, TrackHolder.VideoTrackHolder> = emptyMap()
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
