package com.sample.permissions.module.ui.events

sealed interface PermissionsUiEvent {
    data object OnLocationRequest : PermissionsUiEvent
    data object OnBackgroundLocationRequest : PermissionsUiEvent
    data object OnBluetoothRequest : PermissionsUiEvent
    data object OnNotificationRequest : PermissionsUiEvent
    data object OnCameraRequest : PermissionsUiEvent
    data object OnLocationAndBluetoothRequest : PermissionsUiEvent
}