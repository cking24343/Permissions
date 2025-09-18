package com.permissions.enums

/**
 * These states will act as an audit log for each permission and help inform UX decisions like:
 *
 *  - Whether to re-show a rationale .
 *  - Whether to guide the user to app settings.
 *
 * */
enum class PermissionState {
    NotRequested,       // Never asked
    RationaleShown,     // In-app rationale dialog shown
    SystemPromptShown,  // System dialog triggered
    Granted,            // User granted permission
    Denied,             // User denied permission
    Canceled,           // User canceled rationale dialog
    ManualUpgradeRequested, // User was directed to settings to manually enable precise location
    ManuallyRevoked,     // System says permission is revoked, but last known state was granted
    ServiceDisabled,

    // Location Specific
    PreciseGranted,
    CoarseGranted,
    PreciseDenied,
    PreciseRationaleShown,     // In-app rationale dialog shown
    PreciseDeniedPermanently,
}