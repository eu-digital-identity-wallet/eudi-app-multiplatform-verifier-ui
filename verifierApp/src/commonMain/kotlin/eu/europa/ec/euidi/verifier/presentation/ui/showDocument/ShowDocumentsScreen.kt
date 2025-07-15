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

package eu.europa.ec.euidi.verifier.presentation.ui.showDocument

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import eu.europa.ec.euidi.verifier.presentation.navigation.getFromPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.utils.Constants

@Composable
fun ShowDocumentsScreen(
    navController: NavController,
    viewModel: ShowDocumentsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val documents = navController.getFromPreviousBackStack<ReceivedDocsHolder>(Constants.RECEIVED_DOCUMENTS)
        documents?.let {
            viewModel.setEvent(ShowDocumentViewModelContract.Event.Init(it.items))
        }
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ShowDocumentViewModelContract.Effect.Navigation.NavigateToHome -> {
                    navController.navigate(NavItem.Home) {
                        popUpTo(NavItem.Home) {
                            inclusive = true
                        }
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
                viewModel.setEvent(ShowDocumentViewModelContract.Event.OnBackClick)
            }
        ) {
            Text(
                text = "Back"
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Show documents",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(36.dp))

            Card(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.wrapContentHeight().padding(8.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Number of documents returned: ${state.items.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Address:ognfoenrgiergieigwbegiwbrognfoenrgiergieigwbegiwbr",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                state.items.forEach {
                    DocumentDetailsCard(data = it.claims)
                }
            }

            Button(
                content = {
                    Text("OK")
                },
                onClick = {
                    viewModel.setEvent(ShowDocumentViewModelContract.Event.OnDoneClick)
                }
            )
        }
    }
}

@Composable
fun DocumentDetailsCard(
    data: Map<String, String>
) {
    Card(
        modifier = Modifier
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            data.forEach { field ->
                DocumentDetailsTile(
                    field = field,
                    isLast = field.key == data.keys.last()
                )
            }
        }
    }
}

@Composable
fun DocumentDetailsTile(
    field: Map.Entry<String, String>,
    isLast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = field.key,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = field.value,
            style = MaterialTheme.typography.bodySmall
        )

        if (!isLast) {
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
        }
    }
}