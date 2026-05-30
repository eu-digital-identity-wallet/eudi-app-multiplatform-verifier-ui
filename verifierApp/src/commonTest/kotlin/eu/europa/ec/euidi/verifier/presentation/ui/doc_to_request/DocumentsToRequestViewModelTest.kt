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

package eu.europa.ec.euidi.verifier.presentation.ui.doc_to_request

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.domain.interactor.DocSelectionResult
import eu.europa.ec.euidi.verifier.domain.interactor.DocumentsToRequestInteractor
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.ui.doc_to_request.DocToRequestContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.doc_to_request.DocToRequestContract.Event
import eu.europa.ec.euidi.verifier.testutil.TestData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DocumentsToRequestViewModelTest : MviViewModelTest() {

    private val supportedDocs = listOf(TestData.pidSupportedDocument)
    private val pidDoc = TestData.pidFullRequestedDocument

    private fun interactor(): DocumentsToRequestInteractor = mock {
        everySuspend { getSupportedDocuments() } returns supportedDocs
    }

    @Test
    fun `Init loads supported documents and applies the requested ones`() =
        runTest(testDispatcher) {
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                everySuspend { checkDocumentMode(listOf(pidDoc)) } returns listOf(pidDoc)
            }
            val viewModel = DocumentsToRequestViewModel(interactor)

            viewModel.setEvent(Event.Init(requestedDocs = RequestedDocsHolder(items = listOf(pidDoc))))

            val state = viewModel.uiState.value
            assertEquals(supportedDocs, state.allSupportedDocuments)
            assertEquals(supportedDocs, state.filteredDocuments)
            assertEquals(listOf(pidDoc), state.requestedDocuments)
            assertTrue(state.isButtonEnabled)
        }

    @Test
    fun `Init with no requested docs yields an empty selection`() = runTest(testDispatcher) {
        val viewModel = DocumentsToRequestViewModel(interactor())

        viewModel.setEvent(Event.Init(requestedDocs = null))

        val state = viewModel.uiState.value
        assertTrue(state.requestedDocuments.isEmpty())
        assertFalse(state.isButtonEnabled)
    }

    @Test
    fun `Init reuses already-loaded supported documents on a second call`() =
        runTest(testDispatcher) {
            val interactor = interactor()
            val viewModel = DocumentsToRequestViewModel(interactor)

            viewModel.setEvent(Event.Init(requestedDocs = null))
            viewModel.setEvent(Event.Init(requestedDocs = null))

            verifySuspend(exactly(1)) { interactor.getSupportedDocuments() }
        }

    @Test
    fun `OnDocOptionSelected Updated stores the docs and enables the button`() =
        runTest(testDispatcher) {
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                everySuspend {
                    handleDocumentOptionSelection(any(), any(), any(), any())
                } returns DocSelectionResult.Updated(listOf(pidDoc))
            }
            val viewModel = DocumentsToRequestViewModel(interactor)

            viewModel.setEvent(
                Event.OnDocOptionSelected("PID_DOC", AttestationType.Pid, DocumentMode.FULL)
            )

            val state = viewModel.uiState.value
            assertEquals(listOf(pidDoc), state.requestedDocuments)
            assertTrue(state.isButtonEnabled)
        }

    @Test
    fun `OnDocOptionSelected NavigateToCustomRequest stores docs and emits the effect`() =
        runTest(testDispatcher) {
            val customDoc = TestData.pidCustomRequestedDocument
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                everySuspend {
                    handleDocumentOptionSelection(any(), any(), any(), any())
                } returns DocSelectionResult.NavigateToCustomRequest(emptyList(), customDoc)
            }
            val viewModel = DocumentsToRequestViewModel(interactor)

            viewModel.effect.test {
                viewModel.setEvent(
                    Event.OnDocOptionSelected("PID_DOC", AttestationType.Pid, DocumentMode.CUSTOM)
                )
                assertEquals(
                    Effect.Navigation.NavigateToCustomRequestScreen(customDoc),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(emptyList(), viewModel.uiState.value.requestedDocuments)
        }

    @Test
    fun `OnBackClick navigates to the home screen with no docs`() = runTest(testDispatcher) {
        val viewModel = DocumentsToRequestViewModel(interactor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnBackClick)
            assertEquals(Effect.Navigation.NavigateToHomeScreen(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDoneClick navigates to the home screen with the requested docs`() =
        runTest(testDispatcher) {
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                everySuspend { checkDocumentMode(listOf(pidDoc)) } returns listOf(pidDoc)
            }
            val viewModel = DocumentsToRequestViewModel(interactor)
            viewModel.setEvent(Event.Init(requestedDocs = RequestedDocsHolder(items = listOf(pidDoc))))

            viewModel.effect.test {
                viewModel.setEvent(Event.OnDoneClick)
                assertEquals(
                    Effect.Navigation.NavigateToHomeScreen(requestedDocuments = listOf(pidDoc)),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `OnSearchQueryChanged updates the search term and filtered documents`() =
        runTest(testDispatcher) {
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                every { searchDocuments("PID", supportedDocs) } returns flowOf(supportedDocs)
            }
            val viewModel = DocumentsToRequestViewModel(interactor)
            viewModel.setEvent(Event.Init(requestedDocs = null)) // populates allSupportedDocuments

            viewModel.setEvent(Event.OnSearchQueryChanged("PID"))

            val state = viewModel.uiState.value
            assertEquals("PID", state.searchTerm)
            assertEquals(supportedDocs, state.filteredDocuments)
        }
}
