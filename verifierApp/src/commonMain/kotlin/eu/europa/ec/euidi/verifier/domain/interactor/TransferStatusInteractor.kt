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

import eu.europa.ec.euidi.verifier.core.controller.DataStoreController
import eu.europa.ec.euidi.verifier.core.controller.PrefKey
import eu.europa.ec.euidi.verifier.core.controller.TransferController
import eu.europa.ec.euidi.verifier.core.controller.TransferStatus
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getAttestationTypeFromDocType
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getDisplayName
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentDomain
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_request_label
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface TransferStatusInteractor {

    suspend fun transformToReceivedDocumentsUi(
        requestedDocuments: List<RequestedDocumentUi>,
        receivedDocuments: List<ReceivedDocumentDomain>,
    ): List<ReceivedDocumentUi>

    suspend fun prepareConnection()

    fun startEngagement(qrCode: String)

    suspend fun getConnectionStatus(
        docs: List<RequestedDocumentUi>
    ): Flow<TransferStatus>

    suspend fun getRequestData(
        docs: List<RequestedDocumentUi>
    ): String

    suspend fun stopConnection()
}

class TransferStatusInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val uuidProvider: UuidProvider,
    private val transferController: TransferController,
    private val dataStoreController: DataStoreController,
    private val configProvider: ConfigProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TransferStatusInteractor {

    override suspend fun transformToReceivedDocumentsUi(
        requestedDocuments: List<RequestedDocumentUi>,
        receivedDocuments: List<ReceivedDocumentDomain>,
    ): List<ReceivedDocumentUi> {
        return withContext(dispatcher) {
            receivedDocuments.map { receivedDoc: ReceivedDocumentDomain ->
                ReceivedDocumentUi(
                    id = uuidProvider.provideUuid(),
                    documentType = getAttestationTypeFromDocType(docType = receivedDoc.docType),
                    claims = receivedDoc.claims
                )
            }
        }
    }

    override suspend fun prepareConnection() {
        val certificates = configProvider.getCertificates()
        transferController.initializeVerifier(certificates)
        transferController.initializeTransferManager(
            bleCentralClientMode = getSettingsValue(PrefKey.BLE_CENTRAL_CLIENT),
            blePeripheralServerMode = getSettingsValue(PrefKey.BLE_PERIPHERAL_SERVER),
            useL2Cap = getSettingsValue(PrefKey.USE_L2CAP),
            clearBleCache = getSettingsValue(PrefKey.CLEAR_BLE_CACHE)
        )
    }

    override fun startEngagement(qrCode: String) {
        transferController.startEngagement(qrCode)
    }

    override suspend fun getConnectionStatus(
        docs: List<RequestedDocumentUi>
    ): Flow<TransferStatus> {
        return transferController.sendRequest(
            requestedDocs = docs,
            retainData = getSettingsValue(PrefKey.RETAIN_DATA)
        )
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

    override suspend fun stopConnection() {
        transferController.stopConnection()
    }

    private suspend fun getSettingsValue(prefKey: PrefKey): Boolean {
        return dataStoreController.getBoolean(prefKey) ?: false
    }

    private suspend fun getRequestedDocumentTypes(docs: List<RequestedDocumentUi>): String {
        if (docs.isEmpty()) return ""

        val parts = docs.map { doc ->
            val displayName = doc.documentType.getDisplayName(resourceProvider)
            "${doc.mode.displayName} $displayName"
        }

        return parts.joinToString(separator = "; ")
    }
}