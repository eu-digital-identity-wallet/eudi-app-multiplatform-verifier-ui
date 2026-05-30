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

package eu.europa.ec.euidi.verifier.core.extension

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Base64ExtensionsTest {

    // region encodeToBase64String

    @Test
    fun `encode and decode round-trips with url-safe padded default`() {
        val bytes = "Hello, EUDI!".encodeToByteArray()

        val encoded = bytes.encodeToBase64String()
        val decoded = encoded.decodeBase64ToBytesOrNull()

        assertContentEquals(bytes, decoded)
    }

    @Test
    fun `encode without padding omits the trailing padding characters`() {
        val bytes = byteArrayOf(1, 2, 3, 4, 5)

        val padded = bytes.encodeToBase64String(withPadding = true)
        val unpadded = bytes.encodeToBase64String(withPadding = false)

        assertTrue(padded.endsWith("="))
        assertTrue(!unpadded.contains("="))
    }

    @Test
    fun `encode with the standard (non url-safe) alphabet is decodable`() {
        // 0xFB, 0xFF encodes to characters that differ between the standard and url-safe alphabets.
        val bytes = byteArrayOf(0xFB.toByte(), 0xFF.toByte())

        val standard = bytes.encodeToBase64String(urlSafe = false)
        val urlSafe = bytes.encodeToBase64String(urlSafe = true)

        assertTrue(standard.any { it == '+' || it == '/' })
        assertTrue(urlSafe.any { it == '-' || it == '_' })
        assertContentEquals(bytes, urlSafe.decodeBase64ToBytesOrNull())
    }

    // endregion

    // region decode

    @Test
    fun `decode strips a data-uri prefix before decoding`() {
        val bytes = "abc".encodeToByteArray()
        val dataUri = "data:text/plain;base64," + bytes.encodeToBase64String()

        assertContentEquals(bytes, dataUri.decodeBase64ToBytesOrNull())
    }

    @Test
    fun `decode returns null for blank input`() {
        assertNull("".decodeBase64ToBytesOrNull())
        assertNull("data:text/plain;base64,".decodeBase64ToBytesOrNull())
    }

    @Test
    fun `decode returns null for malformed base64`() {
        assertNull("!!!".decodeBase64ToBytesOrNull())
    }

    @Test
    fun `decodeBase64ToUtf8OrNull round-trips a string`() {
        val original = "héllo"

        val encoded = original.encodeToByteArray().encodeToBase64String()

        assertEquals(original, encoded.decodeBase64ToUtf8OrNull())
    }

    @Test
    fun `decodeBase64ToUtf8OrNull returns null for malformed base64`() {
        assertNull("!!!".decodeBase64ToUtf8OrNull())
    }

    // endregion
}
