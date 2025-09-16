package com.permissions.util


import android.content.Context

// Allows us to pass the application context from the app module to this module
object PermissionsInitializer {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        // Ensure the context is application-level
        appContext = context.applicationContext
    }

    fun getAppContext(): Context {
        if (!::appContext.isInitialized) {
            throw IllegalStateException("NewModuleInitializer is not initialized. Call initialize() first.")
        }
        return appContext
    }
}