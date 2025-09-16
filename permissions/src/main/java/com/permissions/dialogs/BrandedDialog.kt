package com.permissions.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BrandedDialog(
    properties: DialogProperties,
    title: String,
    message: String?,
    positive: String?,
    negative: String?,
    primaryImageRes: Int?,
    secondaryImageRes: Int?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = properties) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (!message.isNullOrBlank()) {
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (negative != null) {
                        TextButton(onClick = onDismiss) { Text(negative) }
                    }
                    if (positive != null) {
                        TextButton(onClick = onConfirm) { Text(positive) }
                    }
                }
            }
        }
    }
}
