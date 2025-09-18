package com.permissions.dialogs.ui

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
import com.permissions.dialogs.model.ConfirmDialogSpec
import com.permissions.dialogs.model.CustomSpec
import com.permissions.dialogs.PermissionRendererRegistry
import com.permissions.enums.UiRequestResult
import com.permissions.dialogs.PermissionRequestService
import com.permissions.dialogs.model.UiRequestSpec
import com.permissions.enums.DismissPolicy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PermissionRequestHost(
    service: PermissionRequestService,
    registry: PermissionRendererRegistry = PermissionRendererRegistry(),
) {
    var current by remember {
        mutableStateOf<Pair<UiRequestSpec, CompletableDeferred<UiRequestResult>>?>(
            null
        )
    }

    LaunchedEffect(Unit) {
        service.requests.collectLatest { current = it }
    }

    LaunchedEffect(Unit) {
        // explicit dismiss by id
        service.dismiss.collect { id ->
            if (current?.first?.id == id) current = null
        }
    }

    current?.let { (spec, deferred) ->
        val entry = registry.resolve(spec)
        val effectivePolicy = entry?.policyOverride ?: spec.dismissPolicy

        val complete: (UiRequestResult) -> Unit = { res ->
            if (!deferred.isCompleted) {
                deferred.complete(res)
            }
            // default behavior: close; sticky: keep open
            if (effectivePolicy == DismissPolicy.OnResult) {
                current = null
            }
        }

        // App override by tag/type
        /*val override = registry.resolve(spec)
        if (override != null) {
            override.Render(spec) { result ->
                complete(result)
                // current = null
            }
            return
        }*/

        val renderer = entry?.renderer
        if (renderer != null) {
            renderer.Render(spec, complete)
        } else {
            // Module defaults (fallbacks)
            when (spec) {
                is ConfirmDialogSpec -> {
                    AlertDialog(
                        onDismissRequest = {
                            complete(UiRequestResult.Dismissed)
                            // current = null
                        },
                        title = { Text(spec.title) },
                        text = { Text(spec.message) },
                        confirmButton = {
                            TextButton(onClick = {
                                complete(UiRequestResult.Confirmed);
                                // current = null
                            }) {
                                Text(spec.positiveText)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                complete(UiRequestResult.Dismissed);
                                // current = null
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
                            complete(UiRequestResult.Confirmed);
                            // current = null
                        },
                        onDismiss = {
                            complete(UiRequestResult.Dismissed);
                            //current = null
                        },
                    )
                }
            }
        }
    }
}
