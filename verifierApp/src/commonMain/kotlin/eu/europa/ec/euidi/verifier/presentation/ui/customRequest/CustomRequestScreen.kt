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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.navigation.getFromPreviousBackStack
import eu.europa.ec.euidi.verifier.navigation.saveToPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.component.ClickableArea
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentScreen
import eu.europa.ec.euidi.verifier.presentation.component.content.ScreenNavigateAction
import eu.europa.ec.euidi.verifier.presentation.component.content.ToolbarConfig
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapListItems
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapStickyBottomContent
import eu.europa.ec.euidi.verifier.presentation.component.wrap.rememberButtonConfig
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

    val checkedCount by remember(state.items) {
        derivedStateOf { state.items.count {
            (it.trailingContentData as? ListItemTrailingContentDataUi.Checkbox)?.checkboxData?.isChecked == true
        }}
    }

    val stickyPrimaryButtonConfig = rememberButtonConfig(
        type = ButtonType.PRIMARY,
        enabled = remember(state.items) {
            derivedStateOf { checkedCount != state.items.size }
        }.value,
        onClick = {
            viewModel.setEvent(CustomRequestContract.Event.OnDoneClick)
        },
        content = { Text("Done") }
    )
    val stickySecondaryButtonConfig = rememberButtonConfig(
        type = ButtonType.SECONDARY,
        onClick = {
            viewModel.setEvent(CustomRequestContract.Event.OnCancelClick)
        },
        content = { Text("Cancel") }
    )

    LaunchedEffect(Unit) {
        viewModel.setEvent(
            CustomRequestContract.Event.Init(
                doc = navController.getFromPreviousBackStack<RequestedDocumentUi>(Constants.REQUESTED_DOCUMENTS)
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
                            key = Constants.REQUESTED_DOCUMENTS,
                            value = effect.requestedDocument
                        )
                    }
                    navController.popBackStack()
                }
            }
        }
    }

    ContentScreen(
        navigatableAction = ScreenNavigateAction.BACKABLE,
        toolBarConfig = ToolbarConfig(
            title = "Custom ${state.requestedDoc?.documentType?.displayName} request"
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
        WrapListItems(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            items = state.items,
            onItemClick = {
                val itemChecked = (it.trailingContentData as ListItemTrailingContentDataUi.Checkbox).checkboxData.isChecked

                viewModel.setEvent(CustomRequestContract.Event.OnItemClicked(it.itemId, !itemChecked))
            },
            clickableAreas = listOf(ClickableArea.TRAILING_CONTENT),
            mainContentVerticalPadding = SPACING_MEDIUM.dp
        )
    }
}