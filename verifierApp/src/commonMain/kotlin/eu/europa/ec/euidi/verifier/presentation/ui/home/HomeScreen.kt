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

package eu.europa.ec.euidi.verifier.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.navigation.NavItem
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue
import eu.europa.ec.euidi.verifier.navigation.getFromCurrentBackStack
import eu.europa.ec.euidi.verifier.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.utils.Constants

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val documents = navController.getFromCurrentBackStack<RequestedDocsHolder>(Constants.REQUESTED_DOCUMENTS)
        documents?.let {
            viewModel.setEvent(HomeViewModelContract.Event.Init(it.items))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeViewModelContract.Effect.Navigation.NavigateToDocToRequestScreen -> {
                    navController.navigate(NavItem.DocToRequest)
                }

                is HomeViewModelContract.Effect.Navigation.NavigateToTransferStatusScreen -> {
                    navController.saveToCurrentBackStack<RequestedDocsHolder>(
                        key = Constants.REQUESTED_DOCUMENTS,
                        value = effect.requestedDocs
                    )
                    navController.navigate(NavItem.TransferStatus)
                }

                HomeViewModelContract.Effect.Navigation.NavigateToSettingsScreen -> {
                    navController.navigate(NavItem.Settings)
                }

                HomeViewModelContract.Effect.Navigation.NavigateToReverseEngagementScreen -> {
                    navController.navigate(NavItem.ReverseEngagement)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Home",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setEvent(HomeViewModelContract.Event.OnSelectDocumentClick) }
        ) {
            Text("Select document")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setEvent(HomeViewModelContract.Event.OnScanQrCodeClick) },
            enabled = state.isScanQrCodeButtonEnabled
        ) {
            Text("Scan QR Code")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setEvent(HomeViewModelContract.Event.OnSettingsClick) }
        ) {
            Text("Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.setEvent(HomeViewModelContract.Event.OnReverseEngagementClick) }
        ) {
            Text("Reverse Engagement")
        }
    }
}