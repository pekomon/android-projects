package com.example.pekomon.cryptoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import android.util.Log

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {
    
    private object PreferencesKeys {
        val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val FAVORITES = stringSetPreferencesKey("favorites")
        val SELECTED_CRYPTOS = stringSetPreferencesKey("selected_cryptos")
        val USER_CRYPTOS = stringPreferencesKey("user_cryptos")
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
    
    val selectedCryptos: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_CRYPTOS] ?: emptySet()
        }
    
    val userCryptos: Flow<List<UserCrypto>> = context.dataStore.data
        .catch { exception ->
            Log.e("PreferencesRepository", "Error reading user cryptos", exception)
            emit(emptyPreferences())
        }
        .map { preferences ->
            try {
                val json = preferences[PreferencesKeys.USER_CRYPTOS] ?: "[]"
                Log.d("PreferencesRepository", "Reading user cryptos: $json")
                Json.decodeFromString<List<UserCrypto>>(json)
            } catch (e: Exception) {
                Log.e("PreferencesRepository", "Error deserializing user cryptos", e)
                emptyList()
            }
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
    
    suspend fun updateSelectedCryptos(cryptos: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_CRYPTOS] = cryptos
        }
    }
    
    suspend fun updateUserCryptos(cryptos: List<UserCrypto>) {
        try {
            val json = Json.encodeToString(cryptos)
            Log.d("PreferencesRepository", "Saving user cryptos: $json")
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.USER_CRYPTOS] = json
            }
        } catch (e: Exception) {
            Log.e("PreferencesRepository", "Error saving user cryptos", e)
        }
    }
} 