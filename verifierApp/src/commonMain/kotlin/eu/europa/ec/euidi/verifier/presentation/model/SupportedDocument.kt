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

package eu.europa.ec.euidi.verifier.presentation.model

data class SupportedDocument(
    val id: String,
    val documentType: AttestationType,
    val modes: List<Mode> = listOf(Mode.FULL, Mode.CUSTOM),
    val formats: List<DocumentFormat> = listOf(DocumentFormat.MsoMdocFormat, DocumentFormat.SdJwtVcFormat)
) {
    enum class Mode(val displayName: String) {
        FULL(displayName = "Full"),
        CUSTOM(displayName = "Custom")
    }

    enum class AttestationType(val displayName: String) {
        PID("PID"),
        MDL("Mdl"),
        AGE_VERIFICATION("Age Verification")
    }

    enum class DocumentFormat(val displayName: String) {
        MsoMdocFormat("MsoMdoc"),
        SdJwtVcFormat("SdJwt")
    }
}