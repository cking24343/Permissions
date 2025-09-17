package com.permissions.dialogs.utils

import com.permissions.dialogs.model.DialogTags
import com.permissions.models.PermissionType

fun PermissionType.toDialogTag(): DialogTags = when (this) {
    PermissionType.Location -> {
        DialogTags.RATIONALE_LOCATION
    }

    PermissionType.BackgroundLocation -> {
        DialogTags.RATIONALE_BACKGROUND_LOCATION
    }

    PermissionType.Bluetooth -> {
        DialogTags.RATIONALE_BLUETOOTH
    }

    PermissionType.Camera -> {
        DialogTags.RATIONALE_CAMERA
    }

    PermissionType.Notification -> {
        DialogTags.RATIONALE_NOTIFICATIONS
    }
}

fun PermissionType.toDialogTagString() =
    toDialogTag().tag
