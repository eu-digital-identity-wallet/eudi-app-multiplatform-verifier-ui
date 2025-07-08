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

    override fun createInitialState(): DocToRequestContract.State = DocToRequestContract.State()

    override fun handleEvent(event: DocToRequestContract.Event) {
        when (event) {
            is DocToRequestContract.Event.Init -> {
                val requestedDocs = event.doc?.let {
                    currentState.requestedDocuments.toMutableList().apply {
                        add(it)
                    }
                }.orEmpty()

                setState {
                    copy(
                        supportedDocuments = AttestationType.entries.map { attestationType ->
                            SupportedDocument(
                                id = uuidProvider.provideUuid(),
                                documentType = attestationType,
                            )
                        },
                        requestedDocuments = requestedDocs
                    )
                }
            }

            is DocToRequestContract.Event.OnDocOptionSelected -> {
                val currentDocs = uiState.value.requestedDocuments.toMutableList()
                val alreadySelected = currentDocs.any {
                    it.documentType == event.docType && it.mode == event.mode
                }

                if (alreadySelected) {
                    // Remove it
                    setState {
                        copy(
                            requestedDocuments = currentDocs.apply {
                                removeAll { it.documentType == event.docType && it.mode == event.mode }
                            }
                        )
                    }
                } else {
                    if (event.mode == SupportedDocument.Mode.CUSTOM) {
                        // Navigate to custom screen — do not add yet
                        val customRequestedDocument = RequestedDocumentUi(
                            id = event.docId,
                            documentType = event.docType,
                            mode = event.mode,
                            claims = SelectableClaimUi.forType(event.docType)
                        )
                        setEffect {
                            DocToRequestContract.Effect.Navigation.NavigateToCustomRequestScreen(customRequestedDocument)
                        }
                    } else {
                        // Add FULL doc directly
                        setState {
                            copy(
                                requestedDocuments = currentDocs.apply {
                                    add(
                                        RequestedDocumentUi(
                                            id = event.docId,
                                            documentType = event.docType,
                                            mode = event.mode,
                                            claims = SelectableClaimUi.forType(event.docType)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            is DocToRequestContract.Event.OnDocFormatSelected -> {
                val doc = currentState.requestedDocuments.firstOrNull { it.id == event.docId }

                doc?.copy(
                    format = event.format
                )?.let {
                    setState {
                        copy(
                            requestedDocuments = currentState.requestedDocuments.toMutableList().apply {
                                set(
                                    index = indexOfFirst { doc -> doc.id == event.docId },
                                    element = it
                                )
                            }
                        )
                    }
                }
            }

            DocToRequestContract.Event.OnBackClick -> {
                setEffect { DocToRequestContract.Effect.Navigation.NavigateToHomeScreen() }
            }

            DocToRequestContract.Event.OnDoneClick -> {
                setEffect {
                    DocToRequestContract.Effect.Navigation.NavigateToHomeScreen(currentState.requestedDocuments)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        currentState.requestedDocuments.takeIf { it.isNotEmpty() }?.let {
            savedStateHandle.set(
                key = Constants.SAVED_STATE_REQUESTED_DOCUMENTS,
                value = currentState.requestedDocuments
            )
        }
    }
}

interface DocToRequestContract {
    sealed interface Event : UiEvent {
        data class Init(val doc: RequestedDocumentUi?) : Event
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
        val supportedDocuments: List<SupportedDocument> = emptyList()
    ) : UiState

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class NavigateToHomeScreen(val requestedDocument: List<RequestedDocumentUi> = emptyList()) : Navigation
            data class NavigateToCustomRequestScreen(val requestedDocuments: RequestedDocumentUi) : Navigation
        }
    }
}