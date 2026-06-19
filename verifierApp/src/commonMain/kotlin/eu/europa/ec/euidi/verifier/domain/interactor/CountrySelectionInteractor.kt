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

    /**
     * Resolves a localized country name from `country_<code>`, falling back to the code itself when
     * no resource exists for it.
     */
    private fun countryName(code: String): String {
        val resource = Res.allStringResources["country_${code.lowercase()}"]
        return resource?.let { resourceProvider.getSharedString(it) } ?: code
    }
}
