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

package eu.europa.ec.euidi.verifier.presentation.ui.showDocument

import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import org.koin.android.annotation.KoinViewModel

interface ShowDocumentViewModelContract {
    sealed interface Event : UiEvent {
        data class Init(
            val items: List<ReceivedDocumentUi>
        ) : Event
        data object OnDoneClick : Event
        data object OnBackClick : Event
    }
    data class State(
        val message: String = "",
        val items: List<ReceivedDocumentUi> = emptyList()
    ) : UiState
    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object NavigateToHome : Navigation
        }
    }
}

@KoinViewModel
class ShowDocumentsViewModel() : BaseViewModel<ShowDocumentViewModelContract.Event, ShowDocumentViewModelContract.State, ShowDocumentViewModelContract.Effect>() {
    override fun createInitialState(): ShowDocumentViewModelContract.State = ShowDocumentViewModelContract.State()

    override fun handleEvent(event: ShowDocumentViewModelContract.Event) {
        when (event) {
            is ShowDocumentViewModelContract.Event.Init -> {
                setState {
                    copy(
                        items = event.items
                    )
                }
            }
            ShowDocumentViewModelContract.Event.OnDoneClick -> {
                setEffect {
                    ShowDocumentViewModelContract.Effect.Navigation.NavigateToHome
                }
            }
            ShowDocumentViewModelContract.Event.OnBackClick -> {

            }
        }
    }
}