package com.permissions.dialogs

import com.permissions.dialogs.model.ConfirmDialogSpec
import com.permissions.dialogs.model.UiRequestResult
import com.permissions.dialogs.model.UiRequestSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class PermissionRequestService {
    private val _requests = MutableSharedFlow<UiRequestSpec>(extraBufferCapacity = 1)
    val requests: SharedFlow<UiRequestSpec> = _requests

    suspend fun request(
        spec: UiRequestSpec
    ): UiRequestResult = withContext(Dispatchers.Main) {
        _requests.tryEmit(spec)
        spec.result.await()
    }

    suspend fun confirm(
        title: String,
        message: String,
        positive: String = "Continue",
        negative: String = "Cancel",
    ): Boolean = request(
        spec = ConfirmDialogSpec(
            title = title,
            message = message,
            positiveText = positive,
            negativeText = negative
        )
    ) == UiRequestResult.Confirmed
}