package com.example.pekomon.cryptoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {
    
    private object PreferencesKeys {
        val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val FAVORITES = stringSetPreferencesKey("favorites")
    }
    
    val selectedCurrency: Flow<Currency> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val currencyName = preferences[PreferencesKeys.SELECTED_CURRENCY] ?: Currency.EUR.name
            Currency.valueOf(currencyName)
        }
    
    val sortOption: Flow<SortOption> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOptionName = preferences[PreferencesKeys.SORT_OPTION] ?: SortOption.NAME_ASC.name
            SortOption.valueOf(sortOptionName)
        }
    
    val favorites: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.FAVORITES] ?: emptySet()
        }
    
    suspend fun updateSelectedCurrency(currency: Currency) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CURRENCY] = currency.name
        }
    }
    
    suspend fun updateSortOption(sortOption: SortOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_OPTION] = sortOption.name
        }
    }
    
    suspend fun updateFavorites(favorites: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAVORITES] = favorites
        }
    }
} 