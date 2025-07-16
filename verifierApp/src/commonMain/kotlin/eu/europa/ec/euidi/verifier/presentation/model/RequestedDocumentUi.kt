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

import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument.AttestationType
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelable
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelize

@CommonParcelize
data class RequestedDocsHolder(
    val items: List<RequestedDocumentUi>
) : CommonParcelable

@CommonParcelize
data class RequestedDocumentUi(
    val id: String,
    val documentType: AttestationType,
    val mode: SupportedDocument.Mode,
    val format: SupportedDocument.DocumentFormat? = null,
    val claims: List<SelectableClaimUi> = emptyList()
) : CommonParcelable

@CommonParcelize
data class SelectableClaimUi(
    val claim: ClaimUi,
    val isSelected: Boolean
) : CommonParcelable {
    companion object {
        fun forType(type: AttestationType): List<SelectableClaimUi> {
            return when (type) {
                AttestationType.PID -> forPid()
                AttestationType.MDL -> forMdl()
                AttestationType.AGE_VERIFICATION -> emptyList()
            }
        }

        fun forPid(): List<SelectableClaimUi> =
            ClaimUi.pidClaims.map { SelectableClaimUi(it, true) }

        fun forMdl(): List<SelectableClaimUi> =
            ClaimUi.mdlClaims.map { SelectableClaimUi(it, true) }
    }
}