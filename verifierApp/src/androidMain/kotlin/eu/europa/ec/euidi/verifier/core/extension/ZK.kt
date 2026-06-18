/*
 * Copyright (c) 2026 European Commission
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

package eu.europa.ec.euidi.verifier.core.extension

import com.kss.euid.zk.sdk.NatMode
import com.kss.euid.zk.sdk.PredicateMode
import com.kss.euid.zk.sdk.ZkPublicStatement
import com.kss.euid.zk.sdk.isoAlpha2ToNumeric
import com.kss.euid.zk.sdk.predicateModeToken
import com.kss.euid.zk.sdk.verifyIdentity
import com.kss.euid.zk.sdk.zkContractV1
import eu.europa.ec.eudi.verifier.core.response.DeviceResponse
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.model.DocumentValidityDomain
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentDomain
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import org.multipaz.crypto.EcPublicKeyDoubleCoordinate
import org.multipaz.mdoc.zkp.ZkDocument
import org.multipaz.mdoc.zkp.ZkSystemSpec
import kotlin.time.ExperimentalTime

const val DEFAULT_MIN_AGE = 18L

/** EU/EEA accepted-country set as ISO-3166 numeric codes (CSV), via the SDK's alpha-2 map. */
val DEFAULT_ACCEPTED_COUNTRIES: String = listOf(
    "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT",
    "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE", "IS", "LI", "NO",
).mapNotNull { isoAlpha2ToNumeric(it) }.joinToString(",")

/**
 * Attaches the STWO ZK spec when this is a PID request asking only for the predicate attributes
 * (`birth_date` and/or `nationality`). The wallet's `getMatchingSystemSpec` only matches when the
 * requested claims are a subset of those, so any other claim mix gracefully falls back to plaintext.
 * Predicate parameters are demo defaults (over-18 / EU-EEA set) until a request UI is added.
 */
internal fun RequestedDocumentUi.intoZkSystemSpecs(): List<ZkSystemSpec> {
    val contract = zkContractV1()
    if (this.documentType.docType != contract.doctypePid) return emptyList()

    val requested = this.claims.map { it.label }.toSet()
    val wantsAge = contract.elementBirthDate in requested
    val wantsNat = contract.elementNationality in requested
    if (!wantsAge && !wantsNat) return emptyList()

    val mode = when {
        wantsAge && wantsNat -> PredicateMode.AND
        wantsAge -> PredicateMode.AGE
        else -> PredicateMode.NAT
    }

    val spec = ZkSystemSpec(id = contract.specIdPid, system = contract.systemName).apply {
        addParam(contract.paramPredicateMode, predicateModeToken(mode))
        if (wantsAge) addParam(contract.paramMinAge, DEFAULT_MIN_AGE)
        if (wantsNat) addParam(contract.paramAcceptedCountries, DEFAULT_ACCEPTED_COUNTRIES)
        addParam(contract.paramVersion, 1L)
        addParam(contract.paramNumAttributes, 2L)
    }
    return listOf(spec)
}

/**
 * Verifies the Zero-Knowledge proofs in a response and maps them to received documents.
 *
 * For each [ZkDocument] we reconstruct the public statement the wallet proved against — issuer key
 * from the proof's cert chain, `today` from the proof timestamp, the nonce from the session
 * transcript, and the predicate (mode/threshold/accepted-set) from the asserted result claims plus
 * our request defaults — then ask the SDK to verify the proof. The asserted boolean claims
 * (e.g. `age_over_18`, `nationality_in_set`) are surfaced as the document's claims.
 */
@OptIn(ExperimentalTime::class)
internal fun DeviceResponse.verifiedZKDocuments(): List<ReceivedDocumentDomain> {
    val contract = zkContractV1()
    return deviceResponse.zkDocuments.mapNotNull { zkDoc ->
        val data = zkDoc.zkDocumentData
        val resultClaims = data.issuerSigned[contract.pidNamespace] ?: return@mapNotNull null

        val natPresent = resultClaims.containsKey(contract.resultNatInSet)
        val minAge = resultClaims.keys
            .firstOrNull { it.startsWith("age_over_") }
            ?.removePrefix("age_over_")
            ?.toUIntOrNull()
        val agePresent = minAge != null

        val issuerKey = data.msoX5chain?.certificates?.firstOrNull()?.ecPublicKey
                as? EcPublicKeyDoubleCoordinate
            ?: return@mapNotNull null

        val statement = ZkPublicStatement(
            specId = data.zkSystemSpecId,
            version = 1u,
            doctype = contract.doctypePid,
            namespace = contract.pidNamespace,
            issuerKeyX = issuerKey.x,
            issuerKeyY = issuerKey.y,
            todayEpochDay = (data.timestamp.epochSeconds / 86_400L).toInt(),
            nonce = sessionTranscript,
            predicateMode = when {
                agePresent && natPresent -> PredicateMode.AND
                agePresent -> PredicateMode.AGE
                else -> PredicateMode.NAT
            },
            ageThresholdYears = if (agePresent) minAge else null,
            acceptedNumericCountries = if (natPresent) {
                DEFAULT_ACCEPTED_COUNTRIES.split(",").mapNotNull { it.toUIntOrNull() }
            } else {
                null
            },
            natMode = NatMode.ANY,
        )

        val verified = runCatching {
            verifyIdentity(statement = statement, proof = zkDoc.proof.toByteArray()).ok
        }.getOrDefault(false)

        ReceivedDocumentDomain(
            isTrusted = verified,
            docType = data.docType,
            claims = resultClaims.keys.associate { claimKey ->
                ClaimItem(label = claimKey) to if (verified) "true" else "unverified"
            },
            validity = DocumentValidityDomain(
                isDeviceSignatureValid = null,
                // The ZK proof attests the issuer (P-256/SHA-256) signature over the predicate.
                isIssuerSignatureValid = verified,
                isDataIntegrityIntact = verified,
                signed = null,
                validFrom = null,
                validUntil = null,
            ),
        )
    }
}