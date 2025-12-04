/*
 * Copyright (c) 2025 European Commission
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

import eu.europa.ec.euidi.verifier.core.controller.PlatformController
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.document_type_employee_id
import eudiverifier.verifierapp.generated.resources.document_type_mdl
import eudiverifier.verifierapp.generated.resources.document_type_pid
import eudiverifier.verifierapp.generated.resources.home_screen_main_button_text_default
import eudiverifier.verifierapp.generated.resources.home_screen_main_button_text_separator
import eudiverifier.verifierapp.generated.resources.home_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HomeInteractorTest {

    /**
     * A test factory function for creating an instance of [HomeInteractor] with its dependencies,
     * suitable for use within a [TestScope].
     *
     * This function simplifies the setup of the interactor for testing by providing default fake
     * implementations for its dependencies. The fakes can be overridden by passing in specific
     * instances.
     *
     * @param platformController A [FakePlatformController] instance. Defaults to a new instance.
     * @param uuidProvider A [UuidProvider] instance. Defaults to a [FakeUuidProvider].
     * @param resourceProvider A [ResourceProvider] instance. Defaults to a [FakeResourceProvider].
     * @return A [Pair] containing the created [HomeInteractor] instance and the
     * [FakePlatformController] used, allowing for verification of interactions with the platform.
     */
    private fun TestScope.createInteractor(
        platformController: FakePlatformController = FakePlatformController(),
        uuidProvider: UuidProvider = FakeUuidProvider(),
        resourceProvider: ResourceProvider = FakeResourceProvider(),
    ): Pair<HomeInteractor, FakePlatformController> {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher

        val interactor = HomeInteractorImpl(
            platformController = platformController,
            uuidProvider = uuidProvider,
            resourceProvider = resourceProvider,
            dispatcher = dispatcher
        )

        return Pair(interactor, platformController)
    }

    //region getScreenTitle

    @Test
    fun `getScreenTitle returns localized title`() = runTest(StandardTestDispatcher()) {
        val (interactor, _) = createInteractor()

        val title = interactor.getScreenTitle()

        assertEquals("Home Screen title", title)
    }

    //endregion

    //region getDefaultMainButtonData
    @Test
    fun `getDefaultMainButtonData uses uuidProvider default text and chevron icon`() =
        runTest(StandardTestDispatcher()) {
            val (interactor, _) = createInteractor()

            val item = interactor.getDefaultMainButtonData()

            assertEquals("uuid-0", item.itemId)

            val main = item.mainContentData
            assertIs<ListItemMainContentDataUi.Text>(main)
            assertEquals("Home Screen main button text default", main.text)

            val trailing = item.trailingContentData
            assertIs<ListItemTrailingContentDataUi.Icon>(trailing)
            assertEquals(AppIcons.ChevronRight, trailing.iconData)

            val secondItem = interactor.getDefaultMainButtonData()
            assertEquals("uuid-1", secondItem.itemId)
        }

    //endregion

    //region formatMainButtonData

    @Test
    fun `formatMainButtonData formats single requested document`() =
        runTest(StandardTestDispatcher()) {
            val (interactor, _) = createInteractor()

            val baseItem = ListItemDataUi(
                itemId = "base-id",
                mainContentData = ListItemMainContentDataUi.Text(
                    text = "Home Screen main button text default"
                ),
                trailingContentData = ListItemTrailingContentDataUi.Icon(
                    iconData = AppIcons.ChevronRight
                )
            )

            val requestedDocs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.FULL,
                    claims = emptyList()
                )
            )

            val updated = interactor.formatMainButtonData(
                requestedDocs = requestedDocs,
                existingMainButtonData = baseItem
            )

            assertEquals(baseItem.itemId, updated.itemId)

            val updatedMain = updated.mainContentData
            assertIs<ListItemMainContentDataUi.Text>(updatedMain)
            assertEquals("Full PID", updatedMain.text)

            val trailing = updated.trailingContentData
            assertIs<ListItemTrailingContentDataUi.Icon>(trailing)
            assertEquals(AppIcons.ChevronRight, trailing.iconData)
        }

    @Test
    fun `formatMainButtonData joins multiple requested documents with separator`() =
        runTest(StandardTestDispatcher()) {
            val (interactor, _) = createInteractor()

            val baseItem = ListItemDataUi(
                itemId = "base-id",
                mainContentData = ListItemMainContentDataUi.Text(
                    text = "Home Screen main button text default"
                ),
                trailingContentData = ListItemTrailingContentDataUi.Icon(
                    iconData = AppIcons.ChevronRight
                )
            )

            val requestedDocs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.FULL,
                    claims = emptyList()
                ),
                RequestedDocumentUi(
                    id = "MDL_DOC",
                    documentType = AttestationType.Mdl,
                    mode = DocumentMode.CUSTOM,
                    claims = emptyList()
                )
            )

            val updated = interactor.formatMainButtonData(
                requestedDocs = requestedDocs,
                existingMainButtonData = baseItem
            )

            val updatedMain = updated.mainContentData as ListItemMainContentDataUi.Text
            assertEquals("Full PID ; Custom MDL", updatedMain.text)
        }

    //endregion

    //region closeApp

    @Test
    fun `closeApp delegates to PlatformController`() =
        runTest(StandardTestDispatcher()) {
            val (interactor, fakePlatformController) = createInteractor()

            assertEquals(0, fakePlatformController.closeAppCalls)

            interactor.closeApp()

            assertEquals(1, fakePlatformController.closeAppCalls)
            assertEquals(0, fakePlatformController.openAppSettingsCalls)
        }

    //endregion

    //region Fakes

    private class FakePlatformController : PlatformController {

        private enum class Function {
            CLOSE_APP,
            OPEN_APP_SETTINGS
        }

        private val functionCallCounts: MutableMap<Function, Int> = mutableMapOf()

        override val buildType
            get() = error("Not used in these tests")

        override val flavorType
            get() = error("Not used in these tests")

        override val appVersion: String
            get() = error("Not used in these tests")

        val closeAppCalls: Int
            get() = functionCallCounts[Function.CLOSE_APP] ?: 0

        val openAppSettingsCalls: Int
            get() = functionCallCounts[Function.OPEN_APP_SETTINGS] ?: 0

        override fun closeApp() {
            functionCallCounts[Function.CLOSE_APP] =
                (functionCallCounts[Function.CLOSE_APP] ?: 0) + 1
        }

        override fun openAppSettings() {
            functionCallCounts[Function.OPEN_APP_SETTINGS] =
                (functionCallCounts[Function.OPEN_APP_SETTINGS] ?: 0) + 1
        }
    }

    private class FakeUuidProvider : UuidProvider {
        private var counter = 0

        override fun provideUuid(): String {
            return "uuid-${counter++}"
        }
    }

    private class FakeResourceProvider : ResourceProvider {

        override fun getSharedString(resource: StringResource): String {
            return when (resource) {
                Res.string.home_screen_title -> "Home Screen title"
                Res.string.home_screen_main_button_text_default -> "Home Screen main button text default"
                Res.string.home_screen_main_button_text_separator -> " ; "
                Res.string.document_type_pid -> "PID"
                Res.string.document_type_mdl -> "MDL"
                Res.string.document_type_employee_id -> "Employee ID"
                else -> "Unknown string"
            }
        }

        override fun getSharedString(
            resource: StringResource,
            vararg formatArgs: Any
        ): String = getSharedString(resource)

        override fun genericErrorMessage(): String = "GENERIC_ERROR"

        override fun genericNetworkErrorMessage(): String = "GENERIC_NETWORK_ERROR"
    }

    //endregion
}