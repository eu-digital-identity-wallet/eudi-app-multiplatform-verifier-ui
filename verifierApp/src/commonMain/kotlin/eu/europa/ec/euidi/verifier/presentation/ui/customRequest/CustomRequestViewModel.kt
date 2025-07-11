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
    : BaseViewModel<CustomRequestContract.Event, CustomRequestContract.State, CustomRequestContract.Effect>() {
    override fun createInitialState(): CustomRequestContract.State = CustomRequestContract.State()

    override fun handleEvent(event: CustomRequestContract.Event) {
        when (event) {
            is CustomRequestContract.Event.Init -> {
                val doc = savedStateHandle.get<RequestedDocumentUi>(Constants.SAVED_STATE_REQUESTED_DOCUMENT) ?: event.doc

                setState {
                    copy(
                        requestedDoc = doc,
                        fields = SelectableClaimUi.forType(doc?.documentType ?: AttestationType.PID)
                    )
                }
            }

            is CustomRequestContract.Event.OnDoneClick -> {
                uiState.value.requestedDoc?.let {
                    val reqDoc = it.copy(
                        documentType = it.documentType,
                        mode = it.mode,
                        claims = uiState.value.fields
                    )

                    setState {
                        copy(
                            requestedDoc = reqDoc
                        )
                    }

                    setEffect {
                        CustomRequestContract.Effect.Navigation.GoBack(reqDoc)
                    }
                } ?: setEffect {
                    CustomRequestContract.Effect.ShowToast("Something went wrong")
                }
            }

            is CustomRequestContract.Event.OnCancelClick -> {
                setEffect {
                    CustomRequestContract.Effect.Navigation.GoBack(null)
                }
            }

            is CustomRequestContract.Event.OnItemChecked -> {
                setState {
                    copy(
                        fields = fields.map {
                            if (it.claim.key == event.identifier) {
                                it.copy(isSelected = event.checked)
                            } else it
                        }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        uiState.value.requestedDoc?.let {
            savedStateHandle.set(
                key = Constants.SAVED_STATE_REQUESTED_DOCUMENT,
                value = RequestedDocumentUi(
                    id = it.id,
                    documentType = it.documentType,
                    mode = it.mode,
                    claims = uiState.value.fields
                )
            )
        }
    }
}

sealed interface CustomRequestContract {
    sealed interface Event : UiEvent {
        data class Init(val doc: RequestedDocumentUi? = null) : Event
        data class OnItemChecked(val identifier: String, val checked: Boolean) : Event
        data object OnDoneClick : Event
        data object OnCancelClick : Event
    }
    data class State(
        val requestedDoc: RequestedDocumentUi? = null,
        val fields: List<SelectableClaimUi> = emptyList(),
        val docType: AttestationType = AttestationType.PID
    ) : UiState
    sealed interface Effect : UiEffect {
        data class ShowToast(val message: String) : Effect
        sealed interface Navigation : Effect {
            data class GoBack(val requestedDocument: RequestedDocumentUi?) : Navigation
        }
    }
}