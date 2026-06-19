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
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimKind
import eu.europa.ec.euidi.verifier.domain.transformer.UiTransformer
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.extension.hasAnyCheckedCheckbox
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.zk_request_nationality_countries_selected
import eudiverifier.verifierapp.generated.resources.zk_request_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface ZkRequestInteractor {

    suspend fun getScreenTitle(attestationType: AttestationType): String

    fun getZkClaims(attestationType: AttestationType): List<ClaimItem>

    suspend fun transformToUiItems(
        documentType: AttestationType,
        claims: List<ClaimItem>
    ): List<ListItemDataUi>

    suspend fun transformToClaimItems(
        sourceClaims: List<ClaimItem>,
        items: List<ListItemDataUi>
    ): List<ClaimItem>

    fun handleItemSelection(
        items: List<ListItemDataUi>,
        identifier: String,
    ): HandleItemSelectionPartialState

    fun selectedCountriesSubtitle(count: Int): String?
}

class ZkRequestInteractorImpl(
    private val configProvider: ConfigProvider,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : ZkRequestInteractor {

    override suspend fun getScreenTitle(attestationType: AttestationType): String =
        withContext(dispatcher) {
            resourceProvider.getSharedString(Res.string.zk_request_screen_title)
        }

    override fun getZkClaims(attestationType: AttestationType): List<ClaimItem> =
        configProvider.supportedDocuments.documents[attestationType]
            .orEmpty()
            .filter { it.kind is ClaimKind.Zk }

    override suspend fun transformToUiItems(
        documentType: AttestationType,
        claims: List<ClaimItem>
    ): List<ListItemDataUi> = withContext(dispatcher) {
        UiTransformer.transformToUiItems(
            fields = claims,
            attestationType = documentType,
            resourceProvider = resourceProvider
        )
    }

    override suspend fun transformToClaimItems(
        sourceClaims: List<ClaimItem>,
        items: List<ListItemDataUi>
    ): List<ClaimItem> = withContext(dispatcher) {
        val checkedIds = items
            .filter { uiItem ->
                val trailing = uiItem.trailingContentData
                trailing is ListItemTrailingContentDataUi.Checkbox && trailing.checkboxData.isChecked
            }
            .map { it.itemId }
            .toSet()

        sourceClaims.filter { it.id in checkedIds }
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

    override fun selectedCountriesSubtitle(count: Int): String? =
        // The picker enforces a minimum of two countries, so the count is either zero (no subtitle)
        // or two-or-more.
        if (count <= 0) {
            null
        } else {
            resourceProvider.getSharedString(
                Res.string.zk_request_nationality_countries_selected,
                count
            )
        }
}
