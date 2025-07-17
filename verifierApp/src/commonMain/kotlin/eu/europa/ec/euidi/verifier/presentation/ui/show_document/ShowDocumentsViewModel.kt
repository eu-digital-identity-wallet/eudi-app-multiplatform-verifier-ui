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

package eu.europa.ec.euidi.verifier.presentation.ui.show_document

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.interactor.ShowDocumentsInteractor
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.model.DocumentUi
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface ShowDocumentViewModelContract {
    data class State(
        val isLoading: Boolean = false,
        val items: List<DocumentUi> = emptyList(),
        val address: String = "",
        val screenTitle: String = "",
    ) : UiState

    sealed interface Event : UiEvent {
        data class Init(
            val address: String,
            val items: List<ReceivedDocumentUi>
        ) : Event
        data object OnDoneClick : Event
        data object OnBackClick : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object NavigateToHome : Navigation
        }
    }
}

@KoinViewModel
class ShowDocumentsViewModel(
    private val interactor: ShowDocumentsInteractor,
    private val resourceProvider: ResourceProvider
) : MviViewModel<ShowDocumentViewModelContract.Event, ShowDocumentViewModelContract.State, ShowDocumentViewModelContract.Effect>() {

    override fun createInitialState(): ShowDocumentViewModelContract.State =
        ShowDocumentViewModelContract.State()

    override fun handleEvent(event: ShowDocumentViewModelContract.Event) {
        when (event) {
            is ShowDocumentViewModelContract.Event.Init -> {
                viewModelScope.launch {
                    setState {
                        copy(
                            isLoading = true
                        )
                    }

                    val title = interactor.getScreenTitle()
                    val transformedItems = interactor.transformToUiItems(
                        items = event.items,
                        resourceProvider = resourceProvider
                    )

                    setState {
                        copy(
                            screenTitle = title,
                            address = event.address,
                            items = transformedItems,
                            isLoading = false
                        )
                    }
                }
            }

            is ShowDocumentViewModelContract.Event.OnDoneClick -> {
                setEffect {
                    ShowDocumentViewModelContract.Effect.Navigation.NavigateToHome
                }
            }

            is ShowDocumentViewModelContract.Event.OnBackClick -> {
                setEffect {
                    ShowDocumentViewModelContract.Effect.Navigation.NavigateToHome
                }
            }
        }
    }
}