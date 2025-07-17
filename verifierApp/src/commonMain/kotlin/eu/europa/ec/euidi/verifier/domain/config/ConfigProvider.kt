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

package eu.europa.ec.euidi.verifier.domain.config

import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelable
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelize

typealias NameSpace = String
typealias Doctype = String

@CommonParcelize
sealed interface AttestationType : CommonParcelable {
    val displayName: String
    val namespace: NameSpace
    val docType: Doctype

    data object Pid : AttestationType {
        override val displayName: String
            get() = "PID"

        override val namespace: String
            get() = "eu.europa.ec.eudi.pid.1"

        override val docType: String
            get() = "eu.europa.ec.eudi.pid.1"
    }

    data object Mdl : AttestationType {
        override val displayName: String
            get() = "mDL"

        override val namespace: String
            get() = "org.iso.18013.5.1.mDL"

        override val docType: String
            get() = "org.iso.18013.5.1"
    }

    data object AgeVerification : AttestationType {
        override val displayName: String
            get() = "Age Verification"

        override val namespace: String
            get() = "eu.europa.ec.eudi.pseudonym.age_over_18.1"

        override val docType: String
            get() = "eu.europa.ec.eudi.pseudonym.age_over_18.1"
    }

    data object OTHER : AttestationType {
        override val displayName: String
            get() = "OTHER"

        override val namespace: String
            get() = ""

        override val docType: String
            get() = ""
    }
}

enum class Mode(val displayName: String) {
    FULL(displayName = "Full"),
    CUSTOM(displayName = "Custom")
}

data class SupportedDocuments(
    val documents: Map<AttestationType, List<ClaimItem>>
)

interface ConfigProvider {
    val supportedDocuments: SupportedDocuments

    fun getDocumentModes(attestationType: AttestationType): List<Mode>
}

class ConfigProviderImpl : ConfigProvider {
    override fun getDocumentModes(attestationType: AttestationType): List<Mode> {
        return when (attestationType) {
            AttestationType.Pid -> listOf(Mode.FULL, Mode.CUSTOM)
            AttestationType.Mdl -> listOf(Mode.FULL, Mode.CUSTOM)
            AttestationType.AgeVerification -> listOf(Mode.FULL, Mode.CUSTOM)
            AttestationType.OTHER -> listOf()
        }
    }

    override val supportedDocuments = SupportedDocuments(
        mapOf(
            AttestationType.Pid to listOf(
                ClaimItem("family_name"),
                ClaimItem("given_name"),
                ClaimItem("birth_date"),
                ClaimItem("expiry_date"),
                ClaimItem("issuing_country"),
                ClaimItem("issuing_authority"),
                ClaimItem("document_number"),
                ClaimItem("portrait"),
                ClaimItem("sex"),
                ClaimItem("nationality"),
                ClaimItem("issuing_jurisdiction"),
                ClaimItem("resident_address"),
                ClaimItem("resident_country"),
                ClaimItem("resident_state"),
                ClaimItem("resident_city"),
                ClaimItem("resident_postal_code"),
                ClaimItem("age_in_years"),
                ClaimItem("age_birth_year"),
                ClaimItem("age_over_18"),
                ClaimItem("issuance_date"),
                ClaimItem("email_address"),
                ClaimItem("resident_street"),
                ClaimItem("resident_house_number"),
                ClaimItem("personal_administrative_number"),
                ClaimItem("mobile_phone_number"),
                ClaimItem("birth_family_name"),
                ClaimItem("birth_given_name"),
                ClaimItem("place_of_birth"),
                ClaimItem("trust_anchor")
            ),
            AttestationType.Mdl to listOf(
                ClaimItem("family_name"),
                ClaimItem("given_name"),
                ClaimItem("birth_date"),
                ClaimItem("expiry_date"),
                ClaimItem("issuing_country"),
                ClaimItem("issuing_authority"),
                ClaimItem("document_number"),
                ClaimItem("portrait"),
                ClaimItem("sex"),
                ClaimItem("nationality"),
                ClaimItem("issuing_jurisdiction"),
                ClaimItem("resident_address"),
                ClaimItem("resident_country"),
                ClaimItem("resident_state"),
                ClaimItem("resident_city"),
                ClaimItem("resident_postal_code"),
                ClaimItem("age_in_years"),
                ClaimItem("age_birth_year"),
                ClaimItem("age_over_18"),
                ClaimItem("driving_privileges"),
                ClaimItem("un_distinguishing_sign"),
                ClaimItem("administrative_number"),
                ClaimItem("height"),
                ClaimItem("weight"),
                ClaimItem("eye_colour"),
                ClaimItem("hair_colour"),
                ClaimItem("birth_place"),
                ClaimItem("resident_address"),
                ClaimItem("portrait_capture_date"),
                ClaimItem("biometric_template_xx"),
                ClaimItem("family_name_national_character"),
                ClaimItem("given_name_national_character"),
                ClaimItem("signature_usual_mark")
            ),
            AttestationType.AgeVerification to listOf(
                ClaimItem("age_over_18"),
                ClaimItem("issuance_date"),
                ClaimItem("user_pseudonym"),
                ClaimItem("expiry_date"),
                ClaimItem("issuing_authority"),
                ClaimItem("issuing_country"),
            )
        )
    )
}