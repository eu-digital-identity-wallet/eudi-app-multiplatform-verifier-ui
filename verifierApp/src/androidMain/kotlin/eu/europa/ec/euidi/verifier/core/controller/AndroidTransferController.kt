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
import android.util.Log
import eu.europa.ec.eudi.verifier.core.EudiVerifier
import eu.europa.ec.eudi.verifier.core.EudiVerifierConfig
import eu.europa.ec.eudi.verifier.core.request.DeviceRequest
import eu.europa.ec.eudi.verifier.core.request.DocRequest
import eu.europa.ec.eudi.verifier.core.response.DeviceResponse
import eu.europa.ec.eudi.verifier.core.transfer.TransferConfig
import eu.europa.ec.eudi.verifier.core.transfer.TransferEvent
import eu.europa.ec.eudi.verifier.core.transfer.TransferManager
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.utils.safeAsync
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentDomain
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.multipaz.crypto.X509Cert
import org.multipaz.mdoc.connectionmethod.MdocConnectionMethod
import org.multipaz.mdoc.connectionmethod.MdocConnectionMethodBle
import org.multipaz.trustmanagement.TrustPoint
import org.multipaz.util.UUID
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class AndroidTransferController(
    private val context: Context,
    private val resourceProvider: ResourceProvider,
) : TransferController {

    private lateinit var transferManager: TransferManager
    private lateinit var eudiVerifier: EudiVerifier

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
        transferManager.startQRDeviceEngagement(qrCode)
    }

    override fun sendRequest(requestedDocs: List<RequestedDocumentUi>) = callbackFlow {

        val request = requestedDocs
            .map { it.transformToDocRequest() }
            .toDeviceRequest()

        val listener = object : TransferEvent.Listener {
            override fun onEvent(event: TransferEvent) {
                when (event) {
                    is TransferEvent.Connecting -> {
                        trySendBlocking(TransferStatus.Connecting)
                    }

                    is TransferEvent.Connected -> {
                        transferManager.sendRequest(request)
                        trySendBlocking(TransferStatus.Connected)
                    }

                    is TransferEvent.DeviceEngagementCompleted -> {
                        trySendBlocking(TransferStatus.DeviceEngagementCompleted)
                    }

                    is TransferEvent.Disconnected -> {
                        trySendBlocking(TransferStatus.Disconnected)
                    }

                    is TransferEvent.Error -> {
                        trySendBlocking(
                            TransferStatus.Error(
                                event.error.localizedMessage
                                    ?: resourceProvider.genericErrorMessage()
                            )
                        )
                    }

                    is TransferEvent.RequestSent -> {
                        trySendBlocking(TransferStatus.RequestSent)
                    }

                    is TransferEvent.ResponseReceived -> {

                        val response = event.response as DeviceResponse

                        response.deviceResponse.documents.forEach { doc ->
                            val isTrusted = eudiVerifier.isDocumentTrusted(doc)
                            println("${doc.docType} Trusted: ${isTrusted.isTrusted}")
                        }

                        val documentClaims = response.documentsClaims
                        val documentValidity = response.documentsValidity

                        for (doc in documentClaims) {

                            for (nameSpace in doc.claims) {
                                Log.d("NameSpace", nameSpace.key)
                                for (element in nameSpace.value) {
                                    Log.d("Element Name-Value", "${element.key}-${element.value}")
                                }
                            }

                            val validity = documentValidity.find { it.docType == doc.docType }
                            Log.d("Device Signature Valid", "${validity?.isDeviceSignatureValid}")
                            Log.d("Issuer Signature Valid", "${validity?.isIssuerSignatureValid}")
                            Log.d("Authentication Method", "${validity?.deviceAuthMethod}")
                            Log.d("Data Integrity Valid", "${validity?.isDataIntegrityIntact}")
                            Log.d("Signed", "${validity?.msoValidity?.signed}")
                            Log.d("Valid From", "${validity?.msoValidity?.validFrom}")
                            Log.d("Valid Until", "${validity?.msoValidity?.validUntil}")

                            validity?.elementMsoResults?.forEach { msoElement ->
                                Log.d(
                                    "Issuer-ElementName-DigestMatched",
                                    "${msoElement.namespace}-${msoElement.identifier}-${msoElement.digestMatched}"
                                )
                            }
                        }
                        trySendBlocking(
                            TransferStatus.OnResponseReceived(
                                ReceivedDocumentDomain(
                                    id = "mock"
                                )
                            )
                        )
                    }
                }
            }
        }

        transferManager.addListener(listener)

        awaitClose {
            transferManager.removeListener(listener)
        }

    }.safeAsync {
        TransferStatus.Error(
            it.localizedMessage ?: resourceProvider.genericErrorMessage()
        )
    }

    @Throws
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

    private fun RequestedDocumentUi.transformToDocRequest(): DocRequest =
        DocRequest(
            docType = this.documentType.docType,
            itemsRequest = mapOf(
                this.documentType.namespace to mapOf(
                    this.claims.first().label to false  // TODO decide what to do with this flag
                )
            ),
            readerAuthCertificate = null
        )

    private fun List<DocRequest>.toDeviceRequest(): DeviceRequest =
        DeviceRequest(
            docRequests = this
        )
}