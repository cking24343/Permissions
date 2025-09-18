package com.permissions.api

import com.permissions.enums.PermissionUiMode

data class PermissionsConfig(
    val defaultUiMode: PermissionUiMode = PermissionUiMode.Normal
)