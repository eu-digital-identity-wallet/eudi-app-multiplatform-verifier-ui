/*
 * Copyright (c) 2025 European Commission
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

package eu.europa.ec.euidi.verifier.presentation.ui.doc_to_request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.model.SupportedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentScreen
import eu.europa.ec.euidi.verifier.presentation.component.content.ScreenNavigateAction
import eu.europa.ec.euidi.verifier.presentation.component.content.ToolbarConfig
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_LARGE
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import eu.europa.ec.euidi.verifier.presentation.component.utils.VSpacer
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapCard
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapChip
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapSearchBar
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapStickyBottomContent
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.navigation.getFromRelevantBackStack
import eu.europa.ec.euidi.verifier.presentation.navigation.popToAndSave
import eu.europa.ec.euidi.verifier.presentation.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.utils.Constants
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.documents_to_request_screen_request_header
import eudiverifier.verifierapp.generated.resources.documents_to_request_screen_search_placeholder
import eudiverifier.verifierapp.generated.resources.documents_to_request_screen_title
import eudiverifier.verifierapp.generated.resources.generic_done
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DocumentsToRequestScreen(
    navController: NavController,
    viewModel: DocumentsToRequestViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ContentScreen(
        navigatableAction = ScreenNavigateAction.BACKABLE,
        toolBarConfig = ToolbarConfig(
            title = stringResource(Res.string.documents_to_request_screen_title)
        ),
        onBack = {
            viewModel.setEvent(DocToRequestContract.Event.OnBackClick)
        },
        stickyBottom = {
            WrapStickyBottomContent(
                stickyBottomModifier = Modifier.padding(it),
                stickyBottomConfig = StickyBottomConfig(
                    type = StickyBottomType.OneButton(
                        config = ButtonConfig(
                            type = ButtonType.PRIMARY,
                            onClick = {
                                viewModel.setEvent(DocToRequestContract.Event.OnDoneClick)
                            },
                            enabled = state.isButtonEnabled,
                            content = {
                                Text(
                                    text = stringResource(
                                        Res.string.generic_done
                                    )
                                )
                            }
                        )
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
                    navigationEffect = navigationEffect,
                    navController = navController
                )
            },
            paddingValues = paddingValues
        )
    }

    LaunchedEffect(Unit) {
        navController.getFromRelevantBackStack<RequestedDocsHolder>(
            key = Constants.REQUESTED_DOCUMENTS,
            remove = true
        )?.let { requestedDocs ->
            viewModel.setEvent(DocToRequestContract.Event.Init(requestedDocs))
        }
    }
}

private fun handleNavigationEffect(
    navigationEffect: DocToRequestContract.Effect.Navigation,
    navController: NavController,
) {
    when (navigationEffect) {
        is DocToRequestContract.Effect.Navigation.NavigateToHomeScreen -> {
            navController.popToAndSave<RequestedDocsHolder>(
                destination = NavItem.Home,
                key = Constants.REQUESTED_DOCUMENTS,
                value = RequestedDocsHolder(items = navigationEffect.requestedDocuments)
            )
        }

        is DocToRequestContract.Effect.Navigation.NavigateToCustomRequestScreen -> {
            navController.saveToCurrentBackStack<RequestedDocumentUi>(
                key = Constants.REQUESTED_DOCUMENTS,
                value = navigationEffect.requestedDocuments
            ).let {
                navController.navigate(route = NavItem.CustomRequest)
            }
        }
    }
}

@Composable
private fun Content(
    state: DocToRequestContract.State,
    effectFlow: Flow<DocToRequestContract.Effect>,
    onEventSend: (DocToRequestContract.Event) -> Unit,
    onNavigationRequested: (DocToRequestContract.Effect.Navigation) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        WrapSearchBar(
            text = state.searchTerm,
            placeholder = stringResource(Res.string.documents_to_request_screen_search_placeholder),
            onValueChange = {
                onEventSend(DocToRequestContract.Event.OnSearchQueryChanged(it))
            },
            onClearClick = {
                onEventSend(DocToRequestContract.Event.OnSearchQueryChanged(""))
            }
        )

        VSpacer.Medium()

        state.filteredDocuments.forEach { supportedDoc ->
            SupportedDocumentItem(
                supportedDoc = supportedDoc,
                requestedDocuments = state.requestedDocuments,
                onEvent = onEventSend
            )

            VSpacer.Large()
        }
    }

    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            when (effect) {
                is DocToRequestContract.Effect.Navigation -> onNavigationRequested(effect)
            }
        }
    }
}

@Composable
fun SupportedDocumentItem(
    supportedDoc: SupportedDocumentUi,
    requestedDocuments: List<RequestedDocumentUi>,
    onEvent: (DocToRequestContract.Event) -> Unit,
) {
    Text(
        text = buildAnnotatedString {
            append(stringResource(Res.string.documents_to_request_screen_request_header))
            append(" ")
            append(supportedDoc.id)
        }
    )

    VSpacer.ExtraSmall()

    SupportedDocumentContentCard(
        supportedDoc = supportedDoc,
        requestedDocuments = requestedDocuments,
        onEvent = onEvent
    )
}

@Composable
fun SupportedDocumentContentCard(
    supportedDoc: SupportedDocumentUi,
    requestedDocuments: List<RequestedDocumentUi>,
    onEvent: (DocToRequestContract.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    WrapCard(
        modifier = modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = SPACING_LARGE.dp,
                    horizontal = SPACING_MEDIUM.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp)
        ) {
            supportedDoc.modes.forEach { mode ->
                val isSelected = requestedDocuments.any {
                    it.id == supportedDoc.id && it.mode == mode
                }

                val label = if (supportedDoc.documentType == AttestationType.AgeVerification) {
                    mode.displayName
                } else {
                    "${mode.displayName} ${supportedDoc.id}"
                }

                WrapChip(
                    leadingIcon = if (isSelected) AppIcons.Check else null,
                    label = {
                        Text(text = label)
                    },
                    selected = isSelected,
                    onClick = {
                        onEvent(
                            DocToRequestContract.Event.OnDocOptionSelected(
                                docId = supportedDoc.id,
                                docType = supportedDoc.documentType,
                                mode = mode
                            )
                        )
                    }
                )
            }
        }
    }
}