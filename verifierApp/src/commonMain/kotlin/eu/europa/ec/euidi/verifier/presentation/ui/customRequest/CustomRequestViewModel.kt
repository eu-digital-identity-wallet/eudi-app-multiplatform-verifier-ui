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

package eu.europa.ec.euidi.verifier.presentation.ui.customRequest

import androidx.lifecycle.SavedStateHandle
import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.model.SelectableClaimUi
import eu.europa.ec.euidi.verifier.presentation.model.SupportedDocument.AttestationType
import eu.europa.ec.euidi.verifier.utils.Constants
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CustomRequestViewModel(
    private val savedStateHandle: SavedStateHandle
)
    : BaseViewModel<CustomRequestViewModelContract.Event, CustomRequestViewModelContract.State, CustomRequestViewModelContract.Effect>() {
    override fun createInitialState(): CustomRequestViewModelContract.State = CustomRequestViewModelContract.State()

    override fun handleEvent(event: CustomRequestViewModelContract.Event) {
        when (event) {
            is CustomRequestViewModelContract.Event.Init -> {
                val doc = savedStateHandle.get<RequestedDocumentUi>(Constants.SAVED_STATE_REQUESTED_DOCUMENT) ?: event.doc

                setState {
                    copy(
                        fields = doc?.claims.orEmpty()
                    )
                }
            }

            is CustomRequestViewModelContract.Event.OnDoneClick -> {
                val doc = RequestedDocumentUi(
                    documentType = AttestationType.PID,
                    claims = currentState.fields
                )

                setEffect {
                    CustomRequestViewModelContract.Effect.Navigation.NavigateToHomeScreen(doc)
                }
            }

            is CustomRequestViewModelContract.Event.OnCancelClick -> {
                setEffect {
                    CustomRequestViewModelContract.Effect.Navigation.GoBack
                }
            }

            is CustomRequestViewModelContract.Event.OnItemChecked -> {
                setState {
                    copy(
                        fields = fields.map {
                            if (it.claim.identifier == event.identifier) {
                                it.copy(isSelected = event.checked)
                            } else it
                        }
                    )
                }

                println(uiState.value.fields)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        savedStateHandle.set(
            key = Constants.SAVED_STATE_REQUESTED_DOCUMENT,
            value = RequestedDocumentUi(
                documentType = currentState.docType,
                claims = currentState.fields
            )
        )
    }
}

sealed interface CustomRequestViewModelContract {
    sealed interface Event : UiEvent {
        data class Init(val doc: RequestedDocumentUi? = null) : Event
        data class OnItemChecked(val identifier: String, val checked: Boolean) : Event
        data object OnDoneClick : Event
        data object OnCancelClick : Event
    }
    data class State(
        val fields: List<SelectableClaimUi> = emptyList(),
        val docType: AttestationType = AttestationType.PID
    ) : UiState
    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class NavigateToHomeScreen(val requestedDocument: RequestedDocumentUi) : Navigation
            data object GoBack : Navigation
        }
    }
}