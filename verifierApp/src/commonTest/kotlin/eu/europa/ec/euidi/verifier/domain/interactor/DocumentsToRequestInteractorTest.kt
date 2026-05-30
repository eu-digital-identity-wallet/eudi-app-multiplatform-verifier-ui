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

package eu.europa.ec.euidi.verifier.domain.interactor

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.domain.config.model.SupportedDocuments
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.testutil.TestData
import eu.europa.ec.euidi.verifier.testutil.documentTypeResourceProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentsToRequestInteractorTest {

    /**
     * Creates an instance of [DocumentsToRequestInteractorImpl] for testing purposes.
     * This is a helper function that sets up the interactor with fake dependencies.
     *
     * @param supportedDocuments The configuration of supported documents to be used by the fake
     * [ConfigProvider]. Defaults to an empty configuration.
     * @return An instance of [DocumentsToRequestInteractorImpl] configured for the test.
     */
    private fun TestScope.createInteractor(
        supportedDocuments: SupportedDocuments = SupportedDocuments(emptyMap())
    ): DocumentsToRequestInteractor {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        return DocumentsToRequestInteractorImpl(
            configProvider = configProvider(supportedDocuments),
            resourceProvider = documentTypeResourceProvider(),
            dispatcher = dispatcher
        )
    }

    // region getSupportedDocuments

    @Test
    fun `getSupportedDocuments maps config to ui model with modes`() =
        runTest(StandardTestDispatcher()) {
            val supportedDocs = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to listOf(TestData.familyNameClaim),
                    AttestationType.Mdl to listOf(TestData.givenNameClaim)
                )
            )

            val interactor = createInteractor(supportedDocs)

            val result = interactor.getSupportedDocuments()

            // We expect one SupportedDocumentUi per attestation type
            assertEquals(2, result.size)

            val pidDoc = result.first { it.documentType == AttestationType.Pid }
            val mdlDoc = result.first { it.documentType == AttestationType.Mdl }

            // id comes from getDisplayName; we don't assert exact value, only that it is not blank
            assertTrue(pidDoc.id.isNotBlank())
            assertTrue(mdlDoc.id.isNotBlank())

            // Modes come from ConfigProvider.getDocumentModes
            assertEquals(listOf(DocumentMode.FULL, DocumentMode.CUSTOM), pidDoc.modes)
            assertEquals(listOf(DocumentMode.FULL), mdlDoc.modes)
        }

    // endregion

    // region getDocumentClaims

    @Test
    fun `getDocumentClaims returns configured claims for attestation type`() =
        runTest(StandardTestDispatcher()) {
            val pidClaims = TestData.pidClaims
            val supportedDocs = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to pidClaims
                )
            )

            val interactor = createInteractor(supportedDocs)

            val result = interactor.getDocumentClaims(AttestationType.Pid)

            assertEquals(pidClaims, result)
        }

    @Test
    fun `getDocumentClaims returns empty list when attestation not configured`() =
        runTest(StandardTestDispatcher()) {
            val supportedDocs = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to listOf(TestData.familyNameClaim)
                )
            )

            val interactor = createInteractor(supportedDocs)

            val result = interactor.getDocumentClaims(AttestationType.Mdl)

            assertTrue(result.isEmpty())
        }

    // endregion

    // region handleDocumentOptionSelection

    @Test
    fun `handleDocumentOptionSelection removes document when already selected with same mode`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()
            val currentDocs = listOf(
                TestData.pidFullRequestedDocument
            )

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            assertTrue(result.docs.isEmpty())
        }

    @Test
    fun `handleDocumentOptionSelection custom mode removes full and navigates to custom`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()
            val currentDocs = listOf(
                TestData.pidFullRequestedDocument
            )

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.CUSTOM
            ) as DocSelectionResult.NavigateToCustomRequest

            // FULL should be removed from docs list
            assertTrue(result.docs.isEmpty())

            // customDoc should be created with empty claims and CUSTOM mode
            assertEquals("PID_DOC", result.customDoc.id)
            assertEquals(AttestationType.Pid, result.customDoc.documentType)
            assertEquals(DocumentMode.CUSTOM, result.customDoc.mode)
            assertTrue(result.customDoc.claims.isEmpty())
        }

    @Test
    fun `handleDocumentOptionSelection custom mode navigates without removing when full not present`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()
            val currentDocs = emptyList<RequestedDocumentUi>()

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.CUSTOM
            ) as DocSelectionResult.NavigateToCustomRequest

            assertTrue(result.docs.isEmpty())
            assertEquals(DocumentMode.CUSTOM, result.customDoc.mode)
        }

    @Test
    fun `handleDocumentOptionSelection full mode adds new doc with all claims when none selected`() =
        runTest(StandardTestDispatcher()) {
            val pidClaims = TestData.pidClaims
            val supportedDocs = SupportedDocuments(
                documents = mapOf(AttestationType.Pid to pidClaims)
            )

            val interactor = createInteractor(supportedDocs)
            val currentDocs = emptyList<RequestedDocumentUi>()

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            assertEquals(1, result.docs.size)
            val doc = result.docs.single()
            assertEquals(DocumentMode.FULL, doc.mode)
            assertEquals(AttestationType.Pid, doc.documentType)
            assertEquals(pidClaims, doc.claims)
        }

    @Test
    fun `handleDocumentOptionSelection full mode replaces existing custom doc for same type`() =
        runTest(StandardTestDispatcher()) {
            val pidClaims = TestData.pidClaims
            val supportedDocs = SupportedDocuments(
                documents = mapOf(AttestationType.Pid to pidClaims)
            )

            val interactor = createInteractor(supportedDocs)

            val currentDocs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.CUSTOM,
                    claims = listOf(TestData.familyNameClaim) // subset
                )
            )

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // Custom doc should be replaced with a FULL doc with all claims
            assertEquals(1, result.docs.size)
            val doc = result.docs.single()
            assertEquals(DocumentMode.FULL, doc.mode)
            assertEquals(pidClaims, doc.claims)
        }

    @Test
    fun `handleDocumentOptionSelection full mode keeps other document types`() =
        runTest(StandardTestDispatcher()) {
            val pidClaims = listOf(TestData.familyNameClaim)
            val supportedDocs = SupportedDocuments(
                documents = mapOf(AttestationType.Pid to pidClaims)
            )

            val interactor = createInteractor(supportedDocs)

            val currentDocs = listOf(
                TestData.mdlFullRequestedDocument
            )

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // We should still have the MDL doc plus the new PID FULL doc
            assertEquals(2, result.docs.size)
            assertTrue(result.docs.any { it.documentType == AttestationType.Mdl })
            assertTrue(result.docs.any { it.documentType == AttestationType.Pid })
        }

    @Test
    fun `handleDocumentOptionSelection removes only the matching type and mode when already selected`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()
            val currentDocs = listOf(
                TestData.pidFullRequestedDocument,
                RequestedDocumentUi(
                    id = "PID_CUSTOM",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.CUSTOM,
                    claims = emptyList()
                ),
                TestData.mdlFullRequestedDocument
            )

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // Only the PID/FULL doc is removed; the PID/CUSTOM and MDL/FULL docs remain.
            assertEquals(2, result.docs.size)
            assertTrue(
                result.docs.any {
                    it.documentType == AttestationType.Pid && it.mode == DocumentMode.CUSTOM
                }
            )
            assertTrue(result.docs.any { it.documentType == AttestationType.Mdl })
        }

    @Test
    fun `handleDocumentOptionSelection custom mode keeps unrelated docs when navigating`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()
            val currentDocs = listOf(
                TestData.mdlFullRequestedDocument
            )

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.CUSTOM
            ) as DocSelectionResult.NavigateToCustomRequest

            // No doc shares the id, so nothing is removed before navigating.
            assertEquals(currentDocs, result.docs)
            assertEquals(DocumentMode.CUSTOM, result.customDoc.mode)
        }

    @Test
    fun `handleDocumentOptionSelection full mode appends when same type and mode exist under another id`() =
        runTest(StandardTestDispatcher()) {
            val pidClaims = listOf(TestData.familyNameClaim)
            val supportedDocs = SupportedDocuments(
                documents = mapOf(AttestationType.Pid to pidClaims)
            )
            val interactor = createInteractor(supportedDocs)

            val currentDocs = listOf(
                RequestedDocumentUi(
                    id = "PID_OTHER",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.FULL,
                    claims = pidClaims
                )
            )

            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // No existing PID doc has a *different* mode, so none are filtered out; the new doc is appended.
            assertEquals(2, result.docs.size)
            assertTrue(
                result.docs.all {
                    it.documentType == AttestationType.Pid && it.mode == DocumentMode.FULL
                }
            )
        }

    // endregion

    // region searchDocuments

    @Test
    fun `searchDocuments filters by document display name`() =
        runTest(StandardTestDispatcher()) {
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()

            val result = interactor.searchDocuments(
                query = "PID",
                documents = docs
            ).first()

            assertEquals(1, result.size)
            assertEquals(AttestationType.Pid, result.single().documentType)
        }

    @Test
    fun `searchDocuments ignores case sensitivity when searching by display name`() =
        runTest(StandardTestDispatcher()) {
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()
            val result = interactor.searchDocuments(
                query = "pid",
                documents = docs
            ).first()

            assertEquals(1, result.size)
            assertEquals(AttestationType.Pid, result.single().documentType)
        }

    @Test
    fun `searchDocuments filters by document mode display name`() =
        runTest(StandardTestDispatcher()) {
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()

            val result = interactor.searchDocuments(
                query = "Custom",
                documents = docs
            ).first()

            assertEquals(1, result.size)
            assertEquals(AttestationType.Mdl, result.single().documentType)
        }

    @Test
    fun `searchDocuments filters by document mode display name ignoring case sensitivity`() =
        runTest(StandardTestDispatcher()) {
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()
            val result = interactor.searchDocuments(
                query = "custom",
                documents = docs
            ).first()

            assertEquals(1, result.size)
            assertEquals(AttestationType.Mdl, result.single().documentType)
        }

    @Test
    fun `searchDocuments returns empty list when no matches`() =
        runTest(StandardTestDispatcher()) {
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()
            val result = interactor.searchDocuments(
                query = "UNKNOWN",
                documents = docs
            ).first()

            assertTrue(result.isEmpty())
        }

    // endregion

    // region checkDocumentMode

    @Test
    fun `checkDocumentMode sets mode to FULL when all claims are selected`() =
        runTest(StandardTestDispatcher()) {
            val pidClaims = TestData.pidClaims
            val supportedDocs = SupportedDocuments(
                documents = mapOf(AttestationType.Pid to pidClaims)
            )

            val interactor = createInteractor(supportedDocs)

            val requestedDocs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.CUSTOM,
                    claims = pidClaims // all claims
                )
            )

            val result = interactor.checkDocumentMode(requestedDocs)

            assertEquals(1, result.size)
            val updated = result.single()
            assertEquals(DocumentMode.FULL, updated.mode)
        }

    @Test
    fun `checkDocumentMode keeps mode when not all claims are selected`() =
        runTest(StandardTestDispatcher()) {
            val pidClaims = TestData.pidClaims
            val supportedDocs = SupportedDocuments(
                documents = mapOf(AttestationType.Pid to pidClaims)
            )

            val interactor = createInteractor(supportedDocs)

            val requestedDocs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.CUSTOM,
                    claims = listOf(TestData.familyNameClaim) // partial
                )
            )

            val result = interactor.checkDocumentMode(requestedDocs)

            val updated = result.single()
            assertEquals(DocumentMode.CUSTOM, updated.mode)
        }

    @Test
    fun `checkDocumentMode keeps mode when document type is not configured`() =
        runTest(StandardTestDispatcher()) {
            // Empty supported documents -> expected claim count resolves to 0 via the elvis fallback,
            // so a doc with claims never matches and the mode is kept.
            val interactor = createInteractor()

            val requestedDocs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.CUSTOM,
                    claims = listOf(TestData.familyNameClaim)
                )
            )

            val result = interactor.checkDocumentMode(requestedDocs)

            assertEquals(DocumentMode.CUSTOM, result.single().mode)
        }

    // endregion

    // region Mocks

    private fun configProvider(supportedDocuments: SupportedDocuments): ConfigProvider {
        val configProvider = mock<ConfigProvider>()
        every { configProvider.supportedDocuments } returns supportedDocuments
        every {
            configProvider.getDocumentModes(AttestationType.Pid)
        } returns listOf(DocumentMode.FULL, DocumentMode.CUSTOM)
        every {
            configProvider.getDocumentModes(AttestationType.Mdl)
        } returns listOf(DocumentMode.FULL)
        every {
            configProvider.getDocumentModes(AttestationType.EmployeeId)
        } returns emptyList()
        return configProvider
    }

    // endregion
}