package com.sample.permissions.module.ui.widgets

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PermissionsButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = { onClick() },
        modifier = modifier,
        enabled = true,
    ) {
        Text(text = text)
    }
}

@Preview
@Composable
fun PreviewPermissionsButton() {
    PermissionsButton(
        text = "Check for Permissions"
    )
}