package io.dolby.app.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDependentButton(
    permissionModel: PermissionModel,
    modifier: Modifier,
    allowText: String,
    actionText: String,
    requestClick: () -> Unit,
    actionClick: () -> Unit,
    buttonType: ButtonType,
    isEnabled: Boolean
) {
    val text = if (permissionModel.hasPermission) {
        actionText
    } else if (permissionModel.shouldShowRationale) {
        allowText
    } else {
        allowText
    }
    StyledButton(
        modifier = modifier,
        buttonText = text,
        onClickAction = {
            if (permissionModel.hasPermission) {
                actionClick()
            } else {
                requestClick()
            }
        },
        buttonType = buttonType,
        isEnabled = isEnabled
    )
}

data class PermissionModel(val hasPermission: Boolean, val shouldShowRationale: Boolean)

@OptIn(ExperimentalPermissionsApi::class)
fun PermissionState.toPermissionModel(): PermissionModel {
    return PermissionModel(status.isGranted, status.shouldShowRationale)
}

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.toPermissionModel(): PermissionModel {
    return PermissionModel(allPermissionsGranted, shouldShowRationale)
}
