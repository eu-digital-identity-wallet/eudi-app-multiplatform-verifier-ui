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

import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.SelectableClaimUi
import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument
import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument.AttestationType
import eu.europa.ec.euidi.verifier.utils.safeLet
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DocToRequestViewModel() :
    BaseViewModel<DocToRequestViewModelContract.Event, DocToRequestViewModelContract.State, DocToRequestViewModelContract.Effect>() {

    override fun createInitialState(): DocToRequestViewModelContract.State = DocToRequestViewModelContract.State()

    override fun handleEvent(event: DocToRequestViewModelContract.Event) {
        when (event) {
            is DocToRequestViewModelContract.Event.Init -> {
                setState {
                    copy(
                        supportedDocuments = AttestationType.entries.map {
                            SupportedDocument(
                                documentType = it,
                            )
                        }
                    )
                }
            }

            is DocToRequestViewModelContract.Event.OnOptionSelected -> {
                setState {
                    copy(
                        selectedDocType = event.docType,
                        selectedMode = event.mode
                    )
                }
            }

            DocToRequestViewModelContract.Event.OnBackClick -> {
                setEffect { DocToRequestViewModelContract.Effect.Navigation.NavigateToHomeScreen(null) }
            }
            DocToRequestViewModelContract.Event.OnDoneClick -> {
                safeLet(
                    currentState.selectedDocType,
                    currentState.selectedMode
                ) { type, mode ->
                    val doc = RequestedDocumentUi(
                        documentType = type,
                        claims = SelectableClaimUi.forType(type)
                    )

                    val navigationEffect = when (mode) {
                        SupportedDocument.Mode.CUSTOM -> DocToRequestViewModelContract.Effect.Navigation.NavigateToCustomRequestScreen(
                            doc
                        )

                        SupportedDocument.Mode.FULL -> DocToRequestViewModelContract.Effect.Navigation.NavigateToHomeScreen(
                            doc
                        )
                    }

                    setEffect { navigationEffect }
                }
            }
        }
    }
}

interface DocToRequestViewModelContract {
    sealed interface Event : UiEvent {
        data class Init(val doc: RequestedDocumentUi?) : Event
        data class OnOptionSelected(val docType: AttestationType, val mode: SupportedDocument.Mode) : Event
        data object OnBackClick : Event
        data object OnDoneClick : Event
    }
    data class State(
        val requestedDocument: RequestedDocumentUi? = null,
        val supportedDocuments: List<SupportedDocument> = emptyList(),
        val selectedDocType: AttestationType? = null,
        val selectedMode: SupportedDocument.Mode? = null
    ) : UiState
    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class NavigateToHomeScreen(val requestedDocument: RequestedDocumentUi?) : Navigation
            data class NavigateToCustomRequestScreen(val requestedDocument: RequestedDocumentUi) : Navigation
        }
    }
}