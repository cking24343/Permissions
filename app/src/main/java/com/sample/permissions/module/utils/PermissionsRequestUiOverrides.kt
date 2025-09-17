package com.sample.permissions.module.utils

import androidx.compose.ui.Modifier
import com.permissions.dialogs.model.CustomSpec
import com.permissions.dialogs.PermissionRendererRegistry
import com.permissions.dialogs.model.DialogTags
import com.permissions.dialogs.utils.register
import com.sample.permissions.module.ui.permissions.BluetoothDialogRationale
import com.sample.permissions.module.ui.permissions.LocationFullscreenRationale

// Examples for overriding the provided rationale dialogs from the permission module
fun buildPermissionsRequestUiOverrides() = PermissionRendererRegistry()
    .register<CustomSpec>(DialogTags.RATIONALE_LOCATION) { uiSpec, uiResult ->
        /** Dialog example **/
        /*LocationDialogRationale(
            modifier = Modifier,
            spec = uiSpec,
            result = uiResult,
        )*/

        /** Fullscreen example **/
        LocationFullscreenRationale(
            result = uiResult,
        )
    }
    .register<CustomSpec>(DialogTags.RATIONALE_BLUETOOTH) { uiSpec, uiResult ->
        BluetoothDialogRationale(
            modifier = Modifier,
            spec = uiSpec,
            result = uiResult,
        )
    }