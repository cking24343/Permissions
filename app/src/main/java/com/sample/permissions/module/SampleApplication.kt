package com.sample.permissions.module

import android.app.Application
import com.permissions.di.permissionsModule
import com.permissions.util.PermissionsInitializer
import com.sample.permissions.module.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Need to initialize the permissions module
        PermissionsInitializer.initialize(this)

        startKoin {
            androidContext(this@SampleApplication)
            modules(appModule)
            modules(permissionsModule)
        }
    }
}