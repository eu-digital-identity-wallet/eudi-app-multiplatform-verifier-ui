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

package eu.europa.ec.euidi.verifier.testutil

import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.domain.model.SupportedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.model.DocumentValidityUi
import eu.europa.ec.euidi.verifier.testutil.TestData.ageClaim
import eu.europa.ec.euidi.verifier.testutil.TestData.familyNameClaim

/**
 * Reusable, immutable test fixtures shared across the common tests.
 *
 * Only values that are duplicated across multiple test files live here. Test-specific variations
 * (e.g. documents with particular claim subsets) are intentionally kept inline in their tests so
 * the assertions stay self-explanatory.
 *
 * Note: these are immutable data-class instances and are safe to share. Mokkery mock instances are
 * NOT shared here because they record interactions for verification — share mock *factories*
 * (see [eu.europa.ec.euidi.verifier.testutil.sequentialUuidProvider]) instead.
 */
object TestData {

    // region Claims
    val familyNameClaim = ClaimItem(label = "family_name")
    val givenNameClaim = ClaimItem(label = "given_name")
    val ageClaim = ClaimItem(label = "age")

    /** The PID claim subset ([familyNameClaim] + [ageClaim]) used by several document tests. */
    val pidClaims = listOf(familyNameClaim, ageClaim)
    // endregion

    // region Requested documents
    val pidFullRequestedDocument = RequestedDocumentUi(
        id = "PID_DOC",
        documentType = AttestationType.Pid,
        mode = DocumentMode.FULL,
    )

    val pidCustomRequestedDocument = RequestedDocumentUi(
        id = "PID_DOC",
        documentType = AttestationType.Pid,
        mode = DocumentMode.CUSTOM,
    )

    val mdlFullRequestedDocument = RequestedDocumentUi(
        id = "MDL_DOC",
        documentType = AttestationType.Mdl,
        mode = DocumentMode.FULL,
    )

    val mdlCustomRequestedDocument = RequestedDocumentUi(
        id = "MDL_DOC",
        documentType = AttestationType.Mdl,
        mode = DocumentMode.CUSTOM,
    )
    // endregion

    // region Validity
    val validDocumentValidityUi = DocumentValidityUi(
        isDeviceSignatureValid = true,
        isIssuerSignatureValid = true,
        isDataIntegrityIntact = true,
        signed = null,
        validFrom = null,
        validUntil = null,
    )
    // endregion

    // region Supported documents
    val pidSupportedDocument = SupportedDocumentUi(
        id = "PID",
        documentType = AttestationType.Pid,
        modes = listOf(DocumentMode.FULL),
    )

    val mdlCustomSupportedDocument = SupportedDocumentUi(
        id = "MDL",
        documentType = AttestationType.Mdl,
        modes = listOf(DocumentMode.CUSTOM),
    )

    /** The PID (full) + MDL (custom) pair used by the search tests. */
    val searchableSupportedDocuments = listOf(pidSupportedDocument, mdlCustomSupportedDocument)
    // endregion

    // region Received documents
    val pidReceivedDocument = ReceivedDocumentUi(
        id = "doc-1",
        documentType = AttestationType.Pid,
        documentValidity = validDocumentValidityUi,
    )
    // endregion
}
