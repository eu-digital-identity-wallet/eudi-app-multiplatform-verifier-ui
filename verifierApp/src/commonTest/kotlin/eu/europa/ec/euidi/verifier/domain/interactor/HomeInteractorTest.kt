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

import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
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
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HomeInteractorTest {

    /**
     * Builds the SUT, [HomeInteractorImpl], with a dispatcher that shares the `runTest` scheduler
     * and Mokkery mocks for its collaborators. Pass overrides to customise behaviour or to verify
     * interactions (e.g. the [PlatformController]).
     */
    private fun TestScope.createInteractor(
        platformController: PlatformController = mock<PlatformController>(MockMode.autoUnit),
        uuidProvider: UuidProvider = sequentialUuidProvider(),
        resourceProvider: ResourceProvider = stringResourceProvider(),
    ): HomeInteractor {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        return HomeInteractorImpl(
            platformController = platformController,
            uuidProvider = uuidProvider,
            resourceProvider = resourceProvider,
            dispatcher = dispatcher
        )
    }

    //region getScreenTitle

    @Test
    fun `getScreenTitle returns localized title`() = runTest(StandardTestDispatcher()) {
        val interactor = createInteractor()

        assertEquals("Home Screen title", interactor.getScreenTitle())
    }

    //endregion

    //region getDefaultMainButtonData

    @Test
    fun `getDefaultMainButtonData uses uuidProvider default text and chevron icon`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

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
            val interactor = createInteractor()

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
            val interactor = createInteractor()

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
    fun `closeApp delegates to PlatformController`() = runTest(StandardTestDispatcher()) {
        val platformController = mock<PlatformController>(MockMode.autoUnit)
        val interactor = createInteractor(platformController = platformController)

        interactor.closeApp()

        verify(exactly(1)) { platformController.closeApp() }
        verify(exactly(0)) { platformController.openAppSettings() }
    }

    //endregion

    //region Mocks

    private fun sequentialUuidProvider(): UuidProvider {
        var counter = 0
        return mock {
            every { provideUuid() } calls { "uuid-${counter++}" }
        }
    }

    private fun stringResourceProvider(): ResourceProvider = mock {
        every { getSharedString(Res.string.home_screen_title) } returns "Home Screen title"
        every {
            getSharedString(Res.string.home_screen_main_button_text_default)
        } returns "Home Screen main button text default"
        every { getSharedString(Res.string.home_screen_main_button_text_separator) } returns " ; "
        every { getSharedString(Res.string.document_type_pid) } returns "PID"
        every { getSharedString(Res.string.document_type_mdl) } returns "MDL"
        every { getSharedString(Res.string.document_type_employee_id) } returns "Employee ID"
    }

    //endregion
}