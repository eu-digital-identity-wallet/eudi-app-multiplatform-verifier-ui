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

package eu.europa.ec.euidi.verifier.presentation.ui.zk_request

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimKind
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.domain.config.model.ZkPredicateValue
import eu.europa.ec.euidi.verifier.domain.interactor.ZkRequestInteractor
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.ui.zk_request.ZkRequestContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.zk_request.ZkRequestContract.Event
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZkRequestViewModelTest : MviViewModelTest() {

    private val ageClaim = ClaimItem(label = "birth_date", kind = ClaimKind.Zk())
    private val nationalityClaim = ClaimItem(label = "nationality", kind = ClaimKind.Zk())
    private val zkClaims = listOf(ageClaim, nationalityClaim)

    private val doc = RequestedDocumentUi(
        id = "PID_DOC",
        documentType = AttestationType.Pid,
        mode = DocumentMode.ZK,
        claims = emptyList()
    )

    private fun checkboxItem(id: String, checked: Boolean) = ListItemDataUi(
        itemId = id,
        mainContentData = ListItemMainContentDataUi.Text(id),
        trailingContentData = ListItemTrailingContentDataUi.Checkbox(
            checkboxData = CheckboxDataUi(isChecked = checked)
        )
    )

    private fun isChecked(item: ListItemDataUi): Boolean {
        val trailing = item.trailingContentData
        return trailing is ListItemTrailingContentDataUi.Checkbox && trailing.checkboxData.isChecked
    }

    private fun interactor(
        uiItems: List<ListItemDataUi> = listOf(
            checkboxItem(ageClaim.id, false),
            checkboxItem(nationalityClaim.id, false),
        ),
    ): ZkRequestInteractor = mock {
        every { getZkClaims(AttestationType.Pid) } returns zkClaims
        everySuspend { transformToUiItems(AttestationType.Pid, zkClaims) } returns uiItems
        everySuspend { getScreenTitle(AttestationType.Pid) } returns "Zero-knowledge request"
        every { selectedCountriesSubtitle(any()) } returns "2 countries selected"
        every { ageOverSubtitle(any()) } returns "Verify age over 18"
    }

    @Test
    fun `Init loads the zk predicate rows and title`() = runTest(testDispatcher) {
        val viewModel = ZkRequestViewModel(interactor())

        viewModel.setEvent(Event.Init(doc = doc))

        val state = viewModel.uiState.value
        assertEquals("Zero-knowledge request", state.screenTitle)
        assertEquals(2, state.items.size)
        // Nothing pre-checked → Done disabled.
        assertFalse(state.primaryButtonEnabled)
    }

    @Test
    fun `Tapping the nationality row when unconfigured opens the country picker`() =
        runTest(testDispatcher) {
            val viewModel = ZkRequestViewModel(interactor())
            viewModel.setEvent(Event.Init(doc = doc))

            viewModel.effect.test {
                viewModel.setEvent(Event.OnItemClicked(nationalityClaim.id))
                assertEquals(
                    Effect.Navigation.OpenCountrySelection(preSelectedCodes = emptyList()),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Selecting countries checks the nationality row, adds the subtitle and enables Done`() =
        runTest(testDispatcher) {
            val viewModel = ZkRequestViewModel(interactor())
            viewModel.setEvent(Event.Init(doc = doc))

            viewModel.setEvent(Event.OnCountriesSelected(listOf("GR", "FR")))

            val state = viewModel.uiState.value
            val natRow = state.items.first { it.itemId == nationalityClaim.id }
            assertTrue(isChecked(natRow))
            assertEquals("2 countries selected", natRow.supportingText)
            assertEquals(listOf("GR", "FR"), state.acceptedCountries)
            assertEquals(
                ClaimKind.Zk(ZkPredicateValue.NationalityIn(listOf("GR", "FR"))),
                state.claims.first { it.label == "nationality" }.kind
            )
            assertTrue(state.primaryButtonEnabled)
        }

    @Test
    fun `Tapping the nationality row when configured clears it without opening the picker`() =
        runTest(testDispatcher) {
            val viewModel = ZkRequestViewModel(interactor())
            viewModel.setEvent(Event.Init(doc = doc))
            viewModel.setEvent(Event.OnCountriesSelected(listOf("GR", "FR")))

            viewModel.setEvent(Event.OnItemClicked(nationalityClaim.id))

            val state = viewModel.uiState.value
            val natRow = state.items.first { it.itemId == nationalityClaim.id }
            assertFalse(isChecked(natRow))
            assertEquals(null, natRow.supportingText)
            assertTrue(state.acceptedCountries.isEmpty())
            assertEquals(
                ClaimKind.Zk(null),
                state.claims.first { it.label == "nationality" }.kind
            )
            assertFalse(state.primaryButtonEnabled)
        }

    @Test
    fun `OnDoneClick goes back with a ZK-mode document carrying the selected predicates`() =
        runTest(testDispatcher) {
            val boundNationalityClaim = nationalityClaim.copy(
                kind = ClaimKind.Zk(ZkPredicateValue.NationalityIn(listOf("GR", "FR")))
            )
            val interactor = interactor().apply {
                everySuspend {
                    transformToClaimItems(any(), any())
                } returns listOf(boundNationalityClaim)
            }
            val viewModel = ZkRequestViewModel(interactor)
            viewModel.setEvent(Event.Init(doc = doc))
            viewModel.setEvent(Event.OnCountriesSelected(listOf("GR", "FR")))

            viewModel.effect.test {
                viewModel.setEvent(Event.OnDoneClick)
                val effect = awaitItem() as Effect.Navigation.GoBack
                val returned = effect.requestedDocuments.items.single()
                assertEquals(DocumentMode.ZK, returned.mode)
                assertEquals(listOf(boundNationalityClaim), returned.claims)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `OnCancelClick goes back with no documents`() = runTest(testDispatcher) {
        val viewModel = ZkRequestViewModel(interactor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnCancelClick)
            val effect = awaitItem() as Effect.Navigation.GoBack
            assertTrue(effect.requestedDocuments.items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Tapping the date of birth row when unconfigured opens the age dialog`() =
        runTest(testDispatcher) {
            val viewModel = ZkRequestViewModel(interactor())
            viewModel.setEvent(Event.Init(doc = doc))

            viewModel.setEvent(Event.OnItemClicked(ageClaim.id))

            assertTrue(viewModel.uiState.value.ageDialogVisible)
            // The row is not toggled by the tap itself.
            assertFalse(isChecked(viewModel.uiState.value.items.first { it.itemId == ageClaim.id }))
        }

    @Test
    fun `Confirming an age threshold checks the row, adds the subtitle and enables Done`() =
        runTest(testDispatcher) {
            val viewModel = ZkRequestViewModel(interactor())
            viewModel.setEvent(Event.Init(doc = doc))

            viewModel.setEvent(Event.OnAgeThresholdConfirmed(18))

            val state = viewModel.uiState.value
            val ageRow = state.items.first { it.itemId == ageClaim.id }
            assertTrue(isChecked(ageRow))
            assertEquals("Verify age over 18", ageRow.supportingText)
            assertEquals(18, state.ageThreshold)
            assertEquals(
                ClaimKind.Zk(ZkPredicateValue.AgeOver(18)),
                state.claims.first { it.label == "birth_date" }.kind
            )
            assertFalse(state.ageDialogVisible)
            assertTrue(state.primaryButtonEnabled)
        }

    @Test
    fun `Tapping the date of birth row when configured clears it without opening the dialog`() =
        runTest(testDispatcher) {
            val viewModel = ZkRequestViewModel(interactor())
            viewModel.setEvent(Event.Init(doc = doc))
            viewModel.setEvent(Event.OnAgeThresholdConfirmed(18))

            viewModel.setEvent(Event.OnItemClicked(ageClaim.id))

            val state = viewModel.uiState.value
            val ageRow = state.items.first { it.itemId == ageClaim.id }
            assertFalse(isChecked(ageRow))
            assertEquals(null, ageRow.supportingText)
            assertEquals(null, state.ageThreshold)
            assertFalse(state.ageDialogVisible)
            assertFalse(state.primaryButtonEnabled)
        }

    @Test
    fun `Dismissing the age dialog hides it without configuring the row`() =
        runTest(testDispatcher) {
            val viewModel = ZkRequestViewModel(interactor())
            viewModel.setEvent(Event.Init(doc = doc))
            viewModel.setEvent(Event.OnItemClicked(ageClaim.id))

            viewModel.setEvent(Event.OnAgeDialogDismissed)

            val state = viewModel.uiState.value
            assertFalse(state.ageDialogVisible)
            assertEquals(null, state.ageThreshold)
            assertFalse(isChecked(state.items.first { it.itemId == ageClaim.id }))
        }
}
