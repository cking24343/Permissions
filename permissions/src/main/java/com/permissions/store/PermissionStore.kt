package com.permissions.store

import com.permissions.models.PermissionState
import com.permissions.models.PermissionType
import com.permissions.repository.DataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface PermissionStore {
    fun observeState(type: PermissionType): Flow<PermissionState>

    suspend fun getState(type: PermissionType): PermissionState
    suspend fun setState(type: PermissionType, state: PermissionState)
}

class PermissionStoreImpl: PermissionStore, KoinComponent {
    // DI
    private val dataStoreRepository: DataStoreRepository by inject()

    override suspend fun getState(type: PermissionType): PermissionState {
        val key = type.dataStoreKey()
        val storedValue = dataStoreRepository.getString(key)
        return try {
            PermissionState.valueOf(storedValue)
        } catch (e: Exception) {
            PermissionState.NotRequested
        }
    }

    override fun observeState(type: PermissionType): Flow<PermissionState> {
        return flow {
            emit(getState(type))
        }
    }

    override suspend fun setState(type: PermissionType, state: PermissionState) {
        val key = type.dataStoreKey()
        dataStoreRepository.setString(key, state.name)
    }
}