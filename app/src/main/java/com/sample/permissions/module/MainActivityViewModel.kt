package com.sample.permissions.module

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.permissions.api.PermissionsManager
import com.sample.permissions.module.ui.events.PermissionsUiEvent
import com.sample.permissions.module.ui.models.MainScreenState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : BaseViewModel() {

    // Events
    val requestLocation = MutableSharedFlow<Unit>()
    val requestBackgroundLocation = MutableSharedFlow<Unit>()
    val requestBluetooth = MutableSharedFlow<Unit>()
    val requestNotifications = MutableSharedFlow<Unit>()
    val requestCamera = MutableSharedFlow<Unit>()
    val requestLocationAndBluetooth = MutableSharedFlow<Unit>()

    private fun onUserWantsToEnableLocation() {
        viewModelScope.launch {
            requestLocation.emit(Unit)
        }
    }

    private fun onUserWantsToEnableBackgroundLocation() {
        viewModelScope.launch {
            requestBackgroundLocation.emit(Unit)
        }
    }

    private fun onUserWantsToEnableBluetooth() {
        viewModelScope.launch {
            requestBluetooth.emit(Unit)
        }
    }

    private fun onUserWantsToEnableNotifications() {
        viewModelScope.launch {
            requestNotifications.emit(Unit)
        }
    }

    private fun onUserWantsToEnableCamera() {
        viewModelScope.launch {
            requestCamera.emit(Unit)
        }
    }

    private fun onUserWantsToEnableLocationAndBluetooth() {
        viewModelScope.launch {
            requestLocationAndBluetooth.emit(Unit)
        }
    }

    // UI Callbacks
    fun onEvent(event: PermissionsUiEvent) {
        when (event) {
            is PermissionsUiEvent.OnLocationRequest -> {
                onUserWantsToEnableLocation()
            }

            is PermissionsUiEvent.OnBackgroundLocationRequest -> {
                onUserWantsToEnableBackgroundLocation()
            }

            is PermissionsUiEvent.OnBluetoothRequest -> {
                onUserWantsToEnableBluetooth()
            }

            is PermissionsUiEvent.OnNotificationRequest -> {
                onUserWantsToEnableNotifications()
            }

            is PermissionsUiEvent.OnCameraRequest -> {
                onUserWantsToEnableCamera()
            }

            is PermissionsUiEvent.OnFindMyWayRequest -> {
                onUserWantsToEnableLocationAndBluetooth()
            }
        }
    }
}

@Composable
fun MainActivityViewModel.toState() = MainScreenState(
    onEvent = ::onEvent,
)