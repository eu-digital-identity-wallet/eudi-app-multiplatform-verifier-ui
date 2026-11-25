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

package eu.europa.ec.euidi.verifier.presentation.ui.home

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import eu.europa.ec.euidi.verifier.domain.interactor.HomeInteractor
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocsHolder
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModelContract.Event
import eu.europa.ec.euidi.verifier.testutil.TestData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeViewModelTest : MviViewModelTest() {

    private val buttonData = ListItemDataUi(
        itemId = "btn",
        mainContentData = ListItemMainContentDataUi.Text("Create request")
    )

    private fun homeInteractor(): HomeInteractor = mock<HomeInteractor>(MockMode.autoUnit) {
        every { getScreenTitle() } returns "Home"
        every { getDefaultMainButtonData() } returns buttonData
    }

    //region OnResume

    @Test
    fun `Given a view model When OnResume is received without documents Then the title and the default main button are loaded`() = runTest(testDispatcher) {
        // Given
        val viewModel = HomeViewModel(homeInteractor())

        // When
        viewModel.setEvent(Event.OnResume(docs = null))

        // Then
        val state = viewModel.uiState.value
        assertEquals("Home", state.screenTitle)
        assertEquals(buttonData, state.mainButtonData)
        assertFalse(state.isLoading)
        assertTrue(state.requestedDocs.isEmpty())
        assertFalse(state.isStickyButtonEnabled)
    }

    @Test
    fun `Given requested documents When OnResume is received Then the main button is formatted and the sticky button is enabled`() =
        runTest(testDispatcher) {
            // Given
            val docs = listOf(TestData.pidFullRequestedDocument)
            val formattedButton = buttonData.copy(
                mainContentData = ListItemMainContentDataUi.Text("Full PID")
            )
            val interactor = mock<HomeInteractor>(MockMode.autoUnit) {
                every { getScreenTitle() } returns "Home"
                every { getDefaultMainButtonData() } returns buttonData
                everySuspend { formatMainButtonData(docs, buttonData) } returns formattedButton
            }
            val viewModel = HomeViewModel(interactor)

            // When
            viewModel.setEvent(Event.OnResume(docs = docs))

            // Then
            val state = viewModel.uiState.value
            assertEquals(docs, state.requestedDocs)
            assertTrue(state.isStickyButtonEnabled)
            assertEquals(formattedButton, state.mainButtonData)
            assertFalse(state.isLoading)
        }

    /**
     * First resume loads the default button; second resume must reuse it (no re-fetch).
     */
    @Test
    fun `Given a view model When it is resumed twice Then the default main button is fetched once and the second resume formats it`() =
        runTest(testDispatcher) {
            // Given
            val docs = listOf(TestData.pidFullRequestedDocument)
            val formattedButton = buttonData.copy(
                mainContentData = ListItemMainContentDataUi.Text("Full PID")
            )
            val interactor = mock<HomeInteractor>(MockMode.autoUnit) {
                every { getScreenTitle() } returns "Home"
                every { getDefaultMainButtonData() } returns buttonData
                everySuspend { formatMainButtonData(docs, buttonData) } returns formattedButton
            }
            val viewModel = HomeViewModel(interactor)

            // When
            viewModel.setEvent(Event.OnResume(docs = null))
            viewModel.setEvent(Event.OnResume(docs = docs))

            // Then
            assertEquals(formattedButton, viewModel.uiState.value.mainButtonData)
            verify(exactly(1)) { interactor.getScreenTitle() }
            verify(exactly(1)) { interactor.getDefaultMainButtonData() }
        }

    //endregion

    //region DismissError

    @Test
    fun `Given a view model When DismissError is received Then there is no error`() = runTest(testDispatcher) {
        // Given
        val viewModel = HomeViewModel(homeInteractor())

        // When
        viewModel.setEvent(Event.DismissError)

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    //endregion

    //region OnBackClicked

    @Test
    fun `Given a view model When OnBackClicked is received Then the interactor closes the app`() = runTest(testDispatcher) {
        // Given
        val interactor = homeInteractor()
        val viewModel = HomeViewModel(interactor)

        // When
        viewModel.setEvent(Event.OnBackClicked)

        // Then
        verify(exactly(1)) { interactor.closeApp() }
    }

    //endregion

    //region navigation effects

    @Test
    fun `Given a view model When OnStickyButtonClicked is received Then it navigates to QR scan with the requested documents`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = HomeViewModel(homeInteractor())

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.OnStickyButtonClicked)

                // Then
                assertEquals(
                    Effect.Navigation.SaveDocsToBackstackAndGoTo(
                        screen = NavItem.QrScan,
                        requestedDocs = RequestedDocsHolder(items = emptyList())
                    ),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given a view model When OnMenuClick is received Then it navigates to the menu screen`() = runTest(testDispatcher) {
        // Given
        val viewModel = HomeViewModel(homeInteractor())

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnMenuClick)

            // Then
            assertEquals(Effect.Navigation.PushScreen(NavItem.Menu), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given a view model When OnTapToCreateRequest is received Then it navigates to the documents-to-request screen`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = HomeViewModel(homeInteractor())

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.OnTapToCreateRequest)

                // Then
                assertEquals(
                    Effect.Navigation.SaveDocsToBackstackAndGoTo(
                        screen = NavItem.DocToRequest,
                        requestedDocs = RequestedDocsHolder(items = emptyList())
                    ),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    //endregion
}
