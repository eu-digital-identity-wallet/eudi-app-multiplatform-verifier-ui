/*
 * Copyright (c) 2025 European Commission
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
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getDisplayName
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.transformer.UiTransformer
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.extension.hasAnyCheckedCheckbox
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.custom_request_nationality_countries_selected
import eudiverifier.verifierapp.generated.resources.custom_request_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CustomRequestInteractor {

    suspend fun getScreenTitle(attestationType: AttestationType): String

    fun getDocumentClaims(attestationType: AttestationType): List<ClaimItem>

    suspend fun transformToClaimItems(
        sourceClaims: List<ClaimItem>,
        items: List<ListItemDataUi>
    ): List<ClaimItem>

    suspend fun transformToUiItems(
        documentType: AttestationType,
        claims: List<ClaimItem>
    ): List<ListItemDataUi>

    fun handleItemSelection(
        items: List<ListItemDataUi>,
        identifier: String,
    ): HandleItemSelectionPartialState

    /**
     * The subtitle shown under the nationality predicate row, reflecting how many countries are
     * currently selected. Returns null for an empty selection so no subtitle is shown.
     */
    fun selectedCountriesSubtitle(count: Int): String?
}

class CustomRequestInteractorImpl(
    private val configProvider: ConfigProvider,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : CustomRequestInteractor {
    override suspend fun getScreenTitle(attestationType: AttestationType): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(
                Res.string.custom_request_screen_title,
                attestationType.getDisplayName(resourceProvider)
            )
        }
    }

    override fun getDocumentClaims(attestationType: AttestationType): List<ClaimItem> {
        return configProvider.supportedDocuments.documents[attestationType].orEmpty()
    }

    override suspend fun transformToClaimItems(
        sourceClaims: List<ClaimItem>,
        items: List<ListItemDataUi>
    ): List<ClaimItem> =
        withContext(dispatcher) {
            val checkedIds = items
                .filter { uiItem ->
                    val trailingContentData = uiItem.trailingContentData
                    trailingContentData is ListItemTrailingContentDataUi.Checkbox &&
                            trailingContentData.checkboxData.isChecked
                }
                .map { uiItem -> uiItem.itemId }
                .toSet()

            // Pair each checked row back to the source claim it was built from (matched by id), so
            // the claim's kind and any ZK predicate value survive the round-trip — rebuilding a
            // ClaimItem from the id string alone would silently drop both.
            sourceClaims.filter { it.id in checkedIds }
        }

    override suspend fun transformToUiItems(
        documentType: AttestationType,
        claims: List<ClaimItem>
    ): List<ListItemDataUi> =
        withContext(dispatcher) {
            UiTransformer.transformToUiItems(
                fields = claims,
                attestationType = documentType,
                resourceProvider = resourceProvider
            )
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

        val hasSelectedItems = updatedItems.hasAnyCheckedCheckbox()

        return HandleItemSelectionPartialState.Updated(
            items = updatedItems,
            hasSelectedItems = hasSelectedItems,
        )
    }

    override fun selectedCountriesSubtitle(count: Int): String? =
        // The picker enforces a minimum of two countries, so the count is either zero (no subtitle)
        // or two-or-more.
        if (count <= 0) {
            null
        } else {
            resourceProvider.getSharedString(
                Res.string.custom_request_nationality_countries_selected,
                count
            )
        }
}

sealed class HandleItemSelectionPartialState {
    data class Updated(
        val items: List<ListItemDataUi>,
        val hasSelectedItems: Boolean,
    ) : HandleItemSelectionPartialState()
}