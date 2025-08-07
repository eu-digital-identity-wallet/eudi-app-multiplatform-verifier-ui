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

    suspend fun transformToReceivedDocumentsUi(
        requestedDocuments: List<RequestedDocumentUi>,
        allDocuments: List<AvailableDocument>,
    ): List<ReceivedDocumentUi>

    fun getConnectionStatus(): Flow<String>

    suspend fun getRequestData(
        docs: List<RequestedDocumentUi>
    ): String

    suspend fun getAvailableDocuments(): List<AvailableDocument>
}

class TransferStatusInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val uuidProvider: UuidProvider,
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

                val claims: Map<ClaimKey, ClaimValue> = matchingDoc
                    ?.let { availableDoc ->
                        requestedDoc.claims
                            .mapNotNull { claimItem ->
                                availableDoc.claims[claimItem.label]?.let { value ->
                                    claimItem.label to value
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
                    "family_name" to "Doe",
                    "given_name" to "John",
                    "birth_date" to "1985-07-12",
                    "expiry_date" to "2030-01-15",
                    "issuing_country" to "USA",
                    "issuing_authority" to "US Department of State",
                    "document_number" to "X12345678",
                    "portrait" to "base64portrait==",
                    "sex" to "Male",
                    "nationality" to "US",
                    "issuing_jurisdiction" to "California",
                    "resident_address" to "123 Main Street, Springfield",
                    "resident_country" to "USA",
                    "resident_state" to "Illinois",
                    "resident_city" to "Springfield",
                    "resident_postal_code" to "62704",
                    "age_in_years" to "40",
                    "age_birth_year" to "1985",
                    "age_over_18" to "true",
                    "issuance_date" to "2020-01-01",
                    "email_address" to "john.doe@example.com",
                    "resident_street" to "Main Street",
                    "resident_house_number" to "123",
                    "personal_administrative_number" to "987654321",
                    "mobile_phone_number" to "+1 555-000-1111",
                    "family_name_birth" to "Doe",
                    "given_name_birth" to "John",
                    "place_of_birth" to "Springfield, Illinois",
                    "trust_anchor" to "gov.usa.pid",
                )
            ),
            AvailableDocument(
                attestationType = AttestationType.Mdl,
                claims = mapOf(
                    "family_name" to "Smith",
                    "given_name" to "Jane",
                    "birth_date" to "1990-03-22",
                    "expiry_date" to "2031-05-10",
                    "issue_date" to "2021-05-10",
                    "issuing_country" to "CAN",
                    "issuing_authority" to "Ontario Transport",
                    "document_number" to "D7654321",
                    "portrait" to "base64portrait==",
                    "sex" to "Female",
                    "nationality" to "CA",
                    "issuing_jurisdiction" to "Ontario",
                    "resident_address" to "456 Maple Avenue, Toronto",
                    "resident_country" to "Canada",
                    "resident_state" to "Ontario",
                    "resident_city" to "Toronto",
                    "resident_postal_code" to "M5H 2N2",
                    "age_in_years" to "35",
                    "age_birth_year" to "1990",
                    "age_over_18" to "true",
                    "driving_privileges" to "Class G",
                    "un_distinguishing_sign" to "CAN",
                    "administrative_number" to "A1234567",
                    "height" to "170 cm",
                    "weight" to "65 kg",
                    "eye_colour" to "Brown",
                    "hair_colour" to "Blonde",
                    "birth_place" to "Toronto, Ontario",
                    "portrait_capture_date" to "2021-04-30",
                    "biometric_template_xx" to "templateData==",
                    "family_name_national_character" to "Smith",
                    "given_name_national_character" to "Jane",
                    "signature_usual_mark" to "base64signature==",
                )
            ),
            AvailableDocument(
                attestationType = AttestationType.AgeVerification,
                claims = mapOf(
                    "age_over_18" to "true",
                    "issuance_date" to "2023-01-01",
                    "user_pseudonym" to "user_123456",
                    "expiry_date" to "2030-12-31",
                    "issuing_authority" to "AgeVerifier Inc.",
                    "issuing_country" to "USA",
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
    val claims: Map<ClaimKey, ClaimValue>
)