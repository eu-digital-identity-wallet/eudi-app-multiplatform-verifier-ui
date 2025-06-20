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

import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import org.koin.android.annotation.KoinViewModel

sealed interface SettingsViewModelContract {
    sealed interface Event : UiEvent {
        data object Init : Event
        data object OnCancelClick : Event
        data object OnBackClick : Event
    }
    data class State(val message: String = "") : UiState
    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object GoBack : Navigation
        }
    }
}

@KoinViewModel
class SettingsViewModel() : BaseViewModel<SettingsViewModelContract.Event, SettingsViewModelContract.State, SettingsViewModelContract.Effect>() {
    override fun createInitialState(): SettingsViewModelContract.State = SettingsViewModelContract.State()

    override fun handleEvent(event: SettingsViewModelContract.Event) {
        when (event) {
            SettingsViewModelContract.Event.Init -> TODO()
            SettingsViewModelContract.Event.OnBackClick -> {
                setEffect {
                    SettingsViewModelContract.Effect.Navigation.GoBack
                }
            }
            SettingsViewModelContract.Event.OnCancelClick -> TODO()
        }
    }
}