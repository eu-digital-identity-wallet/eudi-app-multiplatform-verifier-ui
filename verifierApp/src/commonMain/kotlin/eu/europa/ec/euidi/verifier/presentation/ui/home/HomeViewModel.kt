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

package eu.europa.ec.euidi.verifier.presentation.ui.home

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.interactor.HomeInteractor
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentErrorConfig
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModelContract.State
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface HomeViewModelContract {
    data class State(
        val isLoading: Boolean,
        val error: ContentErrorConfig? = null,

        val screenTitle: String = "",
        val mainButtonData: ListItemDataUi? = null,

        val requestedDocs: List<RequestedDocumentUi> = emptyList(),
        val isStickyButtonEnabled: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data object Init : Event
        data class OnResume(
            val docs: List<RequestedDocumentUi>?
        ) : Event

        data object DismissError : Event
        data object OnBackClicked : Event
        data object OnStickyButtonClicked : Event
        data object OnMenuClick : Event
        data object OnTapToCreateRequest : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class PushScreen(
                val route: NavItem
            ) : Navigation

            data class SaveDocsToBackstackAndGoTo(
                val screen: NavItem,
                val requestedDocs: RequestedDocsHolder
            ) : Navigation
        }
    }
}

@KoinViewModel
class HomeViewModel(
    private val interactor: HomeInteractor,
) : MviViewModel<Event, State, Effect>() {

    override fun createInitialState(): State {
        return State(
            isLoading = true,
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.Init -> {
                setScreenTitleAndMainButtonData()
            }

            is Event.OnResume -> {
                handleOnResume(docs = event.docs)
            }

            is Event.DismissError -> {
                setState { copy(error = null) }
            }

            is Event.OnBackClicked -> {
                closeApp()
            }

            is Event.OnStickyButtonClicked -> {
                setEffect {
                    Effect.Navigation.SaveDocsToBackstackAndGoTo(
                        screen = NavItem.QrScan,
                        requestedDocs = RequestedDocsHolder(
                            items = uiState.value.requestedDocs
                        )
                    )
                }
            }

            is Event.OnMenuClick -> {
                setEffect {
                    Effect.Navigation.PushScreen(
                        route = NavItem.Menu
                    )
                }
            }

            is Event.OnTapToCreateRequest -> {
                setEffect {
                    Effect.Navigation.SaveDocsToBackstackAndGoTo(
                        screen = NavItem.DocToRequest,
                        requestedDocs = RequestedDocsHolder(
                            items = uiState.value.requestedDocs
                        )
                    )
                }
            }
        }
    }

    private fun setScreenTitleAndMainButtonData() {
        viewModelScope.launch {
            setState {
                copy(
                    isLoading = true,
                )
            }

            val screenTitleDeferred = async { interactor.getScreenTitle() }
            val defaultMainButtonDeferred = async { interactor.getDefaultMainButtonData() }

            val screenTitle = screenTitleDeferred.await()
            val defaultMainButtonData = defaultMainButtonDeferred.await()

            setState {
                copy(
                    screenTitle = screenTitle,
                    mainButtonData = defaultMainButtonData,
                    isLoading = false
                )
            }
        }
    }

    private fun handleOnResume(docs: List<RequestedDocumentUi>?) {
        viewModelScope.launch {
            val baseButtonData = uiState.value.mainButtonData ?: return@launch

            if (!docs.isNullOrEmpty()) {
                val updatedButton = interactor.formatMainButtonData(
                    requestedDocs = docs,
                    existingMainButtonData = baseButtonData
                )

                setState {
                    copy(
                        requestedDocs = docs,
                        isStickyButtonEnabled = true,
                        mainButtonData = updatedButton,
                    )
                }
            }
        }
    }

    private fun closeApp() {
        interactor.closeApp()
    }
}