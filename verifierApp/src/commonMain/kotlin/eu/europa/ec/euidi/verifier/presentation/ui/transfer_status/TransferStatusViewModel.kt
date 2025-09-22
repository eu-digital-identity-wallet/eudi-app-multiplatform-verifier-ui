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
import eu.europa.ec.euidi.verifier.core.controller.TransferStatus
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.interactor.TransferStatusInteractor
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentsDomain
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_connected
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_connecting
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_device_engagement_completed
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_disconnected
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_error
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_on_response_received
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_status_request_sent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

sealed interface TransferStatusViewModelContract {
    data class State(
        val isLoading: Boolean = false,
        val requestedDocTypes: String? = null,
        val connectionStatus: String? = null,
        val requestedDocs: List<RequestedDocumentUi> = emptyList(),
        val hasPermissions: Boolean? = null,
        val permissionsRequestInProgress: Boolean = false,
        val engagementStarted: Boolean = false
    ) : UiState

    sealed interface Event : UiEvent {
        data class Init(val docs: List<RequestedDocumentUi>) : Event
        data object RequestPermissions : Event
        data class PermissionReceived(val denied: Boolean) : Event
        data object StartProximity : Event
        data object StopProximity : Event
        data object OnCancelClick : Event
        data object OnBackClick : Event
        data object OpenAppSettings : Event
    }

    sealed interface Effect : UiEffect {
        data object RequestPermissions : Effect
        data object PermissionsGranted : Effect
        data object PermissionsRevoked : Effect
        data object OpenAppSettings : Effect
        sealed interface Navigation : Effect {
            data object GoBack : Navigation
            data class NavigateToShowDocumentsScreen(
                val receivedDocuments: List<ReceivedDocumentUi>,
            ) : Navigation
        }
    }
}

@KoinViewModel
class TransferStatusViewModel(
    private val transferStatusInteractor: TransferStatusInteractor,
    private val resourceProvider: ResourceProvider,
    @InjectedParam private val qrCode: String
) : MviViewModel<TransferStatusViewModelContract.Event, TransferStatusViewModelContract.State, TransferStatusViewModelContract.Effect>() {

    override fun createInitialState(): TransferStatusViewModelContract.State =
        TransferStatusViewModelContract.State()

    override fun handleEvent(event: TransferStatusViewModelContract.Event) {
        when (event) {
            is TransferStatusViewModelContract.Event.Init -> {
                getDocuments(event.docs)
            }

            is TransferStatusViewModelContract.Event.OnCancelClick -> {
                goBack()
            }

            is TransferStatusViewModelContract.Event.OnBackClick -> {
                goBack()
            }

            is TransferStatusViewModelContract.Event.StartProximity -> {
                startProximity()
            }

            is TransferStatusViewModelContract.Event.StopProximity -> {
                viewModelScope.launch {
                    stopProximity()
                }
            }

            is TransferStatusViewModelContract.Event.RequestPermissions -> {
                if (uiState.value.permissionsRequestInProgress) {
                    return
                }
                setState {
                    copy(permissionsRequestInProgress = true)
                }
                setEffect {
                    TransferStatusViewModelContract.Effect.RequestPermissions
                }
            }

            is TransferStatusViewModelContract.Event.PermissionReceived -> {
                setState {
                    copy(
                        hasPermissions = !event.denied,
                        permissionsRequestInProgress = false
                    )
                }
                if (event.denied && uiState.value.engagementStarted) {
                    setEffect {
                        TransferStatusViewModelContract.Effect.PermissionsRevoked
                    }
                } else if (!event.denied && !uiState.value.engagementStarted) {
                    setEffect {
                        TransferStatusViewModelContract.Effect.PermissionsGranted
                    }
                }
            }

            is TransferStatusViewModelContract.Event.OpenAppSettings -> {
                setEffect {
                    TransferStatusViewModelContract.Effect.OpenAppSettings
                }
            }
        }
    }

    private fun getDocuments(docs: List<RequestedDocumentUi>) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val data = transferStatusInteractor.getRequestData(docs)
            setState {
                copy(
                    requestedDocs = docs,
                    requestedDocTypes = data,
                    isLoading = false,
                )
            }
        }
    }

    private suspend fun stopProximity() {
        setState {
            copy(
                engagementStarted = false,
                isLoading = false
            )
        }
        transferStatusInteractor.stopConnection()
    }

    private fun startProximity() {
        viewModelScope.launch {
            setState {
                copy(
                    engagementStarted = true,
                    isLoading = true
                )
            }

            transferStatusInteractor.prepareConnection()

            transferStatusInteractor.getConnectionStatus(
                docs = uiState.value.requestedDocs
            ).onEach { status ->
                handleStatus(status)
            }.launchIn(viewModelScope)

            transferStatusInteractor.startEngagement(qrCode = qrCode)
        }
    }

    private fun handleStatus(status: TransferStatus) {
        setState {
            copy(
                connectionStatus = status.toUiMessage(resourceProvider)
            )
        }

        when (status) {
            is TransferStatus.Error,
            is TransferStatus.Disconnected -> {
                goBack()
            }

            is TransferStatus.OnResponseReceived -> {
                viewModelScope.launch {
                    showDocumentResults(receivedDocs = status.receivedDocs)
                }
            }

            else -> Unit
        }
    }

    private suspend fun showDocumentResults(receivedDocs: ReceivedDocumentsDomain) {
        val transformedDocuments = transferStatusInteractor.transformToReceivedDocumentsUi(
            requestedDocuments = uiState.value.requestedDocs,
            receivedDocuments = receivedDocs.documents
        )

        setEffect {
            TransferStatusViewModelContract.Effect.Navigation.NavigateToShowDocumentsScreen(
                receivedDocuments = transformedDocuments,
            )
        }
    }

    private fun goBack() {
        viewModelScope.launch {
            stopProximity()
            setEffect {
                TransferStatusViewModelContract.Effect.Navigation.GoBack
            }
        }
    }

    private fun TransferStatus.toUiMessage(resourceProvider: ResourceProvider): String {
        val arguments = mutableListOf<String>()
        val resource = when (this) {
            is TransferStatus.Connected -> Res.string.transfer_status_screen_status_connected
            is TransferStatus.Connecting -> Res.string.transfer_status_screen_status_connecting
            is TransferStatus.DeviceEngagementCompleted -> Res.string.transfer_status_screen_status_device_engagement_completed
            is TransferStatus.Disconnected -> Res.string.transfer_status_screen_status_disconnected
            is TransferStatus.Error -> {
                arguments.add(this.message)
                Res.string.transfer_status_screen_status_error
            }

            is TransferStatus.OnResponseReceived -> Res.string.transfer_status_screen_status_on_response_received
            is TransferStatus.RequestSent -> Res.string.transfer_status_screen_status_request_sent
        }

        return resourceProvider.getSharedString(resource, arguments)
    }
}