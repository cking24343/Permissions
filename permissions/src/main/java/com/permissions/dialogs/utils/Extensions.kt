package com.permissions.dialogs.utils

import androidx.compose.runtime.Composable
import com.permissions.dialogs.PermissionRendererRegistry
import com.permissions.dialogs.TypedUiRenderer
import com.permissions.dialogs.model.DialogTags
import com.permissions.dialogs.model.UiRequestResult
import com.permissions.dialogs.model.UiRequestSpec

inline fun <reified T : UiRequestSpec> PermissionRendererRegistry.register(
    tag: DialogTags,
    noinline renderer: @Composable (spec: T, complete: (UiRequestResult) -> Unit) -> Unit
) = register(
    tag = tag.tag,
    type = T::class,
    renderer = TypedUiRenderer(renderer)
)

inline fun <reified T : UiRequestSpec> PermissionRendererRegistry.register(
    noinline renderer: @Composable (spec: T, complete: (UiRequestResult) -> Unit) -> Unit
) = register(
    type = T::class,
    renderer = TypedUiRenderer(renderer)
)