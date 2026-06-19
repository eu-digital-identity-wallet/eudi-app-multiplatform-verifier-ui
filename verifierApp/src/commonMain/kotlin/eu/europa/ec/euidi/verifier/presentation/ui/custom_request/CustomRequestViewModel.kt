/*
 * Copyright (c) 2026 European Commission
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

package eu.europa.ec.euidi.verifier.presentation.ui.custom_request

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimKind
import eu.europa.ec.euidi.verifier.domain.interactor.CustomRequestInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.HandleItemSelectionPartialState
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.extension.hasAnyCheckedCheckbox
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import kotlinx.coroutines.launch

sealed interface CustomRequestContract {
    data class State(
        val screenTitle: String = "",
        val requestedDoc: RequestedDocumentUi? = null,
        val items: List<ListItemDataUi> = emptyList(),
        val primaryButtonEnabled: Boolean = false,
        // Countries chosen for the nationality ZK predicate via the country picker. Held here for
        // pre-checking the picker on reopen. Not yet bound into the ZK claim value (later step).
        val selectedCountries: List<String> = emptyList(),
    ) : UiState {
        val areAllItemsChecked: Boolean
            get() = items.all { item ->
                if (item.trailingContentData is ListItemTrailingContentDataUi.Checkbox) {
                    item.trailingContentData.checkboxData.isChecked
                } else {
                    true
                }
            }
    }

    sealed interface Event : UiEvent {
        data class Init(val doc: RequestedDocumentUi? = null) : Event
        data class OnItemClicked(val identifier: String) : Event
        data object OnDoneClick : Event
        data object OnCancelClick : Event
        data class OnSelectAllClick(val isChecked: Boolean) : Event
        data class OnCountriesSelected(val countryCodes: List<String>) : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class GoBack(val requestedDocuments: RequestedDocsHolder) : Navigation
            data class OpenCountrySelection(val preSelectedCodes: List<String>) : Navigation
        }
    }
}

class CustomRequestViewModel(
    private val interactor: CustomRequestInteractor,
) : MviViewModel<CustomRequestContract.Event, CustomRequestContract.State, CustomRequestContract.Effect>() {
    override fun createInitialState(): CustomRequestContract.State = CustomRequestContract.State()

    override fun handleEvent(event: CustomRequestContract.Event) {
        when (event) {
            is CustomRequestContract.Event.Init -> {
                viewModelScope.launch {
                    val doc = event.doc

                    doc?.let {
                        val attestationType = it.documentType
                        val claims = interactor.getDocumentClaims(attestationType = attestationType)

                        val uiItems = interactor.transformToUiItems(
                            documentType = it.documentType, claims = claims
                        )

                        val screenTitle =
                            interactor.getScreenTitle(attestationType = attestationType)

                        setState {
                            copy(
                                screenTitle = screenTitle,
                                requestedDoc = doc,
                                items = uiItems,
                                primaryButtonEnabled = uiItems.hasAnyCheckedCheckbox(),
                            )
                        }
                    }
                }
            }

            is CustomRequestContract.Event.OnDoneClick -> {
                viewModelScope.launch {
                    uiState.value.requestedDoc?.let {
                        val sourceClaims = interactor.getDocumentClaims(it.documentType)
                        val reqDoc = it.copy(
                            claims = interactor.transformToClaimItems(
                                sourceClaims = sourceClaims,
                                items = uiState.value.items
                            )
                        )

                        setState {
                            copy(
                                requestedDoc = reqDoc
                            )
                        }

                        setEffect {
                            CustomRequestContract.Effect.Navigation.GoBack(
                                RequestedDocsHolder(
                                    items = listOf(reqDoc)
                                )
                            )
                        }
                    }
                }
            }

            is CustomRequestContract.Event.OnCancelClick -> {
                setEffect {
                    CustomRequestContract.Effect.Navigation.GoBack(
                        RequestedDocsHolder(
                            items = emptyList()
                        )
                    )
                }
            }

            is CustomRequestContract.Event.OnItemClicked -> {
                val clickedClaim = uiState.value.requestedDoc?.let { doc ->
                    interactor.getDocumentClaims(doc.documentType)
                        .find { it.id == event.identifier }
                }

                // The nationality ZK predicate isn't a simple toggle: tapping it opens the country
                // picker, pre-filled with the current selection.
                if (clickedClaim != null &&
                    clickedClaim.kind is ClaimKind.Zk &&
                    clickedClaim.label == "nationality"
                ) {
                    setEffect {
                        CustomRequestContract.Effect.Navigation.OpenCountrySelection(
                            preSelectedCodes = uiState.value.selectedCountries
                        )
                    }
                } else {
                    val result = interactor.handleItemSelection(
                        items = uiState.value.items,
                        identifier = event.identifier,
                    )

                    when (result) {
                        is HandleItemSelectionPartialState.Updated -> {
                            setState {
                                copy(
                                    items = result.items,
                                    primaryButtonEnabled = result.hasSelectedItems,
                                )
                            }
                        }
                    }
                }
            }

            is CustomRequestContract.Event.OnCountriesSelected -> {
                setState {
                    copy(selectedCountries = event.countryCodes)
                }
            }

            is CustomRequestContract.Event.OnSelectAllClick -> {
                val updatedItems = uiState.value.items.map { itemDataUi ->
                    if (itemDataUi.trailingContentData is ListItemTrailingContentDataUi.Checkbox) {
                        itemDataUi.copy(
                            trailingContentData = ListItemTrailingContentDataUi.Checkbox(
                                checkboxData = itemDataUi.trailingContentData.checkboxData.copy(
                                    isChecked = event.isChecked
                                )
                            )
                        )
                    } else {
                        itemDataUi
                    }
                }

                setState {
                    copy(
                        items = updatedItems,
                        primaryButtonEnabled = updatedItems.hasAnyCheckedCheckbox()
                    )
                }
            }
        }
    }
}