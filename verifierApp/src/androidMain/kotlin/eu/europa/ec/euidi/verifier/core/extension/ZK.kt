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
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimKind
import eu.europa.ec.euidi.verifier.domain.config.model.ZkPredicateValue
import eu.europa.ec.euidi.verifier.domain.model.DocumentValidityDomain
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentDomain
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import org.multipaz.crypto.EcPublicKeyDoubleCoordinate
import org.multipaz.mdoc.zkp.ZkDocument
import org.multipaz.mdoc.zkp.ZkSystemSpec
import kotlin.time.ExperimentalTime

/**
 * Attaches the STWO ZK spec when this PID request carries one or more zero-knowledge predicate
 * claims ([ClaimKind.Zk]) **with an explicit parameter**. The predicate parameters — age threshold
 * and accepted nationality set — are read off each selected predicate's [ZkPredicateValue]. There
 * are no defaults: a predicate selected without a usable value is not requested, and if no predicate
 * carries a value, no spec is produced and the request proceeds entirely over plaintext.
 *
 * Only ZK claims are considered here; disclosure claims are requested in plaintext separately.
 */
internal fun RequestedDocumentUi.intoZkSystemSpecs(): List<ZkSystemSpec> {
    val contract = zkContractV1()
    if (this.documentType.docType != contract.doctypePid) return emptyList()

    // Pair each selected ZK predicate with its (attribute label, operator-supplied value).
    val zkPredicates = this.claims.mapNotNull { claim ->
        (claim.kind as? ClaimKind.Zk)?.let { claim.label to it.value }
    }
    if (zkPredicates.isEmpty()) return emptyList()

    var minAge: Long? = null
    var acceptedCountries: String? = null

    zkPredicates.forEach { (label, value) ->
        when (label) {
            contract.elementBirthDate ->
                (value as? ZkPredicateValue.AgeOver)?.let { minAge = it.years.toLong() }

            contract.elementNationality ->
                (value as? ZkPredicateValue.NationalityIn)
                    ?.countries
                    ?.mapNotNull { isoAlpha2ToNumeric(it) }
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { acceptedCountries = it.joinToString(",") }
        }
    }

    val wantsAge = minAge != null
    val wantsNat = acceptedCountries != null
    // No parameter resolved to a usable value → the predicate is not valid, so request no ZK.
    if (!wantsAge && !wantsNat) return emptyList()

    val mode = when {
        wantsAge && wantsNat -> PredicateMode.AND
        wantsAge -> PredicateMode.AGE
        else -> PredicateMode.NAT
    }

    val spec = ZkSystemSpec(id = contract.specIdPid, system = contract.systemName).apply {
        addParam(contract.paramPredicateMode, predicateModeToken(mode))
        minAge?.let { addParam(contract.paramMinAge, it) }
        acceptedCountries?.let { addParam(contract.paramAcceptedCountries, it) }
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
            // The accepted nationality set is not carried in the response and there is no default.
            // It must be correlated from the original request (by zkSystemSpecId) before a
            // nationality predicate can be reconstructed; until then a NAT/AND proof cannot verify.
            acceptedNumericCountries = null,
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