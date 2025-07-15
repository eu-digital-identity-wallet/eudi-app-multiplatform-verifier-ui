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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.navigation.getFromCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.navigation.popToAndSave
import eu.europa.ec.euidi.verifier.presentation.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument
import eu.europa.ec.euidi.verifier.presentation.utils.Constants
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DocumentsToRequestScreen(
    navController: NavController,
    viewModel: DocumentsToRequestViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val doc = navController.getFromCurrentBackStack<RequestedDocumentUi>(Constants.REQUESTED_DOCUMENTS)
        viewModel.setEvent(
            DocToRequestContract.Event.Init(
                requestedDoc = doc
            )
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DocToRequestContract.Effect.Navigation.NavigateToHomeScreen -> {
                    navController.popToAndSave<RequestedDocsHolder>(
                        destination = NavItem.Home,
                        key = Constants.REQUESTED_DOCUMENTS,
                        value = RequestedDocsHolder(items = effect.requestedDocuments)
                    )
                }
                is DocToRequestContract.Effect.Navigation.NavigateToCustomRequestScreen -> {
                    navController.saveToCurrentBackStack<RequestedDocumentUi>(
                        key = Constants.REQUESTED_DOCUMENTS,
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

        LazyColumn(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
        ) {
            items(
                items = state.supportedDocuments,
                key = { it.id }
            ) {supportedDoc ->
                val isSelected = remember(state.requestedDocuments) {
                    state.requestedDocuments.any { it.documentType == supportedDoc.documentType }
                }

                Text(
                    text = "Request ${supportedDoc.documentType.displayName}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                SupportedDocumentCard(
                    supportedDoc = supportedDoc,
                    state = state,
                    isSelected = isSelected,
                    onEvent = viewModel::setEvent
                )
            }
        }

        Button(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = {
                viewModel.setEvent(DocToRequestContract.Event.OnDoneClick)
            },
            enabled = state.isButtonEnabled
        ) {
            Text("Done")
        }
    }
}

@Composable
fun SupportedDocumentCard(
    supportedDoc: SupportedDocument,
    state: DocToRequestContract.State,
    isSelected: Boolean,
    onEvent: (DocToRequestContract.Event) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                supportedDoc.modes.forEach { mode ->
                    val isModeSelected = remember(state.requestedDocuments) {
                        state.requestedDocuments.any {
                            it.documentType == supportedDoc.documentType && it.mode == mode
                        }
                    }

                    FilterChip(
                        selected = isModeSelected,
                        onClick = {
                            onEvent(
                                DocToRequestContract.Event.OnDocOptionSelected(
                                    docId = supportedDoc.id,
                                    docType = supportedDoc.documentType,
                                    mode = mode
                                )
                            )
                        },
                        label = { Text(mode.displayName) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(visible = isSelected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    supportedDoc.formats.forEach { format ->
                        val isFormatSelected = remember(state.requestedDocuments) {
                            state.requestedDocuments.any {
                                it.id == supportedDoc.id && it.format == format
                            }
                        }

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isFormatSelected,
                                onClick = {
                                    onEvent(
                                        DocToRequestContract.Event.OnDocFormatSelected(
                                            docId = supportedDoc.id,
                                            format = format
                                        )
                                    )
                                }
                            )
                            Text(text = format.displayName)
                        }
                    }
                }
            }
        }
    }
}