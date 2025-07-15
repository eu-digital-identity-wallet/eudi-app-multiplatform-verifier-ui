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

import eu.europa.ec.euidi.verifier.domain.config.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.domain.model.SupportedDocumentUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface DocumentsToRequestInteractor {
    fun getSupportedDocuments(): List<SupportedDocumentUi>

    fun getDocumentClaims(attestationType: AttestationType): List<ClaimItem>

    fun searchDocuments(query: String, documents: List<SupportedDocumentUi>): Flow<List<SupportedDocumentUi>>
}

class DocumentsToRequestInteractorImpl(
    private val configProvider: ConfigProvider,
): DocumentsToRequestInteractor {
    override fun getSupportedDocuments(): List<SupportedDocumentUi> =
        configProvider.supportedDocuments
            .documents
            .map { (documentType, _) ->
                SupportedDocumentUi(
                    id = documentType.displayName,
                    documentType = documentType,
                    modes = configProvider.getDocumentModes(documentType)
                )
            }

    override fun getDocumentClaims(attestationType: AttestationType): List<ClaimItem> =
        configProvider.supportedDocuments.documents[attestationType].orEmpty()

    override fun searchDocuments(query: String, documents: List<SupportedDocumentUi>): Flow<List<SupportedDocumentUi>> =
        flow {
            val filtered = documents.filter {
                it.documentType.displayName.contains(query, ignoreCase = true)
                        || it.modes.any { mode -> mode.displayName.contains(query, ignoreCase = true) }
            }

            emit(filtered)
        }
}