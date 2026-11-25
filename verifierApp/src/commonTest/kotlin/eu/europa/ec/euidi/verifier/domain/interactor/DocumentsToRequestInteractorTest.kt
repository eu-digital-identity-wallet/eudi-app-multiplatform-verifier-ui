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

    /**
     * One SupportedDocumentUi is expected per attestation type. Its id comes from
     * getDisplayName and is only checked to be non-blank. The modes come from
     * ConfigProvider.getDocumentModes.
     */
    @Test
    fun `Given supported documents for PID and MDL When they are requested Then one UI model per type with its modes is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val supportedDocs = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to listOf(TestData.familyNameClaim),
                    AttestationType.Mdl to listOf(TestData.givenNameClaim)
                )
            )

            val interactor = createInteractor(supportedDocs)

            // When
            val result = interactor.getSupportedDocuments()

            // Then
            assertEquals(2, result.size)

            val pidDoc = result.first { it.documentType == AttestationType.Pid }
            val mdlDoc = result.first { it.documentType == AttestationType.Mdl }

            assertTrue(pidDoc.id.isNotBlank())
            assertTrue(mdlDoc.id.isNotBlank())

            assertEquals(listOf(DocumentMode.FULL, DocumentMode.CUSTOM), pidDoc.modes)
            assertEquals(listOf(DocumentMode.FULL), mdlDoc.modes)
        }

    // endregion

    // region getDocumentClaims

    @Test
    fun `Given configured claims for PID When the document claims are requested for PID Then the configured claims are returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val pidClaims = TestData.pidClaims
            val supportedDocs = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to pidClaims
                )
            )

            val interactor = createInteractor(supportedDocs)

            // When
            val result = interactor.getDocumentClaims(AttestationType.Pid)

            // Then
            assertEquals(pidClaims, result)
        }

    @Test
    fun `Given claims configured for PID only When the document claims are requested for MDL Then an empty list is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val supportedDocs = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to listOf(TestData.familyNameClaim)
                )
            )

            val interactor = createInteractor(supportedDocs)

            // When
            val result = interactor.getDocumentClaims(AttestationType.Mdl)

            // Then
            assertTrue(result.isEmpty())
        }

    // endregion

    // region handleDocumentOptionSelection

    @Test
    fun `Given a selected FULL PID document When the FULL PID option is selected Then the document is removed`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()
            val currentDocs = listOf(
                TestData.pidFullRequestedDocument
            )

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // Then
            assertTrue(result.docs.isEmpty())
        }

    /**
     * The FULL document is removed from the list. The custom document is created with empty
     * claims and the CUSTOM mode.
     */
    @Test
    fun `Given a selected FULL PID document When the CUSTOM PID option is selected Then the FULL document is removed and a custom request is opened`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()
            val currentDocs = listOf(
                TestData.pidFullRequestedDocument
            )

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.CUSTOM
            ) as DocSelectionResult.NavigateToCustomRequest

            // Then
            assertTrue(result.docs.isEmpty())

            assertEquals("PID_DOC", result.customDoc.id)
            assertEquals(AttestationType.Pid, result.customDoc.documentType)
            assertEquals(DocumentMode.CUSTOM, result.customDoc.mode)
            assertTrue(result.customDoc.claims.isEmpty())
        }

    @Test
    fun `Given no selected documents When the CUSTOM PID option is selected Then it navigates to the custom request without removing anything`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()
            val currentDocs = emptyList<RequestedDocumentUi>()

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.CUSTOM
            ) as DocSelectionResult.NavigateToCustomRequest

            // Then
            assertTrue(result.docs.isEmpty())
            assertEquals(DocumentMode.CUSTOM, result.customDoc.mode)
        }

    @Test
    fun `Given PID claims and no selected documents When the FULL PID option is selected Then a FULL document with all claims is added`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val pidClaims = TestData.pidClaims
            val supportedDocs = SupportedDocuments(
                documents = mapOf(AttestationType.Pid to pidClaims)
            )

            val interactor = createInteractor(supportedDocs)
            val currentDocs = emptyList<RequestedDocumentUi>()

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // Then
            assertEquals(1, result.docs.size)
            val doc = result.docs.single()
            assertEquals(DocumentMode.FULL, doc.mode)
            assertEquals(AttestationType.Pid, doc.documentType)
            assertEquals(pidClaims, doc.claims)
        }

    /**
     * The selected CUSTOM document holds only a subset of the claims. It is replaced by a FULL
     * document with all claims.
     */
    @Test
    fun `Given a selected CUSTOM PID document When the FULL PID option is selected Then it is replaced by a FULL document with all claims`() =
        runTest(StandardTestDispatcher()) {
            // Given
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
                    claims = listOf(TestData.familyNameClaim)
                )
            )

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // Then
            assertEquals(1, result.docs.size)
            val doc = result.docs.single()
            assertEquals(DocumentMode.FULL, doc.mode)
            assertEquals(pidClaims, doc.claims)
        }

    /**
     * We should still have the MDL doc plus the new PID FULL doc
     */
    @Test
    fun `Given a selected MDL document When the FULL PID option is selected Then the MDL document is kept and the PID document is added`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val pidClaims = listOf(TestData.familyNameClaim)
            val supportedDocs = SupportedDocuments(
                documents = mapOf(AttestationType.Pid to pidClaims)
            )

            val interactor = createInteractor(supportedDocs)

            val currentDocs = listOf(
                TestData.mdlFullRequestedDocument
            )

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // Then
            assertEquals(2, result.docs.size)
            assertTrue(result.docs.any { it.documentType == AttestationType.Mdl })
            assertTrue(result.docs.any { it.documentType == AttestationType.Pid })
        }

    /**
     * Only the PID/FULL doc is removed; the PID/CUSTOM and MDL/FULL docs remain.
     */
    @Test
    fun `Given PID FULL and PID CUSTOM documents and an MDL FULL document When the FULL PID option is selected Then only the PID FULL document is removed`() =
        runTest(StandardTestDispatcher()) {
            // Given
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

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // Then
            assertEquals(2, result.docs.size)
            assertTrue(
                result.docs.any {
                    it.documentType == AttestationType.Pid && it.mode == DocumentMode.CUSTOM
                }
            )
            assertTrue(result.docs.any { it.documentType == AttestationType.Mdl })
        }

    /**
     * No doc shares the id, so nothing is removed before navigating.
     */
    @Test
    fun `Given a selected MDL document When the CUSTOM PID option is selected Then the MDL document is kept and it navigates to the custom request`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()
            val currentDocs = listOf(
                TestData.mdlFullRequestedDocument
            )

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.CUSTOM
            ) as DocSelectionResult.NavigateToCustomRequest

            // Then
            assertEquals(currentDocs, result.docs)
            assertEquals(DocumentMode.CUSTOM, result.customDoc.mode)
        }

    /**
     * No existing PID doc has a *different* mode, so none are filtered out; the new doc is
     * appended.
     */
    @Test
    fun `Given a FULL PID document under another id When the FULL PID option is selected Then the new document is appended`() =
        runTest(StandardTestDispatcher()) {
            // Given
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

            // When
            val result = interactor.handleDocumentOptionSelection(
                currentDocs = currentDocs,
                docId = "PID_DOC",
                docType = AttestationType.Pid,
                mode = DocumentMode.FULL
            ) as DocSelectionResult.Updated

            // Then
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
    fun `Given searchable documents When searching for PID Then only the PID document is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()

            // When
            val result = interactor.searchDocuments(
                query = "PID",
                documents = docs
            ).first()

            // Then
            assertEquals(1, result.size)
            assertEquals(AttestationType.Pid, result.single().documentType)
        }

    @Test
    fun `Given searchable documents When searching for pid in lower case Then only the PID document is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()

            // When
            val result = interactor.searchDocuments(
                query = "pid",
                documents = docs
            ).first()

            // Then
            assertEquals(1, result.size)
            assertEquals(AttestationType.Pid, result.single().documentType)
        }

    @Test
    fun `Given searchable documents When searching for a mode display name Then only the document with that mode is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()

            // When
            val result = interactor.searchDocuments(
                query = "Custom",
                documents = docs
            ).first()

            // Then
            assertEquals(1, result.size)
            assertEquals(AttestationType.Mdl, result.single().documentType)
        }

    @Test
    fun `Given searchable documents When searching for a mode display name in lower case Then only the document with that mode is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()

            // When
            val result = interactor.searchDocuments(
                query = "custom",
                documents = docs
            ).first()

            // Then
            assertEquals(1, result.size)
            assertEquals(AttestationType.Mdl, result.single().documentType)
        }

    @Test
    fun `Given searchable documents When searching for an unknown term Then an empty list is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val docs = TestData.searchableSupportedDocuments

            val interactor = createInteractor()

            // When
            val result = interactor.searchDocuments(
                query = "UNKNOWN",
                documents = docs
            ).first()

            // Then
            assertTrue(result.isEmpty())
        }

    // endregion

    // region checkDocumentMode

    /**
     * All claims are selected.
     */
    @Test
    fun `Given a CUSTOM document with all claims selected When the document mode is checked Then the mode becomes FULL`() =
        runTest(StandardTestDispatcher()) {
            // Given
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
                    claims = pidClaims
                )
            )

            // When
            val result = interactor.checkDocumentMode(requestedDocs)

            // Then
            assertEquals(1, result.size)
            val updated = result.single()
            assertEquals(DocumentMode.FULL, updated.mode)
        }

    /**
     * Only some of the claims are selected (partial).
     */
    @Test
    fun `Given a CUSTOM document with some claims selected When the document mode is checked Then the mode is kept`() =
        runTest(StandardTestDispatcher()) {
            // Given
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
                    claims = listOf(TestData.familyNameClaim)
                )
            )

            // When
            val result = interactor.checkDocumentMode(requestedDocs)

            // Then
            val updated = result.single()
            assertEquals(DocumentMode.CUSTOM, updated.mode)
        }

    /**
     * Empty supported documents -> expected claim count resolves to 0 via the elvis fallback,
     * so a doc with claims never matches and the mode is kept.
     */
    @Test
    fun `Given a document type that is not configured When the document mode is checked Then the mode is kept`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val requestedDocs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.CUSTOM,
                    claims = listOf(TestData.familyNameClaim)
                )
            )

            // When
            val result = interactor.checkDocumentMode(requestedDocs)

            // Then
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