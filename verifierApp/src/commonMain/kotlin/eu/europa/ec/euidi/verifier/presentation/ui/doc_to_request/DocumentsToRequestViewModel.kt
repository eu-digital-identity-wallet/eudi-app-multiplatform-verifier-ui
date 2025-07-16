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

package eu.europa.ec.euidi.verifier.presentation.ui.doc_to_request

import androidx.lifecycle.SavedStateHandle
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.config.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.Mode
import eu.europa.ec.euidi.verifier.domain.interactor.DocumentsToRequestInteractor
import eu.europa.ec.euidi.verifier.domain.model.SupportedDocumentUi
import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.SelectableClaimUi
import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument
import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument.AttestationType
import eu.europa.ec.euidi.verifier.presentation.utils.Constants
import eu.europa.ec.euidi.verifier.presentation.ui.docToRequest.DocToRequestContract.Effect.Navigation.NavigateToCustomRequestScreen
import eu.europa.ec.euidi.verifier.presentation.ui.docToRequest.DocToRequestContract.Effect.Navigation.NavigateToHomeScreen
import eu.europa.ec.euidi.verifier.provider.UuidProvider
import eu.europa.ec.euidi.verifier.utils.Constants
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DocumentsToRequestViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val interactor: DocumentsToRequestInteractor,
    private val uuidProvider: UuidProvider
) : MviViewModel<DocToRequestContract.Event, DocToRequestContract.State, DocToRequestContract.Effect>() {

    override fun createInitialState(): DocToRequestContract.State {
        val allSupportedDocuments = interactor.getSupportedDocuments()

        return DocToRequestContract.State(
            allSupportedDocuments = allSupportedDocuments,
            filteredDocuments = allSupportedDocuments
        )
    }

    override fun handleEvent(event: DocToRequestContract.Event) {
        when (event) {
            is DocToRequestContract.Event.Init -> {
                val requestedDocs = event.requestedDoc
                    ?.let { uiState.value.requestedDocuments + it }
                    ?: uiState.value.requestedDocuments

                setState {
                    copy(
                        requestedDocuments = requestedDocs,
                        isButtonEnabled = shouldEnableDoneButton(requestedDocs)
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

                    event.mode == Mode.CUSTOM -> {
                        val updatedDocs = if (currentDocs.any { it.id == event.docId && it.mode == Mode.FULL }) {
                            currentDocs.filterNot { it.id == event.docId }
                        } else {
                            currentDocs
                        }

                        setState { copy(requestedDocuments = updatedDocs) }

                        val customDoc = RequestedDocumentUi(
                            id = event.docId,
                            documentType = event.docType,
                            mode = event.mode,
                            claims = emptyList()
                        )

                        setEffect {
                            NavigateToCustomRequestScreen(customDoc)
                        }
                    }

                    else -> {
                        val claims = interactor.getDocumentClaims(event.docType)

                        // Add FULL doc directly
                        val newDoc = RequestedDocumentUi(
                            id = event.docId,
                            documentType = event.docType,
                            mode = event.mode,
                            claims = claims
                        )

                        val updatedRequestedDocs =  currentDocs + newDoc
                        setState {
                            copy(
                                requestedDocuments = updatedRequestedDocs,
                                isButtonEnabled = shouldEnableDoneButton(updatedRequestedDocs)
                            )
                        }
                    }
                }
            }

            DocToRequestContract.Event.OnBackClick -> {
                setEffect { NavigateToHomeScreen() }
            }

            DocToRequestContract.Event.OnDoneClick -> {
                setEffect {
                    NavigateToHomeScreen(
                        requestedDocuments = uiState.value.requestedDocuments
                    )
                }
            }

            is DocToRequestContract.Event.OnSearchQueryChanged -> {
                viewModelScope.launch {
                    val query = event.query

                    interactor.searchDocuments(
                        query = query,
                        documents = uiState.value.allSupportedDocuments
                    ).collect {
                        setState {
                            copy(
                                searchTerm = query,
                                filteredDocuments = it
                            )
                        }
                    }
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
        return requestedDocs.any { it.id in uiState.value.filteredDocuments.map { doc -> doc.id } }
    }
}

sealed interface DocToRequestContract {
    sealed interface Event : UiEvent {
        data class Init(val requestedDoc: RequestedDocumentUi?) : Event
        data class OnSearchQueryChanged(val query: String) : Event

        data class OnDocOptionSelected(
            val docId: String,
            val docType: AttestationType,
            val mode: Mode
        ) : Event

        data object OnBackClick : Event

        data object OnDoneClick : Event
    }

    data class State(
        val requestedDocuments: List<RequestedDocumentUi> = emptyList(),
        val allSupportedDocuments: List<SupportedDocumentUi> = emptyList(),
        val filteredDocuments: List<SupportedDocumentUi> = emptyList(),
        val searchTerm: String = "",
        val isButtonEnabled: Boolean = false
    ) : UiState

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class NavigateToHomeScreen(val requestedDocuments: List<RequestedDocumentUi> = emptyList()) :
                Navigation

            data class NavigateToCustomRequestScreen(val requestedDocuments: RequestedDocumentUi) :
                Navigation
        }
    }
}