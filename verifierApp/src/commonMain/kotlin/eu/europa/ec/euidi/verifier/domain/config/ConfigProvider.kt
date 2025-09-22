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

import eu.europa.ec.euidi.verifier.core.controller.PlatformController
import eu.europa.ec.euidi.verifier.core.controller.model.BuildType
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.domain.config.model.Logger
import eu.europa.ec.euidi.verifier.domain.config.model.SupportedDocuments
import eudiverifier.verifierapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

interface ConfigProvider {
    val supportedDocuments: SupportedDocuments
    val logger: Logger
    fun getDocumentModes(attestationType: AttestationType): List<DocumentMode>
    suspend fun getCertificates(): List<String>
}

class ConfigProviderImpl(private val platformController: PlatformController) : ConfigProvider {
    override fun getDocumentModes(attestationType: AttestationType): List<DocumentMode> {
        return when (attestationType) {
            AttestationType.Pid -> listOf(DocumentMode.FULL, DocumentMode.CUSTOM)
            AttestationType.Mdl -> listOf(DocumentMode.FULL, DocumentMode.CUSTOM)
            AttestationType.AgeVerification -> listOf(DocumentMode.FULL, DocumentMode.CUSTOM)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getCertificates(): List<String> = listOf(
        Res.readBytes("files/certs/pidissuerca02_cz.pem").decodeToString(),
        Res.readBytes("files/certs/pidissuerca02_ee.pem").decodeToString(),
        Res.readBytes("files/certs/pidissuerca02_eu.pem").decodeToString(),
        Res.readBytes("files/certs/pidissuerca02_lu.pem").decodeToString(),
        Res.readBytes("files/certs/pidissuerca02_nl.pem").decodeToString(),
        Res.readBytes("files/certs/pidissuerca02_pt.pem").decodeToString(),
        Res.readBytes("files/certs/pidissuerca02_ut.pem").decodeToString()
    )

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
                ClaimItem("family_name_birth"),
                ClaimItem("given_name_birth"),
                ClaimItem("place_of_birth"),
                ClaimItem("trust_anchor")
            ),
            AttestationType.Mdl to listOf(
                ClaimItem("family_name"),
                ClaimItem("given_name"),
                ClaimItem("birth_date"),
                ClaimItem("expiry_date"),
                ClaimItem("issue_date"),
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

    override val logger: Logger
        get() = when (platformController.buildType) {
            BuildType.DEBUG -> Logger.LEVEL_DEBUG
            BuildType.RELEASE -> Logger.OFF
        }
}