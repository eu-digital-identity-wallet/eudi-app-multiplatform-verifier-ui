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
import eu.europa.ec.euidi.verifier.domain.config.model.DocumentMode
import eu.europa.ec.euidi.verifier.domain.config.model.Logger
import eu.europa.ec.euidi.verifier.domain.model.DocumentValidityDomain
import eu.europa.ec.euidi.verifier.domain.model.ReceivedDocumentDomain
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.model.DocumentValidityUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.document_type_employee_id
import eudiverifier.verifierapp.generated.resources.document_type_mdl
import eudiverifier.verifierapp.generated.resources.document_type_pid
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
    fun `transformToReceivedDocumentsUi maps domain documents to ui models`() =
        runTest(StandardTestDispatcher()) {
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

            val result = interactor.transformToReceivedDocumentsUi(
                requestedDocuments = emptyList(),
                receivedDocuments = received
            )

            assertEquals(1, result.size)
            val doc = result.single()
            assertEquals("uuid-0", doc.id)
            assertEquals(AttestationType.Pid, doc.documentType)
            assertEquals(mapOf(ClaimItem("given_name") to "John"), doc.claims)
            assertEquals(
                DocumentValidityUi(
                    isDeviceSignatureValid = true,
                    isIssuerSignatureValid = true,
                    isDataIntegrityIntact = true,
                    signed = null,
                    validFrom = null,
                    validUntil = null,
                ),
                doc.documentValidity
            )
        }

    @Test
    fun `transformToReceivedDocumentsUi returns empty list for empty input`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val result = interactor.transformToReceivedDocumentsUi(
                requestedDocuments = emptyList(),
                receivedDocuments = emptyList()
            )

            assertTrue(result.isEmpty())
        }

    //endregion

    //region prepareConnection

    @Test
    fun `prepareConnection initializes verifier and transfer manager with stored settings`() =
        runTest(StandardTestDispatcher()) {
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

            interactor.prepareConnection()

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

    @Test
    fun `prepareConnection uses defaults when settings are not stored`() =
        runTest(StandardTestDispatcher()) {
            val transferController = transferController()
            val interactor = createInteractor(
                transferController = transferController,
                dataStoreController = dataStore()
            )

            interactor.prepareConnection()

            // Defaults baked into the interactor (BLE modes on, L2CAP/clear-cache off).
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
    fun `startEngagement delegates the qr code to the transfer controller`() =
        runTest(StandardTestDispatcher()) {
            val transferController = transferController()
            val interactor = createInteractor(transferController = transferController)

            interactor.startEngagement("mdoc:engagement")

            verify(exactly(1)) { transferController.startEngagement("mdoc:engagement") }
        }

    //endregion

    //region getConnectionStatus

    @Test
    fun `getConnectionStatus sends the request with retainData from settings`() =
        runTest(StandardTestDispatcher()) {
            val transferController =
                transferController(statusFlow = flowOf(TransferStatus.RequestSent))
            val interactor = createInteractor(
                transferController = transferController,
                dataStoreController = dataStore(stored = mapOf(PrefKey.RETAIN_DATA to true))
            )

            val docs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.FULL,
                )
            )

            val status = interactor.getConnectionStatus(docs).first()

            assertEquals(TransferStatus.RequestSent, status)
            verify(exactly(1)) { transferController.sendRequest(docs, true) }
        }

    //endregion

    //region getRequestData

    @Test
    fun `getRequestData prefixes the label and joins document types`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val docs = listOf(
                RequestedDocumentUi(
                    id = "PID_DOC",
                    documentType = AttestationType.Pid,
                    mode = DocumentMode.FULL,
                ),
                RequestedDocumentUi(
                    id = "MDL_DOC",
                    documentType = AttestationType.Mdl,
                    mode = DocumentMode.CUSTOM,
                )
            )

            val result = interactor.getRequestData(docs)

            assertEquals("Requesting: Full PID; Custom MDL", result)
        }

    @Test
    fun `getRequestData returns just the label when there are no documents`() =
        runTest(StandardTestDispatcher()) {
            val interactor = createInteractor()

            val result = interactor.getRequestData(emptyList())

            assertEquals("Requesting: ", result)
        }

    //endregion

    //region stopConnection

    @Test
    fun `stopConnection delegates to the transfer controller`() =
        runTest(StandardTestDispatcher()) {
            val transferController = transferController()
            val interactor = createInteractor(transferController = transferController)

            interactor.stopConnection()

            verify(exactly(1)) { transferController.stopConnection() }
        }

    //endregion

    //region Mocks

    private fun sequentialUuidProvider(): UuidProvider {
        var counter = 0
        return mock {
            every { provideUuid() } calls { "uuid-${counter++}" }
        }
    }

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
        val resourceProvider = mock<ResourceProvider>()
        every {
            resourceProvider.getSharedString(Res.string.transfer_status_screen_request_label)
        } returns "Requesting:"
        every { resourceProvider.getSharedString(Res.string.document_type_pid) } returns "PID"
        every { resourceProvider.getSharedString(Res.string.document_type_mdl) } returns "MDL"
        every {
            resourceProvider.getSharedString(Res.string.document_type_employee_id)
        } returns "Employee ID"
        return resourceProvider
    }

    //endregion
}