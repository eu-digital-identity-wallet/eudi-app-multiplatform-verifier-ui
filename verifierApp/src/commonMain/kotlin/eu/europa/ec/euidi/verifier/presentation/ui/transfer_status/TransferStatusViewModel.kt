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

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.interactor.TransferStatusInteractor
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface TransferStatusViewModelContract {
    data class State(
        val requestedDocTypes: String = "",
        val connectionStatus: String = "",
        val requestedDocs: List<RequestedDocumentUi> = emptyList()
    ) : UiState

    sealed interface Event : UiEvent {
        data class Init(val docs: List<RequestedDocumentUi>) : Event
        data object OnCancelClick : Event
        data object OnBackClick : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object GoBack : Navigation
            data class NavigateToShowDocumentsScreen(
                val receivedDocuments: List<ReceivedDocumentUi>,
                val address: String
            ) : Navigation
        }
    }
}

@KoinViewModel
class TransferStatusViewModel(
    private val transferStatusInteractor: TransferStatusInteractor,
) : MviViewModel<TransferStatusViewModelContract.Event, TransferStatusViewModelContract.State, TransferStatusViewModelContract.Effect>() {

    override fun createInitialState(): TransferStatusViewModelContract.State =
        TransferStatusViewModelContract.State()

    override fun handleEvent(event: TransferStatusViewModelContract.Event) {
        when (event) {
            is TransferStatusViewModelContract.Event.Init -> {
                viewModelScope.launch {
                    val data = transferStatusInteractor.getRequestData(event.docs)

                    setState {
                        copy(
                            requestedDocs = event.docs,
                            requestedDocTypes = data
                        )
                    }

                    transferStatusInteractor.getConnectionStatus().collect { status ->
                        setState {
                            copy(
                                connectionStatus = status
                            )
                        }
                    }

                    showDocumentResults()
                }
            }

            is TransferStatusViewModelContract.Event.OnCancelClick -> {
                setEffect {
                    TransferStatusViewModelContract.Effect.Navigation.GoBack
                }
            }

            is TransferStatusViewModelContract.Event.OnBackClick -> {
                setEffect {
                    TransferStatusViewModelContract.Effect.Navigation.GoBack
                }
            }

        }
    }

    private suspend fun showDocumentResults() {
        val allDocuments = transferStatusInteractor.getAvailableDocuments()
        val address = "ble:peripheral_server_mode:uuid=4f0eacf2-963 4-4838-a6dc-65d740aadcf0"

        val transformedDocuments = transferStatusInteractor.transformToReceivedDocumentsUi(
            requestedDocuments = uiState.value.requestedDocs,
            allDocuments = allDocuments
        )

        setEffect {
            TransferStatusViewModelContract.Effect.Navigation.NavigateToShowDocumentsScreen(
                receivedDocuments = transformedDocuments,
                address = address
            )
        }
    }
}