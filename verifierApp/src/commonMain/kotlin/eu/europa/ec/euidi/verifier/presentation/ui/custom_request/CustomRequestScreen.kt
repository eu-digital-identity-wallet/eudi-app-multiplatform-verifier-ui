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

package eu.europa.ec.euidi.verifier.presentation.ui.custom_request

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.component.ClickableArea
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentScreen
import eu.europa.ec.euidi.verifier.presentation.component.content.ScreenNavigateAction
import eu.europa.ec.euidi.verifier.presentation.component.content.ToolbarConfig
import eu.europa.ec.euidi.verifier.presentation.component.extension.withStickyBottomPadding
import eu.europa.ec.euidi.verifier.presentation.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapListItems
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapStickyBottomContent
import eu.europa.ec.euidi.verifier.presentation.component.wrap.rememberButtonConfig
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.navigation.getFromPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.navigation.saveToPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.utils.Constants
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.generic_cancel
import eudiverifier.verifierapp.generated.resources.generic_done
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomRequestScreen(
    navController: NavController,
    viewModel: CustomRequestViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val stickyPrimaryButtonConfig = rememberButtonConfig(
        type = ButtonType.PRIMARY,
        enabled = state.primaryButtonEnabled,
        onClick = {
            viewModel.setEvent(CustomRequestContract.Event.OnDoneClick)
        },
        content = {
            Text(stringResource(Res.string.generic_done))
        }
    )
    val stickySecondaryButtonConfig = rememberButtonConfig(
        type = ButtonType.SECONDARY,
        onClick = {
            viewModel.setEvent(CustomRequestContract.Event.OnCancelClick)
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
            viewModel.setEvent(CustomRequestContract.Event.OnCancelClick)
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
            CustomRequestContract.Event.Init(
                doc = navController.getFromPreviousBackStack<RequestedDocumentUi>(
                    key = Constants.REQUESTED_DOCUMENTS
                )
            )
        )
    }
}

private fun handleNavigationEffect(
    effect: CustomRequestContract.Effect,
    navController: NavController,
) {
    when (effect) {
        is CustomRequestContract.Effect.Navigation.GoBack -> {
            navController.saveToPreviousBackStack(
                key = Constants.REQUESTED_DOCUMENTS,
                value = effect.requestedDocuments
            )
            navController.popBackStack()
        }
    }
}

@Composable
private fun Content(
    state: CustomRequestContract.State,
    effectFlow: Flow<CustomRequestContract.Effect>,
    onEventSend: (CustomRequestContract.Event) -> Unit,
    onNavigationRequested: (CustomRequestContract.Effect.Navigation) -> Unit,
    paddingValues: PaddingValues
) {
    WrapListItems(
        modifier = Modifier
            .withStickyBottomPadding(paddingValues)
            .verticalScroll(rememberScrollState()),
        items = state.items,
        onItemClick = {
            onEventSend(CustomRequestContract.Event.OnItemClicked(it.itemId))
        },
        clickableAreas = listOf(ClickableArea.TRAILING_CONTENT),
        mainContentVerticalPadding = SPACING_MEDIUM.dp
    )

    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            when (effect) {
                is CustomRequestContract.Effect.Navigation -> onNavigationRequested(effect)
            }
        }
    }
}