/*
 * Copyright (c) 2026 European Commission
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

import kotlinx.coroutines.test.runTest
import okio.FileSystem
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DataStoreControllerTest {

    // A real Preferences DataStore backed by a unique file per controller, so tests are isolated.
    // The path is absolute, in the system temporary directory: DataStore's OkioStorage, which it
    // uses on iOS, refuses a relative path.
    private fun controller(): DataStoreController {
        val path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY /
            "test-datastore" / "${Uuid.random()}.preferences_pb"
        return DataStoreControllerImpl(DataStoreControllerImpl.createDataStore { path.toString() })
    }

    @Test
    fun `Given a stored boolean When values are read back Then the stored value is returned and absent keys give the default or null`() = runTest {
        // Given
        val controller = controller()

        controller.putBoolean(PrefKey.RETAIN_DATA, true)

        // Then
        assertEquals(true, controller.getBoolean(PrefKey.RETAIN_DATA))
        assertEquals(false, controller.getBoolean(PrefKey.USE_L2CAP, default = false))
        assertNull(controller.getBoolean(PrefKey.CLEAR_BLE_CACHE))
    }

    @Test
    fun `Given a stored int When it is read back Then the value is returned and an absent key gives the default`() = runTest {
        // Given
        val controller = controller()

        controller.putInt(PrefKey.RETAIN_DATA, 42)

        // Then
        assertEquals(42, controller.getInt(PrefKey.RETAIN_DATA))
        assertEquals(7, controller.getInt(PrefKey.USE_L2CAP, default = 7))
    }

    @Test
    fun `Given a stored string When it is read back Then the same value is returned`() = runTest {
        // Given
        val controller = controller()

        controller.putString(PrefKey.RETAIN_DATA, "hello")

        // When
        val result = controller.getString(PrefKey.RETAIN_DATA)

        // Then
        assertEquals("hello", result)
    }

    @Test
    fun `Given a stored double When it is read back Then the same value is returned`() = runTest {
        // Given
        val controller = controller()

        controller.putDouble(PrefKey.RETAIN_DATA, 1.5)

        // When
        val result = controller.getDouble(PrefKey.RETAIN_DATA)

        // Then
        assertEquals(1.5, result)
    }

    @Test
    fun `Given a stored float When it is read back Then the same value is returned`() = runTest {
        // Given
        val controller = controller()

        controller.putFloat(PrefKey.RETAIN_DATA, 2.5f)

        // When
        val result = controller.getFloat(PrefKey.RETAIN_DATA)

        // Then
        assertEquals(2.5f, result)
    }

    @Test
    fun `Given a stored long When it is read back Then the same value is returned`() = runTest {
        // Given
        val controller = controller()

        controller.putLong(PrefKey.RETAIN_DATA, 99L)

        // When
        val result = controller.getLong(PrefKey.RETAIN_DATA)

        // Then
        assertEquals(99L, result)
    }

    @Test
    fun `Given a stored byte array When it is read back Then the same bytes are returned`() = runTest {
        // Given
        val controller = controller()
        val bytes = byteArrayOf(1, 2, 3)

        controller.putByteArray(PrefKey.RETAIN_DATA, bytes)

        // When
        val result = controller.getByteArray(PrefKey.RETAIN_DATA)

        // Then
        assertContentEquals(bytes, result)
    }

    @Test
    fun `Given a controller When the preference grouping lists are read Then they expose the configured keys`() {
        // Given
        val controller = controller()

        // Then
        assertEquals(listOf(PrefKey.RETAIN_DATA), controller.getGeneralPrefs())
        assertEquals(
            listOf(PrefKey.USE_L2CAP, PrefKey.CLEAR_BLE_CACHE),
            controller.getRetrievalOptionsPrefs()
        )
        assertEquals(
            listOf(PrefKey.BLE_CENTRAL_CLIENT, PrefKey.BLE_PERIPHERAL_SERVER),
            controller.getRetrievalMethodPrefs()
        )
    }
}
