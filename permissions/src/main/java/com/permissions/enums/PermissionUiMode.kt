package com.permissions.enums

enum class PermissionUiMode {
    // Default behavior for show rationales
    Normal,

    // Overridable to allow skipping all module UI for just using system prompts
    NoRationales,
}