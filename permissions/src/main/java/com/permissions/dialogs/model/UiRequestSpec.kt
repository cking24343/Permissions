package com.permissions.dialogs.model

import com.permissions.enums.DismissPolicy
import com.permissions.enums.UiRequestResult
import kotlinx.coroutines.CompletableDeferred

sealed interface UiRequestSpec {
    val result: CompletableDeferred<UiRequestResult>

    // used for targeted overrides
    val tag: String?

    // unique id so we can dismiss this exact UI later
    val id: String?
    val dismissPolicy: DismissPolicy
}