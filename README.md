# Permissions Module

A self-contained Android library module that encapsulates **runtime permission requests** into a single, consistent API.  
It centralizes handling of permissions, rationales, system prompts, and state tracking, while providing flexibility for either **out-of-the-box rationale dialogs** or **headless (no rationale) requests**.

---

## Why this project?

Dealing with Android runtime permissions can be messy:
- Every permission requires its own request and rationale flow.
- System prompts may behave differently across devices and Android versions.
- Apps often duplicate the same boilerplate code.

This module attempts to simplify permission management by:
- **Encapsulating flows** for common runtime permissions.
- **Tracking state** (granted, denied, rationale shown, manually revoked, etc.).
- **Supporting customizable UI** (dialogs or full-screen) for rationales.
- **Allowing headless flows** for apps that just want to trigger system prompts without rationales.

---

## Features

- ✅ Out-of-the-box support for:
  - **Camera**
  - **Bluetooth (S+)**
  - **Notifications (T+)**
  - **Location (Coarse / Fine / Background)**
- ✅ Built-in rationale dialogs with overridable UI.
- ✅ Programmatic (headless) permission requests (`NoRationales` mode).
- ✅ State persistence via a `PermissionStore`.
- ✅ Extensible: plug in custom UI renderers for rationales.

---

## Installation

⚠️ **Not yet published to MavenCentral/JitPack.**  
For now, you’ll need to add this module **locally** to your project:

1. Copy or include the `:permissions` module into your Android project.
2. In your project `settings.gradle`:

```kotlin
include(":app")
include(":permissions")
```

3. In your app’s `build.gradle`:

```kotlin
dependencies {
    implementation(project(":permissions"))
}
```

## Quick Reference

### Attach to your `MainActivity`

```kotlin
class MainActivity : ComponentActivity() {
    private val permissionsManager: PermissionsManager by inject() // sample uses Koin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsManager.attachActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionsManager.detachActivity()
    }
}
```

### Request a permission flow (with rationales)

```kotlin
permissionsManager.requestCameraFlow { result ->
    when (result) {
        PermissionResult.Granted -> { /* proceed */ }
        PermissionResult.Denied -> { /* show error */ }
        PermissionResult.Canceled -> { /* user canceled */ }
        else -> { /* handle other states */ }
    }
}
```

### Request with No Rationales (headless)
Skip all rationale dialogs — only system prompts are shown:

```kotlin
permissionsManager.requestCameraFlow(
    uiMode = PermissionUiMode.NoRationales
) { result ->
    if (result == PermissionResult.Granted) {
        // camera ready
    }
}
```

### Override Rationale UI
You can register your own UI for rationale prompts.
For example, override the Location rationale with a full-screen composable:

```kotlin
class PermissionsRequestUiOverrides {
    fun buildOverrides(registry: PermissionRendererRegistry) {
        registry.register(
            tag = DialogTags.RATIONALE_LOCATION.tag,
            type = CustomSpec::class,
            renderer = TypedUiRenderer { spec, complete ->
                LocationFullscreenRationale(
                    spec = spec,
                    result = complete
                )
            }
        )
    }
}
```
Your app can decide between dialogs, full-screens, or any other UI.

### Sample App
The repo includes a sample app showing:
- DI with Koin to inject the PermissionsManager.
- Examples of requesting camera, location, and Bluetooth permissions.
- Overriding rationales with custom UI.
Run the sample to see working flows in action.

### Roadmap / TODOs
This is a side project intended to grow into a full permissions library.
Currently supported: 
- Camera
- Bluetooth
- Notifications
- Location (fine/coarse/background)

Planned enhancements:
- Add BLUETOOTH_SCAN flow (Android 12+).
- Proper Precise Location upgrade flow (request ACCESS_FINE_LOCATION when coarse already granted).
- Add permissions for:
  - Microphone
  - Contacts
  - Storage / Media (Android 13+ granular permissions)
  - Calendar
  - Phone (Call / Read Phone State)
  - Sensors (Activity Recognition, Body Sensors)
- Better multi-permission batch requests.
- Publish to MavenCentral/JitPack for easy Gradle import.

### License
MIT (or whatever you decide to apply).
