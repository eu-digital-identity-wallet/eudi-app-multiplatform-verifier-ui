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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FlowExtensionsTest {

    @Test
    fun `safeAsync passes values through when no error occurs`() = runTest {
        // Uses the default dispatcher (Dispatchers.IO) on purpose.
        val result = flowOf(1, 2, 3)
            .safeAsync { -1 }
            .toList()

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `safeAsync emits the fallback value when the flow throws`() = runTest {
        val failing = flow {
            emit(1)
            throw RuntimeException("boom")
        }

        val result = failing
            .safeAsync(dispatcher = Dispatchers.Unconfined) { -1 }
            .toList()

        assertEquals(listOf(1, -1), result)
    }
}
