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

package eu.europa.ec.euidi.verifier.presentation.ui.customRequest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
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
import eu.europa.ec.euidi.verifier.navigation.getFromPreviousBackStack
import eu.europa.ec.euidi.verifier.navigation.saveToPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.utils.Constants
import eu.europa.ec.euidi.verifier.utils.ToastManager
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomRequestScreen(
    navController: NavController,
    viewModel: CustomRequestViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setEvent(
            CustomRequestContract.Event.Init(
                doc = navController.getFromPreviousBackStack<RequestedDocumentUi>(Constants.REQUESTED_DOCUMENT)
            )
        )
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CustomRequestContract.Effect.ShowToast -> {
                    ToastManager().showToast(effect.message)
                }
                is CustomRequestContract.Effect.Navigation.GoBack -> {
                    effect.requestedDocument?.let {
                        navController.saveToPreviousBackStack(
                            key = Constants.REQUESTED_DOCUMENT,
                            value = effect.requestedDocument
                        )
                    }
                    navController.popBackStack()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Custom request",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(
                items = state.fields,
                key = { it.claim.identifier }
            ) {item ->

                ListItem(
                    headlineContent = {
                        Text(
                            text = item.claim.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    trailingContent = {
                        Checkbox(
                            checked = item.isSelected,
                            onCheckedChange = { checked ->
                                viewModel.setEvent(
                                    CustomRequestContract.Event.OnItemChecked(item.claim.identifier, checked)
                                )
                            }
                        )
                    }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.setEvent(
                        CustomRequestContract.Event.OnDoneClick
                    )
                }
            ) {
                Text("Done")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {}
            ) {
                Text("Cancel")
            }
        }
    }
}