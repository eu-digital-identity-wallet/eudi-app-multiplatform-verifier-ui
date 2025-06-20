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

package eu.europa.ec.euidi.verifier.presentation.ui.reverseengagement

import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import org.koin.android.annotation.KoinViewModel

sealed interface ReverseEngagementViewModelContract {
    sealed interface Event : UiEvent {
        data object Init : Event
        data object OnCancelClick : Event
        data object OnBackClick : Event
    }
    data class State(val message: String = "") : UiState
    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object NavigateToHome : Navigation
            data object GoBack : Navigation
        }
    }
}

@KoinViewModel
class ReverseEngagementViewModel() : BaseViewModel<ReverseEngagementViewModelContract.Event, ReverseEngagementViewModelContract.State, ReverseEngagementViewModelContract.Effect>() {
    override fun createInitialState(): ReverseEngagementViewModelContract.State = ReverseEngagementViewModelContract.State()

    override fun handleEvent(event: ReverseEngagementViewModelContract.Event) {
        when (event) {
            ReverseEngagementViewModelContract.Event.Init -> {}
            ReverseEngagementViewModelContract.Event.OnBackClick -> {
                setEffect { ReverseEngagementViewModelContract.Effect.Navigation.GoBack }
            }
            ReverseEngagementViewModelContract.Event.OnCancelClick -> {
                setEffect { ReverseEngagementViewModelContract.Effect.Navigation.GoBack }
            }
        }
    }
}