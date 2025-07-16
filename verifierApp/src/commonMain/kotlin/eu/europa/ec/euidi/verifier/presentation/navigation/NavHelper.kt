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

package eu.europa.ec.euidi.verifier.presentation.navigation

import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelable

inline fun <reified T : CommonParcelable> NavController.saveToCurrentBackStack(key: String, value: T) {
    currentBackStackEntry?.savedStateHandle?.set(key, value)
}

inline fun <reified T : CommonParcelable> NavController.saveToPreviousBackStack(key: String, value: T) {
    previousBackStackEntry?.savedStateHandle?.set(key, value)
}

inline fun <reified T : CommonParcelable> NavController.getFromPreviousBackStack(key: String): T? {
    return previousBackStackEntry?.savedStateHandle?.remove(key)
}

inline fun <reified T : CommonParcelable> NavController.getFromCurrentBackStack(key: String): T? {
    return currentBackStackEntry?.savedStateHandle?.remove(key)
}

/**
 * Saves data to a specified destination's saved state handle.
 */
inline fun <reified T : CommonParcelable> NavController.popToAndSave(
    destination: NavItem,
    key: String,
    value: T,
    inclusive: Boolean = false
) {
    getBackStackEntry(destination).savedStateHandle[key] = value
    popBackStack(
        route = destination,
        inclusive = inclusive
    )
}
