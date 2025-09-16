package com.permissions.dialogs

import androidx.compose.runtime.Composable
import kotlin.reflect.KClass

fun interface DialogRenderer {
    @Composable
    fun Render(spec: DialogSpec, complete: (DialogResult) -> Unit)
}

fun interface TypedDialogRenderer<T : DialogSpec> {
    @Composable
    fun Render(spec: T, complete: (DialogResult) -> Unit)
}

class DialogRendererRegistry {
    private val byTag = linkedMapOf<String, DialogRenderer>()
    private val byType = linkedMapOf<KClass<out DialogSpec>, DialogRenderer>()
    private val byTagAndType = linkedMapOf<Pair<String, KClass<out DialogSpec>>, DialogRenderer>()

    fun register(tag: String, renderer: DialogRenderer) = apply {
        byTag[tag] = renderer
    }

    fun <T : DialogSpec> register(
        type: KClass<T>,
        renderer: TypedDialogRenderer<T>
    ) = apply {
        byType[type] = DialogRenderer { spec, complete ->
            // No kotlin-reflect: use Java class check
            if (!type.java.isInstance(spec)) {
                // Mismatch — fail safely instead of crashing
                complete(DialogResult.Dismissed)
                return@DialogRenderer
            }
            @Suppress("UNCHECKED_CAST")
            renderer.Render(spec as T, complete)
        }
    }

    fun <T : DialogSpec> register(
        tag: String,
        type: KClass<T>,
        renderer: TypedDialogRenderer<T>
    ) = apply {
        byTagAndType[tag to type] = DialogRenderer { spec, complete ->
            if (!type.java.isInstance(spec)) {
                complete(DialogResult.Dismissed)
                return@DialogRenderer
            }
            @Suppress("UNCHECKED_CAST")
            renderer.Render(spec as T, complete)
        }
    }

    fun resolve(spec: DialogSpec): DialogRenderer? =
        byTagAndType[spec.tag to spec::class]      // most specific: tag + type
            ?: spec.tag?.let { byTag[it] }         // tag only
            ?: byType[spec::class]                 // type only
}
