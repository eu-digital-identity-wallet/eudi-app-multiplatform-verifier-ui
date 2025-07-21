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

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.interactor.MenuInteractor
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModelContract.State
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuTypeUi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface MenuViewModelContract {
    data class State(
        val isLoading: Boolean,
        val screenTitle: String = "",
        val menuItems: List<MenuItemUi> = emptyList(),
    ) : UiState

    sealed interface Event : UiEvent {
        data object Init : Event
        data object OnBackClicked : Event
        data class MenuItemClicked(
            val itemType: MenuTypeUi,
        ) : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class PushScreen(
                val route: NavItem,
                val popUpTo: NavItem,
                val inclusive: Boolean,
            ) : Navigation

            data class PopTo(
                val route: NavItem,
                val inclusive: Boolean,
            ) : Navigation

            data object Pop : Navigation
        }
    }
}

@KoinViewModel
class MenuViewModel(
    private val interactor: MenuInteractor,
) : MviViewModel<Event, State, Effect>() {

    override fun createInitialState(): State {
        return State(
            isLoading = true,
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.Init -> {
                setTitleAndMenuItems()
            }

            is Event.OnBackClicked -> {
                setEffect { Effect.Navigation.Pop }
            }

            is Event.MenuItemClicked -> {
                handleMenuItemClicked(itemType = event.itemType)
            }
        }
    }

    private fun setTitleAndMenuItems() {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                )
            }

            val titleDeferred = async { interactor.getScreenTitle() }
            val menuItemsDeferred = async { interactor.getMenuItemsUi() }

            val screenTitle = titleDeferred.await()
            val menuItems = menuItemsDeferred.await()

            setState {
                copy(
                    screenTitle = screenTitle,
                    menuItems = menuItems,
                    isLoading = false
                )
            }
        }
    }

    private fun handleMenuItemClicked(itemType: MenuTypeUi) {
        when (itemType) {
            MenuTypeUi.HOME -> {
                popToHome()
            }

            MenuTypeUi.REVERSE_ENGAGEMENT -> {
                pushScreen(
                    route = NavItem.ReverseEngagement,
                    popUpTo = NavItem.Menu,
                    inclusive = false,
                )
            }

            MenuTypeUi.SETTINGS -> {
                pushScreen(
                    route = NavItem.Settings,
                    popUpTo = NavItem.Menu,
                    inclusive = false,
                )
            }
        }
    }

    private fun popToHome() {
        setEffect {
            Effect.Navigation.PopTo(
                route = NavItem.Home,
                inclusive = false,
            )
        }
    }

    private fun pushScreen(route: NavItem, popUpTo: NavItem, inclusive: Boolean) {
        setEffect {
            Effect.Navigation.PushScreen(
                route = route,
                popUpTo = popUpTo,
                inclusive = inclusive,
            )
        }
    }
}