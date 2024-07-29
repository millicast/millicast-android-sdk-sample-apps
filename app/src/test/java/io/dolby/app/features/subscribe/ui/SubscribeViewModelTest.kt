package io.dolby.app.features.subscribe.ui

import com.millicast.Core
import com.millicast.Subscriber
import com.millicast.devices.track.VideoTrack
import com.millicast.subscribers.ProjectionData
import com.millicast.subscribers.SubscriberState
import com.millicast.subscribers.state.SubscriberConnectionState
import com.millicast.subscribers.state.TrackHolder
import com.millicast.utils.Queue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.koin.test.KoinTest
import org.mockito.kotlin.any

class SubscribeViewModelTest : KoinTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var testScope: TestScope
    private val subscribeViewModel: SubscribeViewModel by inject(
        SubscribeViewModel::class.java
    )

    @Before
    fun setup() {
        startKoin {
            modules(
                module {
                    factory { Queue() }
                    viewModel { SubscribeViewModel(get(), true) }
                }
            )
        }
        Dispatchers.setMain(dispatcher)
        testScope = TestScope(dispatcher)
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun `verify that start subscription is called when Subscribe Action is triggered`() = runTest {
        val viewModelSpy = spyk<SubscribeViewModel>(subscribeViewModel)
        viewModelSpy.onUiAction(SubscribeAction.Subscribe)
        verify(exactly = 1) { viewModelSpy.startSubscription() }
    }

    @Test
    fun `verify sequence of subscriber calls when start subscription is called`() = runTest {
        val subscriber = mockSubscriber()
        subscribeViewModel.updateModelState {
            copy(connectionState = SubscriberConnectionState.Connected, subscriber = subscriber)
        }
        subscribeViewModel.onUiAction(SubscribeAction.Subscribe)
        assert(subscribeViewModel.state.value.subscriber != null)
        verifyOrder {
            Core.createSubscriber()
            testScope.launch {
                subscriber.setCredentials(any())
            }

            testScope.launch {
                subscriber.connect()
            }
            testScope.launch {
                subscriber.subscribe()
            }
        }
    }

    @Test
    fun `verify that subscribe is not called if subscription state is not connected`() = runTest {
        val subscriber = mockSubscriber()
        subscribeViewModel.updateModelState {
            copy(connectionState = SubscriberConnectionState.Connecting, subscriber = subscriber)
        }
        subscribeViewModel.onUiAction(SubscribeAction.Subscribe)
        assert(subscribeViewModel.state.value.subscriber != null)
        verifyOrder {
            Core.createSubscriber()
            testScope.launch {
                subscriber.setCredentials(any())
            }

            testScope.launch {
                subscriber.connect()
            }
        }
        verify(exactly = 0) { testScope.launch { subscriber.subscribe(any()) } }
    }

    @Test
    fun `verify that project is called with the correct arguments when resume action is triggered`() =
        runTest {
            val subscriber = mockSubscriber()
            val projectedTracks: LinkedHashMap<String, List<TrackHolder>> = linkedMapOf(
                "CAM2" to listOf(
                    TrackHolder.VideoTrackHolder("1", mockk<VideoTrack>(relaxed = true)),
                    TrackHolder.VideoTrackHolder("2", mockk<VideoTrack>(relaxed = true))
                )
            )
            subscribeViewModel.updateModelState {
                copy(
                    connectionState = SubscriberConnectionState.Subscribed,
                    subscriber = subscriber,
                    tracks = projectedTracks
                )
            }
            subscribeViewModel.onUiAction(SubscribeAction.Resume())
            val projectionArrayList =
                arrayListOf<ProjectionData?>(
                    ProjectionData(mid = "1", layer = null),
                    ProjectionData(mid = "2", layer = null)
                )
            verify(exactly = 1) {
                testScope.launch {
                    subscriber.project(
                        "CAM2",
                        projectionArrayList
                    )
                }
            }
        }

    @Test
    fun `verify that unproject is called with the correct arguments when pause action is triggered`() =
        runTest {
            val subscriber = mockSubscriber()
            val projectedTracks: LinkedHashMap<String, List<TrackHolder>> = linkedMapOf(
                "CAM2" to listOf(
                    TrackHolder.VideoTrackHolder("1", mockk<VideoTrack>(relaxed = true)),
                    TrackHolder.VideoTrackHolder("2", mockk<VideoTrack>(relaxed = true))
                )
            )
            subscribeViewModel.updateModelState {
                copy(
                    connectionState = SubscriberConnectionState.Subscribed,
                    subscriber = subscriber,
                    tracks = projectedTracks
                )
            }
            subscribeViewModel.onUiAction(SubscribeAction.Pause("CAM2"))
            val projectionArrayList = arrayListOf<String?>("1", "2")
            verify(exactly = 1) { testScope.launch { subscriber.unproject(projectionArrayList) } }
        }

    @Test
    fun `verify releasing resources and disconnecting correctly when disconnect action is triggered`() =
        runTest {
            val subscriber = mockSubscriber()
            subscribeViewModel.updateModelState {
                copy(
                    connectionState = SubscriberConnectionState.Subscribed,
                    subscriber = subscriber
                )
            }
            subscribeViewModel.onUiAction(SubscribeAction.Disconnect)
            verify(exactly = 1) { testScope.launch { subscriber.disconnect() } }
            verify(exactly = 1) { testScope.launch { subscriber.release() } }
        }

    private fun mockSubscriber(): Subscriber {
        val subscriber = mockk<Subscriber>(relaxed = true)
        runTest {
            mockkObject(Core)
            every { Core.createSubscriber() } returns subscriber
            val stateFlow = MutableSharedFlow<SubscriberState>()
            stateFlow.emit(
                SubscriberState()
            )
            val tracksFlow = MutableSharedFlow<TrackHolder>()
            stateFlow.emit(
                any()
            )
            every { subscriber.state } returns stateFlow.asSharedFlow()
            every { subscriber.tracks } returns tracksFlow.asSharedFlow()
        }
        return subscriber
    }
}
