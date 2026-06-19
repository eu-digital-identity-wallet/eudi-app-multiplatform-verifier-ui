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

package eu.europa.ec.euidi.verifier.presentation.ui.country_selection

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.interactor.CountrySelectionInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.HandleItemSelectionPartialState
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.model.CountrySelectionHolder
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.country_selection_screen_title
import kotlinx.coroutines.launch

/** Zero-knowledge nationality proofs need at least two accepted countries. */
const val MIN_SELECTED_COUNTRIES = 2

sealed interface CountrySelectionContract {
    data class State(
        val screenTitle: String = "",
        val items: List<ListItemDataUi> = emptyList(),
        val primaryButtonEnabled: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data class Init(val preSelectedCodes: List<String> = emptyList()) : Event
        data class OnItemClicked(val identifier: String) : Event
        data object OnDoneClick : Event
        data object OnCancelClick : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class GoBackWithResult(val result: CountrySelectionHolder) : Navigation
            data object GoBack : Navigation
        }
    }
}

class CountrySelectionViewModel(
    private val interactor: CountrySelectionInteractor,
    private val resourceProvider: ResourceProvider,
) : MviViewModel<CountrySelectionContract.Event, CountrySelectionContract.State, CountrySelectionContract.Effect>() {

    override fun createInitialState(): CountrySelectionContract.State = CountrySelectionContract.State()

    override fun handleEvent(event: CountrySelectionContract.Event) {
        when (event) {
            is CountrySelectionContract.Event.Init -> {
                viewModelScope.launch {
                    val items = interactor.getCountryListItems(event.preSelectedCodes)
                    setState {
                        copy(
                            screenTitle = resourceProvider.getSharedString(
                                Res.string.country_selection_screen_title
                            ),
                            items = items,
                            primaryButtonEnabled = canSubmit(items),
                        )
                    }
                }
            }

            is CountrySelectionContract.Event.OnItemClicked -> {
                val result = interactor.handleItemSelection(
                    items = uiState.value.items,
                    identifier = event.identifier,
                )

                when (result) {
                    is HandleItemSelectionPartialState.Updated -> {
                        setState {
                            copy(
                                items = result.items,
                                primaryButtonEnabled = canSubmit(result.items),
                            )
                        }
                    }
                }
            }

            is CountrySelectionContract.Event.OnDoneClick -> {
                val selected = interactor.selectedCountryCodes(uiState.value.items)
                setEffect {
                    CountrySelectionContract.Effect.Navigation.GoBackWithResult(
                        CountrySelectionHolder(countryCodes = selected)
                    )
                }
            }

            is CountrySelectionContract.Event.OnCancelClick -> {
                setEffect { CountrySelectionContract.Effect.Navigation.GoBack }
            }
        }
    }

    /** ZK requires at least [MIN_SELECTED_COUNTRIES] accepted countries before Done is allowed. */
    private fun canSubmit(items: List<ListItemDataUi>): Boolean =
        interactor.selectedCountryCodes(items).size >= MIN_SELECTED_COUNTRIES
}
