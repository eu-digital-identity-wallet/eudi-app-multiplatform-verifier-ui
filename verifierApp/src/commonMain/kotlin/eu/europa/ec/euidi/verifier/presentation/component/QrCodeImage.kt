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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_SMALL
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapImage
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.content_description_qr_code_image
import org.jetbrains.compose.resources.stringResource
import qrgenerator.qrkitpainter.rememberQrKitPainter

/**
 * Displays a centered QR code.
 *
 * @param qrCode              the raw string to encode as QR.
 * @param modifier            layout modifiers for the outer container.
 * @param contentDescription  description for accessibility (default from resources).
 * @param sizeFraction        fraction of max width (0–1) that the QR occupies.
 * @param backgroundColor     background color behind the QR.
 * @param padding             inner padding around the QR.
 * @param aspectRatio         width:height ratio of the QR (default 1f for square).
 */
@Composable
fun QrCodeImage(
    qrCode: String,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(Res.string.content_description_qr_code_image),
    sizeFraction: Float = 0.7f,
    backgroundColor: Color = Color.White,
    padding: Dp = SPACING_SMALL.dp,
    aspectRatio: Float = 1f,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        WrapImage(
            painter = rememberQrKitPainter(data = qrCode),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth(sizeFraction)
                .aspectRatio(aspectRatio)
                .background(backgroundColor)
                .padding(padding)
        )
    }
}

@ThemeModePreviews
@Composable
private fun QrCodeImagePreview() {
    PreviewTheme {
        QrCodeImage(
            qrCode = "",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}