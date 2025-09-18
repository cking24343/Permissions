package com.permissions.dialogs

import androidx.compose.runtime.Composable
import com.permissions.dialogs.model.RendererEntry
import com.permissions.enums.UiRequestResult
import com.permissions.dialogs.model.UiRequestSpec
import com.permissions.enums.DismissPolicy
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
    private val byTag = linkedMapOf<String, RendererEntry>()
    private val byType = linkedMapOf<KClass<out UiRequestSpec>, RendererEntry>()
    private val byTagAndType =
        linkedMapOf<Pair<String, KClass<out UiRequestSpec>>, RendererEntry>()

    fun register(
        tag: String,
        renderer: PermissionUiRenderer,
        policy: DismissPolicy? = null
    ) = apply {
        byTag[tag] = RendererEntry(
            renderer = renderer,
            policyOverride = policy,
        )
    }

    fun <T : UiRequestSpec> register(
        type: KClass<T>,
        renderer: TypedUiRenderer<T>,
        policy: DismissPolicy? = null,
    ) = apply {
        byType[type] = RendererEntry(
            renderer = PermissionUiRenderer { spec, complete ->
                if (!type.java.isInstance(spec)) {
                    // Mismatch — fail safely instead of crashing
                    complete(UiRequestResult.Dismissed)
                    return@PermissionUiRenderer
                }
                @Suppress("UNCHECKED_CAST")
                renderer.Render(spec as T, complete)
            },
            policyOverride = policy,
        )

    }

    fun <T : UiRequestSpec> register(
        tag: String,
        type: KClass<T>,
        renderer: TypedUiRenderer<T>,
        policy: DismissPolicy? = null,
    ) = apply {
        byTagAndType[tag to type] = RendererEntry(
            renderer = PermissionUiRenderer { spec, complete ->
                if (!type.java.isInstance(spec)) {
                    complete(UiRequestResult.Dismissed)
                    return@PermissionUiRenderer
                }
                @Suppress("UNCHECKED_CAST")
                renderer.Render(spec as T, complete)
            },
            policyOverride = policy,
        )
    }

    fun resolve(spec: UiRequestSpec): RendererEntry? =
        byTagAndType[spec.tag to spec::class]      // most specific: tag + type
            ?: spec.tag?.let { byTag[it] }         // tag only
            ?: byType[spec::class]                 // type only
}
