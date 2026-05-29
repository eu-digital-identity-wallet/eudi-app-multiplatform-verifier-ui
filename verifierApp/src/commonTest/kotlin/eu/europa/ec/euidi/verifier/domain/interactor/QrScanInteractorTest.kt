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
    fun `getScreenTitle returns localized title`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertEquals("QR scan title", interactor.getScreenTitle())
    }

    //endregion

    //region qrCodeIsValid

    @Test
    fun `qrCodeIsValid returns true for mdoc prefix`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertTrue(interactor.qrCodeIsValid("mdoc:abc123"))
    }

    @Test
    fun `qrCodeIsValid is case insensitive for the prefix`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertTrue(interactor.qrCodeIsValid("MDOC:abc123"))
    }

    @Test
    fun `qrCodeIsValid returns false for a non-mdoc prefix`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertFalse(interactor.qrCodeIsValid("https://example.com"))
    }

    @Test
    fun `qrCodeIsValid returns false for an empty string`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertFalse(interactor.qrCodeIsValid(""))
    }

    //endregion

    //region messages

    @Test
    fun `getInvalidQrCodeMessage returns localized message`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertEquals("Invalid QR code", interactor.getInvalidQrCodeMessage())
    }

    @Test
    fun `getMissingDocumentsMessage returns localized message`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            assertEquals("Missing documents", interactor.getMissingDocumentsMessage())
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