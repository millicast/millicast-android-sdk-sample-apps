package io.dolby.app.features.subscribe.ui

import android.util.Log
import com.millicast.Core
import com.millicast.subscribers.Credential
import com.millicast.subscribers.ProjectionData
import com.millicast.subscribers.state.StreamSourceActivity
import com.millicast.subscribers.state.SubscriberConnectionState
import com.millicast.subscribers.state.TrackHolder
import com.millicast.utils.Queue
import io.dolby.app.common.MultipleStatesViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubscribeViewModel(private val queue: Queue, private val isMultiView: Boolean) :
    MultipleStatesViewModel<SubscribeAction, SubscribeUiState, SubscribeModelState, SubscribeEffect>() {
    private val streamIdMap = mutableMapOf<String, StreamSourceActivity>()

    init {
        updateUiState {
            copy(isMultiView = this@SubscribeViewModel.isMultiView)
        }
    }

    override fun initializeUiState(): SubscribeUiState {
        return SubscribeUiState()
    }

    override fun initializeState() = SubscribeModelState()

    override fun reduceToUi(
        state: SubscribeModelState,
        uiState: SubscribeUiState
    ): SubscribeUiState {
        return uiState.copy(
            sourceVideoTracks = if (state.tracks.isNotEmpty()) filterVideoTrackHolder(state.tracks) else emptyMap(),
            shouldShowTracks = (state.connectionState == SubscriberConnectionState.Subscribed)
        )
    }

    private fun filterVideoTrackHolder(tracks: LinkedHashMap<String, List<TrackHolder>>): Map<String, TrackHolder.VideoTrackHolder> {
        val videoTracksMap = tracks.mapValues {
            it.value.filterIsInstance<TrackHolder.VideoTrackHolder>().firstOrNull()
        }
        val nonNullTracks = linkedMapOf<String, TrackHolder.VideoTrackHolder>()
        videoTracksMap.forEach { entry ->
            entry.value?.let {
                nonNullTracks.putIfAbsent(entry.key, it)
            }
        }
        return nonNullTracks
    }

    override fun onUiAction(action: SubscribeAction) {
        when (action) {
            is SubscribeAction.Subscribe -> {
                startSubscription()
            }

            is SubscribeAction.Pause -> {
                action.sourceId?.let {
                    unprojectSource(it)
                } ?: run {
                    unprojectAll()
                }
            }

            is SubscribeAction.Resume -> {
                action.sourceId?.let {
                    projectSource(it)
                } ?: run {
                    projectAll()
                }
            }

            is SubscribeAction.Disconnect -> {
                disconnect()
            }
        }
    }

    fun startSubscription() {
        launchDefaultScope {
            val subscriber = Core.createSubscriber()
            updateModelState {
                copy(subscriber = subscriber)
            }
            launch {
                subscriber.state.map { it.connectionState }.distinctUntilChanged().collect {
                    Log.i(TAG, "SubscriberConnectedEvent $it")
                    updateModelStateAndReduceToUi {
                        copy(
                            connectionState = it
                        )
                    }
                    if (it == SubscriberConnectionState.Connected) {
                        Log.i(TAG, "SubscriberConnectedEvent")
                        subscribe()
                    }
                }
            }
            launch {
                subscriber.tracks.collect { trackHolder ->
                    if (state.value.tracks.size <= 1) { // Special handling here as it is required to get the main source tracks here as they are projected by default
                        updateSourceTracks(MAIN_SOURCE_ID, trackHolder)
                    }
                    when (trackHolder) {
                        is TrackHolder.AudioTrackHolder -> {
                            trackHolder.audioTrack.setVolume(1.0)
                        }

                        else -> {}
                    }
                }
            }
            launch {
                subscriber.state.map { it.streamSourceActivities }
                    .collect { streamSourceActivities ->
                        queue.post {
                            streamSourceActivities.forEach {
                                projectStreamActivityNewTracks(it)
                            }
                        }
                    }
            }
            subscriber.setCredentials(getCredentials())
            subscriber.connect()
            subscribe()
            subscriber.enableStats(true)
            updateModelState {
                copy(
                    subscriber = subscriber
                )
            }
        }
    }

    private fun getCredentials(): Credential {
        return if (uiState.value.isMultiView) {
            Credential(
                streamName = "multiview",
                accountId = "k9Mwad",
                apiUrl = "https://director.millicast.com/api/director/subscribe"
            )
        } else {
            Credential(
                streamName = "singleview",
                accountId = "k9Mwad",
                apiUrl = "https://director.millicast.com/api/director/subscribe"
            )
        }
    }

    private suspend fun subscribe() {
        if (state.value.connectionState == SubscriberConnectionState.Connected) {
            Log.i(TAG, "Subscribe")
            state.value.subscriber?.subscribe()
        }
    }

    /**
     * Project all sources. This method serves the case of resuming after triggering a pause.
     * Whether it was due to coming into foreground mode, or after a user intent to resume
     * via controller.
     * Another case also if we paused and we want to resume, so you have to store sourceId & list of
     * projected tracks.
     * @see [link](https://docs.dolby.io/streaming-apis/docs/android-getting-started-with-subscribing#6-project-media)
     */
    private fun projectAll() {
        launchDefaultScope {
            state.value.tracks.forEach { sourceTracks ->
                val list = arrayListOf<ProjectionData?>()

                // Returning null as it represents the main feed source id
                val sourceId = if (sourceTracks.key == MAIN_SOURCE_ID) null else sourceTracks.key
                sourceTracks.value.forEach {
                    list.add(
                        ProjectionData(
                            trackId = it.track.kind.name.lowercase(),
                            media = it.track.kind.name.lowercase(),
                            mid = it.mid!!
                        )
                    )
                }
                state.value.subscriber?.project(sourceId, list)
            }
        }
    }

    /**
     * Project specific source. This method serves the case of resuming after pausing
     * @param sourceId The target source to be projected
     */
    private fun projectSource(sourceId: String) {
        val list = arrayListOf<ProjectionData?>()
        state.value.tracks[sourceId]?.let { tracks ->
            tracks.forEach {
                list.add(
                    ProjectionData(
                        trackId = it.track.kind.name.lowercase(),
                        media = it.track.kind.name.lowercase(),
                        mid = it.mid!!
                    )
                )
            }
            launchIOScope {
                state.value.subscriber?.project(sourceId, list)
            }
        }
    }

    /**
     * Unproject all sources using mid (The media id), So we're gathering our active mediaId list
     * to unproject while still subscribed.
     */
    private fun unprojectAll() {
        val mediaIdList = state.value.tracks.values.flatMap { trackHolderList ->
            val list = arrayListOf<String?>()
            val mediaIdList = trackHolderList.map { it.mid }
            list.addAll(mediaIdList)
            list
        }
        launchIOScope {
            if (state.value.connectionState == SubscriberConnectionState.Subscribed) {
                state.value.subscriber?.unproject(ArrayList(mediaIdList))
            }
        }
    }

    /**
     * Unproject a specific source
     * @param sourceId The target source to be unprojected
     */
    private fun unprojectSource(sourceId: String) {
        val list = arrayListOf<String?>()
        state.value.tracks[sourceId]?.let { tracks ->
            tracks.forEach {
                list.add(it.mid)
            }
            launchIOScope {
                state.value.subscriber?.unproject(list)
            }
        }
    }

    /**
     * This method project every newly added track for every new source. Considering that we don't
     * Want to project the main feed(Which has a null sourceId) again as it is projected by default.
     * @param sourceId The target source to be projected
     */
    private suspend fun projectStreamActivityNewTracks(streamSourceActivity: StreamSourceActivity) {
        val sourceId = streamSourceActivity.sourceId ?: MAIN_SOURCE_ID
        val cached = streamIdMap.getOrPut(
            sourceId
        ) { streamSourceActivity.copy(activeTracks = emptyArray()) }

        val cachedTracks = cached.activeTracks.toMutableList()
        val newTracks = streamSourceActivity.activeTracks.filter { activeTrack ->
            null == cached.activeTracks.find { it.trackId == activeTrack.trackId }
        }
        cachedTracks.addAll(newTracks)
        streamSourceActivity.copy(activeTracks = cachedTracks.toTypedArray())
        streamIdMap[sourceId] = streamSourceActivity

        streamSourceActivity.sourceId?.let { // Prevent re-projecting the main source
            newTracks.forEach {
                state.value.subscriber?.let { subscriber ->
                    val newlyAddedLocalTrack = subscriber.addRemoteTrackForResult(it.media)
                    val projectionList = arrayListOf<ProjectionData?>(
                        ProjectionData(
                            trackId = it.trackId,
                            media = it.media.name.lowercase(),
                            mid = newlyAddedLocalTrack.mid!!
                        )
                    )
                    subscriber.project(streamSourceActivity.sourceId, projectionList)
                    updateSourceTracks(sourceId, newlyAddedLocalTrack)
                }
            }
        }
    }

    fun disconnect() {
        state.value.subscriber?.disconnect()
        state.value.subscriber?.release()
    }

    private fun updateSourceTracks(sourceId: String, trackHolder: TrackHolder) {
        updateModelStateAndReduceToUi {
            val tracksCopy = linkedMapOf<String, List<TrackHolder>>()
            tracksCopy.putAll(this.tracks)
            tracksCopy.putIfAbsent(sourceId, emptyList())
            val trackHolderList = tracksCopy[sourceId].orEmpty()
            val updatedTrackHolderList =
                if (trackHolderList.indexOfFirst { it.track.kind == trackHolder.track.kind } >= 0) trackHolderList else trackHolderList.plus(
                    trackHolder
                ) // Prevent adding same track holder
            tracksCopy[sourceId] = updatedTrackHolderList
            Log.i(TAG, "sourceTracks ${this.tracks}")
            copy(tracks = tracksCopy)
        }
    }

    companion object {
        private const val TAG = "SubscribeViewModel"
        private const val MAIN_SOURCE_ID = "CAM1"
    }
}
