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
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimKind
import eu.europa.ec.euidi.verifier.domain.config.model.ZkPredicateValue
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
        val claims: List<ClaimItem> = emptyList(),
        val primaryButtonEnabled: Boolean = false,
        val ageDialogVisible: Boolean = false,
    ) : UiState {

        /** The age threshold currently set on the date-of-birth predicate, or null. */
        val ageThreshold: Int?
            get() = zkValue("birth_date")?.let { it as? ZkPredicateValue.AgeOver }?.years

        /** The accepted countries currently set on the nationality predicate, or empty. */
        val acceptedCountries: List<String>
            get() = zkValue("nationality")?.let { it as? ZkPredicateValue.NationalityIn }
                ?.countries
                .orEmpty()

        private fun zkValue(label: String): ZkPredicateValue? =
            (claims.firstOrNull { it.label == label }?.kind as? ClaimKind.Zk)?.value
    }

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
                                claims = claims,
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
                        val reqDoc = doc.copy(
                            claims = interactor.transformToClaimItems(
                                sourceClaims = uiState.value.claims,
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
                val label = uiState.value.claims.find { it.id == event.identifier }?.label

                when {
                    label == "nationality" && uiState.value.acceptedCountries.isNotEmpty() ->
                        configurePredicate(event.identifier, label, value = null, subtitle = null)

                    label == "nationality" -> setEffect {
                        ZkRequestContract.Effect.Navigation.OpenCountrySelection(
                            preSelectedCodes = uiState.value.acceptedCountries
                        )
                    }

                    label == "birth_date" && uiState.value.ageThreshold != null ->
                        configurePredicate(event.identifier, label, value = null, subtitle = null)

                    label == "birth_date" -> setState { copy(ageDialogVisible = true) }

                    else -> {
                        val result = interactor.handleItemSelection(
                            items = uiState.value.items,
                            identifier = event.identifier,
                        )
                        when (result) {
                            is HandleItemSelectionPartialState.Updated -> setState {
                                copy(
                                    items = result.items,
                                    primaryButtonEnabled = result.hasSelectedItems,
                                )
                            }
                        }
                    }
                }
            }

            is ZkRequestContract.Event.OnCountriesSelected -> {
                val value = event.countryCodes
                    .takeIf { it.isNotEmpty() }
                    ?.let { ZkPredicateValue.NationalityIn(it) }
                configurePredicate(
                    rowId = claimId("nationality"),
                    label = "nationality",
                    value = value,
                    subtitle = interactor.selectedCountriesSubtitle(event.countryCodes.size),
                )
            }

            is ZkRequestContract.Event.OnAgeThresholdConfirmed -> {
                configurePredicate(
                    rowId = claimId("birth_date"),
                    label = "birth_date",
                    value = ZkPredicateValue.AgeOver(event.threshold),
                    subtitle = interactor.ageOverSubtitle(event.threshold),
                )
                setState { copy(ageDialogVisible = false) }
            }

            is ZkRequestContract.Event.OnAgeDialogDismissed -> {
                setState { copy(ageDialogVisible = false) }
            }
        }
    }

    private fun configurePredicate(
        rowId: String?,
        label: String,
        value: ZkPredicateValue?,
        subtitle: String?,
    ) {
        val configured = value != null
        val updatedClaims = uiState.value.claims.map { claim ->
            if (claim.kind is ClaimKind.Zk && claim.label == label) {
                claim.copy(kind = ClaimKind.Zk(value))
            } else {
                claim
            }
        }
        val updatedItems = uiState.value.items.map { item ->
            if (item.itemId == rowId) {
                item.copy(
                    supportingText = if (configured) subtitle else null,
                    trailingContentData = ListItemTrailingContentDataUi.Checkbox(
                        checkboxData = CheckboxDataUi(isChecked = configured)
                    )
                )
            } else {
                item
            }
        }
        setState {
            copy(
                claims = updatedClaims,
                items = updatedItems,
                primaryButtonEnabled = updatedItems.hasAnyCheckedCheckbox(),
            )
        }
    }

    private fun claimId(elementLabel: String): String? =
        uiState.value.claims
            .firstOrNull { it.kind is ClaimKind.Zk && it.label == elementLabel }
            ?.id
}
