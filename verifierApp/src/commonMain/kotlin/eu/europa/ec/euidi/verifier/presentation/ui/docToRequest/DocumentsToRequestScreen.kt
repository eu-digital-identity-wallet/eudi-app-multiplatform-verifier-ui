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

package eu.europa.ec.euidi.verifier.presentation.ui.docToRequest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.navigation.NavItem
import eu.europa.ec.euidi.verifier.navigation.getFromPreviousBackStack
import eu.europa.ec.euidi.verifier.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.navigation.saveToPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.utils.Constants
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DocumentsToRequestScreen(
    navController: NavController,
    viewModel: DocToRequestViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setEvent(
            DocToRequestViewModelContract.Event.Init(
                doc = navController.getFromPreviousBackStack<RequestedDocumentUi>(Constants.REQUESTED_DOCUMENT)
            )
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DocToRequestViewModelContract.Effect.Navigation.NavigateToHomeScreen -> {
                    effect.requestedDocument?.let {
                        navController.saveToPreviousBackStack<RequestedDocumentUi>(
                            key = Constants.REQUESTED_DOCUMENT,
                            value = effect.requestedDocument
                        )
                    }

                    navController.popBackStack()
                }
                is DocToRequestViewModelContract.Effect.Navigation.NavigateToCustomRequestScreen -> {
                    navController.saveToCurrentBackStack<RequestedDocumentUi>(
                        key = Constants.REQUESTED_DOCUMENT,
                        value = effect.requestedDocument
                    ).let {
                        navController.navigate(route = NavItem.CustomRequest)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = {
                viewModel.setEvent(DocToRequestViewModelContract.Event.OnBackClick)
            }
        ) {
            Text("Back")
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            state.supportedDocuments.forEach { supportedDoc ->
                Text(
                    text = "Request ${supportedDoc.documentType.displayName}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(12.dp))


                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    supportedDoc.modes.forEach { mode ->
                        FilterChip(
                            selected = state.selectedMode == mode && state.selectedDocType == supportedDoc.documentType,
                            onClick = {
                                viewModel.setEvent(
                                    DocToRequestViewModelContract.Event.OnOptionSelected(
                                        docType = supportedDoc.documentType,
                                        mode = mode
                                    )
                                )
                            },
                            label = {
                                Text(mode.displayName)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.setEvent(DocToRequestViewModelContract.Event.OnDoneClick)
                },
                enabled = state.selectedMode != null,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Done")
            }
        }
    }
}