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
    fun `Init with docs sets title and requested docs`() = runTest(testDispatcher) {
        val viewModel = QrScanViewModel(qrScanInteractor())

        viewModel.setEvent(Event.Init(docs = docs))

        val state = viewModel.uiState.value
        assertEquals("Scan QR", state.screenTitle)
        assertEquals(docs, state.requestedDocs)
        assertFalse(state.isLoading)
        assertFalse(state.finishedScanning)
        assertNull(state.error)
    }

    @Test
    fun `Init with no docs finishes scanning and shows the missing-documents error`() =
        runTest(testDispatcher) {
            val viewModel = QrScanViewModel(qrScanInteractor())

            viewModel.setEvent(Event.Init(docs = null))

            val state = viewModel.uiState.value
            assertTrue(state.finishedScanning)
            assertEquals("Missing documents", state.error?.errorSubTitle)
        }

    //endregion

    //region scanning

    @Test
    fun `OnQrScanned with a valid code navigates to the transfer status screen`() =
        runTest(testDispatcher) {
            val viewModel = QrScanViewModel(qrScanInteractor(qrValid = true))
            viewModel.setEvent(Event.Init(docs = docs))

            viewModel.effect.test {
                viewModel.setEvent(Event.OnQrScanned("mdoc:abc"))
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
    fun `OnQrScanned with an invalid code shows the invalid-code error`() =
        runTest(testDispatcher) {
            val viewModel = QrScanViewModel(qrScanInteractor(qrValid = false))
            viewModel.setEvent(Event.Init(docs = docs))

            viewModel.setEvent(Event.OnQrScanned("bad"))

            val state = viewModel.uiState.value
            assertEquals("Invalid QR", state.error?.errorSubTitle)
            assertTrue(state.finishedScanning)
        }

    @Test
    fun `OnQrScanned is ignored once scanning has finished`() = runTest(testDispatcher) {
        val viewModel = QrScanViewModel(qrScanInteractor())
        // Init with no docs sets finishedScanning = true and the missing-documents error.
        viewModel.setEvent(Event.Init(docs = null))

        viewModel.setEvent(Event.OnQrScanned("mdoc:abc"))

        // Unchanged: still the missing-documents error, no navigation.
        assertEquals("Missing documents", viewModel.uiState.value.error?.errorSubTitle)
    }

    //endregion

    //region errors

    @Test
    fun `DismissError clears the error`() = runTest(testDispatcher) {
        val viewModel = QrScanViewModel(qrScanInteractor())
        viewModel.setEvent(Event.Init(docs = null)) // sets an error

        viewModel.setEvent(Event.DismissError)

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `OnQrScanFailed sets an error whose cancel action pops and clears it`() =
        runTest(testDispatcher) {
            val viewModel = QrScanViewModel(qrScanInteractor())

            viewModel.effect.test {
                viewModel.setEvent(Event.OnQrScanFailed("boom"))

                val error = viewModel.uiState.value.error
                assertEquals("boom", error?.errorSubTitle)
                assertTrue(viewModel.uiState.value.finishedScanning)

                error?.onCancel?.invoke()

                assertNull(viewModel.uiState.value.error)
                assertEquals(Effect.Navigation.Pop, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `OnQrScanFailed is ignored when an error is already shown`() = runTest(testDispatcher) {
        val viewModel = QrScanViewModel(qrScanInteractor())

        viewModel.setEvent(Event.OnQrScanFailed("first"))
        viewModel.setEvent(Event.OnQrScanFailed("second"))

        assertEquals("first", viewModel.uiState.value.error?.errorSubTitle)
    }

    @Test
    fun `OnBackClicked emits Pop`() = runTest(testDispatcher) {
        val viewModel = QrScanViewModel(qrScanInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnBackClicked)
            assertEquals(Effect.Navigation.Pop, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    //endregion
}
