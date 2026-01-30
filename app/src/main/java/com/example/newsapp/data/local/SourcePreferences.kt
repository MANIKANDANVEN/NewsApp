package com.example.newsapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SourcePreferences(private val context: Context) {
    private val Context.dataStore by preferencesDataStore("settings")
    private val SELECTED_SOURCES = stringSetPreferencesKey("selected_sources")

    val selectedSources: Flow<Set<String>> = context.dataStore.data
        .map { it[SELECTED_SOURCES] ?: emptySet() }

    suspend fun saveSources(sources: Set<String>) {
        context.dataStore.edit { it[SELECTED_SOURCES] = sources }
    }
}