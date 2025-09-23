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

package eu.europa.ec.euidi.verifier.presentation.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentScreen
import eu.europa.ec.euidi.verifier.presentation.component.content.ScreenNavigateAction
import eu.europa.ec.euidi.verifier.presentation.component.content.ToolbarConfig
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews
import eu.europa.ec.euidi.verifier.presentation.component.utils.OneTimeLaunchedEffect
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import eu.europa.ec.euidi.verifier.presentation.component.utils.VSpacer
import eu.europa.ec.euidi.verifier.presentation.component.wrap.ButtonType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomConfig
import eu.europa.ec.euidi.verifier.presentation.component.wrap.StickyBottomType
import eu.europa.ec.euidi.verifier.presentation.component.wrap.SwitchDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapListItem
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapStickyBottomContent
import eu.europa.ec.euidi.verifier.presentation.component.wrap.rememberButtonConfig
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModelContract.State
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsTypeUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.generic_cancel
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_methods_description
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_methods_title
import eudiverifier.verifierapp.generated.resources.settings_screen_category_data_retrieval_options_title
import eudiverifier.verifierapp.generated.resources.settings_screen_item_ble_central_client_description_unselected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_ble_central_client_title
import eudiverifier.verifierapp.generated.resources.settings_screen_item_clear_ble_description_unselected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_clear_ble_title
import eudiverifier.verifierapp.generated.resources.settings_screen_item_use_l2cap_description_unselected
import eudiverifier.verifierapp.generated.resources.settings_screen_item_use_l2cap_title
import eudiverifier.verifierapp.generated.resources.settings_screen_title
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val toolbarConfig = remember(state.screenTitle) {
        ToolbarConfig(
            title = state.screenTitle,
        )
    }

    ContentScreen(
        isLoading = state.isLoading,
        toolBarConfig = toolbarConfig,
        navigatableAction = ScreenNavigateAction.BACKABLE,
        onBack = { viewModel.setEvent(Event.OnBackClicked) },
        stickyBottom = { stickyBottomPaddings ->
            StickyBottomSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(stickyBottomPaddings),
                enabled = !state.isLoading,
                onClick = { viewModel.setEvent(Event.OnStickyButtonClicked) }
            )
        }
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
                        type = ButtonType.SECONDARY,
                        enabled = enabled,
                        onClick = onClick,
                        content = {
                            Text(text = stringResource(Res.string.generic_cancel))
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

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues),
    ) {
        items(
            items = state.settingsItems,
            key = {
                when (it) {
                    is SettingsItemUi.CategoryHeader -> it.title + it.description
                    is SettingsItemUi.CategoryItem -> it.type.prefKey
                }
            }
        ) { settingsItemUi ->
            when (settingsItemUi) {
                is SettingsItemUi.CategoryHeader -> {
                    SettingsCategoryHeader(
                        data = settingsItemUi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                is SettingsItemUi.CategoryItem -> {
                    SettingsCategoryItem(
                        data = settingsItemUi,
                        modifier = Modifier.fillMaxWidth(),
                        onItemClick = { settingsTypeUi ->
                            onEventSend(
                                Event.SettingsItemClicked(itemType = settingsTypeUi)
                            )
                        },
                    )

                    if (settingsItemUi.isLastInSection) {
                        VSpacer.Medium()
                    }
                }
            }
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
private fun SettingsCategoryHeader(
    data: SettingsItemUi.CategoryHeader,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = data.title,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )

        data.description?.let { safeDescription ->
            Text(
                text = safeDescription,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SettingsCategoryItem(
    data: SettingsItemUi.CategoryItem,
    modifier: Modifier = Modifier,
    onItemClick: (SettingsTypeUi) -> Unit,
) {
    WrapListItem(
        modifier = modifier,
        item = data.data,
        onItemClick = {
            onItemClick(data.type)
        },
        mainContentVerticalPadding = SPACING_MEDIUM.dp,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    )
}

@ThemeModePreviews
@Composable
private fun ContentPreview() {
    PreviewTheme {
        val settingsItems = listOf(
            SettingsItemUi.CategoryHeader(
                title = stringResource(Res.string.settings_screen_category_data_retrieval_options_title),
            ),
            SettingsItemUi.CategoryItem(
                type = SettingsTypeUi.UseL2Cap,
                data = ListItemDataUi(
                    itemId = "1",
                    mainContentData = ListItemMainContentDataUi.Text(
                        text = stringResource(Res.string.settings_screen_item_use_l2cap_title)
                    ),
                    supportingText = stringResource(Res.string.settings_screen_item_use_l2cap_description_unselected),
                    trailingContentData = ListItemTrailingContentDataUi.Switch(
                        switchData = SwitchDataUi(
                            isChecked = false,
                            enabled = true,
                        )
                    )
                ),
                isLastInSection = false,
            ),
            SettingsItemUi.CategoryItem(
                type = SettingsTypeUi.ClearBleCache,
                data = ListItemDataUi(
                    itemId = "2",
                    mainContentData = ListItemMainContentDataUi.Text(
                        text = stringResource(Res.string.settings_screen_item_clear_ble_title)
                    ),
                    supportingText = stringResource(Res.string.settings_screen_item_clear_ble_description_unselected),
                    trailingContentData = ListItemTrailingContentDataUi.Switch(
                        switchData = SwitchDataUi(
                            isChecked = true,
                            enabled = true,
                        )
                    )
                ),
                isLastInSection = true,
            ),
        )

        Content(
            state = State(
                isLoading = false,
                screenTitle = stringResource(Res.string.settings_screen_title),
                settingsItems = settingsItems,
            ),
            onEventSend = {},
            effectFlow = emptyFlow(),
            onNavigationRequested = {},
            paddingValues = PaddingValues(SPACING_MEDIUM.dp),
        )
    }
}

@ThemeModePreviews
@Composable
private fun SettingsCategoryHeaderPreview() {
    PreviewTheme {
        SettingsCategoryHeader(
            data = SettingsItemUi.CategoryHeader(
                title = stringResource(Res.string.settings_screen_category_data_retrieval_methods_title),
                description = stringResource(Res.string.settings_screen_category_data_retrieval_methods_description),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ThemeModePreviews
@Composable
private fun SettingsCategoryItemPreview() {
    PreviewTheme {
        SettingsCategoryItem(
            data = SettingsItemUi.CategoryItem(
                type = SettingsTypeUi.BleCentralClient,
                data = ListItemDataUi(
                    itemId = "1",
                    mainContentData = ListItemMainContentDataUi.Text(
                        text = stringResource(Res.string.settings_screen_item_ble_central_client_title)
                    ),
                    supportingText = stringResource(Res.string.settings_screen_item_ble_central_client_description_unselected),
                    trailingContentData = ListItemTrailingContentDataUi.Switch(
                        switchData = SwitchDataUi(
                            isChecked = false,
                            enabled = true,
                        )
                    )
                ),
                isLastInSection = true,
            ),
            modifier = Modifier.fillMaxWidth(),
            onItemClick = {},
        )
    }
}