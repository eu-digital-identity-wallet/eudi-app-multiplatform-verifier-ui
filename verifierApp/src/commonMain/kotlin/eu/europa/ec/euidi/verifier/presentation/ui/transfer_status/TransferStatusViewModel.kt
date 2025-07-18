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
    val dummyClaims1: Map<String, String> = mapOf(
        "family_name" to "Doe",
        "given_name" to "John",
        "birth_date" to "1985-07-12",
        "sex" to "Male",
        "nationality" to "US",
        "document_number" to "AA1234567",
        "issuing_country" to "US",
        "expiry_date" to "2030-01-15",
        "resident_address" to "123 Main Street, Springfield",
        "mobile_phone_number" to "+1 555 0001111",
        "email_address" to "john.doe@example.com",
        "place_of_birth" to "Springfield, Illinois"
    )

    val dummyClaims2: Map<String, String> = mapOf(
        "family_name" to "Smith",
        "given_name" to "Jane",
        "birth_date" to "1990-03-22",
        "sex" to "Female",
        "nationality" to "CA",
        "document_number" to "BB7654321",
        "issuing_country" to "CA",
        "expiry_date" to "2031-05-10",
        "resident_address" to "456 Maple Avenue, Toronto",
        "mobile_phone_number" to "+1 416 5552222",
        "email_address" to "jane.smith@example.com",
        "place_of_birth" to "Toronto, Ontario"
    )

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

    private fun showDocumentResults() {
        val allClaims = listOf(dummyClaims1, dummyClaims2)
        val address = "ble:peripheral_server_mode:uuid=4f0eacf2-963 4-4838-a6dc-65d740aadcf0"

        val transformedDocuments =
            transferStatusInteractor.transformToReceivedDocumentsUi(allClaims)

        setEffect {
            TransferStatusViewModelContract.Effect.Navigation.NavigateToShowDocumentsScreen(
                receivedDocuments = transformedDocuments,
                address = address
            )
        }
    }
}