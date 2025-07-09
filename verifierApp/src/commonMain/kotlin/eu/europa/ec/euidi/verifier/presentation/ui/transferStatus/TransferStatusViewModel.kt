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

package eu.europa.ec.euidi.verifier.presentation.ui.transferStatus

import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import org.koin.android.annotation.KoinViewModel

sealed interface TransferStatusViewModelContract {
    sealed interface Event : UiEvent {
        data object Init : Event
        data object OnCancelClick : Event
        data object OnBackClick : Event
        data object OnShowDocumentsClick : Event
    }
    data class State(val message: String = "") : UiState

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object GoBack : Navigation
            data object NavigateToShowDocumentsScreen : Navigation
        }
    }
}

@KoinViewModel
class TransferStatusViewModel : BaseViewModel<TransferStatusViewModelContract.Event, TransferStatusViewModelContract.State, TransferStatusViewModelContract.Effect>() {
    override fun createInitialState(): TransferStatusViewModelContract.State = TransferStatusViewModelContract.State()

    override fun handleEvent(event: TransferStatusViewModelContract.Event) {
        when (event) {
            TransferStatusViewModelContract.Event.Init -> {
                setState {
                    copy(message = "Transfer status")
                }
            }
            TransferStatusViewModelContract.Event.OnCancelClick -> {
                setEffect {
                    TransferStatusViewModelContract.Effect.Navigation.GoBack
                }
            }

            TransferStatusViewModelContract.Event.OnBackClick -> {
                setEffect {
                    TransferStatusViewModelContract.Effect.Navigation.GoBack
                }
            }
            TransferStatusViewModelContract.Event.OnShowDocumentsClick -> {
                setEffect {
                    TransferStatusViewModelContract.Effect.Navigation.NavigateToShowDocumentsScreen
                }
            }
        }
    }
}