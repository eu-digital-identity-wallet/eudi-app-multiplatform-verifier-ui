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

package eu.europa.ec.euidi.verifier.presentation.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemLeadingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentScreen
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentTitle
import eu.europa.ec.euidi.verifier.presentation.component.content.ScreenNavigateAction
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews
import eu.europa.ec.euidi.verifier.presentation.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_SMALL
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapListItem
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModelContract.State
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuTypeUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.menu_screen_item_home_name
import eudiverifier.verifierapp.generated.resources.menu_screen_item_reverse_engagement_name
import eudiverifier.verifierapp.generated.resources.menu_screen_item_settings_name
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MenuScreen(
    navController: NavController,
    viewModel: MenuViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ContentScreen(
        isLoading = state.isLoading,
        navigatableAction = ScreenNavigateAction.BACKABLE,
        onBack = { viewModel.setEvent(Event.OnBackClicked) },
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

    OneTimeLaunchedEffect {
        viewModel.setEvent(Event.Init)
    }
}

private fun handleNavigationEffect(
    navigationEffect: Effect.Navigation,
    navController: NavController,
) {
    when (navigationEffect) {
        is Effect.Navigation.PushScreen -> {
            navController.navigate(route = navigationEffect.route) {
                popUpTo(route = navigationEffect.popUpTo) {
                    inclusive = navigationEffect.inclusive
                }
            }
        }

        is Effect.Navigation.PopTo -> {
            navController.popBackStack(
                route = navigationEffect.route,
                inclusive = navigationEffect.inclusive,
            )
        }

        is Effect.Navigation.Pop -> navController.popBackStack()
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
            .verticalScroll(rememberScrollState())
            .padding(paddingValues),
    ) {
        ContentTitle(
            title = state.screenTitle,
            modifier = Modifier.fillMaxWidth()
        )

        MenuOptions(
            menuOptions = state.menuItems,
            modifier = Modifier.fillMaxSize(),
            onEventSent = onEventSend,
        )
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
private fun MenuOptions(
    menuOptions: List<MenuItemUi>,
    modifier: Modifier = Modifier,
    onEventSent: (Event) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        menuOptions.forEachIndexed { index, menuOption ->
            WrapListItem(
                modifier = Modifier.fillMaxWidth(),
                mainContentVerticalPadding = SPACING_MEDIUM.dp,
                item = menuOption.data,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                onItemClick = {
                    onEventSent(
                        Event.MenuItemClicked(itemType = menuOption.type)
                    )
                }
            )

            if (index != menuOptions.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SPACING_SMALL.dp)
                )
            }
        }
    }
}

@ThemeModePreviews
@Composable
private fun TestPreview() {
    PreviewTheme {
        val chevronRightIcon = ListItemTrailingContentDataUi.Icon(
            iconData = AppIcons.ChevronRight
        )
        val menuItems = buildList {
            add(
                MenuItemUi(
                    type = MenuTypeUi.HOME,
                    data = ListItemDataUi(
                        itemId = "1",
                        mainContentData = ListItemMainContentDataUi.Text(
                            text = stringResource(
                                Res.string.menu_screen_item_home_name
                            )
                        ),
                        leadingContentData = ListItemLeadingContentDataUi.Icon(
                            iconData = AppIcons.Home
                        ),
                        trailingContentData = chevronRightIcon
                    )
                )
            )

            add(
                MenuItemUi(
                    type = MenuTypeUi.REVERSE_ENGAGEMENT,
                    data = ListItemDataUi(
                        itemId = "2",
                        mainContentData = ListItemMainContentDataUi.Text(
                            text = stringResource(
                                Res.string.menu_screen_item_reverse_engagement_name
                            )
                        ),
                        leadingContentData = ListItemLeadingContentDataUi.Icon(
                            iconData = AppIcons.ReverseEngagement
                        ),
                        trailingContentData = chevronRightIcon
                    )
                )
            )

            add(
                MenuItemUi(
                    type = MenuTypeUi.SETTINGS,
                    data = ListItemDataUi(
                        itemId = "3",
                        mainContentData = ListItemMainContentDataUi.Text(
                            text = stringResource(
                                Res.string.menu_screen_item_settings_name
                            )
                        ),
                        leadingContentData = ListItemLeadingContentDataUi.Icon(
                            iconData = AppIcons.Settings
                        ),
                        trailingContentData = chevronRightIcon
                    )
                )
            )
        }
        val state = State(
            isLoading = false,
            screenTitle = "Menu",
            menuItems = menuItems,
        )

        Content(
            state = state,
            onEventSend = {},
            effectFlow = emptyFlow(),
            onNavigationRequested = {},
            paddingValues = PaddingValues(SPACING_MEDIUM.dp),
        )
    }
}