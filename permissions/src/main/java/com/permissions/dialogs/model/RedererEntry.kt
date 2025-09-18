package com.permissions.dialogs.model

import com.permissions.dialogs.PermissionUiRenderer
import com.permissions.enums.DismissPolicy

data class RendererEntry(
    val renderer: PermissionUiRenderer,
    val policyOverride: DismissPolicy? = null
)