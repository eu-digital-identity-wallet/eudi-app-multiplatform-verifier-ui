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
    fun `safeLet invokes the block when both arguments are non-null`() {
        val result = safeLet("a", 1) { p1, p2 -> "$p1$p2" }

        assertEquals("a1", result)
    }

    @Test
    fun `safeLet returns null when the first argument is null`() {
        val first: String? = null

        assertNull(safeLet(first, 1) { _, _ -> "block" })
    }

    @Test
    fun `safeLet returns null when the second argument is null`() {
        val second: Int? = null

        assertNull(safeLet("a", second) { _, _ -> "block" })
    }
}
