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

import eu.europa.ec.euidi.verifier.core.controller.TransferController
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getDisplayName
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

interface TransferStatusInteractor {

    suspend fun transformToReceivedDocumentsUi(
        requestedDocuments: List<RequestedDocumentUi>,
        allDocuments: List<AvailableDocument>,
    ): List<ReceivedDocumentUi>

    fun getConnectionStatus(
        docs: List<RequestedDocumentUi>,
        qrCode: String
    ): Flow<String>

    suspend fun getRequestData(
        docs: List<RequestedDocumentUi>
    ): String

    suspend fun getAvailableDocuments(): List<AvailableDocument>
}

class TransferStatusInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val uuidProvider: UuidProvider,
    private val transferController: TransferController,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TransferStatusInteractor {

    override suspend fun transformToReceivedDocumentsUi(
        requestedDocuments: List<RequestedDocumentUi>,
        allDocuments: List<AvailableDocument>,
    ): List<ReceivedDocumentUi> {
        return withContext(Dispatchers.Default) {

            // Pre-index all available documents by type for fast lookup
            val documentsByType = allDocuments.associateBy { it.attestationType }

            requestedDocuments.map { requestedDoc ->
                val matchingDoc = documentsByType[requestedDoc.documentType]

                val claims: Map<ClaimItem, ClaimValue> = matchingDoc
                    ?.let { availableDoc ->
                        requestedDoc.claims
                            .mapNotNull { claimItem ->
                                availableDoc.claims[claimItem]?.let { value ->
                                    claimItem to value
                                }
                            }
                            .toMap()
                    } ?: emptyMap()

                ReceivedDocumentUi(
                    id = uuidProvider.provideUuid(),
                    documentType = requestedDoc.documentType,
                    claims = claims
                )
            }
        }
    }

    override fun getConnectionStatus(
        docs: List<RequestedDocumentUi>,
        qrCode: String
    ): Flow<String> = flow {
        transferController.initializeVerifier(
            listOf()
        )

        transferController.initializeTransferManager(
            bleCentralClientMode = false,
            blePeripheralServerMode = true,
            useL2Cap = false,
            clearBleCache = false
        )

        transferController.startEngagement(qrCode)

        transferController.sendRequest(docs).collect { status ->
            println("Status: $status")
        }
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

    override suspend fun getAvailableDocuments(): List<AvailableDocument> {
        return withContext(dispatcher) {
            getDummyData()
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

    private fun getDummyData(): List<AvailableDocument> {
        return listOf(
            AvailableDocument(
                attestationType = AttestationType.Pid,
                claims = mapOf(
                    ClaimItem("family_name") to "Doe",
                    ClaimItem("given_name") to "John",
                    ClaimItem("birth_date") to "1985-07-12",
                    ClaimItem("expiry_date") to "2030-01-15",
                    ClaimItem("issuing_country") to "USA",
                    ClaimItem("issuing_authority") to "US Department of State",
                    ClaimItem("document_number") to "X12345678",
                    ClaimItem("portrait") to "base64portrait==",
                    ClaimItem("sex") to "Male",
                    ClaimItem("nationality") to "US",
                    ClaimItem("issuing_jurisdiction") to "California",
                    ClaimItem("resident_address") to "123 Main Street, Springfield",
                    ClaimItem("resident_country") to "USA",
                    ClaimItem("resident_state") to "Illinois",
                    ClaimItem("resident_city") to "Springfield",
                    ClaimItem("resident_postal_code") to "62704",
                    ClaimItem("age_in_years") to "40",
                    ClaimItem("age_birth_year") to "1985",
                    ClaimItem("age_over_18") to "true",
                    ClaimItem("issuance_date") to "2020-01-01",
                    ClaimItem("email_address") to "john.doe@example.com",
                    ClaimItem("resident_street") to "Main Street",
                    ClaimItem("resident_house_number") to "123",
                    ClaimItem("personal_administrative_number") to "987654321",
                    ClaimItem("mobile_phone_number") to "+1 555-000-1111",
                    ClaimItem("family_name_birth") to "Doe",
                    ClaimItem("given_name_birth") to "John",
                    ClaimItem("place_of_birth") to "Springfield, Illinois",
                    ClaimItem("trust_anchor") to "gov.usa.pid",
                )
            ),
            AvailableDocument(
                attestationType = AttestationType.Mdl,
                claims = mapOf(
                    ClaimItem("family_name") to "Smith",
                    ClaimItem("given_name") to "Jane",
                    ClaimItem("birth_date") to "1990-03-22",
                    ClaimItem("expiry_date") to "2031-05-10",
                    ClaimItem("issue_date") to "2021-05-10",
                    ClaimItem("issuing_country") to "CAN",
                    ClaimItem("issuing_authority") to "Ontario Transport",
                    ClaimItem("document_number") to "D7654321",
                    ClaimItem("portrait") to "base64portrait==",
                    ClaimItem("sex") to "Female",
                    ClaimItem("nationality") to "CA",
                    ClaimItem("issuing_jurisdiction") to "Ontario",
                    ClaimItem("resident_address") to "456 Maple Avenue, Toronto",
                    ClaimItem("resident_country") to "Canada",
                    ClaimItem("resident_state") to "Ontario",
                    ClaimItem("resident_city") to "Toronto",
                    ClaimItem("resident_postal_code") to "M5H 2N2",
                    ClaimItem("age_in_years") to "35",
                    ClaimItem("age_birth_year") to "1990",
                    ClaimItem("age_over_18") to "true",
                    ClaimItem("driving_privileges") to "Class G",
                    ClaimItem("un_distinguishing_sign") to "CAN",
                    ClaimItem("administrative_number") to "A1234567",
                    ClaimItem("height") to "170 cm",
                    ClaimItem("weight") to "65 kg",
                    ClaimItem("eye_colour") to "Brown",
                    ClaimItem("hair_colour") to "Blonde",
                    ClaimItem("birth_place") to "Toronto, Ontario",
                    ClaimItem("portrait_capture_date") to "2021-04-30",
                    ClaimItem("biometric_template_xx") to "templateData==",
                    ClaimItem("family_name_national_character") to "Smith",
                    ClaimItem("given_name_national_character") to "Jane",
                    ClaimItem("signature_usual_mark") to "base64signature==",
                )
            ),
            AvailableDocument(
                attestationType = AttestationType.AgeVerification,
                claims = mapOf(
                    ClaimItem("age_over_18") to "true",
                    ClaimItem("issuance_date") to "2023-01-01",
                    ClaimItem("user_pseudonym") to "user_123456",
                    ClaimItem("expiry_date") to "2030-12-31",
                    ClaimItem("issuing_authority") to "AgeVerifier Inc.",
                    ClaimItem("issuing_country") to "USA",
                )
            )
        )
    }
}

sealed class ConnectionStatus {
    data object Connecting : ConnectionStatus()
    data object Connected : ConnectionStatus()
    data class Failed(val reason: String? = null) : ConnectionStatus()
}

//TODO Remove/refactor when integration with Core happens.
data class AvailableDocument(
    val attestationType: AttestationType,
    val claims: Map<ClaimItem, ClaimValue>
)