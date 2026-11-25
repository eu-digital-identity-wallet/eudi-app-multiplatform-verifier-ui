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
    fun `Given requested documents When Init is received Then the supported and requested documents are loaded and the button is enabled`() =
        runTest(testDispatcher) {
            // Given
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                everySuspend { checkDocumentMode(listOf(pidDoc)) } returns listOf(pidDoc)
            }
            val viewModel = DocumentsToRequestViewModel(interactor)

            // When
            viewModel.setEvent(Event.Init(requestedDocs = RequestedDocsHolder(items = listOf(pidDoc))))

            // Then
            val state = viewModel.uiState.value
            assertEquals(supportedDocs, state.allSupportedDocuments)
            assertEquals(supportedDocs, state.filteredDocuments)
            assertEquals(listOf(pidDoc), state.requestedDocuments)
            assertTrue(state.isButtonEnabled)
        }

    @Test
    fun `Given a view model When Init is received without requested documents Then the selection is empty and the button is disabled`() = runTest(testDispatcher) {
        // Given
        val viewModel = DocumentsToRequestViewModel(interactor())

        // When
        viewModel.setEvent(Event.Init(requestedDocs = null))

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.requestedDocuments.isEmpty())
        assertFalse(state.isButtonEnabled)
    }

    @Test
    fun `Given a view model When Init is received twice Then the supported documents are loaded once`() =
        runTest(testDispatcher) {
            // Given
            val interactor = interactor()
            val viewModel = DocumentsToRequestViewModel(interactor)

            // When
            viewModel.setEvent(Event.Init(requestedDocs = null))
            viewModel.setEvent(Event.Init(requestedDocs = null))

            // Then
            verifySuspend(exactly(1)) { interactor.getSupportedDocuments() }
        }

    @Test
    fun `Given an interactor that returns an updated selection When a document option is selected Then the documents are stored and the button is enabled`() =
        runTest(testDispatcher) {
            // Given
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                everySuspend {
                    handleDocumentOptionSelection(any(), any(), any(), any())
                } returns DocSelectionResult.Updated(listOf(pidDoc))
            }
            val viewModel = DocumentsToRequestViewModel(interactor)

            // When
            viewModel.setEvent(
                Event.OnDocOptionSelected("PID_DOC", AttestationType.Pid, DocumentMode.FULL)
            )

            // Then
            val state = viewModel.uiState.value
            assertEquals(listOf(pidDoc), state.requestedDocuments)
            assertTrue(state.isButtonEnabled)
        }

    @Test
    fun `Given an interactor that requests a custom request with no remaining documents When a document option is selected Then the effect is emitted and the requested documents are empty`() =
        runTest(testDispatcher) {
            // Given
            val customDoc = TestData.pidCustomRequestedDocument
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                everySuspend {
                    handleDocumentOptionSelection(any(), any(), any(), any())
                } returns DocSelectionResult.NavigateToCustomRequest(emptyList(), customDoc)
            }
            val viewModel = DocumentsToRequestViewModel(interactor)

            viewModel.effect.test {
                // When
                viewModel.setEvent(
                    Event.OnDocOptionSelected("PID_DOC", AttestationType.Pid, DocumentMode.CUSTOM)
                )

                // Then
                assertEquals(
                    Effect.Navigation.NavigateToCustomRequestScreen(customDoc),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(emptyList(), viewModel.uiState.value.requestedDocuments)
        }

    @Test
    fun `Given a view model When OnBackClick is received Then it navigates to the home screen with no documents`() = runTest(testDispatcher) {
        // Given
        val viewModel = DocumentsToRequestViewModel(interactor())

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnBackClick)

            // Then
            assertEquals(Effect.Navigation.NavigateToHomeScreen(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given initialised requested documents When OnDoneClick is received Then it navigates to the home screen with the requested documents`() =
        runTest(testDispatcher) {
            // Given
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                everySuspend { checkDocumentMode(listOf(pidDoc)) } returns listOf(pidDoc)
            }
            val viewModel = DocumentsToRequestViewModel(interactor)
            viewModel.setEvent(Event.Init(requestedDocs = RequestedDocsHolder(items = listOf(pidDoc))))

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.OnDoneClick)

                // Then
                assertEquals(
                    Effect.Navigation.NavigateToHomeScreen(requestedDocuments = listOf(pidDoc)),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    /**
     * Init populates allSupportedDocuments.
     */
    @Test
    fun `Given loaded supported documents When the search query changes Then the search term and the filtered documents are updated`() =
        runTest(testDispatcher) {
            // Given
            val interactor = mock<DocumentsToRequestInteractor> {
                everySuspend { getSupportedDocuments() } returns supportedDocs
                every { searchDocuments("PID", supportedDocs) } returns flowOf(supportedDocs)
            }
            val viewModel = DocumentsToRequestViewModel(interactor)
            viewModel.setEvent(Event.Init(requestedDocs = null))

            // When
            viewModel.setEvent(Event.OnSearchQueryChanged("PID"))

            // Then
            val state = viewModel.uiState.value
            assertEquals("PID", state.searchTerm)
            assertEquals(supportedDocs, state.filteredDocuments)
        }
}
