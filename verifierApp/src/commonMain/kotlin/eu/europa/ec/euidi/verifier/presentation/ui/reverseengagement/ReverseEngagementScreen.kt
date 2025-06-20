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

package eu.europa.ec.euidi.verifier.presentation.ui.reverseengagement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.navigation.NavItem

@Composable
fun ReverseEngagementScreen(
    navController: NavController,
    viewModel: ReverseEngagementViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

//    LaunchedEffect(Unit) {
//        viewModel.setEvent(ReverseEngagementViewModelContract.Event.Init)
//    }
//
    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ReverseEngagementViewModelContract.Effect.Navigation.NavigateToHome -> {
                    navController.navigate(NavItem.Home) {
                        popUpTo<NavItem.Home>()
                    }
                }

                ReverseEngagementViewModelContract.Effect.Navigation.GoBack -> navController.popBackStack()
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().safeContentPadding(),
    ) {
        IconButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = {
                viewModel.setEvent(ReverseEngagementViewModelContract.Event.OnBackClick)
            }
        ) {
            Text(
                text = "Back"
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Reverse Engagement",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                content = {
                    Text("Cancel")
                },
                onClick = {
                    viewModel.setEvent(ReverseEngagementViewModelContract.Event.OnCancelClick)
                }
            )
        }
    }
}