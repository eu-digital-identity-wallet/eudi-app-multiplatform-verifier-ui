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

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.RadioButton
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
import eu.europa.ec.euidi.verifier.navigation.getFromCurrentBackStack
import eu.europa.ec.euidi.verifier.navigation.popToAndSave
import eu.europa.ec.euidi.verifier.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.utils.Constants
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DocumentsToRequestScreen(
    navController: NavController,
    viewModel: DocumentsToRequestViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val doc = navController.getFromCurrentBackStack<RequestedDocumentUi>(Constants.REQUESTED_DOCUMENT)
        viewModel.setEvent(
            DocToRequestContract.Event.Init(
                doc = doc
            )
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DocToRequestContract.Effect.Navigation.NavigateToHomeScreen -> {
                    navController.popToAndSave<RequestedDocsHolder>(
                        destination = NavItem.Home,
                        key = Constants.REQUESTED_DOCUMENT,
                        value = RequestedDocsHolder(items = effect.requestedDocument)
                    )
                }
                is DocToRequestContract.Effect.Navigation.NavigateToCustomRequestScreen -> {
                    navController.saveToCurrentBackStack<RequestedDocumentUi>(
                        key = Constants.REQUESTED_DOCUMENT,
                        value = effect.requestedDocuments
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
                viewModel.setEvent(DocToRequestContract.Event.OnBackClick)
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
                            selected = state.requestedDocuments.any {
                                it.documentType == supportedDoc.documentType && it.mode == mode
                            },
                            onClick = {
                                viewModel.setEvent(
                                    DocToRequestContract.Event.OnDocOptionSelected(
                                        docId = supportedDoc.id,
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

                AnimatedVisibility(
                    visible = state.requestedDocuments.any { requestedDoc ->
                        requestedDoc.documentType == supportedDoc.documentType
                    }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        supportedDoc.formats.forEach { format ->
                            Row(
                                modifier = Modifier.weight(1f),
                            ) {
                                RadioButton(
                                    selected = state.requestedDocuments.any { requestedDoc ->
                                        requestedDoc.id == supportedDoc.id && requestedDoc.format == format
                                    },
                                    onClick = {
                                        viewModel.setEvent(
                                            DocToRequestContract.Event.OnDocFormatSelected( supportedDoc.id, format)
                                        )
                                    }
                                )

                                Text(text = format.displayName)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.setEvent(DocToRequestContract.Event.OnDoneClick)
                },
                enabled = state.requestedDocuments.isNotEmpty(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Done")
            }
        }
    }
}