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

import eu.europa.ec.euidi.verifier.core.controller.DataStoreController
import eu.europa.ec.euidi.verifier.core.controller.PrefKey
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.SwitchDataUi
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsTypeUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_methods_description
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_methods_title
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_options_title
import eudiverifier.verifierapp.generated.resources.settings_screen_category_general_title
import eudiverifier.verifierapp.generated.resources.settings_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource

interface SettingsInteractor {
    suspend fun togglePrefBoolean(key: PrefKey)
    suspend fun getScreenTitle(): String
    suspend fun getSettingsItemsUi(): List<SettingsItemUi>
}

class SettingsInteractorImpl(
    private val uuidProvider: UuidProvider,
    private val resourceProvider: ResourceProvider,
    private val dataStoreController: DataStoreController,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SettingsInteractor {

    private val generalPrefs = dataStoreController
        .getGeneralPrefs()
        .toSettingsTypeUi()

    private val retrievalOptionsPrefs = dataStoreController
        .getRetrievalOptionsPrefs()
        .toSettingsTypeUi()

    private val retrievalMethodPrefs = dataStoreController
        .getRetrievalMethodPrefs()
        .toSettingsTypeUi()

    /**
     * Toggles the boolean value of a preference.
     *
     * If the preference is a retrieval method and the new value is `false` (i.e., the method is being disabled),
     * this function will first check if any other retrieval method is still active. If no other retrieval
     * method is active, the preference will not be toggled, ensuring at least one retrieval method
     * remains active.
     *
     * @param key The [PrefKey] of the preference to toggle.
     */
    override suspend fun togglePrefBoolean(key: PrefKey) {
        withContext(dispatcher) {
            val currentValue = getPrefBoolean(key)
            val newValue = !currentValue

            val isRetrievalMethodPref = retrievalMethodPrefs.find {
                it.prefKey == key
            } != null

            if (isRetrievalMethodPref && !newValue) {
                val isAnyOtherRetrievalMethodActive = retrievalMethodPrefs
                    .mapNotNull {
                        if (it.prefKey == key)
                            null
                        else
                            getPrefBoolean(it.prefKey)
                    }.any {
                        it
                    }

                if (isAnyOtherRetrievalMethodActive) {
                    dataStoreController.putBoolean(key, newValue)
                } else
                    return@withContext
            } else {
                dataStoreController.putBoolean(key, newValue)
            }
        }
    }

    override suspend fun getScreenTitle(): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(Res.string.settings_screen_title)
        }
    }

    override suspend fun getSettingsItemsUi(): List<SettingsItemUi> {
        return withContext(dispatcher) {
            val all: List<SettingsTypeUi> =
                generalPrefs + retrievalOptionsPrefs + retrievalMethodPrefs

            val preferences: Map<PrefKey, Boolean> = all
                .map { it.prefKey }
                .distinct()
                .associateWith {

                    val defaultValue: Boolean? =
                        retrievalMethodPrefs.find { pref -> it == pref.prefKey }?.let { true }

                    getPrefBoolean(it, defaultValue)
                }

            buildList {
                addAll(
                    buildSection(
                        headerTitle = Res.string.settings_screen_category_general_title,
                        headerDesc = null,
                        sectionItems = generalPrefs,
                        preferences = preferences
                    )
                )
                addAll(
                    buildSection(
                        headerTitle = Res.string.settings_screen_category_data_retrieval_options_title,
                        headerDesc = null,
                        sectionItems = retrievalOptionsPrefs,
                        preferences = preferences
                    )
                )
                addAll(
                    buildSection(
                        headerTitle = Res.string.settings_screen_category_data_retrieval_methods_title,
                        headerDesc = Res.string.settings_screen_category_data_retrieval_methods_description,
                        sectionItems = retrievalMethodPrefs,
                        preferences = preferences
                    )
                )
            }
        }
    }

    private fun buildSection(
        headerTitle: StringResource,
        headerDesc: StringResource? = null,
        sectionItems: List<SettingsTypeUi>,
        preferences: Map<PrefKey, Boolean>
    ): List<SettingsItemUi> {
        return buildList {
            // header
            add(
                SettingsItemUi.CategoryHeader(
                    title = resourceProvider.getSharedString(headerTitle),
                    description = headerDesc?.let { resourceProvider.getSharedString(it) }
                )
            )

            // each item
            sectionItems.forEachIndexed { index, sectionItem ->
                val isLast = index == sectionItems.lastIndex
                val checked = preferences[sectionItem.prefKey] ?: false
                val supportingText = resourceProvider.getSharedString(
                    if (checked) {
                        sectionItem.selectedDescriptionRes
                    } else {
                        sectionItem.unselectedDescriptionRes
                    }
                )
                add(
                    SettingsItemUi.CategoryItem(
                        type = sectionItem,
                        data = ListItemDataUi(
                            itemId = uuidProvider.provideUuid(),
                            mainContentData = ListItemMainContentDataUi.Text(
                                text = resourceProvider.getSharedString(sectionItem.titleRes)
                            ),
                            supportingText = supportingText,
                            trailingContentData = ListItemTrailingContentDataUi.Switch(
                                switchData = SwitchDataUi(isChecked = checked)
                            )
                        ),
                        isLastInSection = isLast,
                    )
                )
            }
        }
    }

    private fun List<PrefKey>.toSettingsTypeUi(): List<SettingsTypeUi> {
        return this.map { prefKey ->
            when (prefKey) {
                PrefKey.RETAIN_DATA -> SettingsTypeUi.RetainData
                PrefKey.USE_L2CAP -> SettingsTypeUi.UseL2Cap
                PrefKey.CLEAR_BLE_CACHE -> SettingsTypeUi.ClearBleCache
                PrefKey.BLE_CENTRAL_CLIENT -> SettingsTypeUi.BleCentralClient
                PrefKey.BLE_PERIPHERAL_SERVER -> SettingsTypeUi.BlePeripheralServer
            }
        }
    }

    /**
     * Retrieves a boolean preference value from the data store.
     *
     * This function fetches the boolean value associated with the given [key]. If the key does not
     * exist or has no value, it defaults to `false`.
     *
     * @param key The [PrefKey] of the preference to retrieve.
     * @return The boolean value of the preference, or `false` if not found.
     */
    private suspend fun getPrefBoolean(key: PrefKey, defaultValue: Boolean? = null): Boolean {
        return dataStoreController.getBoolean(key, defaultValue) ?: false
    }
}