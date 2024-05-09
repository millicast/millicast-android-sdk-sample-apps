package io.dolby.app.common.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.dolby.millicast.androidsdk.sampleapps.R

internal val buttonContentDescriptionId = R.string.button_contentDescription

enum class ButtonType {
    PRIMARY, SECONDARY, DANGER, BASIC
}

@Preview
@Composable
fun StyledButton(
    modifier: Modifier = Modifier.fillMaxSize().heightIn(min = 40.dp),
    buttonText: String = "",
    focusRequester: FocusRequester = FocusRequester(),
    onClickAction: ((Context) -> Unit)? = null,
    buttonType: ButtonType = ButtonType.PRIMARY,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    isLarge: Boolean = false,
    capitalize: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val context = LocalContext.current
    val viewState = ViewState.from(isPressed, isSelected, isFocused, isEnabled)

    val backgroundColor = backgroundColor(state = viewState, buttonType = buttonType)
    val fontColor = if (isEnabled) fontColor(backgroundColor) else MaterialTheme.colors.onSurface
    val borderColor = borderColor(viewState, buttonType)
    val buttonContentDescription = "$buttonText ${ stringResource(id = buttonContentDescriptionId) }"

    Button(
        modifier = modifier.semantics { contentDescription = buttonContentDescription }.focusRequester(focusRequester),
        interactionSource = interactionSource,
        onClick = { onClickAction?.invoke(context) },
        content = {
            Text(
                text = if (capitalize) buttonText.uppercase() else buttonText,
                style = MaterialTheme.typography.button,
                textAlign = TextAlign.Center,
                color = fontColor
            )
        },
        colors = ButtonDefaults.textButtonColors(
            contentColor = fontColor,
            backgroundColor = backgroundColor
        ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, borderColor),
        enabled = isEnabled
    )
}
