package com.pekomon.cryptoapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pekomon.cryptoapp.data.Currency
import com.pekomon.cryptoapp.data.SortOption
import com.pekomon.cryptoapp.data.UserCrypto
import com.pekomon.cryptoapp.domain.model.CryptoAsset
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_CURRENCY = stringPreferencesKey("selected_currency")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val FAVORITES = stringSetPreferencesKey("favorites")
        val SELECTED_CRYPTOS = stringSetPreferencesKey("selected_cryptos")
        val USER_CRYPTOS = stringPreferencesKey("user_cryptos")
        val CACHED_CRYPTO_ASSETS = stringPreferencesKey("cached_crypto_assets")
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
            enumValueOrDefault(currencyName, Currency.EUR)
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
            val sortOptionName = preferences[PreferencesKeys.SORT_OPTION] ?: SortOption.DEFAULT.name
            enumValueOrDefault(sortOptionName, SortOption.DEFAULT)
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
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            try {
                val json = preferences[PreferencesKeys.USER_CRYPTOS] ?: "[]"
                Json.decodeFromString<List<UserCrypto>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        }

    val cachedCryptoAssets: Flow<List<CryptoAsset>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            try {
                val json = preferences[PreferencesKeys.CACHED_CRYPTO_ASSETS] ?: "[]"
                Json.decodeFromString<List<CachedCryptoAsset>>(json).map { it.toDomain() }
            } catch (e: Exception) {
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
        val json = Json.encodeToString(cryptos)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_CRYPTOS] = json
        }
    }

    suspend fun updateCachedCryptoAssets(assets: List<CryptoAsset>) {
        val json = Json.encodeToString(assets.map { CachedCryptoAsset.fromDomain(it) })
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHED_CRYPTO_ASSETS] = json
        }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(
        name: String,
        defaultValue: T
    ): T = enumValues<T>().firstOrNull { it.name == name } ?: defaultValue
}

@Serializable
private data class CachedCryptoAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val marketCapRank: Int?
) {
    fun toDomain(): CryptoAsset {
        return CryptoAsset(
            id = id,
            symbol = symbol,
            name = name,
            marketCapRank = marketCapRank
        )
    }

    companion object {
        fun fromDomain(asset: CryptoAsset): CachedCryptoAsset {
            return CachedCryptoAsset(
                id = asset.id,
                symbol = asset.symbol,
                name = asset.name,
                marketCapRank = asset.marketCapRank
            )
        }
    }
}
