package io.dolby.app.features.subscribe.ui

import android.util.Log
import com.millicast.Core
import com.millicast.subscribers.Credential
import com.millicast.subscribers.remote.RemoteAudioTrack
import com.millicast.subscribers.remote.RemoteVideoTrack
import com.millicast.subscribers.state.SubscriberConnectionState
import io.dolby.app.common.MultipleStatesViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubscribeViewModel(private val isMultiView: Boolean) :
    MultipleStatesViewModel<SubscribeAction, SubscribeUiState, SubscribeModelState, SubscribeEffect>() {
    init {
        updateUiState {
            copy(isMultiView = this@SubscribeViewModel.isMultiView)
        }
    }

    override fun initializeUiState() = SubscribeUiState()

    override fun initializeState() = SubscribeModelState()

    override fun reduceToUi(
        state: SubscribeModelState,
        uiState: SubscribeUiState
    ): SubscribeUiState {
        return uiState.copy(
            sourceVideoTracks = state.tracks,
            shouldShowTracks = (state.connectionState == SubscriberConnectionState.Subscribed),
            audioTrack = state.audioTrack
        )
    }

    override fun onUiAction(action: SubscribeAction) {
        when (action) {
            is SubscribeAction.Subscribe -> {
                startSubscription()
            }

            is SubscribeAction.Disconnect -> {
                disconnect()
            }

            else -> {
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
                        copy(connectionState = it)
                    }
                    if (it == SubscriberConnectionState.Connected) {
                        Log.i(TAG, "SubscriberConnectedEvent")
                        subscribe()
                    }
                }
            }
            launch {
                subscriber.onRemoteTrack.collect { trackHolder ->
                    when (trackHolder) {
                        is RemoteAudioTrack -> {
                            if (state.value.audioTrack == null || state.value.audioTrack?.isActive == false) {
                                trackHolder.setVolume(1.0)
                                updateModelStateAndReduceToUi {
                                    copy(audioTrack = trackHolder)
                                }
                            }
                        }

                        is RemoteVideoTrack -> {
                            updateModelStateAndReduceToUi {
                                val newTracks = ArrayList(state.value.tracks)
                                newTracks.add(trackHolder)
                                copy(tracks = newTracks)
                            }
                        }
                    }
                }
            }
            subscriber.setCredentials(getCredentials())
            subscriber.connect()
            updateModelState {
                copy(subscriber = subscriber)
            }
        }
    }

    private fun getCredentials(): Credential {
        return if (isMultiView) {
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
        if (state.value.connectionState != SubscriberConnectionState.Subscribed) {
            Log.i(TAG, "Subscribe")
            state.value.subscriber?.subscribe()
        }
    }

    fun disconnect() {
        state.value.subscriber?.disconnect()
        state.value.subscriber?.release()
    }

    companion object {
        private const val TAG = "SubscribeViewModel"
    }
}
