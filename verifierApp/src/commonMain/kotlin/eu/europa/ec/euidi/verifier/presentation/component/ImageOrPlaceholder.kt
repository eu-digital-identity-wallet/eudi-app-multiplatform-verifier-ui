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

package eu.europa.ec.euidi.verifier.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapImage
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.content_description_image_or_placeholder_icon
import org.jetbrains.compose.resources.stringResource

@Composable
fun ImageOrPlaceholder(
    modifier: Modifier = Modifier,
    base64Image: String,
    contentScale: ContentScale? = null,
    fallbackIcon: IconDataUi = AppIcons.User,
) {
    // Attempt the decode
    val bitmap = rememberBase64DecodedBitmap(base64Image = base64Image)

    if (bitmap != null) {
        WrapImage(
            modifier = modifier,
            bitmap = bitmap,
            contentDescription = stringResource(resource = Res.string.content_description_image_or_placeholder_icon),
            contentScale = contentScale,
        )
    } else {
        // Either the string was blank, or decode/bitmap conversion failed:
        WrapImage(
            modifier = modifier,
            iconData = fallbackIcon,
        )
    }
}

@ThemeModePreviews
@Composable
private fun ImageOrPlaceholderPreview() {
    PreviewTheme {
        ImageOrPlaceholder(
            base64Image = "",
            modifier = Modifier
        )
    }
}