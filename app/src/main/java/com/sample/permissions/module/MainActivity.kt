package com.sample.permissions.module

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.permissions.api.PermissionsManager
import com.permissions.dialogs.PermissionRequestService
import com.permissions.dialogs.ui.PermissionRequestHost
import com.permissions.enums.PermissionUiMode
import com.permissions.models.PermissionResult
import com.sample.permissions.module.ui.screens.MainScreen
import com.sample.permissions.module.ui.theme.SamplePermissionsModuleTheme
import com.sample.permissions.module.utils.buildPermissionsRequestUiOverrides
import com.sample.permissions.module.utils.launchWhenCreated
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : ComponentActivity() {
    // DI
    private val activityViewModel: MainActivityViewModel by inject()
    private val permissionsManager: PermissionsManager by inject()

    init {
        launchWhenCreated {
            activityViewModel.requestLocationAndBluetooth.collectLatest {
                permissionsManager.requestCombinedLocationAndBluetoothPermissionsFlow(
                    onResult = { type ->
                        Timber.tag("PermissionsManager")
                            .d("MainActivity | Request Combined Permissions Flow completed, result: $type")
                        permissionsToast(
                            "Combined Permissions Flow completed, result: $type"
                        )
                    }
                )
            }
        }
        launchWhenCreated {
            activityViewModel.requestLocation.collectLatest {
                permissionsManager.requestLocationFlow(
                    onResult = { type ->
                        Timber.tag("PermissionsManager")
                            .d("MainActivity | Request Location Permission Flow completed, result: $type")
                        permissionsToast(
                            "Location Permissions Flow completed, result: $type"
                        )
                        when (type) {
                            PermissionResult.Granted -> {
                                //Log.d("PermissionFlow", "location permission granted...")
                            }

                            PermissionResult.Denied -> {
                                //Log.d("PermissionFlow", "location permission denied...")
                            }

                            PermissionResult.Canceled -> {
                                //Log.d("PermissionFlow", "location permission cancelled...")
                            }

                            PermissionResult.ServiceDisabled -> {
                                //Log.d("PermissionFlow", "location service disabled...")
                            }

                            else -> {
                                // do nothing
                            }
                        }
                    },
                )
            }
        }
        launchWhenCreated {
            activityViewModel.requestBackgroundLocation.collectLatest {
                permissionsManager.requestBackgroundLocationPermission()
            }
        }
        launchWhenCreated {
            activityViewModel.requestBluetooth.collectLatest {
                permissionsManager.requestBluetoothFlow(
                    onResult = { type ->
                        Timber.tag("PermissionsManager")
                            .d("MainActivity | Request Bluetooth Permission Flow completed, result: $type")
                        permissionsToast(
                            "Bluetooth Permissions Flow completed, result: $type"
                        )
                        when (type) {
                            PermissionResult.Granted -> {
                                //Log.d("PermissionFlow", "bluetooth permission granted...")
                            }

                            PermissionResult.Denied -> {
                                //Log.d("PermissionFlow", "bluetooth permission denied...")
                            }

                            PermissionResult.Canceled -> {
                                //Log.d("PermissionFlow", "bluetooth permission cancelled...")
                            }

                            PermissionResult.ServiceDisabled -> {
                                //Log.d("PermissionFlow", "bluetooth service disabled...")
                            }

                            else -> {
                                // do nothing
                            }
                        }
                    },
                )
            }
        }
        launchWhenCreated {
            activityViewModel.requestNotifications.collectLatest {
                // Example for "Headless" state where we only want to call the permission manager to
                // handle permission checks and system prompt without rationales.
                permissionsManager.requestNotificationFlow(
                    uiMode = PermissionUiMode.NoRationales,
                    onResult = { type ->
                        Timber.tag("PermissionsManager")
                            .d("MainActivity | Request Notification Permission Flow completed, result: $type")
                        permissionsToast(
                            "Notification Permissions Flow completed, result: $type"
                        )
                        when (type) {
                            PermissionResult.Granted -> {
                                //Log.d("PermissionFlow", "notifications permission granted...")
                            }

                            PermissionResult.Denied -> {
                                //Log.d("PermissionFlow", "notifications permission denied...")
                            }

                            PermissionResult.Canceled -> {
                                //Log.d("PermissionFlow", "notifications permission cancelled...")
                            }

                            else -> {
                                // do nothing...
                            }
                        }
                    },
                )
            }
        }
        launchWhenCreated {
            activityViewModel.requestCamera.collectLatest {
                permissionsManager.requestCameraFlow(
                    onResult = { type ->
                        Timber.tag("PermissionsManager")
                            .d("MainActivity | Request Camera Permission Flow completed, result: $type")
                        permissionsToast(
                            "Camera Permissions Flow completed, result: $type"
                        )
                        when (type) {
                            PermissionResult.Granted -> {
                                //Log.d("PermissionFlow", "camera permission granted...")
                            }

                            PermissionResult.Denied -> {
                                //Log.d("PermissionFlow", "camera permission denied...")
                            }

                            PermissionResult.Canceled -> {
                                //Log.d("PermissionFlow", "camera permission cancelled...")
                            }

                            else -> {
                                // do nothing...
                            }
                        }
                    },
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsManager.attachActivity(this)

        // Setup for UI
        enableEdgeToEdge()
        setContent {
            // Get the Koin instance for the permissions dialog service
            val permissionRequestService: PermissionRequestService = get()

            // Pass in overrides for app specific dialogs to replace permission module ones
            val uiOverrides = remember { buildPermissionsRequestUiOverrides() }

            SamplePermissionsModuleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Using a box to ensure layering of permission request ui to be the top z-index
                    Box(modifier = Modifier.fillMaxSize()) {

                        // Screen Container
                        MainScreen(
                            state = activityViewModel.toState(),
                            modifier = Modifier.padding(innerPadding),
                        )

                        // Mount the Permissions host for overriding default rationales with custom
                        // ones. You only need to mount if you plan on supporting rationales for
                        // your permission requests/system prompts.
                        PermissionRequestHost(
                            service = permissionRequestService,
                            registry = uiOverrides,
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        permissionsManager.detachActivity()
        super.onDestroy()
    }

    private fun permissionsToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }

}