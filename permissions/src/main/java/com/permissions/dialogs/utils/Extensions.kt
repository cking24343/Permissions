package com.permissions.dialogs.utils

import androidx.compose.runtime.Composable
import com.permissions.dialogs.PermissionRendererRegistry
import com.permissions.dialogs.PermissionUiRenderer
import com.permissions.dialogs.TypedUiRenderer
import com.permissions.enums.DialogTags
import com.permissions.enums.UiRequestResult
import com.permissions.dialogs.model.UiRequestSpec
import com.permissions.enums.DismissPolicy

inline fun <reified T : UiRequestSpec> PermissionRendererRegistry.register(
    tag: DialogTags,
    policy: DismissPolicy? = null,
    noinline renderer: @Composable (spec: T, complete: (UiRequestResult) -> Unit) -> Unit,
) = register(
    tag = tag.tag,
    type = T::class,
    renderer = TypedUiRenderer(renderer),
    policy = policy,
)

inline fun <reified T : UiRequestSpec> PermissionRendererRegistry.register(
    policy: DismissPolicy? = null,
    noinline renderer: @Composable (spec: T, complete: (UiRequestResult) -> Unit) -> Unit,
) = register(
    type = T::class,
    renderer = TypedUiRenderer(renderer),
    policy = policy,
)

fun PermissionRendererRegistry.registerSticky(
    tag: String,
    renderer: PermissionUiRenderer
) = register(
    tag = tag,
    renderer = renderer,
    policy = DismissPolicy.KeepUntilExplicitDismiss
)