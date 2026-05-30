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

package eu.europa.ec.euidi.verifier.presentation.ui.menu

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.domain.interactor.MenuInteractor
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuTypeUi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MenuViewModelTest : MviViewModelTest() {

    private val menuItems = listOf(
        MenuItemUi(
            type = MenuTypeUi.HOME,
            data = ListItemDataUi(
                itemId = "home",
                mainContentData = ListItemMainContentDataUi.Text("Home")
            )
        )
    )

    private fun menuInteractor(): MenuInteractor = mock {
        everySuspend { getScreenTitle() } returns "Menu"
        everySuspend { getMenuItemsUi() } returns menuItems
        every { getAppVersion() } returns "1.0.0"
    }

    @Test
    fun `Init loads title, menu items and app version`() = runTest(testDispatcher) {
        val viewModel = MenuViewModel(menuInteractor())

        viewModel.setEvent(Event.Init)

        val state = viewModel.uiState.value
        assertEquals("Menu", state.screenTitle)
        assertEquals(menuItems, state.menuItems)
        assertEquals("1.0.0", state.appVersion)
        assertFalse(state.isLoading)
    }

    @Test
    fun `OnBackClicked emits Pop`() = runTest(testDispatcher) {
        val viewModel = MenuViewModel(menuInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnBackClicked)
            assertEquals(Effect.Navigation.Pop, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `MenuItemClicked HOME pops to the home screen`() = runTest(testDispatcher) {
        val viewModel = MenuViewModel(menuInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.MenuItemClicked(MenuTypeUi.HOME))
            assertEquals(
                Effect.Navigation.PopTo(route = NavItem.Home, inclusive = false),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `MenuItemClicked SETTINGS pushes the settings screen`() = runTest(testDispatcher) {
        val viewModel = MenuViewModel(menuInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.MenuItemClicked(MenuTypeUi.SETTINGS))
            assertEquals(
                Effect.Navigation.PushScreen(
                    route = NavItem.Settings,
                    popUpTo = NavItem.Menu,
                    inclusive = false
                ),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
