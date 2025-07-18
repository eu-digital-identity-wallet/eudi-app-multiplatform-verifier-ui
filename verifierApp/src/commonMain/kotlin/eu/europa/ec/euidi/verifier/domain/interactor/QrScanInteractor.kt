/*
 * Copyright (c) 2023 European Commission
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

import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.qr_scan_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

interface QrScanInteractor {
    suspend fun getScreenTitle(): String
    suspend fun decodeQrCode(code: String): DecodeQrCodePartialState
}

class QrScanInteractorImpl(
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : QrScanInteractor {

    override suspend fun getScreenTitle(): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(Res.string.qr_scan_screen_title)
        }
    }

    override suspend fun decodeQrCode(code: String): DecodeQrCodePartialState {
        //TODO
        return withContext(dispatcher) {
            runCatching {
                delay(1500L)

                DecodeQrCodePartialState.Success
            }.getOrElse {
                DecodeQrCodePartialState.Failure(
                    error = it.message ?: resourceProvider.genericErrorMessage()
                )
            }
        }
    }

}

sealed interface DecodeQrCodePartialState {
    data object Success : DecodeQrCodePartialState
    data class Failure(val error: String) : DecodeQrCodePartialState
}