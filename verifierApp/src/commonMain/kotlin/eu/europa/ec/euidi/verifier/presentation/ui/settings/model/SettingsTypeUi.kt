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

package eu.europa.ec.euidi.verifier.presentation.ui.settings.model

import eu.europa.ec.euidi.verifier.core.controller.PrefKey
import eudiverifier.verifierapp.generated.resources.Res
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
import org.jetbrains.compose.resources.StringResource

/**
 * Represents the different types of settings that can be displayed in the UI.
 *
 * This sealed class defines the structure for each setting type, including its
 * preference key, title resource, and description resource. Each specific setting
 * is implemented as a data object extending this class.
 *
 * @property prefKey The key used to store and retrieve the setting's value in preferences.
 * @property titleRes The string resource ID for the setting's title.
 * @property selectedDescriptionRes The string resource ID for the setting's description when it is selected.
 * @property unselectedDescriptionRes The string resource ID for the setting's description when it is not selected.
 */
sealed class SettingsTypeUi(
    val prefKey: PrefKey,
    val titleRes: StringResource,
    val selectedDescriptionRes: StringResource,
    val unselectedDescriptionRes: StringResource,
) {
    data object RetainData : SettingsTypeUi(
        prefKey = PrefKey.RETAIN_DATA,
        titleRes = Res.string.settings_screen_item_retain_data_title,
        selectedDescriptionRes = Res.string.settings_screen_item_retain_data_description_selected,
        unselectedDescriptionRes = Res.string.settings_screen_item_retain_data_description_unselected,
    )

    data object UseL2Cap : SettingsTypeUi(
        prefKey = PrefKey.USE_L2CAP,
        titleRes = Res.string.settings_screen_item_use_l2cap_title,
        selectedDescriptionRes = Res.string.settings_screen_item_use_l2cap_description_selected,
        unselectedDescriptionRes = Res.string.settings_screen_item_use_l2cap_description_unselected,
    )

    data object ClearBleCache : SettingsTypeUi(
        prefKey = PrefKey.CLEAR_BLE_CACHE,
        titleRes = Res.string.settings_screen_item_clear_ble_title,
        selectedDescriptionRes = Res.string.settings_screen_item_clear_ble_description_selected,
        unselectedDescriptionRes = Res.string.settings_screen_item_clear_ble_description_unselected,
    )

    data object BleCentralClient : SettingsTypeUi(
        prefKey = PrefKey.BLE_CENTRAL_CLIENT,
        titleRes = Res.string.settings_screen_item_ble_central_client_title,
        selectedDescriptionRes = Res.string.settings_screen_item_ble_central_client_description_selected,
        unselectedDescriptionRes = Res.string.settings_screen_item_ble_central_client_description_unselected,
    )

    data object BlePeripheralServer : SettingsTypeUi(
        prefKey = PrefKey.BLE_PERIPHERAL_SERVER,
        titleRes = Res.string.settings_screen_item_ble_peripheral_server_title,
        selectedDescriptionRes = Res.string.settings_screen_item_ble_peripheral_server_description_selected,
        unselectedDescriptionRes = Res.string.settings_screen_item_ble_peripheral_server_description_unselected,
    )
}