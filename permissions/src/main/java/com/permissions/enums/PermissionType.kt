package com.permissions.enums

enum class PermissionType {
    Location,
    BackgroundLocation,
    Bluetooth,
    Camera,
    Notification;

    fun dataStoreKey(): String = "${name.lowercase()}_permission_state"
}