package com.permissions.api

import androidx.activity.ComponentActivity
import com.permissions.models.PermissionResult

interface PermissionsManager {

    fun attachActivity(
        activity: ComponentActivity
    )

    fun detachActivity()

    fun requestLocationFlow(
        onResult: (PermissionResult) -> Unit
    )

    fun requestBluetoothFlow(
        onResult: (PermissionResult) -> Unit
    )

    fun requestFindMyWayPermissionsFlow(
        onResult: (PermissionResult) -> Unit
    )

    fun requestNotificationFlow(
        onResult: (PermissionResult) -> Unit
    )

    fun requestBackgroundLocationPermission()

    fun requestCameraFlow(
        onResult: (PermissionResult) -> Unit
    )

}