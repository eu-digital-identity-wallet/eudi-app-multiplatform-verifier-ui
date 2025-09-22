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

@file:OptIn(ExperimentalTime::class)

package eu.europa.ec.euidi.verifier.presentation.ui.show_document.model

import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.model.DocumentValidityDomain
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelable
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelize
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_is_data_integrity_intact
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_is_device_signature_valid
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_is_issuer_signature_valid
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_signed
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_valid_from
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_valid_until
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class DocumentUi(
    val id: String,
    val docType: String,
    val uiClaims: List<ListItemDataUi>,
    val validityInfo: List<ListItemDataUi>,
)

@CommonParcelize
data class DocumentValidityUi(
    val isDeviceSignatureValid: Boolean,
    val isIssuerSignatureValid: Boolean,
    val isDataIntegrityIntact: Boolean,
    val signed: String?,
    val validFrom: String?,
    val validUntil: String?,
) : CommonParcelable

fun DocumentValidityDomain.toUi(): DocumentValidityUi {
    return DocumentValidityUi(
        isDeviceSignatureValid = isDeviceSignatureValid ?: false,
        isIssuerSignatureValid = isIssuerSignatureValid ?: false,
        isDataIntegrityIntact = isDataIntegrityIntact ?: false,
        signed = signed?.toText(),
        validFrom = validFrom?.toText(),
        validUntil = validUntil?.toText(),
    )
}

fun DocumentValidityUi.toListItems(
    resourceProvider: ResourceProvider,
    uuidProvider: UuidProvider,
): List<ListItemDataUi> {
    val boolItems = listOf(
        buildBoolItem(
            id = uuidProvider.provideUuid(),
            value = isDataIntegrityIntact,
            overlineText = resourceProvider.getSharedString(
                Res.string.show_documents_screen_item_is_data_integrity_intact
            )
        ),
        buildBoolItem(
            id = uuidProvider.provideUuid(),
            value = isDeviceSignatureValid,
            overlineText = resourceProvider.getSharedString(
                Res.string.show_documents_screen_item_is_device_signature_valid
            )
        ),
        buildBoolItem(
            id = uuidProvider.provideUuid(),
            value = isIssuerSignatureValid,
            overlineText = resourceProvider.getSharedString(
                Res.string.show_documents_screen_item_is_issuer_signature_valid
            )
        )
    )

    val optionalStringItems = listOfNotNull(
        buildStringItemOrNull(
            id = uuidProvider.provideUuid(),
            value = signed,
            overlineText = resourceProvider.getSharedString(
                Res.string.show_documents_screen_item_signed
            )
        ),
        buildStringItemOrNull(
            id = uuidProvider.provideUuid(),
            value = validFrom,
            overlineText = resourceProvider.getSharedString(
                Res.string.show_documents_screen_item_valid_from
            )
        ),
        buildStringItemOrNull(
            id = uuidProvider.provideUuid(),
            value = validUntil,
            overlineText = resourceProvider.getSharedString(
                Res.string.show_documents_screen_item_valid_until
            )
        )
    )

    return boolItems + optionalStringItems
}

/**
 * Converts an [Instant] to a formatted string representation.
 *
 * The format used is "dd MMM yyyy", for example "10 Dec 2025".
 *
 * @return The formatted string, or null if the conversion fails.
 */
private fun Instant.toText(): String? {
    return runCatching {
        val localDateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())

        val formatter = LocalDateTime.Format {
            day(padding = Padding.ZERO)
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            year()
        }

        localDateTime.format(formatter)
    }.getOrNull()
}

private fun buildBoolItem(
    id: String,
    value: Boolean,
    overlineText: String
): ListItemDataUi {
    return ListItemDataUi(
        itemId = id,
        mainContentData = ListItemMainContentDataUi.Text(text = value.toString()),
        overlineText = overlineText
    )
}

private fun buildStringItemOrNull(
    id: String,
    value: String?,
    overlineText: String
): ListItemDataUi? {
    return value?.let {
        ListItemDataUi(
            itemId = id,
            mainContentData = ListItemMainContentDataUi.Text(text = it),
            overlineText = overlineText
        )
    }
}