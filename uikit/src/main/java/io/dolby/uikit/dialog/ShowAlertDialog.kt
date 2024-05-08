package io.dolby.uikit.dialog

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import io.dolby.uikit.R
import io.dolby.uikit.button.ButtonType
import io.dolby.uikit.button.StyledButton
import io.dolby.uikit.utils.dpToSp
import io.dolby.uikit.utils.isTV

@Composable
fun ShowAlertDialog(
    title: String = "",
    description: String,
    buttonType: ButtonType = ButtonType.PRIMARY,
    confirmButtonTitle: String = "",
    dismissButtonTitle: String = "",
    onDismissListener: () -> Unit,
    onConfirmButtonListener: () -> Unit,
    isCancelable: Boolean = true
) {
    Column(
        modifier = Modifier
            .focusable(isTV())
    ) {
        val openDialog = remember { mutableStateOf(true) }
        val focusRequester = remember { mutableStateOf(FocusRequester()) }
        if (openDialog.value) {
            if (isTV()) {
                LaunchedEffect(Unit) {
                    focusRequester.value.requestFocus()
                }
            }
            AlertDialog(
                onDismissRequest = {
                    if (isCancelable) {
                        openDialog.value = false
                    }
                    onDismissListener()
                },
                title = {
                    Text(
                        text = title,
                        fontSize = dpToSp(dimensionResource(R.dimen._10sdp)),
                        color = MaterialTheme.colors.onBackground
                    )
                },
                text = {
                    Text(
                        description,
                        fontSize = dpToSp(dimensionResource(R.dimen._9sdp)),
                        color = MaterialTheme.colors.onBackground
                    )
                },
                confirmButton = {
                    if (confirmButtonTitle.isNotEmpty()) {
                        StyledButton(
                            buttonText = confirmButtonTitle,
                            onClickAction = {
                                if (isCancelable) {
                                    openDialog.value = false
                                }
                                onConfirmButtonListener()
                            },
                            buttonType = buttonType,
                            focusRequester = focusRequester.value
                        )
                    }
                },
                dismissButton = {
                    if (dismissButtonTitle.isNotEmpty()) {
                        StyledButton(
                            buttonText = dismissButtonTitle,
                            onClickAction = {
                                if (isCancelable) {
                                    openDialog.value = false
                                }
                                onDismissListener()
                            },
                            buttonType = buttonType,
                            focusRequester = focusRequester.value
                        )
                    }
                }
            )
        }
    }
}
