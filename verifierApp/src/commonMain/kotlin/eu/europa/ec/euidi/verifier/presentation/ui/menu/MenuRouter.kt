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

package eu.europa.ec.euidi.verifier.presentation.ui.menu

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.navigation.noAnimation
import eu.europa.ec.euidi.verifier.presentation.navigation.slideInFromEnd
import eu.europa.ec.euidi.verifier.presentation.navigation.slideOutToEnd

fun NavGraphBuilder.menuScreen(navController: NavController) {
    composable<NavItem.Menu>(
        // when you navigate TO Menu (initial / forward)
        enterTransition = slideInFromEnd(),

        // when you navigate AWAY from Menu (forward), disable it
        exitTransition = noAnimation(),

        // when you pop BACK to Menu, disable any enter‐animation
        popEnterTransition = noAnimation(),

        // when you pop Menu itself, slide it out
        popExitTransition = slideOutToEnd()
    ) {
        MenuScreen(navController)
    }
}