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
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.transformer.UiTransformer.toListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.model.DocumentUi
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.model.toListItems
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.show_documents_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface ShowDocumentsInteractor {
    suspend fun transformToUiItems(
        items: List<ReceivedDocumentUi>
    ): List<DocumentUi>

    suspend fun getScreenTitle(): String
}

class ShowDocumentsInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val uuidProvider: UuidProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ShowDocumentsInteractor {

    override suspend fun transformToUiItems(
        items: List<ReceivedDocumentUi>
    ): List<DocumentUi> {
        return withContext(dispatcher) {
            items.map { document ->
                val claimsUi = document.claims.entries.map { (claimKey, claimValue) ->
                    document.toListItemDataUi(
                        itemId = uuidProvider.provideUuid(),
                        claimKey = claimKey,
                        claimValue = claimValue,
                        resourceProvider = resourceProvider
                    )
                }

                DocumentUi(
                    id = document.id,
                    docType = document.documentType.docType,
                    uiClaims = claimsUi,
                    validityInfo = document.documentValidity.toListItems(
                        resourceProvider = resourceProvider,
                        uuidProvider = uuidProvider
                    )
                )
            }
        }
    }

    override suspend fun getScreenTitle(): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(Res.string.show_documents_screen_title)
        }
    }
}