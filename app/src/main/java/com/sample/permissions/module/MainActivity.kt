package com.sample.permissions.module

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.permissions.api.PermissionsManager
import com.permissions.dialogs.DialogService
import com.permissions.dialogs.PermissionsDialogHost
import com.permissions.models.PermissionResult
import com.sample.permissions.module.ui.models.MainScreenState
import com.sample.permissions.module.ui.screens.MainScreen
import com.sample.permissions.module.ui.theme.SamplePermissionsModuleTheme
import com.sample.permissions.module.ui.widgets.Greeting
import com.sample.permissions.module.utils.buildPermissionsDialogOverrides
import com.sample.permissions.module.utils.launchWhenCreated
import com.sample.permissions.module.utils.launchWhenStarted
import kotlinx.coroutines.flow.collectLatest
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    // DI
    private val activityViewModel: MainActivityViewModel by inject()
    private val permissionsManager: PermissionsManager by inject()

    init {
        launchWhenCreated {
            activityViewModel.requestLocationAndBluetooth.collectLatest {
                permissionsManager.requestFindMyWayPermissionsFlow(
                    onResult = { type ->
                        Log.d(
                            "PermissionsManager",
                            "MainActivity | Request FindMyWay Permissions Flow completed, result: $type"
                        )
                    }
                )
            }
        }
        launchWhenCreated {
            activityViewModel.requestLocation.collectLatest {
                permissionsManager.requestLocationFlow(
                    onResult = { type ->
                        Log.d(
                            "PermissionsManager",
                            "MainActivity | Request Location Permission Flow completed, result: $type"
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
                        Log.d(
                            "PermissionsManager",
                            "MainActivity | Request Bluetooth Permission Flow completed, result: $type"
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
                permissionsManager.requestNotificationFlow(
                    onResult = { type ->
                        Log.d(
                            "PermissionsManager",
                            "MainActivity | Request Notification Permission Flow completed, result: $type"
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
                        Log.d(
                            "PermissionsManager",
                            "MainActivity | Request Camera Permission Flow completed, result: $type"
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
            val dialogService: DialogService = get()

            // Pass in overrides for app specific dialogs to replace permission module ones
            val dialogOverrides = remember { buildPermissionsDialogOverrides() }

            SamplePermissionsModuleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Screen Container
                    MainScreen(
                        state = activityViewModel.toState(),
                        modifier = Modifier.padding(innerPadding),
                    )

                    // Host permission dialogs
                    PermissionsDialogHost(
                        service = dialogService,
                        registry = dialogOverrides,
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        permissionsManager.detachActivity()
        super.onDestroy()
    }

}