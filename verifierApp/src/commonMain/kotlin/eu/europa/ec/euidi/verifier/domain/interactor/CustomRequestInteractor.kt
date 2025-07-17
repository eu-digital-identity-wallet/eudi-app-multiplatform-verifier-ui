/*
 * Copyright (c) 2023 European Commission
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
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.custom_request_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface CustomRequestInteractor {
    suspend fun getScreenTitle(attestationType: String): String
    fun transformToClaimItems(items: List<ListItemDataUi>): List<ClaimItem>
}

class CustomRequestInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CustomRequestInteractor {
    override suspend fun getScreenTitle(attestationType: String): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(
                Res.string.custom_request_screen_title,
                attestationType
            )
        }
    }

    override fun transformToClaimItems(items: List<ListItemDataUi>): List<ClaimItem> {
        return items
            .filter { uiItem ->
                (uiItem.trailingContentData as? ListItemTrailingContentDataUi.Checkbox)
                    ?.checkboxData
                    ?.isChecked != false
            }
            .map { uiItem ->
                ClaimItem(label = uiItem.itemId)
            }
    }
}