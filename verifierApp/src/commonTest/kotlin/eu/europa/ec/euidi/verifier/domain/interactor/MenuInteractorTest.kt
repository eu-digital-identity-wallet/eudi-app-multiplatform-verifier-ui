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

package eu.europa.ec.euidi.verifier.domain.interactor

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.ListItemLeadingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuTypeUi
import eu.europa.ec.euidi.verifier.testutil.sequentialUuidProvider
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.menu_screen_item_home_name
import eudiverifier.verifierapp.generated.resources.menu_screen_item_settings_name
import eudiverifier.verifierapp.generated.resources.menu_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MenuInteractorTest {

    /**
     * Builds the SUT, [MenuInteractorImpl], with a dispatcher that shares the `runTest` scheduler
     * and Mokkery mocks for its collaborators.
     */
    private fun TestScope.createInteractor(
        uuidProvider: UuidProvider = sequentialUuidProvider(),
        resourceProvider: ResourceProvider = stringResourceProvider(),
        configProvider: ConfigProvider = configProvider(version = "1.0.0-Dev"),
    ): MenuInteractor {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        return MenuInteractorImpl(
            uuidProvider = uuidProvider,
            resourceProvider = resourceProvider,
            configProvider = configProvider,
            dispatcher = dispatcher
        )
    }

    //region getScreenTitle

    @Test
    fun `Given an interactor When the screen title is requested Then the localized title is returned`() = runTest(StandardTestDispatcher()) {
        // Given
        val interactor = createInteractor()

        // When
        val result = interactor.getScreenTitle()

        // Then
        assertEquals("Menu title", result)
    }

    //endregion

    //region getMenuItemsUi

    /**
     * Checks the home item, then the settings item.
     */
    @Test
    fun `Given an interactor When the menu items are requested Then the home and settings items are returned in order`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            // When
            val items = interactor.getMenuItemsUi()

            // Then
            assertEquals(2, items.size)

            val home = items[0]
            assertEquals(MenuTypeUi.HOME, home.type)
            assertEquals("uuid-0", home.data.itemId)

            val homeMain = home.data.mainContentData
            assertIs<ListItemMainContentDataUi.Text>(homeMain)
            assertEquals("Home", homeMain.text)

            val homeLeading = home.data.leadingContentData
            assertIs<ListItemLeadingContentDataUi.Icon>(homeLeading)
            assertEquals(AppIcons.Home, homeLeading.iconData)

            val homeTrailing = home.data.trailingContentData
            assertIs<ListItemTrailingContentDataUi.Icon>(homeTrailing)
            assertEquals(AppIcons.ChevronRight, homeTrailing.iconData)

            val settings = items[1]
            assertEquals(MenuTypeUi.SETTINGS, settings.type)
            assertEquals("uuid-1", settings.data.itemId)

            val settingsMain = settings.data.mainContentData
            assertIs<ListItemMainContentDataUi.Text>(settingsMain)
            assertEquals("Settings", settingsMain.text)

            val settingsLeading = settings.data.leadingContentData
            assertIs<ListItemLeadingContentDataUi.Icon>(settingsLeading)
            assertEquals(AppIcons.Settings, settingsLeading.iconData)

            val settingsTrailing = settings.data.trailingContentData
            assertIs<ListItemTrailingContentDataUi.Icon>(settingsTrailing)
            assertEquals(AppIcons.ChevronRight, settingsTrailing.iconData)
        }

    //endregion

    //region getAppVersion

    @Test
    fun `Given an interactor with a configured version When the app version is requested Then the configured version is returned`() = runTest(StandardTestDispatcher()) {
        // Given
        val interactor = createInteractor(
            configProvider = configProvider(version = "9.9.9-Dev")
        )

        // When
        val result = interactor.getAppVersion()

        // Then
        assertEquals("9.9.9-Dev", result)
    }

    //endregion

    //region Mocks

    private fun stringResourceProvider(): ResourceProvider = mock {
        every { getSharedString(Res.string.menu_screen_title) } returns "Menu title"
        every { getSharedString(Res.string.menu_screen_item_home_name) } returns "Home"
        every { getSharedString(Res.string.menu_screen_item_settings_name) } returns "Settings"
    }

    private fun configProvider(version: String): ConfigProvider {
        val configProvider = mock<ConfigProvider>()
        every { configProvider.appVersion } returns version
        return configProvider
    }

    //endregion
}