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

package eu.europa.ec.euidi.verifier.presentation.ui.settings

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.interactor.SettingsInteractor
import eu.europa.ec.euidi.verifier.presentation.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.presentation.mvi.UiEffect
import eu.europa.ec.euidi.verifier.presentation.mvi.UiEvent
import eu.europa.ec.euidi.verifier.presentation.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModelContract.State
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsTypeUi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface SettingsViewModelContract {
    data class State(
        val isLoading: Boolean,
        val screenTitle: String = "",

        val settingsItems: List<SettingsItemUi> = emptyList(),
    ) : UiState

    sealed interface Event : UiEvent {
        data object Init : Event
        data object OnBackClicked : Event
        data object OnStickyButtonClicked : Event

        data class SettingsItemClicked(
            val itemType: SettingsTypeUi,
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
class SettingsViewModel(
    private val interactor: SettingsInteractor,
) : BaseViewModel<Event, State, Effect>() {

    override fun createInitialState(): State {
        return State(
            isLoading = true,
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.Init -> {
                setTitleAndSettingsItems()
            }

            is Event.OnBackClicked -> {
                setEffect { Effect.Navigation.Pop }
            }

            is Event.OnStickyButtonClicked -> {
                popToHome()
            }

            is Event.SettingsItemClicked -> {
                handleSettingsItemClicked(type = event.itemType)
            }
        }
    }

    private fun setTitleAndSettingsItems() {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                )
            }

            val titleDeferred = async { interactor.getScreenTitle() }
            val settingsItemsDeferred = async { interactor.getSettingsItemsUi() }

            val screenTitle = titleDeferred.await()
            val settingsItems = settingsItemsDeferred.await()

            setState {
                copy(
                    screenTitle = screenTitle,
                    settingsItems = settingsItems,
                    isLoading = false
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

    private fun handleSettingsItemClicked(type: SettingsTypeUi) {
        viewModelScope.launch {
            interactor.togglePrefBoolean(type.prefKey)

            val updatedItems = interactor.getSettingsItemsUi()

            setState {
                copy(settingsItems = updatedItems)
            }
        }
    }

}