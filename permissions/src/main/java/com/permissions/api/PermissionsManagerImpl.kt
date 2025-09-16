package com.permissions.api

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.permissions.dialogs.ConfirmSpec
import com.permissions.dialogs.CustomSpec
import com.permissions.dialogs.DialogResult
import com.permissions.dialogs.DialogService
import com.permissions.dialogs.DialogTags
import com.permissions.dialogs.toDialogTag
import com.permissions.models.PermissionResult
import com.permissions.models.PermissionState
import com.permissions.models.PermissionType
import com.permissions.store.PermissionStore
import com.permissions.util.PermissionsInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PermissionsManagerImpl : PermissionsManager, KoinComponent {
    // DI
    private val appContext: Context by lazy { PermissionsInitializer.getAppContext() }
    private val permissionStore: PermissionStore by inject()
    private val dialogs: DialogService by inject()

    private var activity: ComponentActivity? = null

    private var locationLauncher: ActivityResultLauncher<String>? = null
    private var bluetoothLauncher: ActivityResultLauncher<String>? = null
    private var notificationLauncher: ActivityResultLauncher<String>? = null
    private var cameraLauncher: ActivityResultLauncher<String>? = null

    // Flag to help retain if we have shown the precise rationale or not
    private var hasShownPreciseRationale = false

    // Flag to help retain if a user has rejected the precise rationale or not
    private var hasUserRejectedPrecise = false

    private var onResultCallback: ((Boolean) -> Unit)? = null

    private val locationManager by lazy {
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val bluetoothManager by lazy {
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    // Check for device support for bluetooth
    private val bluetoothAvailable by lazy {
        appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    private val bluetoothBLEAvailable by lazy {
        appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    private fun hasLocationFeature(): Boolean {
        return appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)
    }

    private fun isLocationServiceEnabled(): Boolean {
        return locationManager.isLocationEnabled ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun isBluetoothServiceEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    private val isNotificationsEnabled: Boolean
        get() = NotificationManagerCompat.from(appContext).areNotificationsEnabled()

    private val isCameraAvailable: Boolean
        get() = appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    private val isBluetoothAvailable: Boolean
        get() = bluetoothAvailable && bluetoothBLEAvailable

    private val hasFineLocation: Boolean
        get() = ContextCompat.checkSelfPermission(
            activity ?: appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private val hasCoarseLocation: Boolean
        get() = ContextCompat.checkSelfPermission(
            activity ?: appContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private val isBackgroundLocationAccessGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                activity ?: appContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required pre-Android 10
        }

    override fun attachActivity(activity: ComponentActivity) {
        this.activity = activity

        locationLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResultCallback?.invoke(isGranted)
            onResultCallback = null
        }

        bluetoothLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResultCallback?.invoke(isGranted)
            onResultCallback = null
        }

        notificationLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResultCallback?.invoke(isGranted)
            onResultCallback = null
        }

        cameraLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResultCallback?.invoke(isGranted)
            onResultCallback = null
        }

        // Manually revoked check
        launchMain {
            val stored = permissionStore.getState(PermissionType.Location)
            val systemGranted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (stored == PermissionState.Granted && !systemGranted) {
                permissionStore.setState(PermissionType.Location, PermissionState.ManuallyRevoked)
            }
        }
    }

    override fun detachActivity() {
        activity = null
        locationLauncher = null
        bluetoothLauncher = null
        notificationLauncher = null
        cameraLauncher = null
        onResultCallback = null
    }

    // Permission Flows
    override fun requestCameraFlow(onResult: (PermissionResult) -> Unit) {
        launchMain {
            val type = PermissionType.Camera
            if (isCameraAvailable) {
                handlePermissionRequest(
                    type = type,
                    permission = Manifest.permission.CAMERA,
                    rationaleMessage = "This feature requires access to your camera.",
                    launcher = cameraLauncher,
                    onResult = onResult
                )
            } else {
                permissionStore.setState(type, PermissionState.NotRequested)
                onResult(PermissionResult.Unavailable)
            }
        }
    }

    override fun requestBluetoothFlow(onResult: (PermissionResult) -> Unit) {
        launchMain {
            val type = PermissionType.Bluetooth
            if (!isBluetoothAvailable) {
                permissionStore.setState(type, PermissionState.NotRequested)
                onResult(PermissionResult.Unavailable)
                return@launchMain
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                grantPermissionByDefault(type, onResult)
                return@launchMain
            }

            handlePermissionRequest(
                type = type,
                permission = Manifest.permission.BLUETOOTH_CONNECT,
                rationaleMessage = "This feature requires access to BLE beacons via Bluetooth.",
                launcher = bluetoothLauncher,
                onResult = { result ->
                    if (result == PermissionResult.Granted && !isBluetoothServiceEnabled()) {
                        showBluetoothServiceRationale {
                            onResult(PermissionResult.ServiceDisabled)
                        }
                    } else {
                        onResult(result)
                    }
                }
            )
        }
    }

    override fun requestNotificationFlow(onResult: (PermissionResult) -> Unit) {
        launchMain {
            val type = PermissionType.Notification
            if (isNotificationsEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                grantPermissionByDefault(type, onResult)
                return@launchMain
            }

            handlePermissionRequest(
                type = type,
                permission = Manifest.permission.POST_NOTIFICATIONS,
                rationaleMessage = "This app needs permission to send you notifications.",
                launcher = notificationLauncher,
                onResult = onResult
            )
        }
    }

    override fun requestBackgroundLocationPermission() {
        launchMain {
            val type = PermissionType.BackgroundLocation
            if (isBackgroundLocationAccessGranted) {
                permissionStore.setState(type, PermissionState.Granted)
                return@launchMain
            }

            permissionStore.setState(type, PermissionState.RationaleShown)
            showBackgroundLocationRationale(
                onContinue = {
                    navigateToAppSettings()
                },
                onCancel = {
                    launchMain {
                        permissionStore.setState(type, PermissionState.Canceled)
                    }
                }
            )
        }
    }

    // WIP: Location permissions + use cases are still being ironed out
    override fun requestLocationFlow(
        onResult: (PermissionResult) -> Unit
    ) {
        val type = PermissionType.Location
        trackLocationFlow("starting flow | type: $type")

        // Check if user can support location updates on the device, if not then do not proceed
        if (!hasLocationFeature()) {
            trackLocationFlow("Location not available...")
            onResult(PermissionResult.Unavailable)
            return
        }

        trackLocationFlow("Location supported on device, now checking permissions..")
        trackLocationFlow("Logging => hasFineLocation: $hasFineLocation | hasCoarseLocation: $hasCoarseLocation")

        when {
            hasFineLocation -> {
                // User has permission for Precise Location (ie Fine Access)
                // Ensure that the user's device has Location Services enabled
                if (isLocationServiceEnabled()) {
                    trackLocationFlow("Precise location granted (ie: Fine Access).")
                    updatePermissionState(type, PermissionState.PreciseGranted)
                    onResult(PermissionResult.Granted)
                } else {
                    trackLocationFlow("Precise location granted (ie: Fine Access), but location services not enabled. Prompting now...")
                    updatePermissionState(type, PermissionState.ServiceDisabled)
                    showLocationServiceRationale {
                        onResult(PermissionResult.ServiceDisabled)
                    }
                }
            }

            hasCoarseLocation -> {
                // User has permission for Approximate Location (ie Coarse Access)
                trackLocationFlow("Approximate location granted (ie: Coarse Access).")

                updatePermissionState(type, PermissionState.CoarseGranted)

                launchMain {
                    val currentPermissionState = permissionStore.getState(type)
                    trackLocationFlow(
                        "Has Shown Precise Rationale: $hasShownPreciseRationale " +
                                "| PermissionState: $currentPermissionState"
                    )

                    if (!hasShownPreciseRationale) {
                        activity?.let {
                            if (!shouldShowRequestPermissionRationale(
                                    it,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            ) {
                                trackLocationFlow("Can not show in-app rationale to request fine location")
                                hasUserRejectedPrecise = true

                                updatePermissionState(
                                    type,
                                    PermissionState.PreciseDeniedPermanently
                                )

                            } else {
                                trackLocationFlow("Showing in-app rationale to request fine location")
                                hasShownPreciseRationale = true

                                updatePermissionState(type, PermissionState.PreciseRationaleShown)

                            }
                        }

                        /*showPreciseLocationRationale(
                            onAccepted = {
                                trackLocationFlow("User accepted precise rationale, requesting precise location (ie, Fine Access).")

                                activity?.let {
                                    if (!shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                        // We know the system prompt won't show again — take user to Settings
                                        trackLocationFlow("System prompt for Precise Location is unavailable — navigating to app settings.")
                                        showNavigateToSettingsDialog(
                                            title = "Enable Precise Location",
                                            message = "To enable precise location, toggle it on from the app's permission settings."
                                        )
                                        onResult(PermissionResult.Denied)
                                        return@showPreciseLocationRationale
                                    }
                                }

                                onResultCallback = { granted ->
                                    updatePermissionState(
                                        type,
                                        if (granted) {
                                            PermissionState.PreciseGranted
                                        } else {
                                            hasUserRejectedPrecise = true
                                            updatePermissionState(
                                                type,
                                                PermissionState.PreciseDenied
                                            )

                                            //PermissionState.CoarseGranted
                                            PermissionState.PreciseDeniedPermanently
                                        }
                                    )

                                    if (granted && !isLocationServiceEnabled()) {
                                        showLocationServiceRationale {
                                            onResult(PermissionResult.ServiceDisabled)
                                        }
                                    } else {
                                        onResult(
                                            if (granted) {
                                                PermissionResult.Granted
                                            } else {
                                                PermissionResult.Denied
                                            }
                                        )
                                    }
                                }

                                // Attempt to request Precise Location access now
                                locationLauncher?.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        )*/
                        if (currentPermissionState == PermissionState.PreciseDeniedPermanently) {
                            trackLocationFlow("Precise access is permanently denied — redirecting to app settings.")
                            //navigateToAppSettings()
                            showNavigateToSettingsDialog(
                                title = "Enable Precise Location",
                                message = "To enable precise location, toggle it on from the app's permission settings."
                            )
                        } else {
                            showPreciseLocationRationale(
                                onAccepted = {
                                    trackLocationFlow("User accepted precise rationale.")
                                    launchMain {
                                        val currentPermissionState = permissionStore.getState(type)
                                        if (currentPermissionState == PermissionState.PreciseDeniedPermanently) {
                                            trackLocationFlow("Precise access is permanently denied — redirecting to app settings.")
                                            navigateToAppSettings()
                                        } else {
                                            // Launch system permission request for precise access
                                            trackLocationFlow("Requesting precise location via system prompt.")
                                            onResultCallback = { granted ->
                                                val newState = if (granted) {
                                                    PermissionState.PreciseGranted
                                                } else {
                                                    hasUserRejectedPrecise = true
                                                    PermissionState.PreciseDeniedPermanently
                                                }

                                                updatePermissionState(type, newState)

                                                if (granted && !isLocationServiceEnabled()) {
                                                    showLocationServiceRationale {
                                                        onResult(PermissionResult.ServiceDisabled)
                                                    }
                                                } else {
                                                    onResult(
                                                        if (granted) PermissionResult.Granted
                                                        else PermissionResult.Denied
                                                    )
                                                }
                                            }

                                            locationLauncher?.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    }
                                },
                                onDismissed = {
                                    hasUserRejectedPrecise = true
                                    updatePermissionState(
                                        type,
                                        PermissionState.PreciseDeniedPermanently
                                    )
                                    onResult(PermissionResult.Denied)
                                }
                            )
                        }

                    } else {
                        if (hasUserRejectedPrecise) {
                            trackLocationFlow("User previously rejected precise. Directing to settings.")
                            showNavigateToSettingsDialog(
                                title = "Enable Precise Location",
                                message = "You've opted to keep Approximate only. Please enable 'Use precise location' from the location permission section, under the app's settings."
                            )
                        } else {
                            trackLocationFlow("No system dialog available for precise. Marking as rejected and opening settings.")
                            hasUserRejectedPrecise = true
                            updatePermissionState(type, PermissionState.PreciseDenied)
                            showNavigateToSettingsDialog(
                                title = "Enable Precise Location",
                                message = "To enable precise location, please enable 'Use precise location' from the location permission section, under the app's settings."
                            )
                        }
                    }
                }
            }

            else -> {
                // User does note have any permissions for Location
                trackLocationFlow("No location access has been granted (ie, Neither Fine nor Coarse Access)")
                launchMain {
                    val currentPermissionState = permissionStore.getState(type)
                    trackLocationFlow("Current Permission State: $currentPermissionState")
                    // Check to see if we can request fine access or if we have yet to request (ie, NotRequested will be returned if there is not value in the datastore)
                    if (shouldShowRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        currentPermissionState == PermissionState.NotRequested
                    ) {
                        trackLocationFlow("Showing rationale for location access")
                        permissionStore.setState(type, PermissionState.RationaleShown)
                        showRationaleDialog(
                            title = "Location Access Needed",
                            message = "We use your location to show relevant information near you.",
                            dialogTag = type.toDialogTag(),
                            onAccept = {
                                launchMain {
                                    /*handlePermissionRequest(
                                        type = type,
                                        permission = Manifest.permission.ACCESS_FINE_LOCATION,
                                        rationaleMessage = "We use your location to show relevant information near you.",
                                        launcher = locationLauncher,
                                        onResult = { result ->
                                            // Re-check permissions after system prompt
                                            if (hasFineLocation) {
                                                if (isLocationServiceEnabled()) {
                                                    updatePermissionState(
                                                        type,
                                                        PermissionState.PreciseGranted
                                                    )
                                                    onResult(PermissionResult.Granted)
                                                } else {
                                                    updatePermissionState(
                                                        type,
                                                        PermissionState.ServiceDisabled
                                                    )
                                                    showLocationServiceRationale {
                                                        onResult(PermissionResult.ServiceDisabled)
                                                    }
                                                }
                                            } else if (hasCoarseLocation) {
                                                // Fall into the follow-up check for precise
                                                requestLocationFlow(onResult)
                                            } else {
                                                onResult(result)
                                            }
                                        }
                                    )*/

                                    launchMain {
                                        permissionStore.setState(
                                            type,
                                            PermissionState.SystemPromptShown
                                        )
                                    }

                                    onResultCallback = { granted ->
                                        launchMain {
                                            permissionStore.setState(
                                                type,
                                                if (granted) PermissionState.Granted else PermissionState.Denied
                                            )
                                        }
                                        val result =
                                            if (granted) PermissionResult.Granted else PermissionResult.Denied
                                        // Re-check permissions after system prompt
                                        if (hasFineLocation) {
                                            if (isLocationServiceEnabled()) {
                                                updatePermissionState(
                                                    type,
                                                    PermissionState.PreciseGranted
                                                )
                                                onResult(PermissionResult.Granted)
                                            } else {
                                                updatePermissionState(
                                                    type,
                                                    PermissionState.ServiceDisabled
                                                )
                                                showLocationServiceRationale {
                                                    onResult(PermissionResult.ServiceDisabled)
                                                }
                                            }
                                        } else if (hasCoarseLocation) {
                                            // Fall into the follow-up check for precise
                                            requestLocationFlow(onResult)
                                        } else {
                                            onResult(result)
                                        }
                                    }

                                    locationLauncher?.launch(Manifest.permission.ACCESS_FINE_LOCATION)

                                }
                            },
                            onCancel = {
                                launchMain {
                                    permissionStore.setState(type, PermissionState.Canceled)
                                }
                                onResult(PermissionResult.Canceled)
                            }
                        )

                    } else {
                        trackLocationFlow("Location access can not be requested (ie, Either user revoked it or user denied access). Instruct user to open app settings.")

                        if (currentPermissionState in listOf(
                                PermissionState.Granted,
                                PermissionState.PreciseGranted,
                                PermissionState.CoarseGranted
                            )
                        ) {
                            updatePermissionState(type, PermissionState.ManuallyRevoked)
                        } else {
                            updatePermissionState(type, PermissionState.ManualUpgradeRequested)
                        }

                        showNavigateToSettingsDialog(
                            title = "Location Permission Required",
                            message = "Location access could not be requested. Either you've denied it or you have revoked it. Please enable it in system settings."
                        )
                    }
                }
            }
        }
    }

    override fun requestFindMyWayPermissionsFlow(
        onComplete: (PermissionResult) -> Unit
    ) {
        requestLocationFlow { locationResult ->
            if (locationResult == PermissionResult.Granted) {
                requestBluetoothFlow { bluetoothResult ->
                    onComplete(bluetoothResult)
                }
            } else {
                onComplete(locationResult)
            }
        }
    }

    // *** HELPERS ***
    private fun launchMain(block: suspend () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch { block() }
    }

    // Helper method to track permission state changes for the current permission type
    private fun updatePermissionState(
        type: PermissionType,
        state: PermissionState
    ) {
        launchMain {
            permissionStore.setState(type, state)
        }
    }

    /**
     * The function checks whether the app should display a custom rationale UI
     * before requesting a permission from the user.
     * */
    private fun shouldShowRationale(permission: String): Boolean {
        return activity?.let {
            ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
        } ?: false
    }

    private fun grantPermissionByDefault(
        type: PermissionType,
        onResult: (PermissionResult) -> Unit
    ) {
        launchMain {
            permissionStore.setState(type, PermissionState.Granted)
        }
        onResult(PermissionResult.Granted)
    }

    private suspend fun handlePermissionRequest(
        type: PermissionType,
        permission: String,
        rationaleMessage: String,
        launcher: ActivityResultLauncher<String>?,
        onResult: (PermissionResult) -> Unit
    ) {
        val activity = this.activity ?: return onResult(PermissionResult.Denied)

        val isGranted = ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            grantPermissionByDefault(type, onResult)
            return
        }

        val hasShownRationale = permissionStore.getState(type) == PermissionState.RationaleShown

        val shouldShowCustomRationale = !hasShownRationale && !isGranted

        val shouldShowRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

        if (shouldShowCustomRationale || shouldShowRationale) {
            launchMain {
                permissionStore.setState(type, PermissionState.RationaleShown)
            }

            showRationaleDialog(
                message = rationaleMessage,
                dialogTag = type.toDialogTag(),
                onAccept = {
                    launchMain {
                        permissionStore.setState(type, PermissionState.SystemPromptShown)
                    }

                    onResultCallback = { granted ->
                        launchMain {
                            permissionStore.setState(
                                type,
                                if (granted) PermissionState.Granted else PermissionState.Denied
                            )
                        }
                        onResult(if (granted) PermissionResult.Granted else PermissionResult.Denied)
                    }

                    launcher?.launch(permission)
                },
                onCancel = {
                    launchMain {
                        permissionStore.setState(type, PermissionState.Canceled)
                    }
                    onResult(PermissionResult.Canceled)
                }
            )
        } else {
            launchMain {
                permissionStore.setState(type, PermissionState.SystemPromptShown)
            }

            onResultCallback = { granted ->
                launchMain {
                    permissionStore.setState(
                        type,
                        if (granted) PermissionState.Granted else PermissionState.Denied
                    )
                }
                onResult(if (granted) PermissionResult.Granted else PermissionResult.Denied)
            }

            launcher?.launch(permission)
        }
    }

    // *** LOGGING ***
    private fun trackLocationFlow(message: String) =
        trackIt(message, "requestLocationFlow")

    @SuppressLint("LogNotTimber")
    private fun trackIt(
        message: String,
        flow: String? = null,
    ) {
        val tag = "PermissionsManager"
        if (flow.isNullOrEmpty()) {
            Log.d(tag, message)
        } else {
            Log.d(tag, "$flow | $message")
        }
    }

    // *** DIALOGS ***
    private fun showRationaleDialog(
        title: String = "Permissions Required",
        message: String,
        dialogTag: DialogTags,
        onAccept: () -> Unit = {},
        onCancel: () -> Unit = {},
    ) = launchMain {
        val res = dialogs.request(
            CustomSpec(
                title = title,
                message = message,
                positiveText = "Continue",
                negativeText = "Cancel",
                /** key for overrides **/
                tag = dialogTag.tag
            )
        )
        if (res == DialogResult.Confirmed) {
            onAccept()
        } else {
            onCancel()
        }
    }

    private fun showBluetoothServiceRationale(onDismiss: () -> Unit) = launchMain {
        val res = dialogs.request(
            CustomSpec(
                title = "Bluetooth Required",
                message = "Please enable Bluetooth to continue.",
                positiveText = "Enable",
                negativeText = "Cancel",
                tag = DialogTags.RATIONALE_BLUETOOTH_SERVICES.tag
            )
        )
        if (res == DialogResult.Confirmed) {
            navigateToBluetoothSettings()
        } else {
            onDismiss()
        }
    }

    private fun showLocationServiceRationale(onDismiss: () -> Unit) = launchMain {
        val res = dialogs.request(
            CustomSpec(
                title = "Location Required",
                message = "Please enable Location Services to continue.",
                positiveText = "Go to Settings",
                negativeText = "Cancel",
                tag = DialogTags.RATIONALE_LOCATION_SERVICES.tag
            )
        )
        if (res == DialogResult.Confirmed) {
            navigateToLocationSettings()
        } else {
            onDismiss()
        }
    }

    /* private fun showNavigateToSettingsDialog(
         title: String,
         message: String,
         onDismiss: () -> Unit = {},
     ) {
         activity?.let {
             android.app.AlertDialog.Builder(it)
                 .setTitle(title)
                 .setMessage(message)
                 .setPositiveButton("Go to Settings") { _, _ ->
                     navigateToAppSettings()
                 }
                 .setNegativeButton("Cancel", null)
                 .show()
         } ?: onDismiss()
     }*/

    private fun showNavigateToSettingsDialog(
        title: String,
        message: String,
        onDismiss: () -> Unit = {},
    ) = launchMain {
        val res = dialogs.request(
            CustomSpec(
                title = title,
                message = message,
                positiveText = "Go to Settings",
                negativeText = "Cancel",
                /** key for overrides **/
                tag = DialogTags.RATIONALE_APP_SETTINGS.tag,
            )
        )
        if (res == DialogResult.Confirmed) {
            navigateToAppSettings()
        } else {
            onDismiss()
        }
    }

    /*
    private fun showPreciseLocationRationale(
        onAccepted: () -> Unit = {},
        onDismissed: () -> Unit = {}
    ) {
        activity?.let {
            android.app.AlertDialog.Builder(it)
                .setTitle("Enable Precise Location")
                .setMessage("You've allowed location access, but only approximate. For turn-by-turn navigation, we need precise location.")
                .setPositiveButton("Enable Precise") { _, _ -> onAccepted() }
                .setNegativeButton("Not Now") { _, _ -> onDismissed() }
                .show()
        } ?: onDismissed()
    }
    */

    private fun showPreciseLocationRationale(
        onAccepted: () -> Unit = {},
        onDismissed: () -> Unit = {}
    ) = launchMain {
        val res = dialogs.request(
            CustomSpec(
                title = "Enable Precise Location",
                message = "You've allowed location access, but only approximate. For turn-by-turn navigation, we need precise location.",
                positiveText = "Enable Precise",
                negativeText = "Not Now",
                /** key for overrides **/
                tag = DialogTags.RATIONALE_PRECISE_LOCATION.tag,
            )
        )
        if (res == DialogResult.Confirmed) {
            onAccepted()
        } else {
            onDismissed()
        }
    }

    /*
    private fun showStandardLocationRationale(onAccept: () -> Unit) {
        activity?.let {
            android.app.AlertDialog.Builder(it)
                .setTitle("Location Access Needed")
                .setMessage("We use your location to show relevant information near you.")
                .setPositiveButton("Continue") { _, _ -> onAccept() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    */

    private fun showStandardLocationRationale(
        onAccept: () -> Unit,
        ) = launchMain {
        val res = dialogs.request(
            CustomSpec(
                title = "Location Access Needed",
                message = "We use your location to show relevant information near you.",
                positiveText = "Continue",
                negativeText = "Cancel",
                /** key for overrides **/
                tag = DialogTags.RATIONALE_LOCATION.tag,
            )
        )
        if (res == DialogResult.Confirmed) {
            onAccept()
        }
    }

    /*
    private fun showBackgroundLocationRationale(
        message: String = "This app needs background location access to notify you when you are near specific locations.",
        onContinue: () -> Unit,
        onCancel: () -> Unit
    ) {
        android.app.AlertDialog.Builder(activity)
            .setTitle("Background Location Access")
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ -> onContinue() }
            .setNegativeButton("Cancel") { _, _ -> onCancel() }
            .show()
    }
    */

    private fun showBackgroundLocationRationale(
        message: String = "This app needs background location access to notify you when you are near specific locations.",
        onContinue: () -> Unit,
        onCancel: () -> Unit
    ) = launchMain {
        val res = dialogs.request(
            CustomSpec(
                title = "Background Location Access",
                message = message,
                positiveText = "Continue",
                negativeText = "Cancel",
                /** key for overrides **/
                tag = DialogTags.RATIONALE_BACKGROUND_LOCATION.tag,
            )
        )
        if (res == DialogResult.Confirmed) {
            onContinue()
        } else {
            onCancel()
        }
    }


    // *** NAVIGATION ***
    private fun navigateToAppSettings() {
        activity?.let {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", it.packageName, null)
            )
            it.startActivity(intent)
        }
    }

    private fun navigateToLocationSettings() {
        activity?.let {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            it.startActivity(intent)
        }
    }

    private fun navigateToBluetoothSettings() {
        activity?.let {

            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                it.startActivity(intent)
            }
        }
    }
}

/*fun PermissionResult.toPermissionState(): PermissionState = when (this) {
    PermissionResult.Granted -> PermissionState.Granted
    PermissionResult.Denied -> PermissionState.Denied
    PermissionResult.Canceled -> PermissionState.Canceled
    PermissionResult.ServiceDisabled -> PermissionState.ServiceDisabled
    PermissionResult.Unavailable -> PermissionState.NotRequested

    PermissionResult.NeedsPreciseUpgrade -> PermissionState.PreciseRationaleShown
    PermissionResult.NeedsManualPreciseGrant -> PermissionState.ManualUpgradeRequested
}*/


/** Example Compose call
 *
 * WellstarDialog(
 *             onDismissRequest = {
 *                 // treat outside tap/back as dismiss -> clear dialog state
 *                 state.onEvent(WayfindingGozioNavUiEvent.OnExit)
 *             },
 *             bodyContent = {
 *                 DialogContent(
 *                     primaryImage = R.drawable.parking_active_image,
 *                     title = R.string.wayfinding_parking_leaving_title,
 *                     message = R.string.wayfinding_parking_clear_message,
 *                     positiveString = R.string.wayfinding_positive_confirm_clear_parking,
 *                     positiveButtonClick = {
 *                         state.onEvent(WayfindingGozioNavUiEvent.OnClearParking)
 *                     },
 *                     negativeString = R.string.wayfinding_parking_negative,
 *                     negativeButtonClick = {
 *                         state.onEvent(WayfindingGozioNavUiEvent.OnExit)
 *                     },
 *                 )
 *             }
 *         )
 *
 * */
