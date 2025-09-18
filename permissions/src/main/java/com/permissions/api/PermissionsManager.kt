package com.permissions.api

import androidx.activity.ComponentActivity
import com.permissions.enums.PermissionUiMode
import com.permissions.models.PermissionResult

interface PermissionsManager {

    fun attachActivity(
        activity: ComponentActivity
    )

    fun detachActivity()

    fun requestLocationFlow(
        onResult: (PermissionResult) -> Unit,
    )

    fun requestLocationFlow(
        uiMode: PermissionUiMode,
        onResult: (PermissionResult) -> Unit,
    )

    fun requestBluetoothFlow(
        onResult: (PermissionResult) -> Unit,
    )

    fun requestBluetoothFlow(
        uiMode: PermissionUiMode,
        onResult: (PermissionResult) -> Unit,
    )

    fun requestCombinedLocationAndBluetoothPermissionsFlow(
        onResult: (PermissionResult) -> Unit,
    )

    fun requestCombinedLocationAndBluetoothPermissionsFlow(
        uiMode: PermissionUiMode,
        onResult: (PermissionResult) -> Unit,
    )

    fun requestNotificationFlow(
        onResult: (PermissionResult) -> Unit,
    )

    fun requestNotificationFlow(
        uiMode: PermissionUiMode,
        onResult: (PermissionResult) -> Unit,
    )

    fun requestBackgroundLocationPermission()

    fun requestBackgroundLocationPermission(
        uiMode: PermissionUiMode,
    )

    fun requestCameraFlow(
        onResult: (PermissionResult) -> Unit,
    )

    fun requestCameraFlow(
        uiMode: PermissionUiMode,
        onResult: (PermissionResult) -> Unit,
    )

}