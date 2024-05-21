package io.dolby.app.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DolbyButtonsContainer(
    modifier: Modifier = Modifier,
    screenName: String,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    val background = MaterialTheme.colors.background
    Scaffold(
        topBar = {
            TopActionBar()
        },
        bottomBar = {
            DolbyCopyrightFooterView()
        }
    ) { paddingValues ->
        DolbyBackgroundBox(
            modifier = Modifier
                .padding(paddingValues)
                .semantics { contentDescription = screenName }
        ) {
            Column(
                horizontalAlignment = horizontalAlignment,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.Center)
                    .background(background, shape = RoundedCornerShape(4.dp))
                    .clip(MaterialTheme.shapes.large)
                    .padding(horizontal = 30.dp)
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = screenName,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium,
                    color = fontColor(MaterialTheme.colors.background),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}
