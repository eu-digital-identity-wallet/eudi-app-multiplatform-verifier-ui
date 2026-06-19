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

package eu.europa.ec.euidi.verifier.presentation.ui.country_selection

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.interactor.CountrySelectionInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.HandleItemSelectionPartialState
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eu.europa.ec.euidi.verifier.presentation.model.CountrySelectionHolder
import eu.europa.ec.euidi.verifier.presentation.model.CountrySetUi
import eu.europa.ec.euidi.verifier.presentation.ui.country_selection.CountrySelectionContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.country_selection.CountrySelectionContract.Event
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CountrySelectionViewModelTest : MviViewModelTest() {

    private val sets = listOf(
        CountrySetUi(id = "schengen", label = "Schengen"),
        CountrySetUi(id = "eu", label = "EU"),
    )

    private fun checkboxItem(id: String, checked: Boolean) = ListItemDataUi(
        itemId = id,
        mainContentData = ListItemMainContentDataUi.Text(id),
        trailingContentData = ListItemTrailingContentDataUi.Checkbox(
            checkboxData = CheckboxDataUi(isChecked = checked)
        )
    )

    private fun interactor(
        items: List<ListItemDataUi>,
        selected: List<String>,
        matchingSetId: String? = null,
    ): CountrySelectionInteractor = mock {
        everySuspend { getCountryListItems(any()) } returns items
        every { selectedCountryCodes(any()) } returns selected
        every { getCountrySets() } returns sets
        every { matchingCountrySetId(any()) } returns matchingSetId
    }

    private fun resourceProvider(): ResourceProvider = mock {
        every { getSharedString(any()) } returns "Select countries"
    }

    @Test
    fun `Init exposes the chips, pre-checks the supplied countries and enables Done`() =
        runTest(testDispatcher) {
            val items = listOf(
                checkboxItem("GR", true),
                checkboxItem("FR", true),
                checkboxItem("IT", false),
            )
            val viewModel = CountrySelectionViewModel(
                interactor = interactor(items, selected = listOf("GR", "FR")),
                resourceProvider = resourceProvider(),
            )

            viewModel.setEvent(Event.Init(preSelectedCodes = listOf("GR", "FR")))

            val state = viewModel.uiState.value
            assertEquals("Select countries", state.screenTitle)
            assertEquals(items, state.items)
            assertEquals(sets, state.countrySets)
            assertNull(state.selectedSetId)
            assertTrue(state.primaryButtonEnabled)
        }

    @Test
    fun `Init marks the chip active when the pre-selection matches a set`() =
        runTest(testDispatcher) {
            val items = listOf(checkboxItem("GR", true), checkboxItem("FR", true))
            val viewModel = CountrySelectionViewModel(
                interactor = interactor(items, selected = listOf("GR", "FR"), matchingSetId = "eu"),
                resourceProvider = resourceProvider(),
            )

            viewModel.setEvent(Event.Init(preSelectedCodes = listOf("GR", "FR")))

            assertEquals("eu", viewModel.uiState.value.selectedSetId)
        }

    @Test
    fun `Init keeps Done disabled when fewer than two countries are selected`() =
        runTest(testDispatcher) {
            val items = listOf(checkboxItem("GR", true), checkboxItem("FR", false))
            val viewModel = CountrySelectionViewModel(
                interactor = interactor(items, selected = listOf("GR")),
                resourceProvider = resourceProvider(),
            )

            viewModel.setEvent(Event.Init(preSelectedCodes = listOf("GR")))

            assertFalse(viewModel.uiState.value.primaryButtonEnabled)
        }

    @Test
    fun `OnCountrySetClicked applies the set, marks the chip active and enables Done`() =
        runTest(testDispatcher) {
            val initialItems = listOf(checkboxItem("GR", false), checkboxItem("FR", false))
            val schengenItems = listOf(checkboxItem("GR", true), checkboxItem("FR", true))
            val interactor = mock<CountrySelectionInteractor> {
                everySuspend { getCountryListItems(any()) } returns initialItems
                every { getCountrySets() } returns sets
                every { selectedCountryCodes(initialItems) } returns emptyList()
                every { matchingCountrySetId(emptyList()) } returns null
                every { applyCountrySet(initialItems, "schengen") } returns schengenItems
                every { selectedCountryCodes(schengenItems) } returns listOf("GR", "FR")
                every { matchingCountrySetId(listOf("GR", "FR")) } returns "schengen"
            }
            val viewModel = CountrySelectionViewModel(interactor, resourceProvider())
            viewModel.setEvent(Event.Init(preSelectedCodes = emptyList()))

            viewModel.setEvent(Event.OnCountrySetClicked("schengen"))

            val state = viewModel.uiState.value
            assertEquals(schengenItems, state.items)
            assertEquals("schengen", state.selectedSetId)
            assertTrue(state.primaryButtonEnabled)
        }

    @Test
    fun `OnItemClicked re-derives the active chip and the two-selection rule`() =
        runTest(testDispatcher) {
            val initialItems = listOf(
                checkboxItem("GR", true),
                checkboxItem("FR", true),
                checkboxItem("IT", false),
            )
            val toggledItems = listOf(
                checkboxItem("GR", true),
                checkboxItem("FR", true),
                checkboxItem("IT", true),
            )
            val interactor = mock<CountrySelectionInteractor> {
                everySuspend { getCountryListItems(any()) } returns initialItems
                every { getCountrySets() } returns sets
                every { selectedCountryCodes(initialItems) } returns listOf("GR", "FR")
                every { matchingCountrySetId(listOf("GR", "FR")) } returns "schengen"
                every { handleItemSelection(initialItems, "IT") } returns
                        HandleItemSelectionPartialState.Updated(toggledItems, hasSelectedItems = true)
                every { selectedCountryCodes(toggledItems) } returns listOf("GR", "FR", "IT")
                every { matchingCountrySetId(listOf("GR", "FR", "IT")) } returns null
            }
            val viewModel = CountrySelectionViewModel(interactor, resourceProvider())
            viewModel.setEvent(Event.Init(preSelectedCodes = listOf("GR", "FR")))

            viewModel.setEvent(Event.OnItemClicked("IT"))

            val state = viewModel.uiState.value
            assertEquals(toggledItems, state.items)
            // Selection no longer matches a predefined set → chip cleared.
            assertNull(state.selectedSetId)
            assertTrue(state.primaryButtonEnabled)
        }

    @Test
    fun `OnDoneClick goes back with the selected country codes`() = runTest(testDispatcher) {
        val items = listOf(checkboxItem("GR", true), checkboxItem("FR", true))
        val viewModel = CountrySelectionViewModel(
            interactor = interactor(items, selected = listOf("GR", "FR")),
            resourceProvider = resourceProvider(),
        )
        viewModel.setEvent(Event.Init(preSelectedCodes = listOf("GR", "FR")))

        viewModel.effect.test {
            viewModel.setEvent(Event.OnDoneClick)
            assertEquals(
                Effect.Navigation.GoBackWithResult(CountrySelectionHolder(listOf("GR", "FR"))),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnCancelClick goes back without a result`() = runTest(testDispatcher) {
        val viewModel = CountrySelectionViewModel(
            interactor = interactor(emptyList(), selected = emptyList()),
            resourceProvider = resourceProvider(),
        )

        viewModel.effect.test {
            viewModel.setEvent(Event.OnCancelClick)
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
