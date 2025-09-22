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

package eu.europa.ec.euidi.verifier.presentation.ui.transfer_status

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_ADVERTISE
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_CONNECT
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_LE
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.COARSE_LOCATION
import dev.icerock.moko.permissions.location.LOCATION
import eu.europa.ec.euidi.verifier.core.extension.arePermissionsGranted
import eu.europa.ec.euidi.verifier.core.extension.provideAll
import eu.europa.ec.euidi.verifier.presentation.component.ErrorInfo
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentScreen
import eu.europa.ec.euidi.verifier.presentation.component.content.ScreenNavigateAction
import eu.europa.ec.euidi.verifier.presentation.component.content.ToolbarConfig
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews
import eu.europa.ec.euidi.verifier.presentation.component.utils.LifecycleEffect
import eu.europa.ec.euidi.verifier.presentation.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.euidi.verifier.presentation.component.utils.SIZE_SMALL
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_SMALL
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapCard
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapStickyBottomContent
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.navigation.getFromPreviousBackStack
import eu.europa.ec.euidi.verifier.presentation.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.ui.transfer_status.TransferStatusViewModelContract.Event.PermissionReceived
import eu.europa.ec.euidi.verifier.presentation.utils.Constants
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.generic_cancel
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_no_permissions
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_request_status
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_title
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource

@Composable
fun TransferStatusScreen(
    navController: NavController,
    viewModel: TransferStatusViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = ScreenNavigateAction.BACKABLE,
        toolBarConfig = ToolbarConfig(
            title = stringResource(Res.string.transfer_status_screen_title)
        ),
        onBack = {
            viewModel.setEvent(TransferStatusViewModelContract.Event.OnBackClick)
        },
        stickyBottom = { stickyBottomPaddings ->
            StickyBottomSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(stickyBottomPaddings),
                onClick = {
                    viewModel.setEvent(TransferStatusViewModelContract.Event.OnCancelClick)
                },
                enabled = !state.isLoading
            )
        }
    ) { paddingValues ->
        Content(
            state = state,
            effectFlow = viewModel.effect,
            onNavigationRequested = { navigationEffect ->
                handleNavigationEffect(
                    navController = navController,
                    navigationEffect = navigationEffect
                )
            },
            paddingValues = paddingValues,
            onEvent = { event -> viewModel.setEvent(event) }
        )

        LifecycleEffect(
            lifecycleOwner = LocalLifecycleOwner.current,
            lifecycleEvent = Lifecycle.Event.ON_RESUME
        ) {
            viewModel.setEvent(
                TransferStatusViewModelContract.Event.RequestPermissions
            )
        }

        OneTimeLaunchedEffect {
            val requestedDocs = navController.getFromPreviousBackStack<RequestedDocsHolder>(
                key = Constants.REQUESTED_DOCUMENTS
            )
            requestedDocs?.let {
                viewModel.setEvent(TransferStatusViewModelContract.Event.Init(requestedDocs.items))
            }
        }
    }
}

private fun handleNavigationEffect(
    navController: NavController,
    navigationEffect: TransferStatusViewModelContract.Effect.Navigation
) {
    when (navigationEffect) {
        is TransferStatusViewModelContract.Effect.Navigation.GoBack -> navController.popBackStack()

        is TransferStatusViewModelContract.Effect.Navigation.NavigateToShowDocumentsScreen -> {
            navController.saveToCurrentBackStack<ReceivedDocsHolder>(
                key = Constants.RECEIVED_DOCUMENTS,
                value = ReceivedDocsHolder(
                    items = navigationEffect.receivedDocuments,
                )
            )
            navController.navigate(NavItem.ShowDocuments)
        }
    }
}

@Composable
private fun StickyBottomSection(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = modifier
    ) {
        WrapStickyBottomContent(
            stickyBottomModifier = Modifier.fillMaxWidth(),
            stickyBottomConfig = StickyBottomConfig(
                type = StickyBottomType.OneButton(
                    config = ButtonConfig(
                        type = ButtonType.SECONDARY,
                        enabled = enabled,
                        onClick = onClick,
                        content = {
                            Text(
                                text = stringResource(Res.string.generic_cancel)
                            )
                        }
                    )
                )
            )
        )
    }
}

@Composable
private fun Content(
    state: TransferStatusViewModelContract.State,
    effectFlow: Flow<TransferStatusViewModelContract.Effect>,
    onNavigationRequested: (TransferStatusViewModelContract.Effect.Navigation) -> Unit,
    onEvent: (TransferStatusViewModelContract.Event) -> Unit,
    paddingValues: PaddingValues
) {

    val permissionsControllerFactory: PermissionsControllerFactory =
        rememberPermissionsControllerFactory()

    val permissionsController: PermissionsController = remember(permissionsControllerFactory) {
        permissionsControllerFactory.createPermissionsController()
    }

    BindEffect(permissionsController)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (state.hasPermissions == false) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ErrorInfo(
                    informativeText = stringResource(Res.string.transfer_status_screen_no_permissions),
                    modifier = Modifier
                        .clip(RoundedCornerShape(SIZE_SMALL.dp))
                        .clickable {
                            onEvent(TransferStatusViewModelContract.Event.OpenAppSettings)
                        }
                        .padding(SPACING_SMALL.dp),
                    isIconEnabled = true
                )
            }
        } else if (state.hasPermissions != null) {
            state.requestedDocTypes?.let { safeRequestedDocTypes ->
                HeaderInfo(
                    title = safeRequestedDocTypes,
                    subtitle = state.connectionStatus,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            when (effect) {
                is TransferStatusViewModelContract.Effect.Navigation -> {
                    onNavigationRequested(effect)
                }

                is TransferStatusViewModelContract.Effect.RequestPermissions -> {

                    val permissions = buildList {
                        add(Permission.BLUETOOTH_LE)
                        add(Permission.BLUETOOTH_SCAN)
                        add(Permission.BLUETOOTH_CONNECT)
                        add(Permission.BLUETOOTH_ADVERTISE)
                        add(Permission.COARSE_LOCATION)
                        add(Permission.LOCATION)
                    }

                    if (permissionsController.arePermissionsGranted(*permissions.toTypedArray())) {
                        onEvent(PermissionReceived(denied = false))
                    } else {
                        permissionsController.provideAll(*permissions.toTypedArray()).fold(
                            onSuccess = {
                                onEvent(PermissionReceived(denied = false))
                            },
                            onFailure = {
                                onEvent(PermissionReceived(denied = true))
                            }
                        )
                    }
                }

                is TransferStatusViewModelContract.Effect.PermissionsGranted -> {
                    onEvent(TransferStatusViewModelContract.Event.StartProximity)
                }

                is TransferStatusViewModelContract.Effect.PermissionsRevoked -> {
                    onEvent(TransferStatusViewModelContract.Event.StopProximity)
                }

                is TransferStatusViewModelContract.Effect.OpenAppSettings -> {
                    permissionsController.openAppSettings()
                }
            }
        }
    }
}

@Composable
private fun HeaderInfo(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    WrapCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(SPACING_MEDIUM.dp),
            verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val textStyle = MaterialTheme.typography.bodyLarge
            val textColor = MaterialTheme.colorScheme.onSurface

            Text(
                text = title,
                color = textColor,
                style = textStyle,
            )

            subtitle?.let { safeSubtitle ->
                Text(
                    text = stringResource(Res.string.transfer_status_screen_request_status) + safeSubtitle,
                    color = textColor,
                    style = textStyle,
                )
            }
        }
    }
}

@ThemeModePreviews
@Composable
private fun PreviewHeaderInfo() {
    PreviewTheme {
        HeaderInfo(
            title = "Title",
            subtitle = "Subtitle"
        )
    }
}

@ThemeModePreviews
@Composable
private fun PreviewHeaderInfoNoSubtitle() {
    PreviewTheme {
        HeaderInfo(
            title = "Title",
            subtitle = null
        )
    }
}