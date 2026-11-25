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
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.model.SupportedDocuments
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.document_type_employee_id
import eudiverifier.verifierapp.generated.resources.document_type_mdl
import eudiverifier.verifierapp.generated.resources.document_type_pid
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CustomRequestInteractorTest {

    /**
     * Helper to build the SUT (System Under Test), [CustomRequestInteractorImpl], with a
     * dispatcher that shares the `runTest` scheduler. This ensures that coroutines launched
     * by the interactor run deterministically within the test's scope.
     *
     * It initializes the interactor with Mokkery mocks for its collaborators,
     * making it easy to set up different test scenarios.
     *
     * @param supportedDocuments The configuration of supported documents and their claims.
     * Defaults to an empty configuration.
     * @param resourceProvider The provider for retrieving string resources. Defaults to the
     * mock built by [stringResourceProvider].
     * @return An instance of [CustomRequestInteractorImpl] configured for testing.
     */
    private fun TestScope.createInteractor(
        supportedDocuments: SupportedDocuments = SupportedDocuments(emptyMap()),
        resourceProvider: ResourceProvider = stringResourceProvider()
    ): CustomRequestInteractor {
        // Use the dispatcher that runTest is already using
        val testDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher

        return CustomRequestInteractorImpl(
            configProvider = configProvider(supportedDocuments),
            resourceProvider = resourceProvider,
            dispatcher = testDispatcher
        )
    }

    // region getScreenTitle

    /**
     * The resource provider mock maps PID -> "PID" and the title's vararg overload
     * -> "CustomRequestTitle(PID)".
     */
    @Test
    fun `Given an interactor When the screen title for PID is requested Then it combines the title and the attestation display name`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            // When
            val result = interactor.getScreenTitle(AttestationType.Pid)

            // Then
            assertEquals("CustomRequestTitle(PID)", result)
        }

    // endregion

    // region getDocumentClaims

    @Test
    fun `Given configured claims for PID When the document claims are requested for PID Then the configured claims are returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val claimsForPid = listOf(
                ClaimItem("family_name"),
                ClaimItem("given_name")
            )
            val supported = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to claimsForPid
                )
            )
            val interactor = createInteractor(supportedDocuments = supported)

            // When
            val result = interactor.getDocumentClaims(AttestationType.Pid)

            // Then
            assertEquals(claimsForPid, result)
        }

    @Test
    fun `Given claims configured for PID only When the document claims are requested for MDL Then an empty list is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val supported = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to listOf(ClaimItem("family_name"))
                )
            )
            val interactor = createInteractor(supportedDocuments = supported)

            // When
            val result = interactor.getDocumentClaims(AttestationType.Mdl)

            // Then
            assertTrue(result.isEmpty())
        }

    // endregion

    // region transformToClaimItems

    @Test
    fun `Given checked and unchecked items and items without a checkbox When they are transformed to claim items Then only the checked item is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val checked = listItemWithCheckbox(id = "checked", isChecked = true)
            val unchecked = listItemWithCheckbox(id = "unchecked", isChecked = false)
            val noCheckbox = listItemWithoutCheckbox(id = "no_checkbox")

            val input = listOf(checked, unchecked, noCheckbox)

            // When
            val result = interactor.transformToClaimItems(input)

            // Then
            assertEquals(listOf("checked"), result.map { it.label })
        }

    @Test
    fun `Given an unchecked item When it is transformed to claim items Then an empty list is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val unchecked = listItemWithCheckbox(id = "unchecked", isChecked = false)

            // When
            val result = interactor.transformToClaimItems(listOf(unchecked))

            // Then
            assertTrue(result.isEmpty())
        }

    @Test
    fun `Given an item without a checkbox When it is transformed to claim items Then an empty list is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val noCheckbox = listItemWithoutCheckbox(id = "no_checkbox")

            // When
            val result = interactor.transformToClaimItems(listOf(noCheckbox))

            // Then
            assertTrue(result.isEmpty())
        }

    // endregion

    //region transformToUiItems

    /**
     * Each claim yields one UI item whose id is the claim label. Its main content is a text
     * node with non-empty text and its trailing content is a checked checkbox.
     */
    @Test
    fun `Given two claims When they are transformed to UI items Then each is a checked checkbox item with the claim label as id`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val claims = listOf(
                ClaimItem(label = "family_name"),
                ClaimItem(label = "age")
            )

            val interactor = createInteractor(
                supportedDocuments = SupportedDocuments(emptyMap())
            )

            // When
            val result = interactor.transformToUiItems(
                documentType = AttestationType.Pid,
                claims = claims
            )

            // Then
            assertEquals(claims.size, result.size)

            result.forEachIndexed { index, uiItem ->
                val claim = claims[index]

                assertEquals(claim.label, uiItem.itemId)

                val main = uiItem.mainContentData as ListItemMainContentDataUi.Text
                assertTrue(
                    main.text.isNotEmpty(),
                    "Expected non-empty main text for ${claim.label}"
                )

                val trailing = uiItem.trailingContentData
                assertTrue(
                    trailing is ListItemTrailingContentDataUi.Checkbox,
                    "Expected trailing checkbox for ${claim.label}"
                )
                assertTrue(trailing.checkboxData.isChecked)
            }
        }

    //endregion

    // region handleItemSelection

    /**
     * Selecting id_1 toggles its checkbox to checked, so hasSelectedItems becomes true.
     */
    @Test
    fun `Given two unchecked items When the first is selected Then it is checked and hasSelectedItems is true`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val item1 = listItemWithCheckbox(id = "id_1", isChecked = false)
            val item2 = listItemWithCheckbox(id = "id_2", isChecked = false)

            val initialItems = listOf(item1, item2)

            // When
            val result = interactor.handleItemSelection(
                items = initialItems,
                identifier = "id_1"
            ) as HandleItemSelectionPartialState.Updated

            // Then
            val updatedItem1 =
                result.items.first { it.itemId == "id_1" }.trailingContentData as
                        ListItemTrailingContentDataUi.Checkbox

            assertTrue(updatedItem1.checkboxData.isChecked)

            assertTrue(result.hasSelectedItems)
        }

    @Test
    fun `Given one checked and one unchecked item When the checked item is selected Then it is unchecked and hasSelectedItems is false`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val item1 = listItemWithCheckbox(id = "id_1", isChecked = true)
            val item2 = listItemWithCheckbox(id = "id_2", isChecked = false)

            val initialItems = listOf(item1, item2)

            // When
            val result = interactor.handleItemSelection(
                items = initialItems,
                identifier = "id_1"
            ) as HandleItemSelectionPartialState.Updated

            // Then
            val updatedItem1 =
                result.items.first { it.itemId == "id_1" }.trailingContentData as
                        ListItemTrailingContentDataUi.Checkbox

            assertFalse(updatedItem1.checkboxData.isChecked)
            assertFalse(result.hasSelectedItems)
        }

    /**
     * The id matches but the item has no checkbox, so it is returned unchanged.
     */
    @Test
    fun `Given a matched item without a checkbox When it is selected Then it is returned unchanged`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val noCheckbox = listItemWithoutCheckbox(id = "id_1")

            // When
            val result = interactor.handleItemSelection(
                items = listOf(noCheckbox),
                identifier = "id_1"
            ) as HandleItemSelectionPartialState.Updated

            // Then
            assertEquals(noCheckbox, result.items.single())
            assertFalse(result.hasSelectedItems)
        }

    // endregion

    // region helpers

    private fun listItemWithCheckbox(
        id: String,
        isChecked: Boolean
    ): ListItemDataUi {
        return ListItemDataUi(
            itemId = id,
            mainContentData = ListItemMainContentDataUi.Text(text = "Label for $id"),
            supportingText = null,
            trailingContentData = ListItemTrailingContentDataUi.Checkbox(
                checkboxData = CheckboxDataUi(isChecked = isChecked)
            )
        )
    }

    private fun listItemWithoutCheckbox(
        id: String
    ): ListItemDataUi {
        return ListItemDataUi(
            itemId = id,
            mainContentData = ListItemMainContentDataUi.Text(text = "Label for $id"),
            supportingText = null,
            trailingContentData = null
        )
    }

    // endregion

    // region Mocks

    private fun configProvider(supportedDocuments: SupportedDocuments): ConfigProvider {
        val configProvider = mock<ConfigProvider>()
        every { configProvider.supportedDocuments } returns supportedDocuments
        return configProvider
    }

    /**
     * Mocks the [ResourceProvider]. The single-argument overload maps the document-type labels and
     * returns a non-empty fallback for the dynamic claim translations resolved by `UiTransformer`.
     * The vararg overload backs `getScreenTitle`, whose only test exercises the PID document.
     */
    private fun stringResourceProvider(): ResourceProvider {
        val resourceProvider = mock<ResourceProvider>()
        every { resourceProvider.getSharedString(any()) } calls { (resource: StringResource) ->
            when (resource) {
                Res.string.document_type_pid -> "PID"
                Res.string.document_type_mdl -> "MDL"
                Res.string.document_type_employee_id -> "EMPLOYEE_ID"
                else -> "translated"
            }
        }
        every {
            resourceProvider.getSharedString(any(), *any())
        } returns "CustomRequestTitle(PID)"
        return resourceProvider
    }

    // endregion
}