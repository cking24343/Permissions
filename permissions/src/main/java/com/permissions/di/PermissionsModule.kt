package com.permissions.di

import com.google.gson.Gson
import com.permissions.api.PermissionsManager
import com.permissions.api.PermissionsManagerImpl
import com.permissions.dialogs.DialogService
import com.permissions.repository.DataStoreRepositoryImpl
import com.permissions.repository.DataStoreRepository
import com.permissions.store.PermissionStore
import com.permissions.store.PermissionStoreImpl
import org.koin.dsl.module

val permissionsModule = module {
    single { Gson() }

    single<PermissionsManager> { PermissionsManagerImpl() }

    single<DataStoreRepository> { DataStoreRepositoryImpl(get(), get()) }
    single<PermissionStore> { PermissionStoreImpl() }

    // Dialog Module
    single{ DialogService() }
}

