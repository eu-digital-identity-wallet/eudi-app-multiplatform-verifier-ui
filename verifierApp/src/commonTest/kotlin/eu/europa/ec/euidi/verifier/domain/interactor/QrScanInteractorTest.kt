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

package eu.europa.ec.euidi.verifier.domain.interactor

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.qr_scan_screen_error_invalid_code
import eudiverifier.verifierapp.generated.resources.qr_scan_screen_error_missing_documents
import eudiverifier.verifierapp.generated.resources.qr_scan_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QrScanInteractorTest {

    /**
     * Builds the SUT, [QrScanInteractorImpl], with a dispatcher that shares the `runTest`
     * scheduler and a Mokkery mock for the [ResourceProvider].
     */
    private fun TestScope.createInteractor(
        resourceProvider: ResourceProvider = stringResourceProvider()
    ): QrScanInteractor {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        return QrScanInteractorImpl(
            resourceProvider = resourceProvider,
            dispatcher = dispatcher
        )
    }

    //region getScreenTitle

    @Test
    fun `Given an interactor When the screen title is requested Then the localized title is returned`() = runTest(StandardTestDispatcher()) {
        // Given
        val interactor = createInteractor()

        // When
        val result = interactor.getScreenTitle()

        // Then
        assertEquals("QR scan title", result)
    }

    //endregion

    //region qrCodeIsValid

    @Test
    fun `Given an interactor When a QR code with the mdoc prefix is validated Then it is valid`() = runTest(StandardTestDispatcher()) {
        // Given
        val interactor = createInteractor()

        // When
        val result = interactor.qrCodeIsValid("mdoc:abc123")

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given an interactor When a QR code with an upper case mdoc prefix is validated Then it is valid`() = runTest(StandardTestDispatcher()) {
        // Given
        val interactor = createInteractor()

        // When
        val result = interactor.qrCodeIsValid("MDOC:abc123")

        // Then
        assertTrue(result)
    }

    @Test
    fun `Given an interactor When a QR code with another prefix is validated Then it is invalid`() = runTest(StandardTestDispatcher()) {
        // Given
        val interactor = createInteractor()

        // When
        val result = interactor.qrCodeIsValid("https://example.com")

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given an interactor When an empty string is validated Then it is invalid`() = runTest(StandardTestDispatcher()) {
        // Given
        val interactor = createInteractor()

        // When
        val result = interactor.qrCodeIsValid("")

        // Then
        assertFalse(result)
    }

    //endregion

    //region messages

    @Test
    fun `Given an interactor When the invalid QR code message is requested Then the localized message is returned`() = runTest(StandardTestDispatcher()) {
        // Given
        val interactor = createInteractor()

        // When
        val result = interactor.getInvalidQrCodeMessage()

        // Then
        assertEquals("Invalid QR code", result)
    }

    @Test
    fun `Given an interactor When the missing documents message is requested Then the localized message is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            // When
            val result = interactor.getMissingDocumentsMessage()

            // Then
            assertEquals("Missing documents", result)
        }

    //endregion

    //region Mocks

    private fun stringResourceProvider(): ResourceProvider = mock {
        every { getSharedString(Res.string.qr_scan_screen_title) } returns "QR scan title"
        every { getSharedString(Res.string.qr_scan_screen_error_invalid_code) } returns "Invalid QR code"
        every {
            getSharedString(Res.string.qr_scan_screen_error_missing_documents)
        } returns "Missing documents"
    }

    //endregion
}