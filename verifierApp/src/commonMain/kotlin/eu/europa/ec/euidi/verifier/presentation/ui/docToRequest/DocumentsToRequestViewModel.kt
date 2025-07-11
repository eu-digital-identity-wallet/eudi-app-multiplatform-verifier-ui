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

package eu.europa.ec.euidi.verifier.presentation.ui.docToRequest

import androidx.lifecycle.SavedStateHandle
import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.SelectableClaimUi
import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument
import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument.AttestationType
import eu.europa.ec.euidi.verifier.provider.UuidProvider
import eu.europa.ec.euidi.verifier.utils.Constants
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DocumentsToRequestViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val uuidProvider: UuidProvider
) : BaseViewModel<DocToRequestContract.Event, DocToRequestContract.State, DocToRequestContract.Effect>() {

    override fun createInitialState(): DocToRequestContract.State = DocToRequestContract.State(
        supportedDocuments = AttestationType.entries.map { attestationType ->
            SupportedDocument(
                id = uuidProvider.provideUuid(),
                documentType = attestationType,
                formats = SupportedDocument.formatForType(attestationType)
            )
        }
    )

    override fun handleEvent(event: DocToRequestContract.Event) {
        when (event) {
            is DocToRequestContract.Event.Init -> {
                val requestedDocs = event.requestedDoc
                    ?.let { uiState.value.requestedDocuments + it }
                    ?: uiState.value.requestedDocuments

                setState {
                    copy(
                        requestedDocuments = requestedDocs,
                        isButtonEnabled = shouldEnableDoneButton()
                    )
                }
            }

            is DocToRequestContract.Event.OnDocOptionSelected -> {
                val currentDocs = uiState.value.requestedDocuments
                val isAlreadySelected = currentDocs.any {
                    it.id == event.docId && it.mode == event.mode
                }

                when {
                    isAlreadySelected -> {
                        val updatedDocs = currentDocs.filterNot {
                            it.documentType == event.docType && it.mode == event.mode
                        }

                        setState {
                            copy(
                                requestedDocuments = updatedDocs,
                                isButtonEnabled = shouldEnableDoneButton(updatedDocs)
                            )
                        }
                    }

                    event.mode == SupportedDocument.Mode.CUSTOM -> {
                        val updatedDocs = if (currentDocs.any { it.id == event.docId && it.mode == SupportedDocument.Mode.FULL }) {
                            currentDocs.filterNot { it.id == event.docId }
                        } else {
                            currentDocs
                        }

                        setState { copy(requestedDocuments = updatedDocs) }

                        val customDoc = RequestedDocumentUi(
                            id = event.docId,
                            documentType = event.docType,
                            mode = event.mode,
                            claims = SelectableClaimUi.forType(event.docType)
                        )

                        setEffect {
                            DocToRequestContract.Effect.Navigation.NavigateToCustomRequestScreen(customDoc)
                        }
                    }

                    else -> {
                        // Add FULL doc directly
                        val newDoc = RequestedDocumentUi(
                            id = event.docId,
                            documentType = event.docType,
                            mode = event.mode,
                            claims = SelectableClaimUi.forType(event.docType)
                        )
                        setState {
                            copy(requestedDocuments = currentDocs + newDoc)
                        }
                    }
                }
            }

            is DocToRequestContract.Event.OnDocFormatSelected -> {
                val updatedList = uiState.value.requestedDocuments.map { doc ->
                    if (doc.id == event.docId) doc.copy(format = event.format) else doc
                }

                val isAnyFormatSelected = updatedList.any { it.format != null }

                setState {
                    copy(
                        requestedDocuments = updatedList,
                        isButtonEnabled = isAnyFormatSelected
                    )
                }
            }

            DocToRequestContract.Event.OnBackClick -> {
                setEffect { DocToRequestContract.Effect.Navigation.NavigateToHomeScreen() }
            }

            DocToRequestContract.Event.OnDoneClick -> {
                setEffect {
                    DocToRequestContract.Effect.Navigation.NavigateToHomeScreen(
                        requestedDocuments = uiState.value.requestedDocuments
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        uiState.value.requestedDocuments.takeIf { it.isNotEmpty() }?.let {
            savedStateHandle.set(
                key = Constants.SAVED_STATE_REQUESTED_DOCUMENTS,
                value = uiState.value.requestedDocuments
            )
        }
    }

    private fun shouldEnableDoneButton(requestedDocs: List<RequestedDocumentUi> = uiState.value.requestedDocuments): Boolean {
        return requestedDocs.any { it.format != null }
    }
}

sealed interface DocToRequestContract {
    sealed interface Event : UiEvent {
        data class Init(val requestedDoc: RequestedDocumentUi?) : Event
        data class OnDocOptionSelected(val docId: String, val docType: AttestationType, val mode: SupportedDocument.Mode) : Event
        data class OnDocFormatSelected(
            val docId: String,
            val format: SupportedDocument.DocumentFormat
        ) : Event
        data object OnBackClick : Event
        data object OnDoneClick : Event
    }

    data class State(
        val requestedDocuments: List<RequestedDocumentUi> = emptyList(),
        val supportedDocuments: List<SupportedDocument> = emptyList(),
        val isButtonEnabled: Boolean = false
    ) : UiState

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class NavigateToHomeScreen(val requestedDocuments: List<RequestedDocumentUi> = emptyList()) : Navigation
            data class NavigateToCustomRequestScreen(val requestedDocuments: RequestedDocumentUi) : Navigation
        }
    }
}