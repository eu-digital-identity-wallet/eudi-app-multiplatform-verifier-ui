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
    fun `Init with a doc loads title, items and enables the primary button`() =
        runTest(testDispatcher) {
            val viewModel = CustomRequestViewModel(customRequestInteractor())

            viewModel.setEvent(Event.Init(doc = doc))

            val state = viewModel.uiState.value
            assertEquals("Custom PID", state.screenTitle)
            assertEquals(doc, state.requestedDoc)
            assertEquals(uiItems, state.items)
            assertTrue(state.primaryButtonEnabled)
        }

    @Test
    fun `Init with no doc keeps the initial state`() = runTest(testDispatcher) {
        val viewModel = CustomRequestViewModel(customRequestInteractor())

        viewModel.setEvent(Event.Init(doc = null))

        assertEquals(State(), viewModel.uiState.value)
    }

    @Test
    fun `OnCancelClick goes back with no documents`() = runTest(testDispatcher) {
        val viewModel = CustomRequestViewModel(customRequestInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnCancelClick)
            assertEquals(
                Effect.Navigation.GoBack(RequestedDocsHolder(items = emptyList())),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDoneClick goes back with the selected claims`() = runTest(testDispatcher) {
        val selectedClaims = listOf(TestData.familyNameClaim)
        val interactor = mock<CustomRequestInteractor> {
            every { getDocumentClaims(AttestationType.Pid) } returns claims
            everySuspend { transformToUiItems(AttestationType.Pid, claims) } returns uiItems
            everySuspend { getScreenTitle(AttestationType.Pid) } returns "Custom PID"
            everySuspend { transformToClaimItems(claims, uiItems) } returns selectedClaims
        }
        val viewModel = CustomRequestViewModel(interactor)
        viewModel.setEvent(Event.Init(doc = doc))

        viewModel.effect.test {
            viewModel.setEvent(Event.OnDoneClick)
            val effect = awaitItem() as Effect.Navigation.GoBack
            val returnedDoc = effect.requestedDocuments.items.single()
            assertEquals(doc.id, returnedDoc.id)
            assertEquals(selectedClaims, returnedDoc.claims)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnDoneClick without a requested doc does nothing`() = runTest(testDispatcher) {
        val viewModel = CustomRequestViewModel(customRequestInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnDoneClick)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnItemClicked applies the interactor selection result`() = runTest(testDispatcher) {
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

        viewModel.setEvent(Event.OnItemClicked("family_name"))

        val state = viewModel.uiState.value
        assertEquals(toggledItems, state.items)
        assertFalse(state.primaryButtonEnabled)
    }

    @Test
    fun `OnSelectAllClick unchecks every checkbox item and leaves others untouched`() =
        runTest(testDispatcher) {
            val viewModel = CustomRequestViewModel(customRequestInteractor())
            viewModel.setEvent(Event.Init(doc = doc))

            viewModel.setEvent(Event.OnSelectAllClick(isChecked = false))

            val state = viewModel.uiState.value
            val checkbox = state.items
                .first { it.itemId == "family_name" }
                .trailingContentData as ListItemTrailingContentDataUi.Checkbox
            assertFalse(checkbox.checkboxData.isChecked)
            // The non-checkbox item is preserved unchanged.
            assertEquals(nonCheckboxItem, state.items.first { it.itemId == "header" })
            assertFalse(state.primaryButtonEnabled)
        }

    @Test
    fun `areAllItemsChecked is true only when every checkbox item is checked`() {
        val allChecked = State(items = listOf(checkboxItem("a", checked = true), nonCheckboxItem))
        val someUnchecked = State(items = listOf(checkboxItem("a", checked = false)))

        assertTrue(allChecked.areAllItemsChecked)
        assertFalse(someUnchecked.areAllItemsChecked)
    }
}
