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
import eu.europa.ec.euidi.verifier.domain.config.ClaimItem
import eu.europa.ec.euidi.verifier.domain.interactor.DocumentsToRequestInteractor
import eu.europa.ec.euidi.verifier.domain.transformer.UiTransformer
import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.utils.Constants
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CustomRequestViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val documentsToRequestInteractor: DocumentsToRequestInteractor,
    private val resourceProvider: ResourceProvider
) : BaseViewModel<CustomRequestContract.Event, CustomRequestContract.State, CustomRequestContract.Effect>() {
    override fun createInitialState(): CustomRequestContract.State = CustomRequestContract.State()

    override fun handleEvent(event: CustomRequestContract.Event) {
        when (event) {
            is CustomRequestContract.Event.Init -> {
                val doc = savedStateHandle.get<RequestedDocumentUi>(Constants.SAVED_STATE_REQUESTED_DOCUMENT) ?: event.doc

                doc?.let {
                    val claims = documentsToRequestInteractor.getDocumentClaims(attestationType = it.documentType)

                    val uiItems = UiTransformer.transformToUiItems(
                        fields = claims,
                        attestationType = doc.documentType,
                        resourceProvider = resourceProvider
                    )

                    setState {
                        copy(
                            requestedDoc = doc,
                            items = uiItems
                        )
                    }
                }
            }

            is CustomRequestContract.Event.OnDoneClick -> {
                uiState.value.requestedDoc?.let {
                    val reqDoc = it.copy(
                        documentType = it.documentType,
                        mode = it.mode,
                        claims = uiState.value.items.map { listItemDataUi ->
                            ClaimItem(
                                label = listItemDataUi.itemId
                            )
                        }
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

            is CustomRequestContract.Event.OnItemClicked -> {

                setState {
                    copy(
                        items = uiState.value.items.map { item ->
                            if (item.itemId == event.identifier) {
                                item.copy(
                                    trailingContentData = (item.trailingContentData as? ListItemTrailingContentDataUi.Checkbox)?.copy(
                                        checkboxData = CheckboxDataUi(
                                            isChecked = event.isChecked
                                        )
                                    )
                                )
                            } else {
                                item // unchanged
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

//        uiState.value.requestedDoc?.let {
//            savedStateHandle.set(
//                key = Constants.SAVED_STATE_REQUESTED_DOCUMENT,
//                value = RequestedDocumentUi(
//                    id = it.id,
//                    documentType = it.documentType,
//                    mode = it.mode,
//                    claims = uiState.value.fields
//                )
//            )
//        }
    }
}

sealed interface CustomRequestContract {
    sealed interface Event : UiEvent {
        data class Init(val doc: RequestedDocumentUi? = null) : Event
        data class OnItemClicked(val identifier: String, val isChecked: Boolean) : Event
        data object OnDoneClick : Event
        data object OnCancelClick : Event
    }

    data class State(
        val requestedDoc: RequestedDocumentUi? = null,
        val items: List<ListItemDataUi> = emptyList()
    ) : UiState

    sealed interface Effect : UiEffect {
        data class ShowToast(val message: String) : Effect
        sealed interface Navigation : Effect {
            data class GoBack(val requestedDocument: RequestedDocumentUi?) : Navigation
        }
    }
}