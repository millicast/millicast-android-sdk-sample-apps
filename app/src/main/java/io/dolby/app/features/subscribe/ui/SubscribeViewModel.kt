package io.dolby.app.features.subscribe.ui

import android.util.Log
import com.millicast.Core
import com.millicast.subscribers.Credential
import com.millicast.subscribers.ProjectionData
import com.millicast.subscribers.state.StreamSourceActivity
import com.millicast.subscribers.state.SubscriberConnectionState
import com.millicast.subscribers.state.TrackHolder
import com.millicast.utils.Queue
import io.dolby.app.common.StateViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubscribeViewModel(private val queue: Queue) :
    StateViewModel<SubscribeAction, SubscribeState, SubscribeEffect>() {
    private val streamIdMap = mutableMapOf<String, StreamSourceActivity>()
    val sourceVideoTracks =
        uiState.map { state -> filterVideoTrackHolder(state.tracks) } // Exclude sources with audio only

    override fun initializeState() =
        SubscribeState()

    private fun filterVideoTrackHolder(tracks: LinkedHashMap<String, List<TrackHolder>>): Map<String, TrackHolder.VideoTrackHolder> {
        return tracks.mapValues {
            it.value.filterIsInstance<TrackHolder.VideoTrackHolder>().first()
        }
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
            updateUiState {
                copy(subscriber = subscriber)
            }
            launch {
                subscriber.state.map { it.connectionState }.distinctUntilChanged().collect {
                    Log.i(TAG, "SubscriberConnectedEvent $it")
                    updateUiState {
                        copy(
                            connectionState = it,
                            isSubscribed = (it == SubscriberConnectionState.Subscribed)
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
                    if (uiState.value.tracks.size <= 1) { // Special handling here as it is required to get the main source tracks here as they are projected by default
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
            subscriber.setCredentials(
                Credential(
                    streamName = "multiview",
                    accountId = "k9Mwad",
                    apiUrl = "https://director.millicast.com/api/director/subscribe"
                )
            )
            subscriber.connect()
            subscribe()
            subscriber.enableStats(true)
            updateUiState {
                copy(
                    subscriber = subscriber
                )
            }
        }
    }

    private suspend fun subscribe() {
        if (uiState.value.connectionState == SubscriberConnectionState.Connected) {
            Log.i(TAG, "Subscribe")
            uiState.value.subscriber?.subscribe()
        }
    }

    /**
     * Project all sources. This method serves the case of projecting after being unprojected
     */
    private fun projectAll() {
        launchDefaultScope {
            uiState.value.tracks.forEach { sourceTracks ->
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
                uiState.value.subscriber?.project(sourceId, list)
            }
        }
    }

    /**
     * Project specific source. This method serves the case of projecting after being unprojected
     * @param sourceId The target source to be projected
     */
    private fun projectSource(sourceId: String) {
        val list = arrayListOf<ProjectionData?>()
        uiState.value.tracks[sourceId]?.let { tracks ->
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
                uiState.value.subscriber?.project(sourceId, list)
            }
        }
    }

    /**
     * Unproject all sources
     */
    private fun unprojectAll() {
        val mediaIdList = uiState.value.tracks.values.flatMap { trackHolderList ->
            val list = arrayListOf<String?>()
            val mediaIdList = trackHolderList.map { it.mid }
            list.addAll(mediaIdList)
            list
        }
        launchIOScope {
            uiState.value.subscriber?.unproject(ArrayList(mediaIdList))
        }
    }

    /**
     * Unproject a specific source
     * @param sourceId The target source to be unprojected
     */
    private fun unprojectSource(sourceId: String) {
        val list = arrayListOf<String?>()
        uiState.value.tracks[sourceId]?.let { tracks ->
            tracks.forEach {
                list.add(it.mid)
            }
            launchIOScope {
                uiState.value.subscriber?.unproject(list)
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
                uiState.value.subscriber?.let { subscriber ->
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
        launchDefaultScope {
            uiState.value.subscriber?.unsubscribe()
            uiState.value.subscriber?.disconnect()
        }
    }

    private fun updateSourceTracks(sourceId: String, trackHolder: TrackHolder) {
        updateUiState {
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
