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

@file:OptIn(ExperimentalTime::class)

package eu.europa.ec.euidi.verifier.presentation.ui.show_document.model

import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.model.DocumentValidityDomain
import eu.europa.ec.euidi.verifier.testutil.sequentialUuidProvider
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class DocumentValidityMappersTest {

    private fun resourceProvider(): ResourceProvider = mock {
        every { getSharedString(any()) } calls { (_: StringResource) -> "label" }
    }

    //region toUi

    @Test
    fun `toUi maps the booleans and formats the instants`() {
        val instant = Instant.parse("2025-12-10T12:00:00Z")
        val domain = DocumentValidityDomain(
            isDeviceSignatureValid = true,
            isIssuerSignatureValid = false,
            isDataIntegrityIntact = true,
            signed = instant,
            validFrom = instant,
            validUntil = instant,
        )

        val ui = domain.toUi()

        assertEquals(true, ui.isDeviceSignatureValid)
        assertEquals(false, ui.isIssuerSignatureValid)
        assertEquals(true, ui.isDataIntegrityIntact)
        // Formatted as "dd MMM yyyy" (in the system time zone).
        val dateRegex = Regex("""\d{2} \w{3} \d{4}""")
        assertTrue(ui.signed?.matches(dateRegex) == true, "Unexpected date format: ${ui.signed}")
        assertEquals(ui.signed, ui.validFrom)
        assertEquals(ui.signed, ui.validUntil)
    }

    @Test
    fun `toUi defaults null booleans to false and keeps null instants null`() {
        val domain = DocumentValidityDomain(
            isDeviceSignatureValid = null,
            isIssuerSignatureValid = null,
            isDataIntegrityIntact = null,
            signed = null,
            validFrom = null,
            validUntil = null,
        )

        val ui = domain.toUi()

        assertEquals(false, ui.isDeviceSignatureValid)
        assertEquals(false, ui.isIssuerSignatureValid)
        assertEquals(false, ui.isDataIntegrityIntact)
        assertNull(ui.signed)
        assertNull(ui.validFrom)
        assertNull(ui.validUntil)
    }

    //endregion

    //region toListItems

    @Test
    fun `toListItems builds three boolean items plus the non-null string items`() {
        val ui = DocumentValidityUi(
            isDeviceSignatureValid = true,
            isIssuerSignatureValid = false,
            isDataIntegrityIntact = true,
            signed = "10 Dec 2025",
            validFrom = null,         // omitted
            validUntil = "31 Dec 2025",
        )

        val items = ui.toListItems(
            resourceProvider = resourceProvider(),
            uuidProvider = sequentialUuidProvider(),
        )

        // 3 boolean items + signed + validUntil (validFrom is null and dropped) = 5
        assertEquals(5, items.size)
    }

    @Test
    fun `toListItems drops all optional string items when they are null`() {
        val ui = DocumentValidityUi(
            isDeviceSignatureValid = false,
            isIssuerSignatureValid = false,
            isDataIntegrityIntact = false,
            signed = null,
            validFrom = null,
            validUntil = null,
        )

        val items = ui.toListItems(
            resourceProvider = resourceProvider(),
            uuidProvider = sequentialUuidProvider(),
        )

        assertEquals(3, items.size)
    }

    //endregion
}
