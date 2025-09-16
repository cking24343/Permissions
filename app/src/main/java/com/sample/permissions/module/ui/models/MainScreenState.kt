package com.sample.permissions.module.ui.models

import androidx.compose.runtime.State
import com.sample.permissions.module.ui.events.PermissionsUiEvent

data class MainScreenState(
    val onEvent: (PermissionsUiEvent) -> Unit,
) : ComposeState