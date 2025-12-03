/*
 * Copyright (c) 2025 European Commission
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
import eudiverifier.verifierapp.generated.resources.custom_request_screen_title
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
     * It initializes the interactor with optional fake configurations and a resource provider,
     * making it easy to set up different test scenarios.
     *
     * @param supportedDocuments The configuration of supported documents and their claims.
     * Defaults to an empty configuration.
     * @param resourceProvider The provider for retrieving string resources. Defaults to a
     * [FakeResourceProvider].
     * @return An instance of [CustomRequestInteractorImpl] configured for testing.
     */
    private fun TestScope.createInteractor(
        supportedDocuments: SupportedDocuments = SupportedDocuments(emptyMap()),
        resourceProvider: ResourceProvider = FakeResourceProvider
    ): CustomRequestInteractorImpl {
        // Use the dispatcher that runTest is already using
        val testDispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher

        return CustomRequestInteractorImpl(
            configProvider = FakeConfigProvider(supportedDocuments),
            resourceProvider = resourceProvider,
            dispatcher = testDispatcher
        )
    }

    // region getScreenTitle

    @Test
    fun `getScreenTitle combines screen title and attestation display name`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val result = interactor.getScreenTitle(AttestationType.Pid)

            // FakeResourceProvider is defined to:
            // - map PID -> "PID"
            // - map custom_request_screen_title -> "CustomRequestTitle(label)"
            assertEquals("CustomRequestTitle(PID)", result)
        }

    // endregion

    // region getDocumentClaims

    @Test
    fun `getDocumentClaims returns configured claims for attestation type`() =
        runTest(StandardTestDispatcher()) {
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

            val result = interactor.getDocumentClaims(AttestationType.Pid)

            assertEquals(claimsForPid, result)
        }

    @Test
    fun `getDocumentClaims returns empty list when attestation not configured`() =
        runTest(StandardTestDispatcher()) {
            val supported = SupportedDocuments(
                documents = mapOf(
                    AttestationType.Pid to listOf(ClaimItem("family_name"))
                )
            )
            val interactor = createInteractor(supportedDocuments = supported)

            val result = interactor.getDocumentClaims(AttestationType.Mdl)

            assertTrue(result.isEmpty())
        }

    // endregion

    // region transformToClaimItems

    @Test
    fun `transformToClaimItems returns only items with checked checkbox`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val checked = listItemWithCheckbox(id = "checked", isChecked = true)
            val unchecked = listItemWithCheckbox(id = "unchecked", isChecked = false)
            val noCheckbox = listItemWithoutCheckbox(id = "no_checkbox")

            val input = listOf(checked, unchecked, noCheckbox)

            val result = interactor.transformToClaimItems(input)

            assertEquals(listOf("checked"), result.map { it.label })
        }

    @Test
    fun `transformToClaimItems excludes items with unchecked checkbox`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val unchecked = listItemWithCheckbox(id = "unchecked", isChecked = false)

            val result = interactor.transformToClaimItems(listOf(unchecked))

            assertTrue(result.isEmpty())
        }

    @Test
    fun `transformToClaimItems excludes items without checkbox`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val noCheckbox = listItemWithoutCheckbox(id = "no_checkbox")

            val result = interactor.transformToClaimItems(listOf(noCheckbox))

            assertTrue(result.isEmpty())
        }

    // endregion

    //region transformToUiItems

    @Test
    fun `transformToUiItems maps each claim to a checked checkbox ui item with same id`() =
        runTest(StandardTestDispatcher()) {
            // GIVEN
            val claims = listOf(
                ClaimItem(label = "family_name"),
                ClaimItem(label = "age")
            )

            val interactor = createInteractor(
                supportedDocuments = SupportedDocuments(emptyMap()),
                resourceProvider = FakeResourceProvider
            )

            // WHEN
            val result = interactor.transformToUiItems(
                documentType = AttestationType.Pid,
                claims = claims
            )

            // THEN
            // 1) One UI item per claim
            assertEquals(claims.size, result.size)

            result.forEachIndexed { index, uiItem ->
                val claim = claims[index]

                // 2) itemId maps from claim.label
                assertEquals(claim.label, uiItem.itemId)

                // 3) mainContent is a Text node with some non-empty text
                val main = uiItem.mainContentData as ListItemMainContentDataUi.Text
                assertTrue(
                    main.text.isNotEmpty(),
                    "Expected non-empty main text for ${claim.label}"
                )

                // 4) trailingContent is a Checkbox and it's checked
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

    @Test
    fun `handleItemSelection toggles checkbox and updates hasSelectedItems`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val item1 = listItemWithCheckbox(id = "id_1", isChecked = false)
            val item2 = listItemWithCheckbox(id = "id_2", isChecked = false)

            val initialItems = listOf(item1, item2)

            val result = interactor.handleItemSelection(
                items = initialItems,
                identifier = "id_1"
            ) as HandleItemSelectionPartialState.Updated

            // id_1 should be toggled to true
            val updatedItem1 =
                result.items.first { it.itemId == "id_1" }.trailingContentData as
                        ListItemTrailingContentDataUi.Checkbox

            assertTrue(updatedItem1.checkboxData.isChecked)

            // hasSelectedItems should be true because at least one checkbox is now checked
            assertTrue(result.hasSelectedItems)
        }

    @Test
    fun `handleItemSelection allows unchecking the last checked item`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val item1 = listItemWithCheckbox(id = "id_1", isChecked = true)
            val item2 = listItemWithCheckbox(id = "id_2", isChecked = false)

            val initialItems = listOf(item1, item2)

            val result = interactor.handleItemSelection(
                items = initialItems,
                identifier = "id_1"
            ) as HandleItemSelectionPartialState.Updated

            val updatedItem1 =
                result.items.first { it.itemId == "id_1" }.trailingContentData as
                        ListItemTrailingContentDataUi.Checkbox

            assertFalse(updatedItem1.checkboxData.isChecked)
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

    // region Fakes

    /**
     * Very small fake ConfigProvider for these tests.
     *
     * Note: we don't need to construct BuildType, FlavorType, DocumentMode, or Logger,
     * so getters just throw if accessed. The interactor only uses supportedDocuments.
     */
    private class FakeConfigProvider(
        override val supportedDocuments: SupportedDocuments
    ) : ConfigProvider {

        override val buildType
            get() = error("Not used in these tests")

        override val flavorType
            get() = error("Not used in these tests")

        override val appVersion: String
            get() = "test"

        override val logger
            get() = error("Not used in these tests")

        override fun getDocumentModes(attestationType: AttestationType) =
            error("Not used in these tests")

        override suspend fun getCertificates(): List<String> =
            error("Not used in these tests")
    }

    /**
     * Simple fake ResourceProvider that returns predictable strings
     * so we can assert on them in tests.
     */
    private object FakeResourceProvider : ResourceProvider {

        override fun getSharedString(resource: StringResource): String {
            return when (resource) {
                Res.string.document_type_pid -> "PID"
                Res.string.document_type_mdl -> "MDL"
                Res.string.document_type_employee_id -> "EMPLOYEE_ID"
                else -> "UNKNOWN_STRING"
            }
        }

        override fun getSharedString(
            resource: StringResource,
            vararg formatArgs: Any
        ): String {
            return when (resource) {
                Res.string.custom_request_screen_title -> {
                    val label = formatArgs.firstOrNull() as? String ?: ""
                    "CustomRequestTitle($label)"
                }

                else -> "UNKNOWN_FORMATTED_STRING(${formatArgs.joinToString()})"
            }
        }

        override fun genericErrorMessage(): String = "GENERIC_ERROR"

        override fun genericNetworkErrorMessage(): String = "GENERIC_NETWORK_ERROR"
    }

    // endregion
}