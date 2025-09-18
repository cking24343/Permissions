package com.permissions.dialogs

import com.permissions.dialogs.model.ConfirmDialogSpec
import com.permissions.enums.UiRequestResult
import com.permissions.dialogs.model.UiRequestSpec
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class PermissionRequestService {
    private val _requests =
        MutableSharedFlow<Pair<UiRequestSpec, CompletableDeferred<UiRequestResult>>>(
            extraBufferCapacity = 1
        )
    val requests: SharedFlow<Pair<UiRequestSpec, CompletableDeferred<UiRequestResult>>> = _requests

    private val _dismiss = MutableSharedFlow<String>(extraBufferCapacity = 1)

    // emits spec.id to dismiss
    val dismiss = _dismiss as SharedFlow<String>

    suspend fun request(
        spec: UiRequestSpec
    ): UiRequestResult {
        val deferred = CompletableDeferred<UiRequestResult>()
        withContext(Dispatchers.Main) {
            _requests.tryEmit(spec to deferred)
        }
        return deferred.await()
    }

    fun dismiss(id: String) {
        _dismiss.tryEmit(id)
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