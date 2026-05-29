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

import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import eu.europa.ec.euidi.verifier.core.controller.DataStoreController
import eu.europa.ec.euidi.verifier.core.controller.PrefKey
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsTypeUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_methods_description
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_methods_title
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_options_title
import eudiverifier.verifierapp.generated.resources.settings_screen_category_general_title
import eudiverifier.verifierapp.generated.resources.settings_screen_item_ble_central_client_description_selected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_ble_central_client_description_unselected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_ble_central_client_title
import eudiverifier.verifierapp.generated.resources.settings_screen_item_ble_peripheral_server_description_selected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_ble_peripheral_server_description_unselected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_ble_peripheral_server_title
import eudiverifier.verifierapp.generated.resources.settings_screen_item_clear_ble_description_selected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_clear_ble_description_unselected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_clear_ble_title
import eudiverifier.verifierapp.generated.resources.settings_screen_item_retain_data_description_selected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_retain_data_description_unselected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_retain_data_title
import eudiverifier.verifierapp.generated.resources.settings_screen_item_use_l2cap_description_selected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_use_l2cap_description_unselected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_use_l2cap_title
import eudiverifier.verifierapp.generated.resources.settings_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsInteractorTest {

    /**
     * Builds the SUT, [SettingsInteractorImpl], with a dispatcher that shares the `runTest`
     * scheduler. The [DataStoreController] is built by the caller so its [putBoolean] writes can
     * be verified.
     */
    private fun TestScope.createInteractor(
        uuidProvider: UuidProvider = sequentialUuidProvider(),
        resourceProvider: ResourceProvider = stringResourceProvider(),
        dataStoreController: DataStoreController = dataStore(),
    ): SettingsInteractor {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        return SettingsInteractorImpl(
            uuidProvider = uuidProvider,
            resourceProvider = resourceProvider,
            dataStoreController = dataStoreController,
            dispatcher = dispatcher
        )
    }

    //region getScreenTitle

    @Test
    fun `getScreenTitle returns localized title`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertEquals("Settings title", interactor.getScreenTitle())
    }

    //endregion

    //region getSettingsItemsUi

    @Test
    fun `getSettingsItemsUi builds three sections with defaults`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor(dataStoreController = dataStore())

            val items = interactor.getSettingsItemsUi()

            // 3 category headers + (1 general + 2 retrieval options + 2 retrieval methods) items.
            assertEquals(8, items.size)

            // General section
            val generalHeader = items[0] as SettingsItemUi.CategoryHeader
            assertEquals("General", generalHeader.title)
            assertNull(generalHeader.description)

            val retainItem = items[1] as SettingsItemUi.CategoryItem
            assertEquals(SettingsTypeUi.RetainData, retainItem.type)
            assertEquals("uuid-0", retainItem.data.itemId)
            assertEquals(
                "Retain data",
                (retainItem.data.mainContentData as ListItemMainContentDataUi.Text).text
            )
            // No stored value and not a retrieval method -> defaults to unchecked.
            assertFalse(
                (retainItem.data.trailingContentData as ListItemTrailingContentDataUi.Switch)
                    .switchData.isChecked
            )
            assertEquals("Retain data OFF", retainItem.data.supportingText)
            assertTrue(retainItem.isLastInSection)

            // Retrieval options section
            val optionsHeader = items[2] as SettingsItemUi.CategoryHeader
            assertEquals("Retrieval options", optionsHeader.title)
            assertNull(optionsHeader.description)

            val useL2CapItem = items[3] as SettingsItemUi.CategoryItem
            assertEquals(SettingsTypeUi.UseL2Cap, useL2CapItem.type)
            assertFalse(useL2CapItem.isLastInSection)

            val clearBleItem = items[4] as SettingsItemUi.CategoryItem
            assertEquals(SettingsTypeUi.ClearBleCache, clearBleItem.type)
            assertTrue(clearBleItem.isLastInSection)

            // Retrieval methods section (carries a header description)
            val methodsHeader = items[5] as SettingsItemUi.CategoryHeader
            assertEquals("Retrieval methods", methodsHeader.title)
            assertEquals("Retrieval methods description", methodsHeader.description)

            val centralItem = items[6] as SettingsItemUi.CategoryItem
            assertEquals(SettingsTypeUi.BleCentralClient, centralItem.type)
            // Retrieval methods default to checked.
            assertTrue(
                (centralItem.data.trailingContentData as ListItemTrailingContentDataUi.Switch)
                    .switchData.isChecked
            )
            assertEquals("BLE central ON", centralItem.data.supportingText)
            assertFalse(centralItem.isLastInSection)

            val peripheralItem = items[7] as SettingsItemUi.CategoryItem
            assertEquals(SettingsTypeUi.BlePeripheralServer, peripheralItem.type)
            assertTrue(peripheralItem.isLastInSection)
        }

    @Test
    fun `getSettingsItemsUi reflects stored values and selected descriptions`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor(
                dataStoreController = dataStore(
                    stored = mapOf(
                        PrefKey.RETAIN_DATA to true,
                        PrefKey.BLE_CENTRAL_CLIENT to false,
                    )
                )
            )

            val items = interactor.getSettingsItemsUi()

            val retainItem = items[1] as SettingsItemUi.CategoryItem
            assertTrue(
                (retainItem.data.trailingContentData as ListItemTrailingContentDataUi.Switch)
                    .switchData.isChecked
            )
            assertEquals("Retain data ON", retainItem.data.supportingText)

            val centralItem = items[6] as SettingsItemUi.CategoryItem
            // Stored false overrides the retrieval-method default of true.
            assertFalse(
                (centralItem.data.trailingContentData as ListItemTrailingContentDataUi.Switch)
                    .switchData.isChecked
            )
            assertEquals("BLE central OFF", centralItem.data.supportingText)
        }

    //endregion

    //region togglePrefBoolean

    @Test
    fun `togglePrefBoolean toggles a general preference`() = runTest(StandardTestDispatcher()) {
        val dataStoreController = dataStore()
        val interactor = createInteractor(dataStoreController = dataStoreController)

        interactor.togglePrefBoolean(PrefKey.RETAIN_DATA)

        verifySuspend(exactly(1)) { dataStoreController.putBoolean(PrefKey.RETAIN_DATA, true) }
    }

    @Test
    fun `togglePrefBoolean enabling a retrieval method stores the new value`() =
        runTest(StandardTestDispatcher()) {
            val dataStoreController = dataStore(
                stored = mapOf(PrefKey.BLE_CENTRAL_CLIENT to false)
            )
            val interactor = createInteractor(dataStoreController = dataStoreController)

            interactor.togglePrefBoolean(PrefKey.BLE_CENTRAL_CLIENT)

            verifySuspend(exactly(1)) {
                dataStoreController.putBoolean(PrefKey.BLE_CENTRAL_CLIENT, true)
            }
        }

    @Test
    fun `togglePrefBoolean keeps the last active retrieval method enabled`() =
        runTest(StandardTestDispatcher()) {
            val dataStoreController = dataStore(
                stored = mapOf(
                    PrefKey.BLE_CENTRAL_CLIENT to true,
                    PrefKey.BLE_PERIPHERAL_SERVER to false,
                )
            )
            val interactor = createInteractor(dataStoreController = dataStoreController)

            // Attempting to disable the only active retrieval method must be a no-op.
            interactor.togglePrefBoolean(PrefKey.BLE_CENTRAL_CLIENT)

            verifySuspend(exactly(0)) { dataStoreController.putBoolean(any(), any()) }
        }

    @Test
    fun `togglePrefBoolean disables a retrieval method when another stays active`() =
        runTest(StandardTestDispatcher()) {
            val dataStoreController = dataStore(
                stored = mapOf(
                    PrefKey.BLE_CENTRAL_CLIENT to true,
                    PrefKey.BLE_PERIPHERAL_SERVER to true,
                )
            )
            val interactor = createInteractor(dataStoreController = dataStoreController)

            interactor.togglePrefBoolean(PrefKey.BLE_CENTRAL_CLIENT)

            verifySuspend(exactly(1)) {
                dataStoreController.putBoolean(PrefKey.BLE_CENTRAL_CLIENT, false)
            }
        }

    //endregion

    //region Mocks

    private fun sequentialUuidProvider(): UuidProvider {
        var counter = 0
        return mock {
            every { provideUuid() } calls { "uuid-${counter++}" }
        }
    }

    /**
     * Mocks the [DataStoreController]. The three pref-grouping lists mirror production so the
     * interactor classifies keys the same way. `getBoolean` returns the stored value or falls back
     * to the supplied default (matching the real implementation), and `putBoolean` is left to the
     * `autoUnit` mode so the writes can be verified per test.
     */
    private fun dataStore(stored: Map<PrefKey, Boolean> = emptyMap()): DataStoreController {
        val dataStore = mock<DataStoreController>(MockMode.autoUnit)
        every { dataStore.getGeneralPrefs() } returns listOf(PrefKey.RETAIN_DATA)
        every {
            dataStore.getRetrievalOptionsPrefs()
        } returns listOf(PrefKey.USE_L2CAP, PrefKey.CLEAR_BLE_CACHE)
        every {
            dataStore.getRetrievalMethodPrefs()
        } returns listOf(PrefKey.BLE_CENTRAL_CLIENT, PrefKey.BLE_PERIPHERAL_SERVER)
        everySuspend {
            dataStore.getBoolean(
                any(),
                any()
            )
        } calls { (key: PrefKey, default: Boolean?) ->
            stored[key] ?: default
        }
        return dataStore
    }

    private fun stringResourceProvider(): ResourceProvider {
        val resourceProvider = mock<ResourceProvider>()
        every { resourceProvider.getSharedString(any()) } calls { (resource: StringResource) ->
            when (resource) {
                Res.string.settings_screen_title -> "Settings title"

                Res.string.settings_screen_category_general_title -> "General"
                Res.string.settings_screen_category_data_retrieval_options_title -> "Retrieval options"
                Res.string.settings_screen_category_data_retrieval_methods_title -> "Retrieval methods"
                Res.string.settings_screen_category_data_retrieval_methods_description -> "Retrieval methods description"

                Res.string.settings_screen_item_retain_data_title -> "Retain data"
                Res.string.settings_screen_item_retain_data_description_selected -> "Retain data ON"
                Res.string.settings_screen_item_retain_data_description_unselected -> "Retain data OFF"

                Res.string.settings_screen_item_use_l2cap_title -> "Use L2CAP"
                Res.string.settings_screen_item_use_l2cap_description_selected -> "L2CAP ON"
                Res.string.settings_screen_item_use_l2cap_description_unselected -> "L2CAP OFF"

                Res.string.settings_screen_item_clear_ble_title -> "Clear BLE cache"
                Res.string.settings_screen_item_clear_ble_description_selected -> "Clear BLE ON"
                Res.string.settings_screen_item_clear_ble_description_unselected -> "Clear BLE OFF"

                Res.string.settings_screen_item_ble_central_client_title -> "BLE central"
                Res.string.settings_screen_item_ble_central_client_description_selected -> "BLE central ON"
                Res.string.settings_screen_item_ble_central_client_description_unselected -> "BLE central OFF"

                Res.string.settings_screen_item_ble_peripheral_server_title -> "BLE peripheral"
                Res.string.settings_screen_item_ble_peripheral_server_description_selected -> "BLE peripheral ON"
                Res.string.settings_screen_item_ble_peripheral_server_description_unselected -> "BLE peripheral OFF"

                else -> "STR"
            }
        }
        return resourceProvider
    }

    //endregion
}