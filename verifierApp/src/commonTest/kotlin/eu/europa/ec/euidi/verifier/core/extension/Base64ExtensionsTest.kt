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
    fun `Given bytes When they are encoded and decoded with the defaults Then the original bytes are returned`() {
        // Given
        val bytes = "Hello, EUDI!".encodeToByteArray()

        // When
        val encoded = bytes.encodeToBase64String()
        val decoded = encoded.decodeBase64ToBytesOrNull()

        // Then
        assertContentEquals(bytes, decoded)
    }

    @Test
    fun `Given bytes that need padding When they are encoded with and without padding Then only the padded string ends with an equals sign`() {
        // Given
        val bytes = byteArrayOf(1, 2, 3, 4, 5)

        // When
        val padded = bytes.encodeToBase64String(withPadding = true)
        val unpadded = bytes.encodeToBase64String(withPadding = false)

        // Then
        assertTrue(padded.endsWith("="))
        assertTrue(!unpadded.contains("="))
    }

    /**
     * 0xFB, 0xFF encodes to characters that differ between the standard and url-safe alphabets.
     */
    @Test
    fun `Given bytes that encode differently per alphabet When encoded with the standard and url-safe alphabets Then each uses its own characters and the url-safe string decodes`() {
        // Given
        val bytes = byteArrayOf(0xFB.toByte(), 0xFF.toByte())

        // When
        val standard = bytes.encodeToBase64String(urlSafe = false)
        val urlSafe = bytes.encodeToBase64String(urlSafe = true)

        // Then
        assertTrue(standard.any { it == '+' || it == '/' })
        assertTrue(urlSafe.any { it == '-' || it == '_' })
        assertContentEquals(bytes, urlSafe.decodeBase64ToBytesOrNull())
    }

    // endregion

    // region decode

    @Test
    fun `Given a data-uri prefixed string When it is decoded Then the prefix is stripped and the bytes are returned`() {
        // Given
        val bytes = "abc".encodeToByteArray()
        val dataUri = "data:text/plain;base64," + bytes.encodeToBase64String()

        // When
        val result = dataUri.decodeBase64ToBytesOrNull()

        // Then
        assertContentEquals(bytes, result)
    }

    @Test
    fun `Given an empty string and an empty data-uri payload When they are decoded Then null is returned`() {
        // Then
        assertNull("".decodeBase64ToBytesOrNull())
        assertNull("data:text/plain;base64,".decodeBase64ToBytesOrNull())
    }

    @Test
    fun `Given malformed base64 When it is decoded Then null is returned`() {
        // When
        val result = "!!!".decodeBase64ToBytesOrNull()

        // Then
        assertNull(result)
    }

    @Test
    fun `Given a string with an accented character When it is encoded and decoded Then the same string is returned`() {
        // Given
        val original = "héllo"

        // When
        val encoded = original.encodeToByteArray().encodeToBase64String()

        // Then
        assertEquals(original, encoded.decodeBase64ToUtf8OrNull())
    }

    @Test
    fun `Given malformed base64 When it is decoded to UTF-8 Then null is returned`() {
        // When
        val result = "!!!".decodeBase64ToUtf8OrNull()

        // Then
        assertNull(result)
    }

    // endregion
}
