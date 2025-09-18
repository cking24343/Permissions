package com.sample.permissions.module.utils

import androidx.compose.ui.Modifier
import com.permissions.dialogs.model.CustomSpec
import com.permissions.dialogs.PermissionRendererRegistry
import com.permissions.enums.DialogTags
import com.permissions.dialogs.utils.register
import com.permissions.enums.DismissPolicy
import com.sample.permissions.module.ui.permissions.BluetoothDialogRationale
import com.sample.permissions.module.ui.permissions.BluetoothFullscreenRationale
import com.sample.permissions.module.ui.permissions.LocationFullscreenRationale

// Examples for overriding the provided rationale dialogs from the permission module
fun buildPermissionsRequestUiOverrides() = PermissionRendererRegistry()
    .register<CustomSpec>(
        tag = DialogTags.RATIONALE_LOCATION,
        policy = DismissPolicy.KeepUntilExplicitDismiss,
    ) { uiSpec, uiResult ->
        /** Dialog example **/
        /* LocationDialogRationale(
            modifier = Modifier,
            spec = uiSpec,
            result = uiResult,
        )*/

        /** Fullscreen example **/
        LocationFullscreenRationale(
            spec = uiSpec,
            result = uiResult,
        )
    }
    .register<CustomSpec>(
        tag = DialogTags.RATIONALE_BLUETOOTH,
        policy = DismissPolicy.KeepUntilExplicitDismiss,
    ) { uiSpec, uiResult ->
        /** Dialog example **/
        /* BluetoothDialogRationale(
            modifier = Modifier,
            spec = uiSpec,
            result = uiResult,
        )*/

        /** Fullscreen example **/
        BluetoothFullscreenRationale(
            spec = uiSpec,
            result = uiResult,
        )
    }