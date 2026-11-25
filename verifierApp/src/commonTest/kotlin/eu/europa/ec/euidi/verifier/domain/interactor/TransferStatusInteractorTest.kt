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
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode.Companion.exactly
import eu.europa.ec.euidi.verifier.core.controller.DataStoreController
import eu.europa.ec.euidi.verifier.core.controller.PrefKey
import eu.europa.ec.euidi.verifier.core.controller.TransferController
import eu.europa.ec.euidi.verifier.core.controller.TransferStatus
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.config.model.Logger
import eu.europa.ec.euidi.verifier.domain.model.DocumentValidityDomain
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentDomain
import eu.europa.ec.euidi.verifier.testutil.TestData
import eu.europa.ec.euidi.verifier.testutil.documentTypeResourceProvider
import eu.europa.ec.euidi.verifier.testutil.sequentialUuidProvider
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.transfer_status_screen_request_label
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TransferStatusInteractorTest {

    /**
     * Builds the SUT, [TransferStatusInteractorImpl], with a dispatcher that shares the `runTest`
     * scheduler and Mokkery mocks for its collaborators. Tests that verify interactions create the
     * relevant mock explicitly and pass it in.
     */
    private fun TestScope.createInteractor(
        resourceProvider: ResourceProvider = stringResourceProvider(),
        uuidProvider: UuidProvider = sequentialUuidProvider(),
        transferController: TransferController = transferController(),
        dataStoreController: DataStoreController = dataStore(),
        configProvider: ConfigProvider = configProvider(),
    ): TransferStatusInteractor {
        val dispatcher = coroutineContext[ContinuationInterceptor] as CoroutineDispatcher
        return TransferStatusInteractorImpl(
            resourceProvider = resourceProvider,
            uuidProvider = uuidProvider,
            transferController = transferController,
            dataStoreController = dataStoreController,
            configProvider = configProvider,
            dispatcher = dispatcher
        )
    }

    //region transformToReceivedDocumentsUi

    @Test
    fun `Given a received document When it is transformed to UI models Then one UI document with its claims and validity is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val received = listOf(
                ReceivedDocumentDomain(
                    isTrusted = true,
                    docType = AttestationType.Pid.docType,
                    claims = mapOf(ClaimItem("given_name") to "John"),
                    validity = DocumentValidityDomain(
                        isDeviceSignatureValid = true,
                        isIssuerSignatureValid = true,
                        isDataIntegrityIntact = true,
                        signed = null,
                        validFrom = null,
                        validUntil = null,
                    )
                )
            )

            // When
            val result = interactor.transformToReceivedDocumentsUi(
                requestedDocuments = emptyList(),
                receivedDocuments = received
            )

            // Then
            assertEquals(1, result.size)
            val doc = result.single()
            assertEquals("uuid-0", doc.id)
            assertEquals(AttestationType.Pid, doc.documentType)
            assertEquals(mapOf(ClaimItem("given_name") to "John"), doc.claims)
            assertEquals(TestData.validDocumentValidityUi, doc.documentValidity)
        }

    @Test
    fun `Given no received documents When they are transformed to UI models Then an empty list is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            // When
            val result = interactor.transformToReceivedDocumentsUi(
                requestedDocuments = emptyList(),
                receivedDocuments = emptyList()
            )

            // Then
            assertTrue(result.isEmpty())
        }

    //endregion

    //region prepareConnection

    @Test
    fun `Given stored settings and configured certificates When the connection is prepared Then the verifier and the transfer manager are initialised with them`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val transferController = transferController()
            val interactor = createInteractor(
                transferController = transferController,
                dataStoreController = dataStore(
                    stored = mapOf(
                        PrefKey.BLE_CENTRAL_CLIENT to false,
                        PrefKey.BLE_PERIPHERAL_SERVER to true,
                        PrefKey.USE_L2CAP to true,
                        PrefKey.CLEAR_BLE_CACHE to true,
                    )
                ),
                configProvider = configProvider(
                    certificates = listOf("CERT_A", "CERT_B"),
                    logger = Logger.LEVEL_DEBUG
                )
            )

            // When
            interactor.prepareConnection()

            // Then
            verify(exactly(1)) {
                transferController.initializeVerifier(
                    listOf("CERT_A", "CERT_B"),
                    Logger.LEVEL_DEBUG
                )
            }
            verify(exactly(1)) {
                transferController.initializeTransferManager(
                    bleCentralClientMode = false,
                    blePeripheralServerMode = true,
                    useL2Cap = true,
                    clearBleCache = true
                )
            }
        }

    /**
     * Defaults baked into the interactor (BLE modes on, L2CAP/clear-cache off).
     */
    @Test
    fun `Given no stored settings When the connection is prepared Then the transfer manager is initialised with the defaults`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val transferController = transferController()
            val interactor = createInteractor(
                transferController = transferController,
                dataStoreController = dataStore()
            )

            // When
            interactor.prepareConnection()

            // Then
            verify(exactly(1)) {
                transferController.initializeTransferManager(
                    bleCentralClientMode = true,
                    blePeripheralServerMode = true,
                    useL2Cap = false,
                    clearBleCache = false
                )
            }
        }

    //endregion

    //region startEngagement

    @Test
    fun `Given an interactor When engagement starts with a QR code Then the transfer controller receives the code`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val transferController = transferController()
            val interactor = createInteractor(transferController = transferController)

            // When
            interactor.startEngagement("mdoc:engagement")

            // Then
            verify(exactly(1)) { transferController.startEngagement("mdoc:engagement") }
        }

    //endregion

    //region getConnectionStatus

    @Test
    fun `Given a stored retainData setting When the connection status is requested Then the request is sent with retainData and the status is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val transferController =
                transferController(statusFlow = flowOf(TransferStatus.RequestSent))
            val interactor = createInteractor(
                transferController = transferController,
                dataStoreController = dataStore(stored = mapOf(PrefKey.RETAIN_DATA to true))
            )

            val docs = listOf(TestData.pidFullRequestedDocument)

            // When
            val status = interactor.getConnectionStatus(docs).first()

            // Then
            assertEquals(TransferStatus.RequestSent, status)
            verify(exactly(1)) { transferController.sendRequest(docs, true) }
        }

    //endregion

    //region getRequestData

    @Test
    fun `Given two requested documents When the request data is requested Then the label is prefixed and the document types are joined`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            val docs = listOf(
                TestData.pidFullRequestedDocument,
                TestData.mdlCustomRequestedDocument,
            )

            // When
            val result = interactor.getRequestData(docs)

            // Then
            assertEquals("Requesting: Full PID; Custom MDL", result)
        }

    @Test
    fun `Given no requested documents When the request data is requested Then only the label is returned`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val interactor = createInteractor()

            // When
            val result = interactor.getRequestData(emptyList())

            // Then
            assertEquals("Requesting: ", result)
        }

    //endregion

    //region stopConnection

    @Test
    fun `Given an interactor When the connection is stopped Then the transfer controller stops it`() =
        runTest(StandardTestDispatcher()) {
            // Given
            val transferController = transferController()
            val interactor = createInteractor(transferController = transferController)

            // When
            interactor.stopConnection()

            // Then
            verify(exactly(1)) { transferController.stopConnection() }
        }

    //endregion

    //region Mocks

    private fun transferController(
        statusFlow: Flow<TransferStatus> = flowOf(TransferStatus.Connected)
    ): TransferController {
        // autoUnit backs the Unit-returning delegations; sendRequest needs an explicit answer.
        val transferController = mock<TransferController>(MockMode.autoUnit)
        every { transferController.sendRequest(any(), any()) } returns statusFlow
        return transferController
    }

    /**
     * Returns the raw stored value (null when absent) and ignores the supplied default on purpose,
     * so the interactor's own `?: default` fallback in getSettingsValue is exercised for missing keys.
     */
    private fun dataStore(stored: Map<PrefKey, Boolean> = emptyMap()): DataStoreController {
        val dataStore = mock<DataStoreController>()
        everySuspend { dataStore.getBoolean(any(), any()) } calls { (key: PrefKey, _: Boolean?) ->
            stored[key]
        }
        return dataStore
    }

    private fun configProvider(
        certificates: List<String> = listOf("CERT"),
        logger: Logger = Logger.LEVEL_DEBUG,
    ): ConfigProvider {
        val configProvider = mock<ConfigProvider>()
        everySuspend { configProvider.getCertificates() } returns certificates
        every { configProvider.logger } returns logger
        return configProvider
    }

    private fun stringResourceProvider(): ResourceProvider {
        val resourceProvider = documentTypeResourceProvider()
        every {
            resourceProvider.getSharedString(Res.string.transfer_status_screen_request_label)
        } returns "Requesting:"
        return resourceProvider
    }

    //endregion
}