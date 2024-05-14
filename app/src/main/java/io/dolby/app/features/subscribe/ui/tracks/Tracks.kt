package io.dolby.app.features.subscribe.ui.tracks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.dolby.app.features.subscribe.ui.SubscribeViewModel

@Composable
fun Tracks(subscribeViewModel: SubscribeViewModel, modifier: Modifier = Modifier) {
    val uiState by subscribeViewModel.uiState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (uiState.isMultiView) Arrangement.Top else Arrangement.Center
    ) {
        items(uiState.sourceVideoTracks.size) {
            VideoTrack(uiState.sourceVideoTracks.entries.elementAt(it))
        }
    }
}
