/*
 * Copyright (c) 2026 European Commission
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

package eu.europa.ec.euidi.verifier.presentation.ui.zk_request

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.component.ClickableArea
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentScreen
import eu.europa.ec.euidi.verifier.presentation.component.content.ScreenNavigateAction
import eu.europa.ec.euidi.verifier.presentation.component.content.ToolbarConfig
import eu.europa.ec.euidi.verifier.presentation.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_SMALL
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.RadioButtonDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapListItems
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapRadioButton
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapStickyBottomContent
import eu.europa.ec.euidi.verifier.presentation.component.wrap.rememberButtonConfig
import eu.europa.ec.euidi.verifier.presentation.model.CountrySelectionHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.navigation.getFromPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.navigation.getFromRelevantBackStack
import eu.europa.ec.euidi.verifier.presentation.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.navigation.saveToPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.utils.Constants
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.generic_cancel
import eudiverifier.verifierapp.generated.resources.generic_done
import eudiverifier.verifierapp.generated.resources.generic_ok
import eudiverifier.verifierapp.generated.resources.zk_request_age_dialog_title
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val AGE_THRESHOLD_OPTIONS = listOf(15, 16, 18, 21)

@Composable
fun ZkRequestScreen(
    navController: NavController,
    viewModel: ZkRequestViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val stickyPrimaryButtonConfig = rememberButtonConfig(
        type = ButtonType.PRIMARY,
        enabled = state.primaryButtonEnabled,
        onClick = {
            viewModel.setEvent(ZkRequestContract.Event.OnDoneClick)
        },
        content = {
            Text(stringResource(Res.string.generic_done))
        }
    )
    val stickySecondaryButtonConfig = rememberButtonConfig(
        type = ButtonType.SECONDARY,
        onClick = {
            viewModel.setEvent(ZkRequestContract.Event.OnCancelClick)
        },
        content = {
            Text(stringResource(Res.string.generic_cancel))
        }
    )

    ContentScreen(
        navigatableAction = ScreenNavigateAction.BACKABLE,
        toolBarConfig = ToolbarConfig(
            title = state.screenTitle
        ),
        onBack = {
            viewModel.setEvent(ZkRequestContract.Event.OnCancelClick)
        },
        stickyBottom = { paddingValues ->
            WrapStickyBottomContent(
                stickyBottomModifier = Modifier.padding(paddingValues = paddingValues),
                stickyBottomConfig = StickyBottomConfig(
                    type = StickyBottomType.TwoButtons(
                        primaryButtonConfig = stickyPrimaryButtonConfig,
                        secondaryButtonConfig = stickySecondaryButtonConfig
                    )
                )
            )
        }
    ) { paddingValues ->
        Content(
            state = state,
            effectFlow = viewModel.effect,
            onEventSend = viewModel::setEvent,
            onNavigationRequested = { navigationEffect ->
                handleNavigationEffect(
                    effect = navigationEffect,
                    navController = navController
                )
            },
            paddingValues = paddingValues
        )
    }

    OneTimeLaunchedEffect {
        viewModel.setEvent(
            ZkRequestContract.Event.Init(
                doc = navController.getFromPreviousBackStack<RequestedDocumentUi>(
                    key = Constants.REQUESTED_DOCUMENTS
                )
            )
        )
    }

    // Runs again whenever this screen re-enters composition, e.g. after returning from the country
    // picker, so the chosen countries flow back in. Returns null (no-op) on the first composition.
    LaunchedEffect(Unit) {
        navController.getFromRelevantBackStack<CountrySelectionHolder>(
            key = Constants.COUNTRY_SELECTION_RESULT,
            remove = true
        )?.let { result ->
            viewModel.setEvent(ZkRequestContract.Event.OnCountriesSelected(result.countryCodes))
        }
    }

    if (state.ageDialogVisible) {
        AgeThresholdDialog(
            currentThreshold = state.selectedAgeThreshold,
            onConfirm = { viewModel.setEvent(ZkRequestContract.Event.OnAgeThresholdConfirmed(it)) },
            onDismiss = { viewModel.setEvent(ZkRequestContract.Event.OnAgeDialogDismissed) },
        )
    }
}

@Composable
private fun AgeThresholdDialog(
    currentThreshold: Int?,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var chosen by remember { mutableStateOf(currentThreshold) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.zk_request_age_dialog_title)) },
        text = {
            Column {
                AGE_THRESHOLD_OPTIONS.forEach { age ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { chosen = age }
                            .padding(vertical = SPACING_SMALL.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WrapRadioButton(
                            radioButtonData = RadioButtonDataUi(
                                isSelected = chosen == age,
                                onCheckedChange = { chosen = age }
                            )
                        )
                        Text(
                            text = age.toString(),
                            modifier = Modifier.padding(start = SPACING_SMALL.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            // At least one threshold must be picked before confirming.
            TextButton(
                enabled = chosen != null,
                onClick = { chosen?.let(onConfirm) }
            ) {
                Text(text = stringResource(Res.string.generic_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.generic_cancel))
            }
        }
    )
}

private fun handleNavigationEffect(
    effect: ZkRequestContract.Effect,
    navController: NavController,
) {
    when (effect) {
        is ZkRequestContract.Effect.Navigation.GoBack -> {
            navController.saveToPreviousBackStack(
                key = Constants.REQUESTED_DOCUMENTS,
                value = effect.requestedDocuments
            )
            navController.popBackStack()
        }

        is ZkRequestContract.Effect.Navigation.OpenCountrySelection -> {
            navController.saveToCurrentBackStack(
                key = Constants.COUNTRY_SELECTION_PRESELECTED,
                value = CountrySelectionHolder(countryCodes = effect.preSelectedCodes)
            )
            navController.navigate(route = NavItem.CountrySelection)
        }
    }
}

@Composable
private fun Content(
    state: ZkRequestContract.State,
    effectFlow: Flow<ZkRequestContract.Effect>,
    onEventSend: (ZkRequestContract.Event) -> Unit,
    onNavigationRequested: (ZkRequestContract.Effect.Navigation) -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
    ) {
        WrapListItems(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            items = state.items,
            onItemClick = {
                onEventSend(ZkRequestContract.Event.OnItemClicked(it.itemId))
            },
            clickableAreas = listOf(ClickableArea.TRAILING_CONTENT),
            mainContentVerticalPadding = SPACING_MEDIUM.dp
        )
    }

    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            when (effect) {
                is ZkRequestContract.Effect.Navigation -> onNavigationRequested(effect)
            }
        }
    }
}
