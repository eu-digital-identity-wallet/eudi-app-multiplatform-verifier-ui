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

import android.content.Context
import android.util.Base64
import eu.europa.ec.eudi.verifier.core.EudiVerifier
import eu.europa.ec.eudi.verifier.core.EudiVerifierConfig
import eu.europa.ec.eudi.verifier.core.request.DeviceRequest
import eu.europa.ec.eudi.verifier.core.request.DocRequest
import eu.europa.ec.eudi.verifier.core.response.DeviceResponse
import eu.europa.ec.eudi.verifier.core.response.DocumentClaims
import eu.europa.ec.eudi.verifier.core.transfer.TransferConfig
import eu.europa.ec.eudi.verifier.core.transfer.TransferEvent
import eu.europa.ec.eudi.verifier.core.transfer.TransferManager
import eu.europa.ec.euidi.verifier.core.extension.flattenedClaims
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.model.DocumentValidityDomain
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentDomain
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentsDomain
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.datetime.toStdlibInstant
import org.multipaz.crypto.X509Cert
import org.multipaz.mdoc.connectionmethod.MdocConnectionMethod
import org.multipaz.mdoc.connectionmethod.MdocConnectionMethodBle
import org.multipaz.mdoc.response.DeviceResponseParser
import org.multipaz.trustmanagement.TrustPoint
import org.multipaz.util.UUID
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AndroidTransferController(
    private val context: Context,
    private val resourceProvider: ResourceProvider,
) : TransferController {

    private var transferManager: TransferManager? = null
    private lateinit var eudiVerifier: EudiVerifier
    private var listener: TransferEvent.Listener? = null

    private val _statuses = MutableSharedFlow<TransferStatus>(
        replay = 1,
        extraBufferCapacity = 8,
    )
    private val statuses: SharedFlow<TransferStatus> = _statuses.asSharedFlow()

    override fun initializeVerifier(certificates: List<String>) {
        if (::eudiVerifier.isInitialized.not()) {

            val x509Certificates = certificates.mapNotNull {
                pemToX509Certificate(it).getOrNull()
            }

            val trustPoints: List<TrustPoint> = x509Certificates.map {
                TrustPoint(X509Cert(it.encoded))
            }

            val config = EudiVerifierConfig {
                trustPoints(trustPoints)
            }

            eudiVerifier = EudiVerifier(context, config)
        }
    }

    override fun initializeTransferManager(
        bleCentralClientMode: Boolean,
        blePeripheralServerMode: Boolean,
        useL2Cap: Boolean,
        clearBleCache: Boolean
    ) {
        val connectionMethods = listOf<MdocConnectionMethod>(
            MdocConnectionMethodBle(
                supportsPeripheralServerMode = blePeripheralServerMode,
                supportsCentralClientMode = bleCentralClientMode,
                peripheralServerModeUuid = UUID.randomUUID(),
                centralClientModeUuid = UUID.randomUUID()
            )
        )

        transferManager = eudiVerifier.createTransferManager {
            addEngagementMethod(TransferConfig.EngagementMethod.QR, connectionMethods)
        }
    }

    override fun startEngagement(qrCode: String) {
        transferManager?.startQRDeviceEngagement(qrCode)
    }

    override fun sendRequest(
        requestedDocs: List<RequestedDocumentUi>,
        retainData: Boolean,
    ): Flow<TransferStatus> {
        requireNotNull(transferManager) {
            "TransferManager is not initialized. Call initializeTransferManager() before sendRequest()."
        }

        val request = requestedDocs
            .map { it.transformToDocRequest(retainData) }
            .toDeviceRequest()

        val eventsListener = object : TransferEvent.Listener {
            override fun onEvent(event: TransferEvent) {
                with(_statuses) {
                    when (event) {
                        is TransferEvent.Connecting -> {
                            tryEmit(TransferStatus.Connecting)
                        }

                        is TransferEvent.Connected -> {
                            transferManager?.sendRequest(request)
                            tryEmit(TransferStatus.Connected)
                        }

                        is TransferEvent.DeviceEngagementCompleted -> {
                            tryEmit(TransferStatus.DeviceEngagementCompleted)
                        }

                        is TransferEvent.Disconnected -> {
                            tryEmit(TransferStatus.Disconnected)
                        }

                        is TransferEvent.Error -> {
                            tryEmit(
                                TransferStatus.Error(
                                    event.error.localizedMessage
                                        ?: resourceProvider.genericErrorMessage()
                                )
                            )
                        }

                        is TransferEvent.RequestSent -> {
                            tryEmit(TransferStatus.RequestSent)
                        }

                        is TransferEvent.ResponseReceived -> {
                            val response = event.response as DeviceResponse

                            val receivedDocuments = response.deviceResponse.documents
                                .zip(response.documentsClaims) { parserDoc: DeviceResponseParser.Document, claimsDoc: DocumentClaims ->
                                    val isTrusted =
                                        eudiVerifier.isDocumentTrusted(document = parserDoc).isTrusted
                                    val validity = response.documentsValidity
                                        .find { it.docType == claimsDoc.docType }

                                    ReceivedDocumentDomain(
                                        isTrusted = isTrusted,
                                        docType = claimsDoc.docType,
                                        claims = claimsDoc.flattenedClaims(resourceProvider),
                                        validity = DocumentValidityDomain(
                                            isDeviceSignatureValid = validity?.isDeviceSignatureValid,
                                            isIssuerSignatureValid = validity?.isIssuerSignatureValid,
                                            isDataIntegrityIntact = validity?.isDataIntegrityIntact,
                                            signed = validity?.msoValidity?.signed?.toStdlibInstant(),
                                            validFrom = validity?.msoValidity?.validFrom?.toStdlibInstant(),
                                            validUntil = validity?.msoValidity?.validUntil?.toStdlibInstant(),
                                        )
                                    )
                                }

                            tryEmit(
                                TransferStatus.OnResponseReceived(
                                    receivedDocs = ReceivedDocumentsDomain(documents = receivedDocuments)
                                )
                            )
                        }
                    }
                }
            }
        }

        listener?.let { transferManager?.removeListener(it) }
        listener = eventsListener
        transferManager?.addListener(eventsListener)
        return statuses
    }

    override fun stopConnection() {
        listener?.let { transferManager?.removeListener(it) }
        listener = null
        transferManager?.stopSession()
        transferManager = null
    }

    private fun pemToX509Certificate(pem: String): Result<X509Certificate> {
        return runCatching {
            val base64 = pem
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replace("\\s+".toRegex(), "")

            val der = Base64.decode(base64, Base64.DEFAULT)
            val cf = CertificateFactory.getInstance("X.509")

            cf.generateCertificate(ByteArrayInputStream(der)) as X509Certificate
        }
    }

    private fun RequestedDocumentUi.transformToDocRequest(
        retainData: Boolean
    ): DocRequest {
        val requestedClaims: Map<String, Boolean> = this
            .claims
            .associate { claimItem: ClaimItem ->
                claimItem.label to retainData
            }

        return DocRequest(
            docType = this.documentType.docType,
            itemsRequest = mapOf(
                this.documentType.namespace to requestedClaims
            ),
            readerAuthCertificate = null
        )
    }

    private fun List<DocRequest>.toDeviceRequest(): DeviceRequest =
        DeviceRequest(
            docRequests = this
        )
}