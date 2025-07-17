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
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.AttestationType
import eu.europa.ec.euidi.verifier.presentation.model.ClaimKey
import eu.europa.ec.euidi.verifier.presentation.model.ClaimValue
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface TransferStatusInteractor {
    fun getRequestedDocumentTypes(docs: List<RequestedDocumentUi>): String

    fun transformToReceivedDocumentsUi(claims: List<Map<ClaimKey, ClaimValue>>): List<ReceivedDocumentUi>

    suspend fun getScreenTitle(): String
}

class TransferStatusInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val uuidProvider: UuidProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TransferStatusInteractor {
    override fun getRequestedDocumentTypes(docs: List<RequestedDocumentUi>): String {
        if (docs.isEmpty()) return ""

        return docs.joinToString(separator = "; ") {
            "${it.mode.displayName} ${it.documentType.displayName}"
        }
    }

    override fun transformToReceivedDocumentsUi(
        claims: List<Map<ClaimKey, ClaimValue>>
    ): List<ReceivedDocumentUi> {
        return claims.filter { it.isNotEmpty() }
            .map { claims ->
                ReceivedDocumentUi(
                    id = uuidProvider.provideUuid(),
                    documentType = AttestationType.Pid,
                    claims = claims
                )
            }
    }

    override suspend fun getScreenTitle(): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(Res.string.transfer_status_screen_title)
        }
    }
}