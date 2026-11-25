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

package eu.europa.ec.euidi.verifier.presentation.ui.custom_request

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.interactor.CustomRequestInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.HandleItemSelectionPartialState
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.ui.custom_request.CustomRequestContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.custom_request.CustomRequestContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.custom_request.CustomRequestContract.State
import eu.europa.ec.euidi.verifier.testutil.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomRequestViewModelTest : MviViewModelTest() {

    private val doc = TestData.pidCustomRequestedDocument
    private val claims = listOf(TestData.familyNameClaim)

    private fun checkboxItem(id: String, checked: Boolean) = ListItemDataUi(
        itemId = id,
        mainContentData = ListItemMainContentDataUi.Text(id),
        trailingContentData = ListItemTrailingContentDataUi.Checkbox(
            checkboxData = CheckboxDataUi(isChecked = checked)
        )
    )

    private val nonCheckboxItem = ListItemDataUi(
        itemId = "header",
        mainContentData = ListItemMainContentDataUi.Text("header")
    )

    // A checkbox item (checked) plus a non-checkbox item, exercising both branches of the
    // select-all mapping and the `areAllItemsChecked` computation.
    private val uiItems = listOf(checkboxItem("family_name", checked = true), nonCheckboxItem)

    private fun customRequestInteractor(): CustomRequestInteractor = mock {
        every { getDocumentClaims(AttestationType.Pid) } returns claims
        everySuspend { transformToUiItems(AttestationType.Pid, claims) } returns uiItems
        everySuspend { getScreenTitle(AttestationType.Pid) } returns "Custom PID"
    }

    @Test
    fun `Given a view model When Init is received with a document Then the title and the items are loaded and the primary button is enabled`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = CustomRequestViewModel(customRequestInteractor())

            // When
            viewModel.setEvent(Event.Init(doc = doc))

            // Then
            val state = viewModel.uiState.value
            assertEquals("Custom PID", state.screenTitle)
            assertEquals(doc, state.requestedDoc)
            assertEquals(uiItems, state.items)
            assertTrue(state.primaryButtonEnabled)
        }

    @Test
    fun `Given a view model When Init is received without a document Then the initial state is kept`() = runTest(testDispatcher) {
        // Given
        val viewModel = CustomRequestViewModel(customRequestInteractor())

        // When
        viewModel.setEvent(Event.Init(doc = null))

        // Then
        assertEquals(State(), viewModel.uiState.value)
    }

    @Test
    fun `Given a view model When OnCancelClick is received Then it goes back with no documents`() = runTest(testDispatcher) {
        // Given
        val viewModel = CustomRequestViewModel(customRequestInteractor())

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnCancelClick)

            // Then
            assertEquals(
                Effect.Navigation.GoBack(RequestedDocsHolder(items = emptyList())),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given an initialised view model When OnDoneClick is received Then it goes back with the selected claims`() = runTest(testDispatcher) {
        // Given
        val selectedClaims = listOf(TestData.familyNameClaim)
        val interactor = mock<CustomRequestInteractor> {
            every { getDocumentClaims(AttestationType.Pid) } returns claims
            everySuspend { transformToUiItems(AttestationType.Pid, claims) } returns uiItems
            everySuspend { getScreenTitle(AttestationType.Pid) } returns "Custom PID"
            everySuspend { transformToClaimItems(uiItems) } returns selectedClaims
        }
        val viewModel = CustomRequestViewModel(interactor)
        viewModel.setEvent(Event.Init(doc = doc))

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnDoneClick)

            // Then
            val effect = awaitItem() as Effect.Navigation.GoBack
            val returnedDoc = effect.requestedDocuments.items.single()
            assertEquals(doc.id, returnedDoc.id)
            assertEquals(selectedClaims, returnedDoc.claims)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given a view model without a document When OnDoneClick is received Then nothing is emitted`() = runTest(testDispatcher) {
        // Given
        val viewModel = CustomRequestViewModel(customRequestInteractor())

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnDoneClick)

            // Then
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given an interactor that returns a toggled selection When an item is clicked Then the items are updated and the primary button is disabled`() = runTest(testDispatcher) {
        // Given
        val toggledItems = listOf(checkboxItem("family_name", checked = false), nonCheckboxItem)
        val interactor = mock<CustomRequestInteractor> {
            every { getDocumentClaims(AttestationType.Pid) } returns claims
            everySuspend { transformToUiItems(AttestationType.Pid, claims) } returns uiItems
            everySuspend { getScreenTitle(AttestationType.Pid) } returns "Custom PID"
            every {
                handleItemSelection(uiItems, "family_name")
            } returns HandleItemSelectionPartialState.Updated(
                items = toggledItems,
                hasSelectedItems = false
            )
        }
        val viewModel = CustomRequestViewModel(interactor)
        viewModel.setEvent(Event.Init(doc = doc))

        // When
        viewModel.setEvent(Event.OnItemClicked("family_name"))

        // Then
        val state = viewModel.uiState.value
        assertEquals(toggledItems, state.items)
        assertFalse(state.primaryButtonEnabled)
    }

    /**
     * The non-checkbox item is preserved unchanged.
     */
    @Test
    fun `Given an initialised view model When select all is unchecked Then every checkbox item is unchecked and other items are untouched`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = CustomRequestViewModel(customRequestInteractor())
            viewModel.setEvent(Event.Init(doc = doc))

            // When
            viewModel.setEvent(Event.OnSelectAllClick(isChecked = false))

            // Then
            val state = viewModel.uiState.value
            val checkbox = state.items
                .first { it.itemId == "family_name" }
                .trailingContentData as ListItemTrailingContentDataUi.Checkbox
            assertFalse(checkbox.checkboxData.isChecked)
            assertEquals(nonCheckboxItem, state.items.first { it.itemId == "header" })
            assertFalse(state.primaryButtonEnabled)
        }

    @Test
    fun `Given states with all and with some checkboxes checked When areAllItemsChecked is read Then it is true only when every checkbox item is checked`() {
        // Given
        val allChecked = State(items = listOf(checkboxItem("a", checked = true), nonCheckboxItem))
        val someUnchecked = State(items = listOf(checkboxItem("a", checked = false)))

        // Then
        assertTrue(allChecked.areAllItemsChecked)
        assertFalse(someUnchecked.areAllItemsChecked)
    }
}
