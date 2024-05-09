package io.dolby.app.common.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

abstract class ColorPalette {
    open val white = Color(0xFFFFFFFF)
    open val blackDark = Color(0xFF14141A)

    open val purple = Color(0xFFAA33FF)
    open val razzmatazz = Color(0xFFE52222)
    open val grayDark = Color(0xFF3D3D46)
    open val grayMedium = Color(0xFF34343B)
    open val grayLight = Color(0xFFB9B9BA)

    open val grayDarkFont = Color(0xFF2C2C31)
    open val grayMediumFont = Color(0xFF6A6A66)
    open val grayLightFont = Color(0xFFD8D8D8)

    // Color's outside of Dolby palette
    open val transparent = Color.Transparent
    open val transparentGray = Color(0xAA6A6A66)
    open val black = Color.Black
    open val yellow = Color(0xFFFFFF00)
    open val red = Color(0xFFFF0000)

    open val extraLightGray = Color(0xFFBBBBBF)
    open val whiteGray = Color(0xFFE6E6E7)
    open val lightRed = Color(0xFFE52222)

    open val neutralColor25 = Color(0xFFFCFCFF)
    open val neutralColor300 = Color(0xFF959599)
    open val neutralColor600 = Color(0xFF525259)
    open val neutralColor800 = Color(0xFF292930)
    open val typographyTeritiary = Color(0xFFBBBBBF)

    abstract fun asList(): Colors
}

class DarkThemeColors : ColorPalette() {
    override fun asList(): Colors {
        return darkColors(
            primary = neutralColor25,
            primaryVariant = purple,
            onPrimary = white,
            secondary = blackDark,
            secondaryVariant = neutralColor25,
            onSecondary = white,
            background = blackDark,
            onBackground = white,
            surface = grayMedium,
            onSurface = grayLightFont,
            error = razzmatazz,
            onError = white
        )
    }
}

@Composable
fun fontColor(backgroundColor: Color): Color {
    return when (backgroundColor) {
        MaterialTheme.colors.primary -> DarkThemeColors().grayDarkFont
        MaterialTheme.colors.primaryVariant -> MaterialTheme.colors.onPrimary
        MaterialTheme.colors.secondary -> MaterialTheme.colors.onSecondary
        MaterialTheme.colors.secondaryVariant -> DarkThemeColors().grayDarkFont
        MaterialTheme.colors.background -> MaterialTheme.colors.onBackground
        MaterialTheme.colors.surface -> MaterialTheme.colors.onSurface
        MaterialTheme.colors.error -> MaterialTheme.colors.onError
        DarkThemeColors().grayMedium -> MaterialTheme.colors.onPrimary
        else -> Color.Unspecified
    }
}

@Composable
internal fun borderColor(state: ViewState, buttonType: ButtonType): Color {
    return when (buttonType) {
        ButtonType.PRIMARY, ButtonType.DANGER, ButtonType.BASIC -> backgroundColor(
            state,
            buttonType = buttonType
        )
        ButtonType.SECONDARY -> when (state) {
            ViewState.Pressed,
            ViewState.Selected -> backgroundColor(state, buttonType)
            ViewState.Disabled -> MaterialTheme.colors.surface
            ViewState.Focused -> MaterialTheme.colors.secondaryVariant
            ViewState.Unknown -> MaterialTheme.colors.primaryVariant
        }
    }
}

@Composable
internal fun backgroundColor(state: ViewState, buttonType: ButtonType): Color {
    return when (state) {
        ViewState.Disabled -> {
            when (buttonType) {
                ButtonType.PRIMARY -> MaterialTheme.colors.surface
                else -> MaterialTheme.colors.secondary
            }
        }
        ViewState.Pressed, ViewState.Selected -> {
            when (buttonType) {
                ButtonType.PRIMARY -> MaterialTheme.colors.primary
                else -> MaterialTheme.colors.secondaryVariant
            }
        }
        ViewState.Focused -> {
            when (buttonType) {
                ButtonType.PRIMARY -> MaterialTheme.colors.primary
                else -> MaterialTheme.colors.secondaryVariant
            }
        }
        ViewState.Unknown -> {
            when (buttonType) {
                ButtonType.PRIMARY -> MaterialTheme.colors.primaryVariant
                ButtonType.DANGER -> MaterialTheme.colors.error
                ButtonType.BASIC -> MaterialTheme.colors.surface
                ButtonType.SECONDARY -> MaterialTheme.colors.secondary
            }
        }
    }
}
