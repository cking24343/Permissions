package com.permissions.dialogs

import androidx.compose.runtime.Composable
import com.permissions.dialogs.model.UiRequestResult
import com.permissions.dialogs.model.UiRequestSpec
import kotlin.reflect.KClass

fun interface PermissionUiRenderer {
    @Composable
    fun Render(spec: UiRequestSpec, complete: (UiRequestResult) -> Unit)
}

fun interface TypedUiRenderer<T : UiRequestSpec> {
    @Composable
    fun Render(spec: T, complete: (UiRequestResult) -> Unit)
}

class PermissionRendererRegistry {
    private val byTag = linkedMapOf<String, PermissionUiRenderer>()
    private val byType = linkedMapOf<KClass<out UiRequestSpec>, PermissionUiRenderer>()
    private val byTagAndType =
        linkedMapOf<Pair<String, KClass<out UiRequestSpec>>, PermissionUiRenderer>()

    fun register(tag: String, renderer: PermissionUiRenderer) = apply {
        byTag[tag] = renderer
    }

    fun <T : UiRequestSpec> register(
        type: KClass<T>,
        renderer: TypedUiRenderer<T>
    ) = apply {
        byType[type] = PermissionUiRenderer { spec, complete ->
            if (!type.java.isInstance(spec)) {
                // Mismatch — fail safely instead of crashing
                complete(UiRequestResult.Dismissed)
                return@PermissionUiRenderer
            }
            @Suppress("UNCHECKED_CAST")
            renderer.Render(spec as T, complete)
        }
    }

    fun <T : UiRequestSpec> register(
        tag: String,
        type: KClass<T>,
        renderer: TypedUiRenderer<T>
    ) = apply {
        byTagAndType[tag to type] = PermissionUiRenderer { spec, complete ->
            if (!type.java.isInstance(spec)) {
                complete(UiRequestResult.Dismissed)
                return@PermissionUiRenderer
            }
            @Suppress("UNCHECKED_CAST")
            renderer.Render(spec as T, complete)
        }
    }

    fun resolve(spec: UiRequestSpec): PermissionUiRenderer? =
        byTagAndType[spec.tag to spec::class]      // most specific: tag + type
            ?: spec.tag?.let { byTag[it] }         // tag only
            ?: byType[spec::class]                 // type only
}
