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

package eu.europa.ec.euidi.verifier.core.controller

import eu.europa.ec.euidi.verifier.domain.config.model.Logger
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentsDomain
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import kotlinx.coroutines.flow.Flow

interface TransferController {

    fun initializeVerifier(certificates: List<String>, logger: Logger)

    fun initializeTransferManager(
        bleCentralClientMode: Boolean,
        blePeripheralServerMode: Boolean,
        useL2Cap: Boolean,
        clearBleCache: Boolean
    )

    fun startEngagement(qrCode: String)

    fun sendRequest(
        requestedDocs: List<RequestedDocumentUi>,
        retainData: Boolean,
    ): Flow<TransferStatus>

    fun stopConnection()
}

sealed class TransferStatus() {
    data class Error(val message: String) : TransferStatus()
    data object Connected : TransferStatus()
    data object Connecting : TransferStatus()
    data object DeviceEngagementCompleted : TransferStatus()
    data object Disconnected : TransferStatus()
    data object RequestSent : TransferStatus()
    data class OnResponseReceived(val receivedDocs: ReceivedDocumentsDomain) : TransferStatus()
}