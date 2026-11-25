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

    /**
     * Formatted as "dd MMM yyyy" (in the system time zone).
     */
    @Test
    fun `Given a validity domain with values and instants When it is mapped to UI Then the booleans are kept and the instants are formatted`() {
        // Given
        val instant = Instant.parse("2025-12-10T12:00:00Z")
        val domain = DocumentValidityDomain(
            isDeviceSignatureValid = true,
            isIssuerSignatureValid = false,
            isDataIntegrityIntact = true,
            signed = instant,
            validFrom = instant,
            validUntil = instant,
        )

        // When
        val ui = domain.toUi()

        // Then
        assertEquals(true, ui.isDeviceSignatureValid)
        assertEquals(false, ui.isIssuerSignatureValid)
        assertEquals(true, ui.isDataIntegrityIntact)
        val dateRegex = Regex("""\d{2} \w{3} \d{4}""")
        assertTrue(ui.signed?.matches(dateRegex) == true, "Unexpected date format: ${ui.signed}")
        assertEquals(ui.signed, ui.validFrom)
        assertEquals(ui.signed, ui.validUntil)
    }

    @Test
    fun `Given a validity domain with nulls When it is mapped to UI Then booleans default to false and instants stay null`() {
        // Given
        val domain = DocumentValidityDomain(
            isDeviceSignatureValid = null,
            isIssuerSignatureValid = null,
            isDataIntegrityIntact = null,
            signed = null,
            validFrom = null,
            validUntil = null,
        )

        // When
        val ui = domain.toUi()

        // Then
        assertEquals(false, ui.isDeviceSignatureValid)
        assertEquals(false, ui.isIssuerSignatureValid)
        assertEquals(false, ui.isDataIntegrityIntact)
        assertNull(ui.signed)
        assertNull(ui.validFrom)
        assertNull(ui.validUntil)
    }

    //endregion

    //region toListItems

    /**
     * validFrom is null and dropped, so the result holds the three boolean items plus signed
     * and validUntil, five in all.
     */
    @Test
    fun `Given a validity UI with one null date When it is mapped to list items Then three boolean items and the non-null dates are returned`() {
        // Given
        val ui = DocumentValidityUi(
            isDeviceSignatureValid = true,
            isIssuerSignatureValid = false,
            isDataIntegrityIntact = true,
            signed = "10 Dec 2025",
            validFrom = null,
            validUntil = "31 Dec 2025",
        )

        // When
        val items = ui.toListItems(
            resourceProvider = resourceProvider(),
            uuidProvider = sequentialUuidProvider(),
        )

        // Then
        assertEquals(5, items.size)
    }

    @Test
    fun `Given a validity UI with all dates null When it is mapped to list items Then only the three boolean items are returned`() {
        // Given
        val ui = DocumentValidityUi(
            isDeviceSignatureValid = false,
            isIssuerSignatureValid = false,
            isDataIntegrityIntact = false,
            signed = null,
            validFrom = null,
            validUntil = null,
        )

        // When
        val items = ui.toListItems(
            resourceProvider = resourceProvider(),
            uuidProvider = sequentialUuidProvider(),
        )

        // Then
        assertEquals(3, items.size)
    }

    //endregion
}
