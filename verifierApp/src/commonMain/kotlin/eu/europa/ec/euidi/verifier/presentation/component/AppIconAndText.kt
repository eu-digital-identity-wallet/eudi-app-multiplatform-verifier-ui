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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_SMALL
import eu.europa.ec.euidi.verifier.presentation.component.wrap.WrapImage

@Immutable
data class AppIconAndTextDataUi(
    val appIcon: IconDataUi = AppIcons.LogoPlain,
    val appText: IconDataUi = AppIcons.LogoText,
)

private val DefaultAppIconAndTextData = AppIconAndTextDataUi()

@Composable
fun AppIconAndText(
    modifier: Modifier = Modifier,
    appIconAndTextData: AppIconAndTextDataUi = DefaultAppIconAndTextData,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = SPACING_SMALL.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.Top
    ) {
        WrapImage(iconData = appIconAndTextData.appIcon)
        WrapImage(
            iconData = appIconAndTextData.appText,
            colorFilter = ColorFilter.tint(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun AppIconAndTextPreview() {
    PreviewTheme {
        AppIconAndText(
            appIconAndTextData = AppIconAndTextDataUi(
                appIcon = AppIcons.LogoPlain,
                appText = AppIcons.LogoText,
            )
        )
    }
}