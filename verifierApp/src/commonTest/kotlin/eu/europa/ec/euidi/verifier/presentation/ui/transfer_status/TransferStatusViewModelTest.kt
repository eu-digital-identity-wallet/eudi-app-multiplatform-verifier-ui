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

package eu.europa.ec.euidi.verifier.presentation.ui.transfer_status

import app.cash.turbine.test
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import eu.europa.ec.euidi.verifier.core.controller.TransferStatus
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.interactor.TransferStatusInteractor
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentsDomain
import eu.europa.ec.euidi.verifier.presentation.MviViewModelTest
import eu.europa.ec.euidi.verifier.presentation.ui.transfer_status.TransferStatusViewModelContract.Effect
import eu.europa.ec.euidi.verifier.presentation.ui.transfer_status.TransferStatusViewModelContract.Event
import eu.europa.ec.euidi.verifier.testutil.TestData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransferStatusViewModelTest : MviViewModelTest() {

    private val qrCode = "mdoc:test"
    private val docs = listOf(TestData.pidFullRequestedDocument)

    private fun transferInteractor(
        statusFlow: Flow<TransferStatus> = flowOf()
    ): TransferStatusInteractor = mock(MockMode.autoUnit) {
        everySuspend { getRequestData(any()) } returns "Requesting: Full PID"
        everySuspend { getConnectionStatus(any()) } returns statusFlow
    }

    private fun resourceProvider(): ResourceProvider = mock {
        every { getSharedString(any(), *any()) } returns "status"
    }

    private fun viewModel(
        interactor: TransferStatusInteractor = transferInteractor(),
        resourceProvider: ResourceProvider = resourceProvider(),
    ) = TransferStatusViewModel(interactor, resourceProvider, qrCode)

    //region Init

    @Test
    fun `Init loads the requested document types`() = runTest(testDispatcher) {
        val viewModel = viewModel()

        viewModel.setEvent(Event.Init(docs = docs))

        val state = viewModel.uiState.value
        assertEquals("Requesting: Full PID", state.requestedDocTypes)
        assertEquals(docs, state.requestedDocs)
        assertFalse(state.isLoading)
    }

    //endregion

    //region permissions

    @Test
    fun `RequestPermissions marks progress and emits the request effect`() =
        runTest(testDispatcher) {
            val viewModel = viewModel()

            viewModel.effect.test {
                viewModel.setEvent(Event.RequestPermissions)
                assertEquals(Effect.RequestPermissions, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertTrue(viewModel.uiState.value.permissionsRequestInProgress)
        }

    @Test
    fun `RequestPermissions is ignored while a request is already in progress`() =
        runTest(testDispatcher) {
            val viewModel = viewModel()

            viewModel.effect.test {
                viewModel.setEvent(Event.RequestPermissions)
                assertEquals(Effect.RequestPermissions, awaitItem())

                viewModel.setEvent(Event.RequestPermissions)
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `PermissionReceived granted before engagement emits PermissionsGranted`() =
        runTest(testDispatcher) {
            val viewModel = viewModel()

            viewModel.effect.test {
                viewModel.setEvent(Event.PermissionReceived(denied = false))
                assertEquals(Effect.PermissionsGranted, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            val state = viewModel.uiState.value
            assertEquals(true, state.hasPermissions)
            assertFalse(state.permissionsRequestInProgress)
        }

    @Test
    fun `PermissionReceived denied during engagement emits PermissionsRevoked`() =
        runTest(testDispatcher) {
            val viewModel = viewModel(interactor = transferInteractor(statusFlow = flowOf()))
            viewModel.setEvent(Event.Init(docs = docs))
            viewModel.setEvent(Event.StartProximity) // engagementStarted = true

            viewModel.effect.test {
                viewModel.setEvent(Event.PermissionReceived(denied = true))
                assertEquals(Effect.PermissionsRevoked, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(false, viewModel.uiState.value.hasPermissions)
        }

    @Test
    fun `PermissionReceived denied before engagement emits no effect`() =
        runTest(testDispatcher) {
            val viewModel = viewModel()

            viewModel.effect.test {
                viewModel.setEvent(Event.PermissionReceived(denied = true))
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(false, viewModel.uiState.value.hasPermissions)
        }

    @Test
    fun `PermissionReceived granted during engagement emits no effect`() =
        runTest(testDispatcher) {
            val viewModel = viewModel(interactor = transferInteractor(statusFlow = flowOf()))
            viewModel.setEvent(Event.Init(docs = docs))
            viewModel.setEvent(Event.StartProximity) // engagementStarted = true

            viewModel.effect.test {
                viewModel.setEvent(Event.PermissionReceived(denied = false))
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(true, viewModel.uiState.value.hasPermissions)
        }

    @Test
    fun `OpenAppSettings emits the open-settings effect`() = runTest(testDispatcher) {
        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.setEvent(Event.OpenAppSettings)
            assertEquals(Effect.OpenAppSettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    //endregion

    //region proximity / connection status

    @Test
    fun `StartProximity prepares the connection, starts engagement and maps non-terminal statuses`() =
        runTest(testDispatcher) {
            val interactor = transferInteractor(
                statusFlow = flowOf(
                    TransferStatus.Connected,
                    TransferStatus.Connecting,
                    TransferStatus.DeviceEngagementCompleted,
                    TransferStatus.RequestSent,
                )
            )
            val viewModel = viewModel(interactor = interactor)
            viewModel.setEvent(Event.Init(docs = docs))

            viewModel.setEvent(Event.StartProximity)

            assertTrue(viewModel.uiState.value.engagementStarted)
            assertEquals("status", viewModel.uiState.value.connectionStatus)
            verifySuspend(exactly(1)) { interactor.prepareConnection() }
            verify(exactly(1)) { interactor.startEngagement(qrCode) }
        }

    @Test
    fun `An Error status navigates back`() = runTest(testDispatcher) {
        val interactor = transferInteractor(statusFlow = flowOf(TransferStatus.Error("boom")))
        val viewModel = viewModel(interactor = interactor)
        viewModel.setEvent(Event.Init(docs = docs))

        viewModel.effect.test {
            viewModel.setEvent(Event.StartProximity)
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `A Disconnected status navigates back`() = runTest(testDispatcher) {
        val interactor = transferInteractor(statusFlow = flowOf(TransferStatus.Disconnected))
        val viewModel = viewModel(interactor = interactor)
        viewModel.setEvent(Event.Init(docs = docs))

        viewModel.effect.test {
            viewModel.setEvent(Event.StartProximity)
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `An OnResponseReceived status navigates to the show-documents screen`() =
        runTest(testDispatcher) {
            val receivedUi = listOf(TestData.pidReceivedDocument)
            val interactor = mock<TransferStatusInteractor>(MockMode.autoUnit) {
                everySuspend { getRequestData(any()) } returns "Requesting: Full PID"
                everySuspend {
                    getConnectionStatus(any())
                } returns flowOf(
                    TransferStatus.OnResponseReceived(ReceivedDocumentsDomain(documents = emptyList()))
                )
                everySuspend { transformToReceivedDocumentsUi(any(), any()) } returns receivedUi
            }
            val viewModel = viewModel(interactor = interactor)
            viewModel.setEvent(Event.Init(docs = docs))

            viewModel.effect.test {
                viewModel.setEvent(Event.StartProximity)
                assertEquals(
                    Effect.Navigation.NavigateToShowDocumentsScreen(receivedDocuments = receivedUi),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `StopProximity stops the connection and resets engagement state`() =
        runTest(testDispatcher) {
            val interactor = transferInteractor()
            val viewModel = viewModel(interactor = interactor)

            viewModel.setEvent(Event.StopProximity)

            val state = viewModel.uiState.value
            assertFalse(state.engagementStarted)
            assertFalse(state.isLoading)
            verifySuspend(exactly(1)) { interactor.stopConnection() }
        }

    //endregion

    //region back / cancel

    @Test
    fun `OnCancelClick stops the connection and navigates back`() = runTest(testDispatcher) {
        val interactor = transferInteractor()
        val viewModel = viewModel(interactor = interactor)

        viewModel.effect.test {
            viewModel.setEvent(Event.OnCancelClick)
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        verifySuspend(exactly(1)) { interactor.stopConnection() }
    }

    @Test
    fun `OnBackClick navigates back`() = runTest(testDispatcher) {
        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.setEvent(Event.OnBackClick)
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    //endregion
}
