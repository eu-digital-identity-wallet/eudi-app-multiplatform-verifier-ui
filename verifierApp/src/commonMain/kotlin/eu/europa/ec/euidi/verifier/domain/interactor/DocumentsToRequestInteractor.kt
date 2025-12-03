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
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.domain.model.SupportedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface DocumentsToRequestInteractor {

    suspend fun getSupportedDocuments(): List<SupportedDocumentUi>

    fun getDocumentClaims(attestationType: AttestationType): List<ClaimItem>

    suspend fun handleDocumentOptionSelection(
        currentDocs: List<RequestedDocumentUi>,
        docId: String,
        docType: AttestationType,
        mode: DocumentMode
    ): DocSelectionResult

    fun searchDocuments(
        query: String,
        documents: List<SupportedDocumentUi>
    ): Flow<List<SupportedDocumentUi>>

    suspend fun checkDocumentMode(requestedDocs: List<RequestedDocumentUi>): List<RequestedDocumentUi>
}

class DocumentsToRequestInteractorImpl(
    private val configProvider: ConfigProvider,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : DocumentsToRequestInteractor {

    override suspend fun getSupportedDocuments(): List<SupportedDocumentUi> {
        return withContext(dispatcher) {
            configProvider.supportedDocuments
                .documents
                .map { (documentType, _) ->
                    SupportedDocumentUi(
                        id = documentType.getDisplayName(resourceProvider),
                        documentType = documentType,
                        modes = configProvider.getDocumentModes(documentType)
                    )
                }
        }
    }

    override fun getDocumentClaims(attestationType: AttestationType): List<ClaimItem> =
        configProvider.supportedDocuments.documents[attestationType].orEmpty()

    override suspend fun handleDocumentOptionSelection(
        currentDocs: List<RequestedDocumentUi>,
        docId: String,
        docType: AttestationType,
        mode: DocumentMode
    ): DocSelectionResult =
        withContext(dispatcher) {
            // Case 1: already selected → remove it
            if (currentDocs.any { it.id == docId && it.mode == mode }) {
                val updated = currentDocs.filterNot {
                    it.documentType == docType && it.mode == mode
                }
                return@withContext DocSelectionResult.Updated(updated)
            }

            // Case 2: Custom mode → remove FULL if exists, then navigate
            if (mode == DocumentMode.CUSTOM) {
                val updated =
                    if (currentDocs.any { it.id == docId && it.mode == DocumentMode.FULL }) {
                        currentDocs.filterNot { it.id == docId }
                    } else {
                        currentDocs
                    }

                val customDoc = RequestedDocumentUi(
                    id = docId,
                    documentType = docType,
                    mode = mode,
                    claims = emptyList()
                )

                return@withContext DocSelectionResult.NavigateToCustomRequest(updated, customDoc)
            }

            // Case 3: Full mode → handle Full Mode selection
            val updatedDocs = handleFullDocumentSelection(
                currentDocs = currentDocs,
                docId = docId,
                docType = docType,
                mode = mode
            )

            return@withContext DocSelectionResult.Updated(updatedDocs)
        }

    override fun searchDocuments(
        query: String,
        documents: List<SupportedDocumentUi>
    ): Flow<List<SupportedDocumentUi>> =
        flow {
            val filtered = documents.filter {
                it.documentType.getDisplayName(resourceProvider).contains(query, ignoreCase = true)
                        || it.modes.any { mode ->
                    mode.displayName.contains(
                        query,
                        ignoreCase = true
                    )
                }
            }

            emit(filtered)
        }

    override suspend fun checkDocumentMode(requestedDocs: List<RequestedDocumentUi>): List<RequestedDocumentUi> =
        withContext(dispatcher) {
            requestedDocs.map { requestedDoc ->
                val expectedClaimsCount =
                    configProvider.supportedDocuments.documents[requestedDoc.documentType]?.size
                        ?: 0

                if (requestedDoc.claims.size == expectedClaimsCount) {
                    requestedDoc.copy(mode = DocumentMode.FULL)
                } else requestedDoc
            }
        }

    private fun handleFullDocumentSelection(
        currentDocs: List<RequestedDocumentUi>,
        docId: String,
        docType: AttestationType,
        mode: DocumentMode
    ): List<RequestedDocumentUi> {
        val filteredDocs = currentDocs
            .takeIf { docs ->
                docs.any { it.documentType == docType && it.mode != mode }
            }
            ?.filterNot { it.documentType == docType }
            ?: currentDocs

        return filteredDocs + RequestedDocumentUi(
            id = docId,
            documentType = docType,
            mode = mode,
            claims = getDocumentClaims(docType)
        )
    }
}

sealed class DocSelectionResult {
    data class Updated(val docs: List<RequestedDocumentUi>) : DocSelectionResult()
    data class NavigateToCustomRequest(
        val docs: List<RequestedDocumentUi>,
        val customDoc: RequestedDocumentUi
    ) : DocSelectionResult()
}