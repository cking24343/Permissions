package com.permissions.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PermissionsDialog(
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit = {},
    bodyContent: @Composable (ColumnScope.() -> Unit),
) {
   Dialog(
        // We are copying the passed properties then setting usePlatformDefaultWidth to false
        properties = properties.let {
            DialogProperties(
                dismissOnBackPress = it.dismissOnBackPress,
                dismissOnClickOutside = it.dismissOnClickOutside,
                securePolicy = it.securePolicy,
                usePlatformDefaultWidth = false,
            )
        },
        onDismissRequest = { onDismissRequest() },
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(intrinsicSize = IntrinsicSize.Min)
                // Applying a min default padding value
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            content = bodyContent,
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
        )
    }
}

/*
@Preview(heightDp = 800)
@ExperimentalFoundationApi
@Composable
private fun PreviewDialog() {
    Preview {
        WellstarDialog(
            onDismissRequest = {},
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.grid_1),
                modifier = Modifier.padding(Dimens.grid_2),
            ) {
                Text(
                    text = "This is a test dialog preview.",
                )
                WellstarButton(
                    buttonTextString = "Preview CTA",
                )
            }
        }
    }
}*/
/*
@Preview(heightDp = 800)
@ExperimentalFoundationApi
@Composable
private fun PreviewDialogWithPadding() {
    Preview {
        WellstarDialog(
            modifier =  Modifier.padding(horizontal = Dimens.grid_3),
            onDismissRequest = {},
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.grid_1),
                modifier = Modifier.padding(Dimens.grid_2),
            ) {
                Text(
                    text = "This is a test dialog preview.",
                )
                WellstarButton(
                    buttonTextString = "Preview CTA",
                )
            }
        }
    }
}
*/