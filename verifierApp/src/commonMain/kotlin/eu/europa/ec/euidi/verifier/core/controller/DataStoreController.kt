/*
 * Copyright (c) 2023 European Commission
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
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import eu.europa.ec.euidi.verifier.core.crypto.KeyStore
import eu.europa.ec.euidi.verifier.core.utils.fromBase64
import eu.europa.ec.euidi.verifier.core.utils.toBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath

object PrefKey {
    const val FOO = "FOO"
}

class DataStoreController(
    val dataStore: DataStore<Preferences>
) {

    suspend inline fun <reified T> save(key: String, value: T) {
        dataStore.edit { preferences ->
            preferences[byteArrayPreferencesKey(key)] = serialize(value)
        }
    }

    inline fun <reified T> retrieve(key: String): Flow<T?> = flow {
        val value = dataStore.data.map { preferences ->
            preferences[byteArrayPreferencesKey(key)]
        }.firstOrNull()
        value?.let {
            emit(deserialize(it))
        } ?: emit(null)
    }


    companion object {
        const val DATASTORE_FILENAME = "verifier.preferences_pb"

        fun createDataStore(producePath: () -> String): DataStore<Preferences> {
            return PreferenceDataStoreFactory.createWithPath { producePath().toPath() }
        }

        inline fun <reified T> serialize(value: T): ByteArray {
            val unencryptedString = Json.Default.encodeToString(value)
            val unencryptedBytes = unencryptedString.toByteArray()
            val encryptedBytes = KeyStore.encrypt(unencryptedBytes)
            return encryptedBytes.toBase64()
        }

        inline fun <reified T> deserialize(value: ByteArray): T {
            val decryptedBytes = KeyStore.decrypt(value.fromBase64())
            val decryptedString = decryptedBytes.decodeToString()
            return Json.Default.decodeFromString(decryptedString)
        }
    }
}