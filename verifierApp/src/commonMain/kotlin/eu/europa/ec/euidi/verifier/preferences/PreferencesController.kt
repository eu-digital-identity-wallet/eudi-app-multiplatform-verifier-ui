package eu.europa.ec.euidi.verifier.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import eu.europa.ec.euidi.verifier.util.fromBase64
import eu.europa.ec.euidi.verifier.util.toBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class PreferencesController(
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
        inline fun <reified T> serialize(value: T): ByteArray {
            val unencryptedString = Json.encodeToString(value)
            val unencryptedBytes = unencryptedString.toByteArray()
            val encryptedBytes = Crypto.encrypt(unencryptedBytes)
            return encryptedBytes.toBase64()
        }

        inline fun <reified T> deserialize(value: ByteArray): T {
            val decryptedBytes = Crypto.decrypt(value.fromBase64())
            val decryptedString = decryptedBytes.decodeToString()
            return Json.decodeFromString(decryptedString)
        }
    }
}