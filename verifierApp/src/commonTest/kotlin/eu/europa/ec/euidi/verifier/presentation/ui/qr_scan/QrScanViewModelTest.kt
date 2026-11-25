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

package eu.europa.ec.euidi.verifier.presentation.ui.qr_scan

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.domain.interactor.QrScanInteractor
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.ui.qr_scan.QrScanViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.qr_scan.QrScanViewModelContract.Event
import eu.europa.ec.euidi.verifier.testutil.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QrScanViewModelTest : MviViewModelTest() {

    private val docs = listOf(TestData.pidFullRequestedDocument)

    private fun qrScanInteractor(qrValid: Boolean = true): QrScanInteractor = mock {
        everySuspend { getScreenTitle() } returns "Scan QR"
        everySuspend { getInvalidQrCodeMessage() } returns "Invalid QR"
        everySuspend { getMissingDocumentsMessage() } returns "Missing documents"
        every { qrCodeIsValid(any()) } returns qrValid
    }

    //region Init

    @Test
    fun `Given a view model When Init is received with documents Then the title and the requested documents are set`() = runTest(testDispatcher) {
        // Given
        val viewModel = QrScanViewModel(qrScanInteractor())

        // When
        viewModel.setEvent(Event.Init(docs = docs))

        // Then
        val state = viewModel.uiState.value
        assertEquals("Scan QR", state.screenTitle)
        assertEquals(docs, state.requestedDocs)
        assertFalse(state.isLoading)
        assertFalse(state.finishedScanning)
        assertNull(state.error)
    }

    @Test
    fun `Given a view model When Init is received without documents Then scanning finishes with the missing-documents error`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = QrScanViewModel(qrScanInteractor())

            // When
            viewModel.setEvent(Event.Init(docs = null))

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.finishedScanning)
            assertEquals("Missing documents", state.error?.errorSubTitle)
        }

    //endregion

    //region scanning

    @Test
    fun `Given a started scan When a valid code is scanned Then it navigates to the transfer status screen and scanning finishes`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = QrScanViewModel(qrScanInteractor(qrValid = true))
            viewModel.setEvent(Event.Init(docs = docs))

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.OnQrScanned("mdoc:abc"))

                // Then
                assertEquals(
                    Effect.Navigation.NavigateToTransferStatusScreen(
                        requestedDocs = RequestedDocsHolder(items = docs),
                        qrCode = "mdoc:abc"
                    ),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
            assertTrue(viewModel.uiState.value.finishedScanning)
        }

    @Test
    fun `Given a started scan When an invalid code is scanned Then the invalid-code error is shown and scanning finishes`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = QrScanViewModel(qrScanInteractor(qrValid = false))
            viewModel.setEvent(Event.Init(docs = docs))

            // When
            viewModel.setEvent(Event.OnQrScanned("bad"))

            // Then
            val state = viewModel.uiState.value
            assertEquals("Invalid QR", state.error?.errorSubTitle)
            assertTrue(state.finishedScanning)
        }

    /**
     * Init without documents finishes the scan with the missing-documents error; a later scan
     * leaves that error in place.
     */
    @Test
    fun `Given a finished scan When another code is scanned Then it is ignored and the error is unchanged`() = runTest(testDispatcher) {
        // Given
        val viewModel = QrScanViewModel(qrScanInteractor())
        viewModel.setEvent(Event.Init(docs = null))

        // When
        viewModel.setEvent(Event.OnQrScanned("mdoc:abc"))

        // Then
        assertEquals("Missing documents", viewModel.uiState.value.error?.errorSubTitle)
    }

    //endregion

    //region errors

    /**
     * The Init event with no docs sets an error.
     */
    @Test
    fun `Given a view model showing an error When DismissError is received Then the error is cleared`() = runTest(testDispatcher) {
        // Given
        val viewModel = QrScanViewModel(qrScanInteractor())
        viewModel.setEvent(Event.Init(docs = null))

        // When
        viewModel.setEvent(Event.DismissError)

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `Given a view model When a scan fails and the error is cancelled Then the error is set and then cleared and Pop is emitted`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = QrScanViewModel(qrScanInteractor())

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.OnQrScanFailed("boom"))

                // Then
                val error = viewModel.uiState.value.error
                assertEquals("boom", error?.errorSubTitle)
                assertTrue(viewModel.uiState.value.finishedScanning)

                // When
                error?.onCancel?.invoke()

                // Then
                assertNull(viewModel.uiState.value.error)
                assertEquals(Effect.Navigation.Pop, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given a view model When two scan failures are received Then only the first error is kept`() = runTest(testDispatcher) {
        // Given
        val viewModel = QrScanViewModel(qrScanInteractor())

        // When
        viewModel.setEvent(Event.OnQrScanFailed("first"))
        viewModel.setEvent(Event.OnQrScanFailed("second"))

        // Then
        assertEquals("first", viewModel.uiState.value.error?.errorSubTitle)
    }

    @Test
    fun `Given a view model When OnBackClicked is received Then Pop is emitted`() = runTest(testDispatcher) {
        // Given
        val viewModel = QrScanViewModel(qrScanInteractor())

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnBackClicked)

            // Then
            assertEquals(Effect.Navigation.Pop, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    //endregion
}
