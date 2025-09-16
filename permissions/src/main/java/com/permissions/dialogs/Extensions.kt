package com.permissions.dialogs

import androidx.compose.runtime.Composable

inline fun <reified T : DialogSpec> DialogRendererRegistry.register(
    tag: DialogTags,
    noinline renderer: @Composable (spec: T, complete: (DialogResult) -> Unit) -> Unit
) = register(
    tag = tag.tag,
    type = T::class,
    renderer = TypedDialogRenderer(renderer)
)

inline fun <reified T : DialogSpec> DialogRendererRegistry.register(
    noinline renderer: @Composable (spec: T, complete: (DialogResult) -> Unit) -> Unit
) = register(
    type = T::class,
    renderer = TypedDialogRenderer(renderer)
)