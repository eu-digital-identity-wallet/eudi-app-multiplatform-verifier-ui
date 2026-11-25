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

package eu.europa.ec.euidi.verifier.presentation.ui.show_document

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.domain.interactor.ShowDocumentsInteractor
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.ShowDocumentViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.ShowDocumentViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.model.DocumentUi
import eu.europa.ec.euidi.verifier.testutil.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ShowDocumentsViewModelTest : MviViewModelTest() {

    private val receivedDocs = listOf(TestData.pidReceivedDocument)

    private val documentUi = DocumentUi(
        id = "doc-1",
        docType = "pid",
        uiClaims = emptyList(),
        validityInfo = emptyList()
    )

    private fun showDocumentsInteractor(): ShowDocumentsInteractor = mock {
        everySuspend { getScreenTitle() } returns "Documents"
        everySuspend { transformToUiItems(receivedDocs) } returns listOf(documentUi)
    }

    @Test
    fun `Given a view model When Init is received Then the title and the transformed items are shown and loading is off`() = runTest(testDispatcher) {
        // Given
        val viewModel = ShowDocumentsViewModel(showDocumentsInteractor())

        // When
        viewModel.setEvent(Event.Init(items = receivedDocs))

        // Then
        val state = viewModel.uiState.value
        assertEquals("Documents", state.screenTitle)
        assertEquals(listOf(documentUi), state.items)
        assertFalse(state.isLoading)
    }

    @Test
    fun `Given a view model When OnDoneClick is received Then it pops to the home screen`() = runTest(testDispatcher) {
        // Given
        val viewModel = ShowDocumentsViewModel(showDocumentsInteractor())

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnDoneClick)

            // Then
            assertEquals(
                Effect.Navigation.PopTo(route = NavItem.Home, inclusive = false),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given a view model When OnBackClick is received Then it pops to the home screen`() = runTest(testDispatcher) {
        // Given
        val viewModel = ShowDocumentsViewModel(showDocumentsInteractor())

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnBackClick)

            // Then
            assertEquals(
                Effect.Navigation.PopTo(route = NavItem.Home, inclusive = false),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
