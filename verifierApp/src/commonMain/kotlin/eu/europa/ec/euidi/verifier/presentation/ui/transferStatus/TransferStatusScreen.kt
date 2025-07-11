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

package eu.europa.ec.euidi.verifier.presentation.ui.transferStatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.navigation.NavItem
import eu.europa.ec.euidi.verifier.navigation.getFromPreviousBackStack
import eu.europa.ec.euidi.verifier.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.utils.Constants
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransferStatusScreen(
    navController: NavController,
    viewModel: TransferStatusViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val requestedDocs = navController.getFromPreviousBackStack<RequestedDocsHolder>(
            key = Constants.REQUESTED_DOCUMENTS
        )

        requestedDocs?.let {
            viewModel.setEvent(TransferStatusViewModelContract.Event.Init(requestedDocs.items))
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TransferStatusViewModelContract.Effect.Navigation.GoBack -> navController.popBackStack()

                is TransferStatusViewModelContract.Effect.Navigation.NavigateToShowDocumentsScreen -> {
                    navController.saveToCurrentBackStack<ReceivedDocsHolder>(
                        key = Constants.RECEIVED_DOCUMENTS,
                        value = ReceivedDocsHolder(items = effect.receivedDocuments)
                    )
                    navController.navigate(NavItem.ShowDocuments)
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = {
                viewModel.setEvent(TransferStatusViewModelContract.Event.OnBackClick)
            }
        ) {
            Text(
                text = "Back"
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Transfer Status",
                style = MaterialTheme.typography.headlineMedium
            )

            Column(
                modifier = Modifier.padding(bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.connectionStatus
                )

                Text(
                    text = state.message
                )
            }

            Button(
                modifier = Modifier.padding(bottom = 4.dp),
                content = {
                    Text("Cancel")
                },
                onClick = {
                    viewModel.setEvent(TransferStatusViewModelContract.Event.OnCancelClick)
                }
            )
        }
    }
}