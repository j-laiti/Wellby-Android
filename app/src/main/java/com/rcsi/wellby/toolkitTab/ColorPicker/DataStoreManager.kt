package com.rcsi.wellby.toolkitTab.ColorPicker
// data store for the app color and quote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.booleanPreferencesKey


object DataStoreManager {
    private const val DATA_STORE_FILE_NAME = "settings_data_store"
    private val Context.dataStore by preferencesDataStore(DATA_STORE_FILE_NAME)
    private val SELECTED_PRIMARY_COLOR_KEY = stringPreferencesKey("selected_primary_color")
    private val SELECTED_SECONDARY_COLOR_KEY = stringPreferencesKey("selected_secondary_color")

    suspend fun saveSelectedPrimaryColor(context: Context, color: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_PRIMARY_COLOR_KEY] = color
        }
    }

    suspend fun saveSelectedSecondaryColor(context: Context, color: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_SECONDARY_COLOR_KEY] = color
        }
    }

    val Context.selectedPrimaryColorFlow: Flow<String?> get() = dataStore.data
        .map { preferences ->
            preferences[SELECTED_PRIMARY_COLOR_KEY]
        }

    val Context.selectedSecondaryColorFlow: Flow<String?> get() = dataStore.data
        .map { preferences ->
            preferences[SELECTED_SECONDARY_COLOR_KEY]
        }


    private val DISPLAY_QUOTE_KEY = booleanPreferencesKey("display_quote")

    suspend fun saveDisplayQuoteSetting(context: Context, display: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DISPLAY_QUOTE_KEY] = display
        }
    }

    val Context.displayQuoteFlow: Flow<Boolean> get() = dataStore.data
        .map { preferences ->
            preferences[DISPLAY_QUOTE_KEY] as? Boolean ?: true
        }

}
