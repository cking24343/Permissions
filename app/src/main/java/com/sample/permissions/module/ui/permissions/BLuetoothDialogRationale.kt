package com.sample.permissions.module.ui.permissions


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.permissions.dialogs.model.CustomSpec
import com.permissions.dialogs.model.UiRequestResult

@Composable
fun BluetoothDialogRationale(
    spec: CustomSpec,
    modifier: Modifier = Modifier,
    result: (UiRequestResult) -> Unit = {},
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = { result(UiRequestResult.Dismissed) },
        title = { Text("⚡ ${spec.title}") },
        text = { Text(spec.message.orEmpty()) },
        confirmButton = {
            Button(onClick = { result(UiRequestResult.Confirmed) }) {
                Text(spec.positiveText ?: "Enable")
            }
        },
        dismissButton = {
            TextButton(onClick = { result(UiRequestResult.Dismissed) }) {
                Text(spec.negativeText ?: "Cancel")
            }
        }
    )
}