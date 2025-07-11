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

import eu.europa.ec.euidi.verifier.utils.CommonParcelable
import eu.europa.ec.euidi.verifier.utils.CommonParcelize

typealias ClaimKey = String
typealias ClaimValue = String

@CommonParcelize
data class ReceivedDocsHolder(
    val items: List<ReceivedDocumentUi>
) : CommonParcelable

@CommonParcelize
data class ReceivedDocumentUi(
    val id: String,
    val documentType: DocumentType,
    val claims: Map<ClaimKey, ClaimValue> = emptyMap()
) : CommonParcelable

enum class DocumentType(val displayName: String) {
    PID("PID"),
    MDL("org.iso.18013.5.1.mDL"),
    AGE_VERIFICATION("Age Verification")
}