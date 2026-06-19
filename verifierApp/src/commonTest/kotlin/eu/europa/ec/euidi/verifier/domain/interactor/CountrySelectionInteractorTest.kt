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
import dev.mokkery.matcher.any
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.repository.CountryRepository
import eu.europa.ec.euidi.verifier.domain.repository.CountrySet
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CountrySelectionInteractorTest {

    private val testSets = listOf(
        CountrySet(id = "schengen", countryCodes = listOf("GR", "FR")),
        CountrySet(id = "eu", countryCodes = listOf("GR", "FR", "IT")),
    )

    private fun TestScope.createInteractor(
        codes: List<String> = listOf("GR", "FR", "IT"),
    ): CountrySelectionInteractor {
        val testDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher

        val repository = mock<CountryRepository> {
            every { getAllCountryCodes() } returns codes
            every { getCountrySets() } returns testSets
        }
        val resourceProvider = mock<ResourceProvider> {
            every { getSharedString(any()) } returns "Country"
        }

        return CountrySelectionInteractorImpl(
            countryRepository = repository,
            resourceProvider = resourceProvider,
            dispatcher = testDispatcher
        )
    }

    @Test
    fun `getCountryListItems builds one checkbox row per code and pre-checks the selected ones`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor(codes = listOf("GR", "FR", "IT"))

            val items = interactor.getCountryListItems(preSelectedCodes = listOf("GR", "IT"))

            assertEquals(listOf("GR", "FR", "IT"), items.map { it.itemId })

            // Row label ends with the ISO-2 code in parentheses, e.g. "Country (GR)".
            items.forEach { item ->
                val text = (item.mainContentData as ListItemMainContentDataUi.Text).text
                assertTrue(text.endsWith("(${item.itemId})"), "Unexpected label: $text")
            }

            assertTrue(items.first { it.itemId == "GR" }.isChecked())
            assertTrue(items.first { it.itemId == "IT" }.isChecked())
            assertFalse(items.first { it.itemId == "FR" }.isChecked())
        }

    @Test
    fun `selectedCountryCodes returns only the checked rows`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        val items = interactor.getCountryListItems(preSelectedCodes = listOf("GR", "FR"))

        assertEquals(listOf("GR", "FR"), interactor.selectedCountryCodes(items))
    }

    @Test
    fun `handleItemSelection toggles the targeted row`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        val items = interactor.getCountryListItems(preSelectedCodes = emptyList())
        val result = interactor.handleItemSelection(items, identifier = "FR")
                as HandleItemSelectionPartialState.Updated

        assertTrue(result.items.first { it.itemId == "FR" }.isChecked())
        assertTrue(result.hasSelectedItems)
    }

    @Test
    fun `getCountrySets exposes the repository sets`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertEquals(listOf("schengen", "eu"), interactor.getCountrySets().map { it.id })
    }

    @Test
    fun `applyCountrySet checks exactly the members of the set`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()
            val items = interactor.getCountryListItems(preSelectedCodes = emptyList())

            val applied = interactor.applyCountrySet(items, setId = "schengen")

            assertTrue(applied.first { it.itemId == "GR" }.isChecked())
            assertTrue(applied.first { it.itemId == "FR" }.isChecked())
            assertFalse(applied.first { it.itemId == "IT" }.isChecked())
        }

    @Test
    fun `matchingCountrySetId returns the set whose members match, else null`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            // Order-independent match.
            assertEquals("schengen", interactor.matchingCountrySetId(listOf("FR", "GR")))
            assertEquals("eu", interactor.matchingCountrySetId(listOf("GR", "FR", "IT")))
            assertEquals(null, interactor.matchingCountrySetId(listOf("GR")))
        }

    private fun ListItemDataUi.isChecked(): Boolean {
        val trailing = trailingContentData
        return trailing is ListItemTrailingContentDataUi.Checkbox && trailing.checkboxData.isChecked
    }
}
