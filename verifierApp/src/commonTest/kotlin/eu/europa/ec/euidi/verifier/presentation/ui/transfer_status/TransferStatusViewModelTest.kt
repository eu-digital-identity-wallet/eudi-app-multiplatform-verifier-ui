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
    fun `Given a view model When Init is received Then the requested document types are loaded`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.setEvent(Event.Init(docs = docs))

        // Then
        val state = viewModel.uiState.value
        assertEquals("Requesting: Full PID", state.requestedDocTypes)
        assertEquals(docs, state.requestedDocs)
        assertFalse(state.isLoading)
    }

    //endregion

    //region permissions

    @Test
    fun `Given a view model When RequestPermissions is received Then the request effect is emitted and progress is marked`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel()

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.RequestPermissions)

                // Then
                assertEquals(Effect.RequestPermissions, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertTrue(viewModel.uiState.value.permissionsRequestInProgress)
        }

    @Test
    fun `Given a view model When RequestPermissions is received twice Then the second one is ignored while the first is in progress`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel()

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.RequestPermissions)

                // Then
                assertEquals(Effect.RequestPermissions, awaitItem())

                // When
                viewModel.setEvent(Event.RequestPermissions)

                // Then
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given a view model before engagement When permissions are granted Then PermissionsGranted is emitted and the request is no longer in progress`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel()

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.PermissionReceived(denied = false))

                // Then
                assertEquals(Effect.PermissionsGranted, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            val state = viewModel.uiState.value
            assertEquals(true, state.hasPermissions)
            assertFalse(state.permissionsRequestInProgress)
        }

    /**
     * StartProximity sets engagementStarted = true.
     */
    @Test
    fun `Given an engagement in progress When permissions are denied Then PermissionsRevoked is emitted and permissions are cleared`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel(interactor = transferInteractor(statusFlow = flowOf()))
            viewModel.setEvent(Event.Init(docs = docs))
            viewModel.setEvent(Event.StartProximity)

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.PermissionReceived(denied = true))

                // Then
                assertEquals(Effect.PermissionsRevoked, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(false, viewModel.uiState.value.hasPermissions)
        }

    @Test
    fun `Given a view model before engagement When permissions are denied Then no effect is emitted and permissions stay cleared`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel()

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.PermissionReceived(denied = true))

                // Then
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(false, viewModel.uiState.value.hasPermissions)
        }

    /**
     * StartProximity sets engagementStarted = true.
     */
    @Test
    fun `Given an engagement in progress When permissions are granted Then no effect is emitted and permissions are set`() =
        runTest(testDispatcher) {
            // Given
            val viewModel = viewModel(interactor = transferInteractor(statusFlow = flowOf()))
            viewModel.setEvent(Event.Init(docs = docs))
            viewModel.setEvent(Event.StartProximity)

            viewModel.effect.test {
                // When
                viewModel.setEvent(Event.PermissionReceived(denied = false))

                // Then
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(true, viewModel.uiState.value.hasPermissions)
        }

    @Test
    fun `Given a view model When OpenAppSettings is received Then the open-settings effect is emitted`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel()

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OpenAppSettings)

            // Then
            assertEquals(Effect.OpenAppSettings, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    //endregion

    //region proximity / connection status

    @Test
    fun `Given an initialised view model with non-terminal statuses When StartProximity is received Then the connection is prepared and engagement starts and the status is mapped`() =
        runTest(testDispatcher) {
            // Given
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

            // When
            viewModel.setEvent(Event.StartProximity)

            // Then
            assertTrue(viewModel.uiState.value.engagementStarted)
            assertEquals("status", viewModel.uiState.value.connectionStatus)
            verifySuspend(exactly(1)) { interactor.prepareConnection() }
            verify(exactly(1)) { interactor.startEngagement(qrCode) }
        }

    @Test
    fun `Given a connection that reports an error When StartProximity is received Then it navigates back`() = runTest(testDispatcher) {
        // Given
        val interactor = transferInteractor(statusFlow = flowOf(TransferStatus.Error("boom")))
        val viewModel = viewModel(interactor = interactor)
        viewModel.setEvent(Event.Init(docs = docs))

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.StartProximity)

            // Then
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given a connection that reports a disconnect When StartProximity is received Then it navigates back`() = runTest(testDispatcher) {
        // Given
        val interactor = transferInteractor(statusFlow = flowOf(TransferStatus.Disconnected))
        val viewModel = viewModel(interactor = interactor)
        viewModel.setEvent(Event.Init(docs = docs))

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.StartProximity)

            // Then
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given a connection that reports a received response When StartProximity is received Then it navigates to the show-documents screen`() =
        runTest(testDispatcher) {
            // Given
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
                // When
                viewModel.setEvent(Event.StartProximity)

                // Then
                assertEquals(
                    Effect.Navigation.NavigateToShowDocumentsScreen(receivedDocuments = receivedUi),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `Given a view model When StopProximity is received Then the connection is stopped and the engagement state is reset`() =
        runTest(testDispatcher) {
            // Given
            val interactor = transferInteractor()
            val viewModel = viewModel(interactor = interactor)

            // When
            viewModel.setEvent(Event.StopProximity)

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.engagementStarted)
            assertFalse(state.isLoading)
            verifySuspend(exactly(1)) { interactor.stopConnection() }
        }

    //endregion

    //region back / cancel

    @Test
    fun `Given a view model When OnCancelClick is received Then it navigates back and the connection is stopped`() = runTest(testDispatcher) {
        // Given
        val interactor = transferInteractor()
        val viewModel = viewModel(interactor = interactor)

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnCancelClick)

            // Then
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        verifySuspend(exactly(1)) { interactor.stopConnection() }
    }

    @Test
    fun `Given a view model When OnBackClick is received Then it navigates back`() = runTest(testDispatcher) {
        // Given
        val viewModel = viewModel()

        viewModel.effect.test {
            // When
            viewModel.setEvent(Event.OnBackClick)

            // Then
            assertEquals(Effect.Navigation.GoBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    //endregion
}
