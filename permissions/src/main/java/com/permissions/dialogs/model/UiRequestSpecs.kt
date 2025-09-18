package com.permissions.dialogs.model

import kotlinx.coroutines.CompletableDeferred
import androidx.compose.ui.window.DialogProperties
import com.permissions.enums.DismissPolicy
import com.permissions.enums.UiRequestResult

/** Simple two-button confirm */
data class ConfirmDialogSpec(
    val title: String,
    val message: String,
    val positiveText: String = "Continue",
    val negativeText: String = "Cancel",
    override val tag: String? = null,
    override val id: String = java.util.UUID.randomUUID().toString(),
    override val dismissPolicy: DismissPolicy = DismissPolicy.OnResult,
    override val result: CompletableDeferred<UiRequestResult> = CompletableDeferred()
) : UiRequestSpec

/** Branded dialog (module default) */
data class CustomSpec(
    val properties: DialogProperties = DialogProperties(),
    // pass raw strings so this works from your :permissions module
    val title: String,
    val message: String? = null,
    val positiveText: String? = null,
    val negativeText: String? = null,
    // optional images / styling flags your DialogContent needs
    val primaryImageRes: Int? = null,
    val primaryImageContentDescription: String? = null,
    val secondaryImageRes: Int? = null,
    val secondaryImageContentDescription: String? = null,
    override val tag: String? = null,
    override val id: String = java.util.UUID.randomUUID().toString(),
    override val dismissPolicy: DismissPolicy = DismissPolicy.OnResult,
    override val result: CompletableDeferred<UiRequestResult> = CompletableDeferred()
) : UiRequestSpec
