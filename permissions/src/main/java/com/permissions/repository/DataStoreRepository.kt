package com.permissions.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import kotlin.reflect.KClass

interface DataStoreRepository {
    suspend fun getString(key: String, defaultValue: String = ""): String
    suspend fun getInt(key: String): Int?
    suspend fun getLong(key: String): Long?
    suspend fun getDouble(key: String): Double?
    suspend fun getFloat(key: String): Float?
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    // allowing nullables to clear preferences when needed
    suspend fun setString(key: String, value: String?)
    suspend fun setInt(key: String, value: Int?)
    suspend fun setLong(key: String, value: Long?)
    suspend fun setDouble(key: String, value: Double?)
    suspend fun setFloat(key: String, value: Float?)
    suspend fun setBoolean(key: String, value: Boolean?)

    // allowing custom storage of objects when needed
    suspend fun <T : Any> getObject(key: String, kClass: KClass<T>): T?
    suspend fun <T> setObject(key: String, value: T?)
}

// TODO: make sure you give this a unique name that reflects a permission vault specific to the app
const val PREFERENCES_DATASTORE_NAME = "sample_permissions_vault"

// Note: This is at the top level of the file, outside of any classes.
private val Context.dataStore by preferencesDataStore(PREFERENCES_DATASTORE_NAME)

class DataStoreRepositoryImpl(
    val app: Context,
    val gson: Gson,
) : DataStoreRepository {

    private val dataStore = app.dataStore

    override suspend fun getString(key: String, defaultValue: String): String {
        val dataStoreKey = stringPreferencesKey(key)
        return getPreferences()[dataStoreKey] ?: defaultValue
    }

    override suspend fun getInt(key: String): Int? {
        val dataStoreKey = intPreferencesKey(key)
        return getPreferences()[dataStoreKey]
    }

    override suspend fun getLong(key: String): Long? {
        val dataStoreKey = longPreferencesKey(key)
        return getPreferences()[dataStoreKey]
    }

    override suspend fun getDouble(key: String): Double? {
        val dataStoreKey = doublePreferencesKey(key)
        return getPreferences()[dataStoreKey]
    }

    override suspend fun getFloat(key: String): Float? {
        val dataStoreKey = floatPreferencesKey(key)
        return getPreferences()[dataStoreKey]
    }

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val dataStoreKey = booleanPreferencesKey(key)
        return getPreferences()[dataStoreKey] ?: defaultValue
    }

    override suspend fun <T : Any> getObject(key: String, kClass: KClass<T>): T? {
        return get(gson, key, kClass)
    }

    override suspend fun setString(key: String, value: String?) {
        if (value == null) {
            removePreferences(stringPreferencesKey(key))
        } else {
            editPreferences(
                stringPreferencesKey(key),
                value,
            )
        }
    }

    override suspend fun setInt(key: String, value: Int?) {
        if (value == null) {
            removePreferences(intPreferencesKey(key))
        } else {
            editPreferences(
                intPreferencesKey(key),
                value,
            )
        }
    }

    override suspend fun setLong(key: String, value: Long?) {
        if (value == null) {
            removePreferences(longPreferencesKey(key))
        } else {
            editPreferences(
                longPreferencesKey(key),
                value,
            )
        }
    }

    override suspend fun setDouble(key: String, value: Double?) {
        if (value == null) {
            removePreferences(doublePreferencesKey(key))
        } else {
            editPreferences(
                doublePreferencesKey(key),
                value,
            )
        }
    }

    override suspend fun setFloat(key: String, value: Float?) {
        if (value == null) {
            removePreferences(floatPreferencesKey(key))
        } else {
            editPreferences(
                floatPreferencesKey(key),
                value,
            )
        }
    }

    override suspend fun setBoolean(key: String, value: Boolean?) {
        if (value == null) {
            removePreferences(booleanPreferencesKey(key))
        } else {
            editPreferences(
                booleanPreferencesKey(key),
                value,
            )
        }
    }

    override suspend fun <T> setObject(key: String, value: T?) {
        if (value == null) {
            removePreferences(stringPreferencesKey(key))
        } else {
            set(gson, key, value)
        }
    }

    private suspend fun getPreferences() = dataStore.data.firstOrNull() ?: emptyPreferences()

    private suspend fun <T : Any> editPreferences(dataStoreKey: Preferences.Key<T>, value: T) {
        try {
            dataStore.edit { preferences ->
                preferences[dataStoreKey] = value
            }
        } catch (exception: Exception) {
            Timber.e("Unable to save datastore preference: ${exception.message}")
        }
    }

    private suspend fun <T : Any> removePreferences(dataStoreKey: Preferences.Key<T>) {
        try {
            dataStore.edit { preferences ->
                preferences.remove(dataStoreKey)
            }
        } catch (exception: Exception) {
            Timber.e("Unable to remove datastore preference: ${exception.message}")
        }
    }

    private suspend fun <T : Any> get(gson: Gson, key: String, c: KClass<T>): T? {
        val dataStoreKey = stringPreferencesKey(key)
        val value = getPreferences()[dataStoreKey]
        return try {
            value?.let {
                gson.fromJson(it, c.java)
            }
        } catch (exception: Exception) {
            Timber.e(
                "Exception occurred. " +
                        "Unable to retrieve from DataStore, " +
                        "exception: ${exception.message}",
            )
            null
        }
    }

    private suspend fun <T> set(gson: Gson, key: String, `object`: T) {
        editPreferences(
            stringPreferencesKey(key),
            gson.toJson(`object`),
        )
    }
}
