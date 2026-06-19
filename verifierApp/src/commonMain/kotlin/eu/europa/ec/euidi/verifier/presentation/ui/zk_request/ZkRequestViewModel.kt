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

package eu.europa.ec.euidi.verifier.presentation.ui.zk_request

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimKind
import eu.europa.ec.euidi.verifier.domain.interactor.HandleItemSelectionPartialState
import eu.europa.ec.euidi.verifier.domain.interactor.ZkRequestInteractor
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.extension.hasAnyCheckedCheckbox
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import kotlinx.coroutines.launch

sealed interface ZkRequestContract {
    data class State(
        val screenTitle: String = "",
        val requestedDoc: RequestedDocumentUi? = null,
        val items: List<ListItemDataUi> = emptyList(),
        val primaryButtonEnabled: Boolean = false,
        val selectedCountries: List<String> = emptyList(),
        val selectedAgeThreshold: Int? = null,
        val ageDialogVisible: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class Init(val doc: RequestedDocumentUi? = null) : Event
        data class OnItemClicked(val identifier: String) : Event
        data object OnDoneClick : Event
        data object OnCancelClick : Event
        data class OnCountriesSelected(val countryCodes: List<String>) : Event
        data class OnAgeThresholdConfirmed(val threshold: Int) : Event
        data object OnAgeDialogDismissed : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class GoBack(val requestedDocuments: RequestedDocsHolder) : Navigation
            data class OpenCountrySelection(val preSelectedCodes: List<String>) : Navigation
        }
    }
}

class ZkRequestViewModel(
    private val interactor: ZkRequestInteractor,
) : MviViewModel<ZkRequestContract.Event, ZkRequestContract.State, ZkRequestContract.Effect>() {

    override fun createInitialState(): ZkRequestContract.State = ZkRequestContract.State()

    override fun handleEvent(event: ZkRequestContract.Event) {
        when (event) {
            is ZkRequestContract.Event.Init -> {
                viewModelScope.launch {
                    event.doc?.let { doc ->
                        val claims = interactor.getZkClaims(doc.documentType)
                        val uiItems = interactor.transformToUiItems(
                            documentType = doc.documentType,
                            claims = claims
                        )
                        val screenTitle = interactor.getScreenTitle(doc.documentType)
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

            is ZkRequestContract.Event.OnDoneClick -> {
                viewModelScope.launch {
                    uiState.value.requestedDoc?.let { doc ->
                        val sourceClaims = interactor.getZkClaims(doc.documentType)
                        val reqDoc = doc.copy(
                            claims = interactor.transformToClaimItems(
                                sourceClaims = sourceClaims,
                                items = uiState.value.items
                            )
                        )

                        setState { copy(requestedDoc = reqDoc) }
                        setEffect {
                            ZkRequestContract.Effect.Navigation.GoBack(
                                RequestedDocsHolder(items = listOf(reqDoc))
                            )
                        }
                    }
                }
            }

            is ZkRequestContract.Event.OnCancelClick -> {
                setEffect {
                    ZkRequestContract.Effect.Navigation.GoBack(
                        RequestedDocsHolder(items = emptyList())
                    )
                }
            }

            is ZkRequestContract.Event.OnItemClicked -> {
                val clickedClaim = uiState.value.requestedDoc?.let { doc ->
                    interactor.getZkClaims(doc.documentType).find { it.id == event.identifier }
                }

                val label = clickedClaim?.label

                when {
                    label == "nationality" && uiState.value.selectedCountries.isNotEmpty() -> {
                        val updatedItems = setRow(
                            uiState.value.items, event.identifier, checked = false, subtitle = null
                        )
                        setState {
                            copy(
                                selectedCountries = emptyList(),
                                items = updatedItems,
                                primaryButtonEnabled = updatedItems.hasAnyCheckedCheckbox(),
                            )
                        }
                    }

                    label == "nationality" -> {
                        setEffect {
                            ZkRequestContract.Effect.Navigation.OpenCountrySelection(
                                preSelectedCodes = uiState.value.selectedCountries
                            )
                        }
                    }

                    label == "birth_date" && uiState.value.selectedAgeThreshold != null -> {
                        val updatedItems = setRow(
                            uiState.value.items, event.identifier, checked = false, subtitle = null
                        )
                        setState {
                            copy(
                                selectedAgeThreshold = null,
                                items = updatedItems,
                                primaryButtonEnabled = updatedItems.hasAnyCheckedCheckbox(),
                            )
                        }
                    }

                    label == "birth_date" -> {
                        setState { copy(ageDialogVisible = true) }
                    }

                    else -> {
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
            }

            is ZkRequestContract.Event.OnCountriesSelected -> {
                val subtitle = interactor.selectedCountriesSubtitle(event.countryCodes.size)
                val hasCountries = event.countryCodes.isNotEmpty()
                val updatedItems = setRow(
                    uiState.value.items, claimId("nationality"), checked = hasCountries, subtitle = subtitle
                )
                setState {
                    copy(
                        selectedCountries = event.countryCodes,
                        items = updatedItems,
                        primaryButtonEnabled = updatedItems.hasAnyCheckedCheckbox(),
                    )
                }
            }

            is ZkRequestContract.Event.OnAgeThresholdConfirmed -> {
                val updatedItems = setRow(
                    uiState.value.items,
                    claimId("birth_date"),
                    checked = true,
                    subtitle = interactor.ageOverSubtitle(event.threshold),
                )
                setState {
                    copy(
                        selectedAgeThreshold = event.threshold,
                        ageDialogVisible = false,
                        items = updatedItems,
                        primaryButtonEnabled = updatedItems.hasAnyCheckedCheckbox(),
                    )
                }
            }

            is ZkRequestContract.Event.OnAgeDialogDismissed -> {
                setState { copy(ageDialogVisible = false) }
            }
        }
    }

    private fun setRow(
        items: List<ListItemDataUi>,
        rowId: String?,
        checked: Boolean,
        subtitle: String?,
    ): List<ListItemDataUi> = items.map { item ->
        if (item.itemId == rowId) {
            item.copy(
                supportingText = subtitle,
                trailingContentData = ListItemTrailingContentDataUi.Checkbox(
                    checkboxData = CheckboxDataUi(isChecked = checked)
                )
            )
        } else {
            item
        }
    }

    private fun claimId(elementLabel: String): String? =
        uiState.value.requestedDoc?.let { doc ->
            interactor.getZkClaims(doc.documentType)
                .firstOrNull { it.kind is ClaimKind.Zk && it.label == elementLabel }
                ?.id
        }
}
