package io.dolby.app.features.subscribe.ui.tracks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.dolby.app.features.subscribe.ui.SubscribeViewModel

@Composable
fun Tracks(subscribeViewModel: SubscribeViewModel, modifier: Modifier = Modifier) {
    val sourceTracks by subscribeViewModel.sourceVideoTracks.collectAsState(emptyMap())
    LazyColumn(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
        items(sourceTracks.size) {
            VideoTrack(sourceTracks.entries.elementAt(it))
        }
    }
}
