package com.sample.permissions.module.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.permissions.dialogs.CustomSpec
import com.permissions.dialogs.DialogRendererRegistry
import com.permissions.dialogs.DialogResult
import com.permissions.dialogs.DialogTags
import com.permissions.dialogs.register

// Example: override the ** Bluetooth rationale ** only
// app module
fun buildPermissionsDialogOverrides() = DialogRendererRegistry()
    .register<CustomSpec>(DialogTags.RATIONALE_LOCATION) { spec, complete ->
        AlertDialog(
            onDismissRequest = { complete(DialogResult.Dismissed) },
            title = { Text("YO ${spec.title}") },
            text  = { Text(spec.message.orEmpty()) },
            confirmButton = {
                Button(onClick = { complete(DialogResult.Confirmed) }) {
                    Text(spec.positiveText ?: "NOW!")
                }
            },
            dismissButton = {
                TextButton(onClick = { complete(DialogResult.Dismissed) }) {
                    Text(spec.negativeText ?: "Cancel")
                }
            }
        )
    }
    .register<CustomSpec>(DialogTags.RATIONALE_PRECISE_LOCATION) { spec, complete ->
        AlertDialog(
            onDismissRequest = { complete(DialogResult.Dismissed) },
            title = { Text("I NEED MORE") },
            text  = { Text("we need precisie, get that approx crap outta here!") },
            confirmButton = {
                Button(onClick = { complete(DialogResult.Confirmed) }) {
                    Text(spec.positiveText ?: "NOW!")
                }
            },
            dismissButton = {
                TextButton(onClick = { complete(DialogResult.Dismissed) }) {
                    Text(spec.negativeText ?: "Cancel")
                }
            }
        )
    }
    .register<CustomSpec>(DialogTags.RATIONALE_BLUETOOTH) { spec, complete ->
        AlertDialog(
            onDismissRequest = { complete(DialogResult.Dismissed) },
            title = { Text("⚡ ${spec.title}") },
            text  = { Text(spec.message.orEmpty()) },
            confirmButton = {
                Button(onClick = { complete(DialogResult.Confirmed) }) {
                    Text(spec.positiveText ?: "Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { complete(DialogResult.Dismissed) }) {
                    Text(spec.negativeText ?: "Cancel")
                }
            }
        )
    }
    .register<CustomSpec>(DialogTags.RATIONALE_CAMERA) { spec, complete ->
        AlertDialog(
            onDismissRequest = { complete(DialogResult.Dismissed) },
            title = { Text("📷 ${spec.title}") },
            text  = { Text(spec.message.orEmpty()) },
            confirmButton = {
                Button(onClick = { complete(DialogResult.Confirmed) }) {
                    Text(spec.positiveText ?: "Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { complete(DialogResult.Dismissed) }) {
                    Text(spec.negativeText ?: "Cancel")
                }
            }
        )
    }

// You could also override *all* CustomSpec by type:
// .register(CustomSpec::class, DialogRenderer { spec, complete -> /* ... */ })
