package com.permissions.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PermissionsDialogHost(
    service: DialogService,
    registry: DialogRendererRegistry = DialogRendererRegistry(),
) {
    var current by remember { mutableStateOf<DialogSpec?>(null) }

    LaunchedEffect(Unit) {
        service.requests.collectLatest { current = it }
    }

    current?.let { spec ->
        val complete: (DialogResult) -> Unit = { res ->
            if (!spec.result.isCompleted) {
                spec.result.complete(res)
            }
        }

        // 1) App override by tag/type
        val override = registry.resolve(spec)
        if (override != null) {
            override.Render(spec) { result ->
                complete(result); current = null
            }
            return
        }

        // 2) Module defaults (fallbacks)
        when (spec) {
            is ConfirmSpec -> {
                AlertDialog(
                    onDismissRequest = { complete(DialogResult.Dismissed); current = null },
                    title = { Text(spec.title) },
                    text = { Text(spec.message) },
                    confirmButton = {
                        TextButton(onClick = {
                            complete(DialogResult.Confirmed);
                            current = null
                        }) {
                            Text(spec.positiveText)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            complete(DialogResult.Dismissed);
                            current = null
                        }) {
                            Text(spec.negativeText)
                        }
                    }
                )
            }

            is CustomSpec -> {
                BrandedDialog(
                    // your module’s default look
                    properties = spec.properties.let { properties ->
                        DialogProperties(
                            dismissOnBackPress = properties.dismissOnBackPress,
                            dismissOnClickOutside = properties.dismissOnClickOutside,
                            usePlatformDefaultWidth = true
                        )
                    },
                    title = spec.title,
                    message = spec.message,
                    positive = spec.positiveText,
                    negative = spec.negativeText,
                    primaryImageRes = spec.primaryImageRes,
                    secondaryImageRes = spec.secondaryImageRes,
                    onConfirm = {
                        complete(DialogResult.Confirmed);
                        current = null
                    },
                    onDismiss = {
                        complete(DialogResult.Dismissed);
                        current = null
                    },
                )
            }
        }
    }
}
