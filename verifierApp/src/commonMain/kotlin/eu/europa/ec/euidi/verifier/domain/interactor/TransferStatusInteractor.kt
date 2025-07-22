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
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getDisplayName
import eu.europa.ec.euidi.verifier.presentation.model.ClaimKey
import eu.europa.ec.euidi.verifier.presentation.model.ClaimValue
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_request_label
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_connected
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_connecting
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_failed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface TransferStatusInteractor {

    fun transformToReceivedDocumentsUi(claims: List<Map<ClaimKey, ClaimValue>>): List<ReceivedDocumentUi>

    fun getConnectionStatus(): Flow<String>

    suspend fun getRequestData(
        docs: List<RequestedDocumentUi>
    ): String
}

class TransferStatusInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val uuidProvider: UuidProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TransferStatusInteractor {

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

    override fun getConnectionStatus(): Flow<String> = flow {
        emit(ConnectionStatus.Connecting.toUserFriendlyString())

        delay(3000)

        emit(ConnectionStatus.Connected.toUserFriendlyString())
    }

    override suspend fun getRequestData(
        docs: List<RequestedDocumentUi>
    ): String {
        return withContext(dispatcher) {
            val requestedDocTypes = getRequestedDocumentTypes(docs)
            val requestLabel =
                resourceProvider.getSharedString(Res.string.transfer_status_screen_request_label)

            "$requestLabel $requestedDocTypes"
        }
    }

    private suspend fun getRequestedDocumentTypes(docs: List<RequestedDocumentUi>): String {
        if (docs.isEmpty()) return ""

        val parts = docs.map { doc ->
            val displayName = doc.documentType.getDisplayName(resourceProvider)
            "${doc.mode.displayName} $displayName"
        }

        return parts.joinToString(separator = "; ")
    }

    private suspend fun ConnectionStatus.toUserFriendlyString(): String =
        when (this) {
            is ConnectionStatus.Connecting -> resourceProvider.getSharedString(
                Res.string.transfer_status_screen_status_connecting
            )

            is ConnectionStatus.Connected -> resourceProvider.getSharedString(
                Res.string.transfer_status_screen_status_connected
            )

            is ConnectionStatus.Failed -> resourceProvider.getSharedString(
                Res.string.transfer_status_screen_status_failed
            )
        }
}

sealed class ConnectionStatus {
    data object Connecting : ConnectionStatus()
    data object Connected : ConnectionStatus()
    data class Failed(val reason: String? = null) : ConnectionStatus()
}