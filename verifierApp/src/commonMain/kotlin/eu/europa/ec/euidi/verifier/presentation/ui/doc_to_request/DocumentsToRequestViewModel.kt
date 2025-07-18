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

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.config.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.Mode
import eu.europa.ec.euidi.verifier.domain.interactor.DocumentsToRequestInteractor
import eu.europa.ec.euidi.verifier.domain.model.SupportedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface DocToRequestContract {
    data class State(
        val requestedDocuments: List<RequestedDocumentUi> = emptyList(),
        val allSupportedDocuments: List<SupportedDocumentUi> = emptyList(),
        val filteredDocuments: List<SupportedDocumentUi> = emptyList(),
        val searchTerm: String = "",
        val isButtonEnabled: Boolean = false
    ) : UiState

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

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class NavigateToHomeScreen(
                val requestedDocuments: List<RequestedDocumentUi> = emptyList()
            ) : Navigation
            data class NavigateToCustomRequestScreen(
                val requestedDocuments: RequestedDocumentUi
            ) : Navigation
        }
    }
}

@KoinViewModel
class DocumentsToRequestViewModel(
    private val interactor: DocumentsToRequestInteractor,
) : MviViewModel<DocToRequestContract.Event, DocToRequestContract.State, DocToRequestContract.Effect>() {

    override fun createInitialState(): DocToRequestContract.State = DocToRequestContract.State()

    override fun handleEvent(event: DocToRequestContract.Event) {
        when (event) {
            is DocToRequestContract.Event.Init -> {
                viewModelScope.launch {
                    val allSupportedDocuments = uiState.value.allSupportedDocuments.ifEmpty {
                        interactor.getSupportedDocuments()
                    }

                    val currentDocs = uiState.value.requestedDocuments

                    val updatedDocs = event.requestedDoc?.let { requestedDocUi ->
                        currentDocs + interactor.checkDocumentMode(requestedDocUi)
                    } ?: currentDocs

                    setState {
                        copy(
                            allSupportedDocuments = allSupportedDocuments,
                            filteredDocuments = allSupportedDocuments,
                            requestedDocuments = updatedDocs,
                            isButtonEnabled = shouldEnableDoneButton(updatedDocs)
                        )
                    }
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
                            DocToRequestContract.Effect.Navigation.NavigateToCustomRequestScreen(
                                customDoc
                            )
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
                setEffect { DocToRequestContract.Effect.Navigation.NavigateToHomeScreen() }
            }

            DocToRequestContract.Event.OnDoneClick -> {
                setEffect {
                    DocToRequestContract.Effect.Navigation.NavigateToHomeScreen(
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

    private fun shouldEnableDoneButton(
        requestedDocs: List<RequestedDocumentUi> = uiState.value.requestedDocuments
    ): Boolean =
        requestedDocs.any {
            it.id in uiState.value.filteredDocuments.map { doc -> doc.id }
        }
}