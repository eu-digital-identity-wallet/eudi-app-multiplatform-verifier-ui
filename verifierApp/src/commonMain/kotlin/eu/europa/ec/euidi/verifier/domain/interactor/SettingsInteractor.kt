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
import eudiverifier.verifierapp.generated.resources.settings_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource

interface SettingsInteractor {
    suspend fun getPrefBoolean(key: PrefKey): Boolean
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

    private val retrievalOptionsPrefs = listOf(
        SettingsTypeUi.UseL2Cap,
        SettingsTypeUi.ClearBleCache,
    )

    private val retrievalMethodPrefs = listOf(
        SettingsTypeUi.Http,
        SettingsTypeUi.BleCentralClient,
        SettingsTypeUi.BlePeripheralServer,
    )

    /**
     * Retrieves a boolean preference.
     *
     * @param key The [PrefKey] of the preference to retrieve.
     * @return The boolean value of the preference, or `false` if the preference is not set.
     */
    override suspend fun getPrefBoolean(key: PrefKey): Boolean {
        return dataStoreController.getBoolean(key) ?: false
    }

    /**
     * Toggles the boolean value of a preference.
     *
     * @param key The [PrefKey] of the preference to toggle.
     */
    override suspend fun togglePrefBoolean(key: PrefKey) {
        dataStoreController.putBoolean(key, !getPrefBoolean(key))
    }

    override suspend fun getScreenTitle(): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(Res.string.settings_screen_title)
        }
    }

    override suspend fun getSettingsItemsUi(): List<SettingsItemUi> {
        return withContext(dispatcher) {
            val all: List<SettingsTypeUi> =
                retrievalOptionsPrefs + retrievalMethodPrefs

            val preferences: Map<PrefKey, Boolean> = all
                .map { it.prefKey }
                .distinct()
                .associateWith { getPrefBoolean(it) }

            buildList {
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

    private suspend fun buildSection(
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
                add(
                    SettingsItemUi.CategoryItem(
                        type = sectionItem,
                        data = ListItemDataUi(
                            itemId = uuidProvider.provideUuid(),
                            mainContentData = ListItemMainContentDataUi.Text(
                                text = resourceProvider.getSharedString(sectionItem.titleRes)
                            ),
                            supportingText = resourceProvider.getSharedString(sectionItem.descriptionRes),
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
}