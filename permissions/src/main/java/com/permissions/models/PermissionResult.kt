package com.permissions.models

sealed class PermissionResult {
    data object Granted : PermissionResult()
    data object Denied : PermissionResult()

    // User closed rationale dialog without granting or denying
    data object Canceled : PermissionResult()

    // Supports use cases where secondary check fails due to a dependant service being disabled
    data object ServiceDisabled : PermissionResult()

    // Supports use cases where a permission is not relevant due to the device not supporting it
    data object Unavailable : PermissionResult()
}
