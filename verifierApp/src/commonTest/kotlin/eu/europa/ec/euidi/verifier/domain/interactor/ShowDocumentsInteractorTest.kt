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

import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.model.DocumentValidityUi
import eu.europa.ec.euidi.verifier.testutil.sequentialUuidProvider
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.document_type_pid
import eudiverifier.verifierapp.generated.resources.show_documents_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShowDocumentsInteractorTest {

    /**
     * Builds the SUT, [ShowDocumentsInteractorImpl], with a dispatcher that shares the `runTest`
     * scheduler and Mokkery mocks for its collaborators.
     */
    private fun TestScope.createInteractor(
        resourceProvider: ResourceProvider = stringResourceProvider(),
        uuidProvider: UuidProvider = sequentialUuidProvider(),
    ): ShowDocumentsInteractor {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        return ShowDocumentsInteractorImpl(
            resourceProvider = resourceProvider,
            uuidProvider = uuidProvider,
            dispatcher = dispatcher
        )
    }

    //region getScreenTitle

    @Test
    fun `getScreenTitle returns localized title`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertEquals("Show documents title", interactor.getScreenTitle())
    }

    //endregion

    //region transformToUiItems

    @Test
    fun `transformToUiItems returns empty list for empty input`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            assertTrue(interactor.transformToUiItems(emptyList()).isEmpty())
        }

    @Test
    fun `transformToUiItems maps a received document to a DocumentUi`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val document = ReceivedDocumentUi(
                id = "doc-1",
                documentType = AttestationType.Pid,
                claims = mapOf(ClaimItem("given_name") to "John"),
                documentValidity = DocumentValidityUi(
                    isDeviceSignatureValid = true,
                    isIssuerSignatureValid = false,
                    isDataIntegrityIntact = true,
                    signed = "2025-01-01",
                    validFrom = "2025-01-02",
                    validUntil = "2025-12-31",
                )
            )

            val result = interactor.transformToUiItems(listOf(document))

            assertEquals(1, result.size)
            val documentUi = result.single()

            assertEquals("doc-1", documentUi.id)
            assertEquals(AttestationType.Pid.docType, documentUi.docType)

            // One UI claim per claim entry, with an id provided by the uuid provider.
            assertEquals(1, documentUi.uiClaims.size)
            assertEquals("uuid-0", documentUi.uiClaims.single().itemId)
            val claimMain = documentUi.uiClaims.single().mainContentData
            assertTrue(claimMain is ListItemMainContentDataUi.Text)
            assertEquals("John", claimMain.text)

            // 3 boolean validity items + 3 non-null string validity items.
            assertEquals(6, documentUi.validityInfo.size)
        }

    //endregion

    //region Mocks

    /**
     * The interactor resolves claim translations dynamically via `UiTransformer.getClaimTranslation`,
     * which may request arbitrary string resources. A `calls`-based answer maps the resources we
     * assert on and returns a non-empty fallback for the rest.
     */
    private fun stringResourceProvider(): ResourceProvider = mock {
        every { getSharedString(any()) } calls { (resource: StringResource) ->
            when (resource) {
                Res.string.show_documents_screen_title -> "Show documents title"
                Res.string.document_type_pid -> "PID"
                else -> "translated"
            }
        }
    }

    //endregion
}