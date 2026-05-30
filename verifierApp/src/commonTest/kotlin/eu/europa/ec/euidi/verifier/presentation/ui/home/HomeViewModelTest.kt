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
    fun `OnResume with no docs loads title and default main button`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(homeInteractor())

        viewModel.setEvent(Event.OnResume(docs = null))

        val state = viewModel.uiState.value
        assertEquals("Home", state.screenTitle)
        assertEquals(buttonData, state.mainButtonData)
        assertFalse(state.isLoading)
        assertTrue(state.requestedDocs.isEmpty())
        assertFalse(state.isStickyButtonEnabled)
    }

    @Test
    fun `OnResume with docs formats the main button and enables the sticky button`() =
        runTest(testDispatcher) {
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

            viewModel.setEvent(Event.OnResume(docs = docs))

            val state = viewModel.uiState.value
            assertEquals(docs, state.requestedDocs)
            assertTrue(state.isStickyButtonEnabled)
            assertEquals(formattedButton, state.mainButtonData)
            assertFalse(state.isLoading)
        }

    @Test
    fun `OnResume twice reuses the already-loaded main button without re-fetching`() =
        runTest(testDispatcher) {
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

            // First resume loads the default button; second resume must reuse it (no re-fetch).
            viewModel.setEvent(Event.OnResume(docs = null))
            viewModel.setEvent(Event.OnResume(docs = docs))

            assertEquals(formattedButton, viewModel.uiState.value.mainButtonData)
            verify(exactly(1)) { interactor.getScreenTitle() }
            verify(exactly(1)) { interactor.getDefaultMainButtonData() }
        }

    //endregion

    //region DismissError

    @Test
    fun `DismissError clears the error`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(homeInteractor())

        viewModel.setEvent(Event.DismissError)

        assertNull(viewModel.uiState.value.error)
    }

    //endregion

    //region OnBackClicked

    @Test
    fun `OnBackClicked delegates to the interactor to close the app`() = runTest(testDispatcher) {
        val interactor = homeInteractor()
        val viewModel = HomeViewModel(interactor)

        viewModel.setEvent(Event.OnBackClicked)

        verify(exactly(1)) { interactor.closeApp() }
    }

    //endregion

    //region navigation effects

    @Test
    fun `OnStickyButtonClicked emits navigation to QR scan with the requested docs`() =
        runTest(testDispatcher) {
            val viewModel = HomeViewModel(homeInteractor())

            viewModel.effect.test {
                viewModel.setEvent(Event.OnStickyButtonClicked)

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
    fun `OnMenuClick emits navigation to the menu screen`() = runTest(testDispatcher) {
        val viewModel = HomeViewModel(homeInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnMenuClick)

            assertEquals(Effect.Navigation.PushScreen(NavItem.Menu), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnTapToCreateRequest emits navigation to the documents-to-request screen`() =
        runTest(testDispatcher) {
            val viewModel = HomeViewModel(homeInteractor())

            viewModel.effect.test {
                viewModel.setEvent(Event.OnTapToCreateRequest)

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
