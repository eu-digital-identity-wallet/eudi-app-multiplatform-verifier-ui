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
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.domain.interactor.DocSelectionResult
import eu.europa.ec.euidi.verifier.domain.interactor.DocumentsToRequestInteractor
import eu.europa.ec.euidi.verifier.domain.model.SupportedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
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
        data class Init(val requestedDocs: RequestedDocsHolder?) : Event
        data class OnSearchQueryChanged(val query: String) : Event
        data class OnDocOptionSelected(
            val docId: String,
            val docType: AttestationType,
            val mode: DocumentMode
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

                    val updatedDocs = event.requestedDocs?.let { requestedDocsUi ->
                        interactor.checkDocumentMode(currentDocs + requestedDocsUi.items)
                    }.orEmpty()

                    setState {
                        copy(
                            allSupportedDocuments = allSupportedDocuments,
                            filteredDocuments = allSupportedDocuments,
                            requestedDocuments = updatedDocs
                        )
                    }

                    checkEnableDoneButton(updatedDocs)
                }
            }

            is DocToRequestContract.Event.OnDocOptionSelected -> {
                viewModelScope.launch {
                    val result = interactor.handleDocumentOptionSelection(
                        currentDocs = uiState.value.requestedDocuments,
                        docId = event.docId,
                        docType = event.docType,
                        mode = event.mode
                    )

                    when (result) {
                        is DocSelectionResult.Updated -> {
                            setState { copy(requestedDocuments = result.docs) }
                            checkEnableDoneButton(result.docs)
                        }

                        is DocSelectionResult.NavigateToCustomRequest -> {
                            setState { copy(requestedDocuments = result.docs) }
                            setEffect {
                                DocToRequestContract.Effect.Navigation.NavigateToCustomRequestScreen(
                                    result.customDoc
                                )
                            }
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

    private fun checkEnableDoneButton(
        requestedDocs: List<RequestedDocumentUi> = uiState.value.requestedDocuments
    ) {
        requestedDocs.any {
            it.id in uiState.value.filteredDocuments.map { doc -> doc.id }
        }

        setState {
            copy(isButtonEnabled = requestedDocs.isNotEmpty())
        }
    }
}