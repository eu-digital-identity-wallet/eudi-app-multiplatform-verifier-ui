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
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class DataStoreControllerTest {

    // A real Preferences DataStore backed by a unique temp file per controller, so tests are isolated.
    private fun controller(): DataStoreController {
        val path = "build/test-datastore/${Uuid.random()}.preferences_pb"
        return DataStoreControllerImpl(DataStoreControllerImpl.createDataStore { path })
    }

    @Test
    fun `boolean is stored and read back, falling back to the default when absent`() = runTest {
        val controller = controller()

        controller.putBoolean(PrefKey.RETAIN_DATA, true)

        assertEquals(true, controller.getBoolean(PrefKey.RETAIN_DATA))
        assertEquals(false, controller.getBoolean(PrefKey.USE_L2CAP, default = false))
        assertNull(controller.getBoolean(PrefKey.CLEAR_BLE_CACHE))
    }

    @Test
    fun `int is stored and read back, with a default fallback`() = runTest {
        val controller = controller()

        controller.putInt(PrefKey.RETAIN_DATA, 42)

        assertEquals(42, controller.getInt(PrefKey.RETAIN_DATA))
        assertEquals(7, controller.getInt(PrefKey.USE_L2CAP, default = 7))
    }

    @Test
    fun `string is stored and read back`() = runTest {
        val controller = controller()

        controller.putString(PrefKey.RETAIN_DATA, "hello")

        assertEquals("hello", controller.getString(PrefKey.RETAIN_DATA))
    }

    @Test
    fun `double is stored and read back`() = runTest {
        val controller = controller()

        controller.putDouble(PrefKey.RETAIN_DATA, 1.5)

        assertEquals(1.5, controller.getDouble(PrefKey.RETAIN_DATA))
    }

    @Test
    fun `float is stored and read back`() = runTest {
        val controller = controller()

        controller.putFloat(PrefKey.RETAIN_DATA, 2.5f)

        assertEquals(2.5f, controller.getFloat(PrefKey.RETAIN_DATA))
    }

    @Test
    fun `long is stored and read back`() = runTest {
        val controller = controller()

        controller.putLong(PrefKey.RETAIN_DATA, 99L)

        assertEquals(99L, controller.getLong(PrefKey.RETAIN_DATA))
    }

    @Test
    fun `byteArray is stored and read back`() = runTest {
        val controller = controller()
        val bytes = byteArrayOf(1, 2, 3)

        controller.putByteArray(PrefKey.RETAIN_DATA, bytes)

        assertContentEquals(bytes, controller.getByteArray(PrefKey.RETAIN_DATA))
    }

    @Test
    fun `preference grouping lists expose the configured keys`() {
        val controller = controller()

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
