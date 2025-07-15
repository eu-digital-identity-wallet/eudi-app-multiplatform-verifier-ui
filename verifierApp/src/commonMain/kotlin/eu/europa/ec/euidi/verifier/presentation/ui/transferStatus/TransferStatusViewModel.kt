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

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.presentation.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.presentation.mvi.UiEffect
import eu.europa.ec.euidi.verifier.presentation.mvi.UiEvent
import eu.europa.ec.euidi.verifier.presentation.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.model.DocumentType
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface TransferStatusViewModelContract {
    sealed interface Event : UiEvent {
        data class Init(val docs: List<RequestedDocumentUi>) : Event
        data object OnCancelClick : Event
        data object OnBackClick : Event
    }
    data class State(
        val message: String = "",
        val connectionStatus: String = "",
        val requestedDocs: List<RequestedDocumentUi> = emptyList()
    ) : UiState

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object GoBack : Navigation
            data class NavigateToShowDocumentsScreen(val receivedDocuments: List<ReceivedDocumentUi>): Navigation
        }
    }
}

@KoinViewModel
class TransferStatusViewModel(
    private val uuidProvider: UuidProvider
) : BaseViewModel<TransferStatusViewModelContract.Event, TransferStatusViewModelContract.State, TransferStatusViewModelContract.Effect>() {
    override fun createInitialState(): TransferStatusViewModelContract.State = TransferStatusViewModelContract.State()

    override fun handleEvent(event: TransferStatusViewModelContract.Event) {
        when (event) {
            is TransferStatusViewModelContract.Event.Init -> {
                val requestDocsTypes = formatRequestedDocuments(event.docs)

                setState {
                    copy(
                        requestedDocs = event.docs.orEmpty(),
                        connectionStatus = "Connecting...",
                        message = "Requesting $requestDocsTypes"
                    )
                }

                viewModelScope.launch {
                    delay(4000)

                    setState {
                        copy(
                            connectionStatus = "Connected"
                        )
                    }

                    showDocumentResults()
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

        }
    }

    val dummyClaimData: Map<String, String> = mapOf(
        // Common Claims
        "Family Name(s)" to "Doe",
        "Given Name(s)" to "John",
        "Date of Birth" to "1990-01-01",
        "Expiry Date" to "2030-12-31",
        "Issuing Country" to "GR",
        "Issuing Authority" to "Greek Authority",
        "Document Number" to "AB1234567",
        "Portrait Image" to "base64_encoded_image_string",
        "Sex" to "M",
        "Nationality" to "Greek",
        "Issuing Jurisdiction" to "GR-Central",
        "Resident Address" to "123 Main Street, Athens",
        "Resident Country" to "GR",
        "Resident State" to "Attica",
        "Resident City" to "Athens",
        "Resident Postal Code" to "10434",
        "Age in Years" to "35",
        "Age at Birth" to "1990",
        "Age Over 18" to "true",

        // PID Claims
        "Issuance Date" to "2023-01-01",
        "Email Address" to "john.doe@example.com",
        "Resident Street" to "Main Street",
        "Resident House Number" to "123",
        "Personal Administrative Number" to "GR987654321",
        "Mobile Phone Number" to "+306912345678",
        "Birth Family Name" to "Doe",
        "Birth Given Name" to "John",
        "Place of Birth" to "Athens, Greece",
        "Trust Anchor" to "gov.gr"
    )

    private fun formatRequestedDocuments(docs: List<RequestedDocumentUi>?): String {
        if (docs == null) return ""

        return docs.joinToString(separator = "; ") { "${it.mode.displayName} ${it.documentType.displayName}" }
    }

    private fun showDocumentResults() {
        val documents = listOf(
            ReceivedDocumentUi(
                id = uuidProvider.provideUuid(),
                documentType = DocumentType.PID,
                claims = dummyClaimData
            )
        )

        setEffect {
            TransferStatusViewModelContract.Effect.Navigation.NavigateToShowDocumentsScreen(documents)
        }
    }
}