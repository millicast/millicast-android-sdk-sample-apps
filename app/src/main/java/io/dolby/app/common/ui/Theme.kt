package io.dolby.app.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MillicastTheme(content: @Composable () -> Unit) {
    val colors = getColorPalette().asList()

    MaterialTheme(
        colors = colors,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun getColorPalette(): ColorPalette {
    return if (isSystemInDarkTheme()) DarkThemeColors() else DarkThemeColors() // LightThemeColors()
}
