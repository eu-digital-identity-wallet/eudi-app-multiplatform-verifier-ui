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

package eu.europa.ec.euidi.verifier.presentation.ui.settings

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import eu.europa.ec.euidi.verifier.core.controller.PrefKey
import eu.europa.ec.euidi.verifier.domain.interactor.SettingsInteractor
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.navigation.NavItem
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModelContract.Event
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.settings.model.SettingsTypeUi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsViewModelTest : MviViewModelTest() {

    private val settingsItems = listOf<SettingsItemUi>(
        SettingsItemUi.CategoryHeader(title = "General")
    )
    private val refreshedItems = listOf<SettingsItemUi>(
        SettingsItemUi.CategoryHeader(title = "General"),
        SettingsItemUi.CategoryHeader(title = "Updated")
    )

    private fun settingsInteractor(
        items: List<SettingsItemUi> = settingsItems
    ): SettingsInteractor = mock(MockMode.autoUnit) {
        everySuspend { getScreenTitle() } returns "Settings"
        everySuspend { getSettingsItemsUi() } returns items
    }

    @Test
    fun `Init loads title and settings items`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsInteractor())

        viewModel.setEvent(Event.Init)

        val state = viewModel.uiState.value
        assertEquals("Settings", state.screenTitle)
        assertEquals(settingsItems, state.settingsItems)
        assertFalse(state.isLoading)
    }

    @Test
    fun `OnBackClicked emits Pop`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnBackClicked)
            assertEquals(Effect.Navigation.Pop, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnStickyButtonClicked pops to the home screen`() = runTest(testDispatcher) {
        val viewModel = SettingsViewModel(settingsInteractor())

        viewModel.effect.test {
            viewModel.setEvent(Event.OnStickyButtonClicked)
            assertEquals(
                Effect.Navigation.PopTo(route = NavItem.Home, inclusive = false),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SettingsItemClicked toggles the preference and refreshes the items`() =
        runTest(testDispatcher) {
            val interactor = settingsInteractor(items = refreshedItems)
            val viewModel = SettingsViewModel(interactor)

            viewModel.setEvent(Event.SettingsItemClicked(SettingsTypeUi.RetainData))

            verifySuspend(exactly(1)) { interactor.togglePrefBoolean(PrefKey.RETAIN_DATA) }
            assertEquals(refreshedItems, viewModel.uiState.value.settingsItems)
        }

    @Test
    fun `PushScreen navigation effect exposes its route, popUpTo and inclusive flag`() {
        // PushScreen is part of the navigation contract consumed by SettingsScreen's exhaustive
        // when, even though the ViewModel itself only emits Pop / PopTo.
        val effect = Effect.Navigation.PushScreen(
            route = NavItem.Settings,
            popUpTo = NavItem.Menu,
            inclusive = true,
        )
        val same = Effect.Navigation.PushScreen(
            route = NavItem.Settings,
            popUpTo = NavItem.Menu,
            inclusive = true,
        )

        assertEquals(NavItem.Settings, effect.route)
        assertEquals(NavItem.Menu, effect.popUpTo)
        assertTrue(effect.inclusive)
        assertEquals(same, effect)
    }
}
