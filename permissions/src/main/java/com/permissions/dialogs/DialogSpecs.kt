package com.permissions.dialogs

import kotlinx.coroutines.CompletableDeferred
import androidx.compose.ui.window.DialogProperties

enum class DialogResult {
    Confirmed,
    Dismissed,
}

sealed interface DialogSpec {
    val result: CompletableDeferred<DialogResult>

    // used for targeted overrides
    val tag: String?
}

/** Simple two-button confirm */
data class ConfirmSpec(
    val title: String,
    val message: String,
    val positiveText: String = "Continue",
    val negativeText: String = "Cancel",
    override val tag: String? = null,
    override val result: CompletableDeferred<DialogResult> = CompletableDeferred()
) : DialogSpec

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
    override val result: CompletableDeferred<DialogResult> = CompletableDeferred()
) : DialogSpec
