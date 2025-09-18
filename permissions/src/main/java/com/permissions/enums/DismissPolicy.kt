package com.permissions.enums

enum class DismissPolicy {
    /** default: close the UI when the override calls complete(…) */
    OnResult,

    /** keep UI visible until the library/app explicitly dismisses it */
    KeepUntilExplicitDismiss,
}