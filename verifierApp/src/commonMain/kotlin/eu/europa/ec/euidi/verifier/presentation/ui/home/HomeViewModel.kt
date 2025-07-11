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

import eu.europa.ec.euidi.verifier.mvi.BaseViewModel
import eu.europa.ec.euidi.verifier.mvi.UiEffect
import eu.europa.ec.euidi.verifier.mvi.UiEvent
import eu.europa.ec.euidi.verifier.mvi.UiState
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel() : BaseViewModel<HomeViewModelContract.Event, HomeViewModelContract.State, HomeViewModelContract.Effect>() {

    override fun createInitialState(): HomeViewModelContract.State = HomeViewModelContract.State()

    override fun handleEvent(event: HomeViewModelContract.Event) {
        when (event) {
            is HomeViewModelContract.Event.Init -> {
               setState {
                   copy(
                       requestedDocs = event.docs.orEmpty(),
                       isScanQrCodeButtonEnabled = event.docs.isNullOrEmpty().not()
                   )
               }
            }

            HomeViewModelContract.Event.OnSelectDocumentClick -> {
                setEffect {
                    HomeViewModelContract.Effect.Navigation.NavigateToDocToRequestScreen
                }
            }

            HomeViewModelContract.Event.OnScanQrCodeClick -> {
                setEffect {
                    HomeViewModelContract.Effect.Navigation.NavigateToTransferStatusScreen(
                        requestedDocs = RequestedDocsHolder(
                            items = uiState.value.requestedDocs
                        )
                    )
                }
            }

            HomeViewModelContract.Event.OnSettingsClick -> {
                setEffect {
                    HomeViewModelContract.Effect.Navigation.NavigateToSettingsScreen
                }
            }

            HomeViewModelContract.Event.OnReverseEngagementClick -> {
                setEffect {
                    HomeViewModelContract.Effect.Navigation.NavigateToReverseEngagementScreen
                }
            }
        }
    }
}

interface HomeViewModelContract {
    sealed interface Event : UiEvent {
        data class Init(val docs: List<RequestedDocumentUi>?) : Event
        data object OnSelectDocumentClick : Event
        data object OnScanQrCodeClick : Event
        data object OnSettingsClick : Event
        data object OnReverseEngagementClick : Event
    }

    data class State(
        val requestedDocs: List<RequestedDocumentUi> = emptyList(),
        val isScanQrCodeButtonEnabled: Boolean = false
    ) : UiState

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data object NavigateToDocToRequestScreen : Navigation
            data class NavigateToTransferStatusScreen(val requestedDocs: RequestedDocsHolder) : Navigation
            data object NavigateToSettingsScreen : Navigation
            data object NavigateToReverseEngagementScreen : Navigation
        }
    }
}