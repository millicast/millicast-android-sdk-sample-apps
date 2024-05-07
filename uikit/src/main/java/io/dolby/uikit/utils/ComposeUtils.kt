package io.dolby.uikit.utils

import android.content.res.Configuration
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) {
    if (isTV() && dp.value < 16) // Min Tv font size is 16sp, So we set it to a higher value
        {
            Dp(16.0f).toSp()
        } else {
        dp.toSp()
    }
}

@Composable
fun isTablet(): Boolean = with(LocalConfiguration.current.screenWidthDp) {
    return (this >= 600)
}

fun LazyListState.isLastVisibleItem(index: Int) =
    layoutInfo.visibleItemsInfo.lastOrNull()?.index == index

@Composable
fun isTV(): Boolean = with(LocalContext.current) {
    return (this.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK))
}

@Composable
fun isLandscapeMode(): Boolean {
    val configuration = LocalConfiguration.current
    return when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            true
        }

        else -> {
            false
        }
    }
}

private class CustomHighlightIndicationInstance(
    isEnabledState: State<Boolean>,
    private val indicationElement: CustomHighlightIndication.IndicationElement
) :
    IndicationInstance {
    private val isEnabled by isEnabledState
    override fun ContentDrawScope.drawIndication() {
        drawContent()
        if (isEnabled) {
            when (indicationElement) {
                CustomHighlightIndication.IndicationElement.CIRLCE -> {
                    drawRect(color = Color.White, alpha = 0.4f)
                }

                else -> {
                    drawRect(color = Color.White, alpha = 0.4f)
                }
            }
        }
    }
}

class CustomHighlightIndication(private val indicationElement: IndicationElement = IndicationElement.CIRLCE) :
    Indication {
    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource):
        IndicationInstance {
        val isFocusedState = interactionSource.collectIsFocusedAsState()
        return remember(interactionSource) {
            CustomHighlightIndicationInstance(
                isEnabledState = isFocusedState,
                indicationElement = indicationElement
            )
        }
    }

    enum class IndicationElement {
        CIRLCE,
        RECTANGLE
    }
}
