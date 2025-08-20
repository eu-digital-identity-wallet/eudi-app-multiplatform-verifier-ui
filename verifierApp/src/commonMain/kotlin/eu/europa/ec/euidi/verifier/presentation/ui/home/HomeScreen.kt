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

package eu.europa.ec.euidi.verifier.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.component.AppIconAndText
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentScreen
import eu.europa.ec.euidi.verifier.presentation.component.content.ScreenNavigateAction
import eu.europa.ec.euidi.verifier.presentation.component.utils.LifecycleEffect
import eu.europa.ec.euidi.verifier.presentation.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_EXTRA_LARGE
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_SMALL
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapIconButton
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapImage
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapListItem
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapStickyBottomContent
import eu.europa.ec.euidi.verifier.presentation.component.wrap.rememberButtonConfig
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.navigation.getFromCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.navigation.saveToCurrentBackStack
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModelContract.State
import eu.europa.ec.euidi.verifier.presentation.utils.Constants
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.home_screen_nfc_title
import eudiverifier.verifierapp.generated.resources.home_screen_sticky_bottom_text
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ContentScreen(
        isLoading = state.isLoading,
        topBar = {
            TopBar(onMenuClick = { viewModel.setEvent(Event.OnMenuClick) })
        },
        navigatableAction = ScreenNavigateAction.NONE,
        onBack = { viewModel.setEvent(Event.OnBackClicked) },
        stickyBottom = { stickyBottomPaddings ->
            StickyBottomSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(stickyBottomPaddings),
                enabled = state.isStickyButtonEnabled,
                onClick = { viewModel.setEvent(Event.OnStickyButtonClicked) }
            )
        },
        contentErrorConfig = state.error,
    ) { padding ->
        Content(
            state = state,
            onEventSend = viewModel::setEvent,
            effectFlow = viewModel.effect,
            onNavigationRequested = { navigationEffect ->
                handleNavigationEffect(navigationEffect, navController)
            },
            paddingValues = padding,
        )

    }

    LifecycleEffect(
        lifecycleOwner = LocalLifecycleOwner.current,
        lifecycleEvent = Lifecycle.Event.ON_RESUME
    ) {
        val documents = navController
            .getFromCurrentBackStack<RequestedDocsHolder>(Constants.REQUESTED_DOCUMENTS)
        viewModel.setEvent(Event.OnResume(docs = documents?.items))
    }

    OneTimeLaunchedEffect {
        viewModel.setEvent(Event.Init)
    }
}

@Composable
private fun TopBar(
    onMenuClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = SPACING_SMALL.dp)
    ) {
        WrapIconButton(
            iconData = AppIcons.Menu,
            modifier = Modifier.align(Alignment.CenterStart),
            customTint = MaterialTheme.colorScheme.onSurface,
            onClick = onMenuClick,
        )

        AppIconAndText(
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private fun handleNavigationEffect(
    navigationEffect: Effect.Navigation,
    navController: NavController,
) {
    when (navigationEffect) {
        is Effect.Navigation.PushScreen -> {
            navController.navigate(route = navigationEffect.route)
        }

        is Effect.Navigation.SaveDocsToBackstackAndGoTo -> {
            navController.saveToCurrentBackStack<RequestedDocsHolder>(
                key = Constants.REQUESTED_DOCUMENTS,
                value = navigationEffect.requestedDocs
            )
            navController.navigate(route = navigationEffect.screen)
        }
    }
}

@Composable
private fun StickyBottomSection(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(modifier = modifier) {
        WrapStickyBottomContent(
            stickyBottomModifier = Modifier.fillMaxWidth(),
            stickyBottomConfig = StickyBottomConfig(
                type = StickyBottomType.OneButton(
                    config = rememberButtonConfig(
                        type = ButtonType.PRIMARY,
                        enabled = enabled,
                        onClick = onClick,
                        content = {
                            Text(text = stringResource(Res.string.home_screen_sticky_bottom_text))
                        }
                    )
                )
            )
        )
    }
}

@Composable
private fun Content(
    state: State,
    onEventSend: (Event) -> Unit,
    effectFlow: Flow<Effect>,
    onNavigationRequested: (Effect.Navigation) -> Unit,
    paddingValues: PaddingValues,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        ScreenTitle(
            title = state.screenTitle,
            modifier = Modifier.fillMaxWidth()
        )

        state.mainButtonData?.let { safeMainButtonData ->
            MainSection(
                mainButtonData = safeMainButtonData,
                modifier = Modifier.fillMaxSize(),
                onTapToCreateRequest = {
                    onEventSend(Event.OnTapToCreateRequest)
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            when (effect) {
                is Effect.Navigation -> onNavigationRequested(effect)
            }
        }
    }
}

@Composable
private fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
private fun MainSection(
    mainButtonData: ListItemDataUi,
    modifier: Modifier = Modifier,
    onTapToCreateRequest: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        MainButton(
            data = mainButtonData,
            modifier = Modifier.fillMaxWidth(),
            onClick = onTapToCreateRequest,
        )
        NfcSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SPACING_EXTRA_LARGE.dp)
        )
    }
}

@Composable
private fun MainButton(
    modifier: Modifier = Modifier,
    data: ListItemDataUi,
    onClick: () -> Unit,
) {
    WrapListItem(
        modifier = modifier,
        item = data,
        onItemClick = { onClick() },
        mainContentVerticalPadding = SPACING_MEDIUM.dp
    )
}

@Composable
private fun NfcSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(
            space = SPACING_SMALL.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.home_screen_nfc_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
        )
        WrapImage(
            modifier = Modifier.padding(SPACING_MEDIUM.dp),
            iconData = AppIcons.Nfc
        )
    }
}