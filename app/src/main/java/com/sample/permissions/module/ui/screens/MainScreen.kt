package com.sample.permissions.module.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sample.permissions.module.ui.events.PermissionsUiEvent
import com.sample.permissions.module.ui.models.MainScreenState
import com.sample.permissions.module.ui.theme.SamplePermissionsModuleTheme
import com.sample.permissions.module.ui.widgets.Greeting
import com.sample.permissions.module.ui.widgets.PermissionsButton

@Composable
fun MainScreen(
    state: MainScreenState,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        Greeting(
            modifier = Modifier.padding(horizontal = 48.dp),
            name = ", welcome to the permissions module playground"
        )

        PermissionsButton(
            text = "Check location permissions/services",
            onClick = {
                state.onEvent(PermissionsUiEvent.OnLocationRequest)
            }
        )

        PermissionsButton(
            text = "Check background location permission",
            onClick = {
                state.onEvent(PermissionsUiEvent.OnBackgroundLocationRequest)
            }
        )

        PermissionsButton(
            text = "Check bluetooth permissions/services",
            onClick = {
                state.onEvent(PermissionsUiEvent.OnBluetoothRequest)
            }
        )

        PermissionsButton(
            text = "Check notification permission",
            onClick = {
                state.onEvent(PermissionsUiEvent.OnNotificationRequest)
            }
        )

        PermissionsButton(
            text = "Check camera permission",
            onClick = {
                state.onEvent(PermissionsUiEvent.OnCameraRequest)
            }
        )

        PermissionsButton(
            text = "Check FindMyWay permissions",
            onClick = {
                state.onEvent(PermissionsUiEvent.OnFindMyWayRequest)
            }
        )

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    SamplePermissionsModuleTheme {
        MainScreen(
            state = MainScreenState { }
        )
    }
}
