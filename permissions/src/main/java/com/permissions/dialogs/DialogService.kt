package com.permissions.dialogs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.CompletableDeferred

class DialogService {
    private val _requests = MutableSharedFlow<DialogSpec>(extraBufferCapacity = 1)
    val requests: SharedFlow<DialogSpec> = _requests

    suspend fun request(
        spec: DialogSpec
    ): DialogResult = withContext(Dispatchers.Main) {
        _requests.tryEmit(spec)
        spec.result.await()
    }

    suspend fun confirm(
        title: String,
        message: String,
        positive: String = "Continue",
        negative: String = "Cancel",
    ): Boolean =
        request(ConfirmSpec(title, message, positive, negative)) == DialogResult.Confirmed
}