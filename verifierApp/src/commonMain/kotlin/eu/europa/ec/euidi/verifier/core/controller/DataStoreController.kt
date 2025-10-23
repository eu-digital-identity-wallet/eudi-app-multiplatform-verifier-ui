/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package eu.europa.ec.euidi.verifier.core.controller

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

enum class PrefKey(val identifier: String) {
    RETAIN_DATA("retain_data"),
    USE_L2CAP("use_l2cap"),
    CLEAR_BLE_CACHE("clear_ble_cache"),
    BLE_CENTRAL_CLIENT("ble_central_client"),
    BLE_PERIPHERAL_SERVER("ble_peripheral_server"),
}

private val generalPrefs = listOf(
    PrefKey.RETAIN_DATA,
)

private val retrievalOptionsPrefs = listOf(
    PrefKey.USE_L2CAP,
    PrefKey.CLEAR_BLE_CACHE,
)

private val retrievalMethodPrefs = listOf(
    PrefKey.BLE_CENTRAL_CLIENT,
    PrefKey.BLE_PERIPHERAL_SERVER,
)

interface DataStoreController {
    suspend fun putBoolean(key: PrefKey, value: Boolean)
    suspend fun putInt(key: PrefKey, value: Int)
    suspend fun putString(key: PrefKey, value: String)
    suspend fun putDouble(key: PrefKey, value: Double)
    suspend fun putFloat(key: PrefKey, value: Float)
    suspend fun putByteArray(key: PrefKey, value: ByteArray)
    suspend fun putLong(key: PrefKey, value: Long)

    suspend fun getBoolean(key: PrefKey, default: Boolean? = null): Boolean?
    suspend fun getInt(key: PrefKey, default: Int? = null): Int?
    suspend fun getString(key: PrefKey, default: String? = null): String?
    suspend fun getDouble(key: PrefKey, default: Double? = null): Double?
    suspend fun getFloat(key: PrefKey, default: Float? = null): Float?
    suspend fun getByteArray(key: PrefKey, default: ByteArray? = null): ByteArray?
    suspend fun getLong(key: PrefKey, default: Long? = null): Long?

    fun getGeneralPrefs(): List<PrefKey>
    fun getRetrievalOptionsPrefs(): List<PrefKey>
    fun getRetrievalMethodPrefs(): List<PrefKey>
}

class DataStoreControllerImpl(
    val dataStore: DataStore<Preferences>
) : DataStoreController {
    override suspend fun putBoolean(key: PrefKey, value: Boolean) =
        savePreference(booleanPreferencesKey(key.identifier), value)

    override suspend fun putInt(key: PrefKey, value: Int) =
        savePreference(intPreferencesKey(key.identifier), value)

    override suspend fun putString(key: PrefKey, value: String) =
        savePreference(stringPreferencesKey(key.identifier), value)

    override suspend fun putDouble(key: PrefKey, value: Double) =
        savePreference(doublePreferencesKey(key.identifier), value)

    override suspend fun putFloat(key: PrefKey, value: Float) =
        savePreference(floatPreferencesKey(key.identifier), value)

    override suspend fun putByteArray(key: PrefKey, value: ByteArray) =
        savePreference(byteArrayPreferencesKey(key.identifier), value)

    override suspend fun putLong(key: PrefKey, value: Long) =
        savePreference(longPreferencesKey(key.identifier), value)

    override suspend fun getBoolean(key: PrefKey, default: Boolean?): Boolean? =
        readPreference(
            preferencesKey = booleanPreferencesKey(key.identifier),
            defaultValue = default
        )

    override suspend fun getInt(key: PrefKey, default: Int?): Int? =
        readPreference(
            preferencesKey = intPreferencesKey(key.identifier),
            defaultValue = default
        )

    override suspend fun getString(key: PrefKey, default: String?): String? =
        readPreference(
            preferencesKey = stringPreferencesKey(key.identifier),
            defaultValue = default
        )

    override suspend fun getDouble(key: PrefKey, default: Double?): Double? =
        readPreference(
            preferencesKey = doublePreferencesKey(key.identifier),
            defaultValue = default
        )

    override suspend fun getFloat(key: PrefKey, default: Float?): Float? =
        readPreference(
            preferencesKey = floatPreferencesKey(key.identifier),
            defaultValue = default
        )

    override suspend fun getByteArray(key: PrefKey, default: ByteArray?): ByteArray? =
        readPreference(
            preferencesKey = byteArrayPreferencesKey(key.identifier),
            defaultValue = default
        )

    override suspend fun getLong(key: PrefKey, default: Long?): Long? =
        readPreference(
            preferencesKey = longPreferencesKey(key.identifier),
            defaultValue = default
        )

    override fun getGeneralPrefs(): List<PrefKey> = generalPrefs

    override fun getRetrievalOptionsPrefs(): List<PrefKey> = retrievalOptionsPrefs

    override fun getRetrievalMethodPrefs(): List<PrefKey> = retrievalMethodPrefs

    private suspend fun <T> savePreference(
        key: Preferences.Key<T>,
        value: T
    ) {
        dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    private suspend fun <T> readPreference(
        preferencesKey: Preferences.Key<T>,
        defaultValue: T?
    ): T? {
        return dataStore.data
            .map { prefs -> prefs[preferencesKey] }
            .firstOrNull() ?: defaultValue
    }

    companion object {
        const val DATASTORE_FILENAME = "verifier.preferences_pb"

        fun createDataStore(producePath: () -> String): DataStore<Preferences> {
            return PreferenceDataStoreFactory.createWithPath { producePath().toPath() }
        }
    }
}