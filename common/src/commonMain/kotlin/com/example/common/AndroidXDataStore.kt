package com.example.common

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

internal val LAST_UPDATE = longPreferencesKey("last_update")

public object AndroidXDataStore {
    private val dataStore = PreferenceDataStoreFactory.createWithPath { "androidx.preferences_pb".toPath() }

    public val lastUpdate: Flow<Long> = dataStore.data.map { it[LAST_UPDATE] ?: 0 }

    public suspend fun updateLastUpdate(time: Long) {
        updatePref(LAST_UPDATE, time)
    }

    private suspend fun <T> updatePref(key: Preferences.Key<T>, value: T) = dataStore.edit { it[key] = value }
}