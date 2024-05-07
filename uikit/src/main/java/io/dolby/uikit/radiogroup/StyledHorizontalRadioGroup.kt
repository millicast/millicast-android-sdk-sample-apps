package io.dolby.uikit.radiogroup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.dolby.uikit.R
import io.dolby.uikit.utils.dpToSp
import java.util.Locale

@Composable
fun StyledHorizontalRadioGroup(
    radioOptions: List<String>,
    selected: String,
    onRadioButtonSelected: (String) -> Unit,
    radioGroupTitleCompose: @Composable () -> Unit,
    isEnabled: Boolean = true,
    showDivider: Boolean = true
) {
    if (radioOptions.isNotEmpty()) {
        val (selectedOption, onOptionSelected) = rememberSaveable {
            mutableStateOf(radioOptions[0])
        }
        val localFocusManager = LocalFocusManager.current
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen._10sdp)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen._10sdp))

        ) {
            radioGroupTitleCompose()
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                items(radioOptions) { item ->
                    Row(
                        Modifier
                            .padding(
                                horizontal = dimensionResource(id = R.dimen._3sdp),
                                vertical = dimensionResource(id = R.dimen._5sdp)
                            )
                            .wrapContentSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val focusState = remember { mutableStateOf(false) }
                        val radioFocus = remember { mutableStateOf(FocusRequester()) }
                        val annotatedString = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colors.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dpToSp((dimensionResource(id = R.dimen._9sdp)))
                                )
                            ) { append("  $item  ") }
                        }
                        StyledToggleButton(
                            modifier = Modifier,
                            isSelected = item.lowercase(Locale.getDefault()) == selected.lowercase(
                                Locale.getDefault()
                            ),
                            onclick = {
                                if (isEnabled) {
                                    onOptionSelected(item)
                                    onRadioButtonSelected(item)
                                }
                            },
                            content = {
                                ClickableText(
                                    text = annotatedString,
                                    onClick = {
                                        onOptionSelected(item)
                                        onRadioButtonSelected(item)
                                    }
                                )
                            }
                        )
                    }
                }
            }
            if (showDivider) {
                Divider(
                    color = MaterialTheme.colors.secondaryVariant,
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(
                            bottom = dimensionResource(
                                id = R.dimen._3sdp
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun StyledToggleButton(
    content: @Composable () -> Unit,
    modifier: Modifier,
    isSelected: Boolean,
    onclick: () -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .width(IntrinsicSize.Max)
            .then(modifier),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(),
        onClick = onclick
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
                .then(
                    if (isSelected) Modifier.background(Color(0xFF2F313F)) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
