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

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import eu.europa.ec.euidi.verifier.presentation.ui.customRequest.customRequestScreen
import eu.europa.ec.euidi.verifier.presentation.ui.docToRequest.docToRequestScreen
import eu.europa.ec.euidi.verifier.presentation.ui.home.homeScreen
import eu.europa.ec.euidi.verifier.presentation.ui.menu.menuScreen
import eu.europa.ec.euidi.verifier.presentation.ui.reverseengagement.reverseEngagementScreen
import eu.europa.ec.euidi.verifier.presentation.ui.settings.settingsScreen
import eu.europa.ec.euidi.verifier.presentation.ui.showDocument.showDocumentsScreen
import eu.europa.ec.euidi.verifier.presentation.ui.transferStatus.transferStatusScreen

@Composable
fun VerifierNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavItem.Home
    ) {
        homeScreen(navController)
        menuScreen(navController)
        docToRequestScreen(navController)
        customRequestScreen(navController)
        transferStatusScreen(navController)
        showDocumentsScreen(navController)
        reverseEngagementScreen(navController)
        settingsScreen(navController)
    }
}