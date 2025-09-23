/*
 * Copyright (c) 2025 European Commission
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

package eu.europa.ec.euidi.verifier.presentation.ui.qr_scan

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.interactor.QrScanInteractor
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentErrorConfig
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.ui.qr_scan.QrScanViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.qr_scan.QrScanViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.qr_scan.QrScanViewModelContract.State
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface QrScanViewModelContract {
    data class State(
        val isLoading: Boolean,
        val error: ContentErrorConfig? = null,

        val screenTitle: String = "",
        val finishedScanning: Boolean = false,

        val requestedDocs: List<RequestedDocumentUi> = emptyList(),
    ) : UiState

    sealed interface Event : UiEvent {
        data class Init(
            val docs: List<RequestedDocumentUi>?
        ) : Event

        data object OnBackClicked : Event
        data object DismissError : Event
        data class OnQrScanned(val code: String) : Event
        data class OnQrScanFailed(val error: String) : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object Pop : Navigation
            data class NavigateToTransferStatusScreen(
                val requestedDocs: RequestedDocsHolder,
                val qrCode: String
            ) : Navigation
        }
    }
}

@KoinViewModel
class QrScanViewModel(
    private val interactor: QrScanInteractor,
) : MviViewModel<Event, State, Effect>() {

    override fun createInitialState(): State {
        return State(
            isLoading = true,
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.Init -> {
                setScreenTitle()

                event.docs?.let { safeDocs ->
                    setDocuments(safeDocs)
                } //TODO what happens if docs are null?

            }

            is Event.OnBackClicked -> {
                goBack()
            }

            is Event.DismissError -> {
                setState { copy(error = null) }
            }

            is Event.OnQrScanned -> {
                handleQrScanned(qrCode = event.code)
            }

            is Event.OnQrScanFailed -> {
                handleQrScanned(qrCode = null)
                handleQrScanFailed(error = event.error)
            }
        }
    }

    private fun setScreenTitle() {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                )
            }

            val screenTitle = interactor.getScreenTitle()

            setState {
                copy(
                    screenTitle = screenTitle,
                    isLoading = false
                )
            }
        }
    }

    private fun goToTransferStatusScreen(qrCode: String) {
        setEffect {
            Effect.Navigation.NavigateToTransferStatusScreen(
                requestedDocs = RequestedDocsHolder(
                    items = uiState.value.requestedDocs
                ),
                qrCode = qrCode
            )
        }
    }

    private fun handleQrScanFailed(error: String) {
        setState {
            copy(
                error = ContentErrorConfig(
                    errorSubTitle = error,
                    onCancel = {
                        setEvent(Event.DismissError)
                        goBack()
                    }
                )
            )
        }
    }

    private fun handleQrScanned(qrCode: String?) {
        if (uiState.value.finishedScanning) {
            return
        }

        qrCode?.let { safeQrCode ->
            if (!interactor.qrCodeIsValid(safeQrCode)) {
                return
            }

            setState {
                copy(finishedScanning = true)
            }
            goToTransferStatusScreen(safeQrCode)
        }
    }

    private fun goBack() {
        setEffect { Effect.Navigation.Pop }
    }

    private fun setDocuments(docs: List<RequestedDocumentUi>) {
        setState {
            copy(
                requestedDocs = docs
            )
        }
    }

}