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

package eu.europa.ec.euidi.verifier.presentation.ui.reverse_engagement

import androidx.lifecycle.viewModelScope
import eu.europa.ec.euidi.verifier.domain.interactor.CreateQrPartialState
import eu.europa.ec.euidi.verifier.domain.interactor.ReverseEngagementInteractor
import eu.europa.ec.euidi.verifier.presentation.architecture.MviViewModel
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEffect
import eu.europa.ec.euidi.verifier.presentation.architecture.UiEvent
import eu.europa.ec.euidi.verifier.presentation.architecture.UiState
import eu.europa.ec.euidi.verifier.presentation.component.content.ContentErrorConfig
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.ui.reverse_engagement.ReverseEngagementViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.reverse_engagement.ReverseEngagementViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.reverse_engagement.ReverseEngagementViewModelContract.State
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed interface ReverseEngagementViewModelContract {
    data class State(
        val isLoading: Boolean,
        val error: ContentErrorConfig? = null,

        val screenTitle: String = "",
        val informativeMessage: String = "",
        val qrCode: String? = null,
    ) : UiState

    sealed interface Event : UiEvent {
        data object Init : Event
        data object DismissError : Event
        data object OnBackClicked : Event
        data object OnStickyButtonClicked : Event
    }

    sealed interface Effect : UiEffect {
        sealed interface Navigation : Effect {
            data class PushScreen(
                val route: NavItem,
                val popUpTo: NavItem,
                val inclusive: Boolean,
            ) : Navigation

            data class PopTo(
                val route: NavItem,
                val inclusive: Boolean,
            ) : Navigation

            data object Pop : Navigation
        }
    }
}

@KoinViewModel
class ReverseEngagementViewModel(
    private val interactor: ReverseEngagementInteractor,
) : MviViewModel<Event, State, Effect>() {

    override fun createInitialState(): State {
        return State(
            isLoading = true,
        )
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.Init -> {
                viewModelScope.launch {
                    setTitleAndInformativeMessage()
                    createQrCode()
                }
            }

            is Event.DismissError -> {
                setState {
                    copy(error = null)
                }
            }

            is Event.OnBackClicked -> {
                setEffect { Effect.Navigation.Pop }
            }

            is Event.OnStickyButtonClicked -> {
                popToHome()
            }
        }
    }

    private suspend fun setTitleAndInformativeMessage() = coroutineScope {
        setState {
            copy(
                isLoading = true,
            )
        }

        val titleDeferred = async { interactor.getScreenTitle() }
        val informativeMessageDeferred = async { interactor.getInformativeMessage() }

        val screenTitle = titleDeferred.await()
        val informativeMessage = informativeMessageDeferred.await()

        setState {
            copy(
                screenTitle = screenTitle,
                informativeMessage = informativeMessage,
                isLoading = false
            )
        }
    }

    private suspend fun createQrCode() {
        setState {
            copy(isLoading = true)
        }

        val createQrResult = interactor.createQr()

        when (createQrResult) {
            is CreateQrPartialState.Failure -> {
                setState {
                    copy(
                        error = ContentErrorConfig(
                            errorSubTitle = createQrResult.error,
                            onRetry = {
                                viewModelScope.launch {
                                    createQrCode()
                                }
                            },
                            onCancel = {
                                setEvent(Event.DismissError)
                                popToHome()
                            }
                        ),
                        isLoading = false,
                    )
                }
            }

            is CreateQrPartialState.Success -> {
                setState {
                    copy(
                        error = null,
                        qrCode = createQrResult.qr,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun popToHome() {
        setEffect {
            Effect.Navigation.PopTo(
                route = NavItem.Home,
                inclusive = false,
            )
        }
    }

}