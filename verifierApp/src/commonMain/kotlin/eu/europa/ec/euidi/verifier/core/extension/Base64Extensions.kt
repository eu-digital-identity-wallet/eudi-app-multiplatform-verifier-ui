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

package eu.europa.ec.euidi.verifier.core.extension

import kotlin.io.encoding.Base64

fun ByteArray.encodeToBase64String(urlSafe: Boolean = true, withPadding: Boolean = true): String {
    val codec = if (urlSafe)
        Base64.UrlSafe
    else
        Base64.Default
    return if (withPadding) {
        codec.encode(source = this)
    } else {
        codec.withPadding(option = Base64.PaddingOption.ABSENT)
            .encode(source = this)
    }
}

fun String.decodeBase64ToBytesOrNull(): ByteArray? {
    // Strip optional data-URI header if present
    val raw = this.substringAfter(delimiter = ',', missingDelimiterValue = this).trim()
    if (raw.isEmpty()) return null

    // Detect URL-safe alphabet
    val isUrlSafe = raw.indexOfAny(charArrayOf('-', '_')) >= 0

    // Pad to multiple of 4 (kotlin Base64 is lenient with missing padding, but we add it to be safe)
    val padCount = (4 - raw.length % 4) % 4
    val padded = raw + "=".repeat(padCount)

    return runCatching {
        if (isUrlSafe) {
            Base64.UrlSafe.decode(source = padded)
        } else {
            Base64.Default.decode(source = padded)
        }
    }.getOrNull()
}

fun String.decodeBase64ToUtf8OrNull(): String? =
    decodeBase64ToBytesOrNull()?.decodeToString()