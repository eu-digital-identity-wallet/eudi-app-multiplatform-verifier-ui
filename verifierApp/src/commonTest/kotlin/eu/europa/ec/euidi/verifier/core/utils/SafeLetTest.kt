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

package eu.europa.ec.euidi.verifier.core.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SafeLetTest {

    @Test
    fun `Given two non-null arguments When safeLet is called Then the block result is returned`() {
        // When
        val result = safeLet("a", 1) { p1, p2 -> "$p1$p2" }

        // Then
        assertEquals("a1", result)
    }

    @Test
    fun `Given a null first argument When safeLet is called Then null is returned`() {
        // Given
        val first: String? = null

        // Then
        assertNull(safeLet(first, 1) { _, _ -> "block" })
    }

    @Test
    fun `Given a null second argument When safeLet is called Then null is returned`() {
        // Given
        val second: Int? = null

        // Then
        assertNull(safeLet("a", second) { _, _ -> "block" })
    }
}
