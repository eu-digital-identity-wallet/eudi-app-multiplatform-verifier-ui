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

package eu.europa.ec.euidi.verifier.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * A Composable function that decodes a Base64 encoded image string into an [ImageBitmap].
 *
 * This function uses `produceState` to perform the decoding asynchronously on the `Dispatchers.Default`
 * to avoid blocking the UI thread. It handles potential data URI headers, detects whether the
 * Base64 string is URL-safe or standard, and applies necessary padding before decoding.
 *
 * If the decoding is successful, it returns the [ImageBitmap]. If any error occurs during
 * the decoding process (e.g., invalid Base64 string, unsupported image format), it returns `null`.
 *
 * The decoded [ImageBitmap] is remembered across recompositions as long as the input `base64Image`
 * string remains the same. If `base64Image` changes, the decoding process will be re-triggered.
 *
 * @param base64Image The Base64 encoded string of the image. This can optionally include a
 *                    data URI scheme prefix (e.g., "data:image/png;base64,...").
 * @return An [ImageBitmap] if decoding is successful, or `null` otherwise.
 */
@OptIn(ExperimentalEncodingApi::class)
@Composable
fun rememberBase64DecodedBitmap(base64Image: String): ImageBitmap? {
    // produceState will launch a coroutine on the Composition’s scope
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, base64Image) {
        // run everything in a Default dispatcher to avoid UI jank
        val bmp = withContext(Dispatchers.Default) {
            runCatching {
                // 1) strip any data URI header
                val raw = base64Image.substringAfter(',', base64Image).trim()

                // 2) detect URL-safe vs standard
                val isUrlSafe = raw.indexOfAny(charArrayOf('-', '_')) >= 0

                // 3) pad to multiple of 4
                val padCount = (4 - raw.length % 4) % 4
                val padded = raw + "=".repeat(padCount)

                // 4) decode bytes
                val bytes = if (isUrlSafe) {
                    Base64.UrlSafe.decode(padded)
                } else {
                    Base64.Default.decode(padded)
                }

                // 5) to Compose ImageBitmap
                bytes.decodeToImageBitmap()
            }.getOrNull()  // null on any failure
        }

        // feed the result back into the state
        value = bmp
    }

    return imageBitmap
}