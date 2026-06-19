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

import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.repository.CountryRepository
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.extension.hasAnyCheckedCheckbox
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eu.europa.ec.euidi.verifier.presentation.model.CountrySetUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.allStringResources
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CountrySelectionInteractor {

    /**
     * Builds the country rows. Each row shows "Country name (ISO-2)" and a checkbox that starts
     * checked when its code is in [preSelectedCodes].
     */
    suspend fun getCountryListItems(preSelectedCodes: List<String>): List<ListItemDataUi>

    fun handleItemSelection(
        items: List<ListItemDataUi>,
        identifier: String,
    ): HandleItemSelectionPartialState

    /** The ISO-2 codes of the currently checked rows. */
    fun selectedCountryCodes(items: List<ListItemDataUi>): List<String>

    /** The selectable country-set chips, in display order. */
    fun getCountrySets(): List<CountrySetUi>

    /** Re-checks [items] so that exactly the countries of the set [setId] are selected. */
    fun applyCountrySet(items: List<ListItemDataUi>, setId: String): List<ListItemDataUi>

    /**
     * The id of the country set whose members exactly match [selectedCodes], or null when the
     * selection doesn't correspond to any predefined set.
     */
    fun matchingCountrySetId(selectedCodes: List<String>): String?
}

class CountrySelectionInteractorImpl(
    private val countryRepository: CountryRepository,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : CountrySelectionInteractor {

    override suspend fun getCountryListItems(
        preSelectedCodes: List<String>
    ): List<ListItemDataUi> = withContext(dispatcher) {
        val preSelected = preSelectedCodes.toSet()
        countryRepository.getAllCountryCodes().map { code ->
            ListItemDataUi(
                itemId = code,
                mainContentData = ListItemMainContentDataUi.Text(
                    text = "${countryName(code)} ($code)"
                ),
                trailingContentData = ListItemTrailingContentDataUi.Checkbox(
                    checkboxData = CheckboxDataUi(
                        isChecked = code in preSelected
                    )
                )
            )
        }
    }

    override fun handleItemSelection(
        items: List<ListItemDataUi>,
        identifier: String,
    ): HandleItemSelectionPartialState {
        val updatedItems = items.map { item ->
            if (item.itemId == identifier && item.trailingContentData is ListItemTrailingContentDataUi.Checkbox) {
                item.copy(
                    trailingContentData = (item.trailingContentData)
                        .copy(
                            checkboxData = CheckboxDataUi(
                                isChecked = !item.trailingContentData.checkboxData.isChecked
                            )
                        )
                )
            } else item
        }

        return HandleItemSelectionPartialState.Updated(
            items = updatedItems,
            hasSelectedItems = updatedItems.hasAnyCheckedCheckbox(),
        )
    }

    override fun selectedCountryCodes(items: List<ListItemDataUi>): List<String> =
        items
            .filter { item ->
                val trailing = item.trailingContentData
                trailing is ListItemTrailingContentDataUi.Checkbox && trailing.checkboxData.isChecked
            }
            .map { it.itemId }

    override fun getCountrySets(): List<CountrySetUi> =
        countryRepository.getCountrySets().map { set ->
            CountrySetUi(id = set.id, label = countrySetLabel(set.id))
        }

    override fun applyCountrySet(
        items: List<ListItemDataUi>,
        setId: String
    ): List<ListItemDataUi> {
        val codes = countryRepository.getCountrySets()
            .firstOrNull { it.id == setId }
            ?.countryCodes
            ?.toSet()
            .orEmpty()

        return items.map { item ->
            val trailing = item.trailingContentData
            if (trailing is ListItemTrailingContentDataUi.Checkbox) {
                item.copy(
                    trailingContentData = trailing.copy(
                        checkboxData = CheckboxDataUi(isChecked = item.itemId in codes)
                    )
                )
            } else {
                item
            }
        }
    }

    override fun matchingCountrySetId(selectedCodes: List<String>): String? {
        val selected = selectedCodes.toSet()
        return countryRepository.getCountrySets()
            .firstOrNull { it.countryCodes.toSet() == selected }
            ?.id
    }

    /**
     * Resolves a localized country name from `country_<code>`, falling back to the code itself when
     * no resource exists for it.
     */
    private fun countryName(code: String): String {
        val resource = Res.allStringResources["country_${code.lowercase()}"]
        return resource?.let { resourceProvider.getSharedString(it) } ?: code
    }

    /**
     * Resolves a localized country-set label from `country_set_<id>`, falling back to the id when
     * no resource exists for it.
     */
    private fun countrySetLabel(id: String): String {
        val resource = Res.allStringResources["country_set_$id"]
        return resource?.let { resourceProvider.getSharedString(it) } ?: id
    }
}
