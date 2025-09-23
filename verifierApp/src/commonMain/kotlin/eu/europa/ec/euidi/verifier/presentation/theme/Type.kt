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

package eu.europa.ec.euidi.verifier.presentation.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.roboto_light
import eudiverifier.verifierapp.generated.resources.roboto_medium
import eudiverifier.verifierapp.generated.resources.roboto_regular
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun appFontFamily() = FontFamily(
    Font(Res.font.roboto_light, weight = FontWeight.W300),
    Font(Res.font.roboto_regular, weight = FontWeight.W400),
    Font(Res.font.roboto_medium, weight = FontWeight.W500),
)

@Composable
fun appTypography(): Typography {
    val appFontFamily = appFontFamily()

    return remember {
        with(Typography()) {
            copy(
                displayLarge = displayLarge.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 57.sp,
                    letterSpacing = TextUnit(
                        value = -0.25f,
                        type = TextUnitType.Sp
                    ),
                ),
                displayMedium = displayMedium.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 45.sp,
                    letterSpacing = TextUnit(
                        value = 0f,
                        type = TextUnitType.Sp
                    ),
                ),
                displaySmall = displaySmall.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 36.sp,
                    letterSpacing = TextUnit(
                        value = 0f,
                        type = TextUnitType.Sp
                    ),
                ),

                headlineLarge = headlineLarge.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 32.sp,
                    letterSpacing = TextUnit(
                        value = 0f,
                        type = TextUnitType.Sp
                    ),
                ),
                headlineMedium = headlineMedium.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 28.sp,
                    letterSpacing = TextUnit(
                        value = 0f,
                        type = TextUnitType.Sp
                    ),
                ),
                headlineSmall = headlineSmall.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 24.sp,
                    letterSpacing = TextUnit(
                        value = 0f,
                        type = TextUnitType.Sp
                    ),
                ),

                titleLarge = titleLarge.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 22.sp,
                    letterSpacing = TextUnit(
                        value = 0f,
                        type = TextUnitType.Sp
                    ),
                ),
                titleMedium = titleMedium.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp,
                    letterSpacing = TextUnit(
                        value = 0.15f,
                        type = TextUnitType.Sp
                    ),
                ),
                titleSmall = titleSmall.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                    letterSpacing = TextUnit(
                        value = 0.1f,
                        type = TextUnitType.Sp
                    ),
                ),

                labelLarge = labelLarge.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W500,
                    fontSize = 14.sp,
                    letterSpacing = TextUnit(
                        value = 0.1f,
                        type = TextUnitType.Sp
                    ),
                ),
                labelMedium = labelMedium.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W500,
                    fontSize = 12.sp,
                    letterSpacing = TextUnit(
                        value = 0.5f,
                        type = TextUnitType.Sp
                    ),
                ),
                labelSmall = labelSmall.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W500,
                    fontSize = 11.sp,
                    letterSpacing = TextUnit(
                        value = 0.5f,
                        type = TextUnitType.Sp
                    ),
                ),

                bodyLarge = bodyLarge.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 16.sp,
                    letterSpacing = TextUnit(
                        value = 0.5f,
                        type = TextUnitType.Sp
                    ),
                ),
                bodyMedium = bodyMedium.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    letterSpacing = TextUnit(
                        value = 0.25f,
                        type = TextUnitType.Sp
                    ),
                ),
                bodySmall = bodySmall.copy(
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 12.sp,
                    letterSpacing = TextUnit(
                        value = 0.4f,
                        type = TextUnitType.Sp
                    ),
                ),
            )
        }
    }
}


@Composable
@Preview
private fun TypographyDisplayPreview() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypographyStyle(
                style = MaterialTheme.typography.displayLarge,
                styleName = "Display Large",
            )
            TypographyStyle(
                style = MaterialTheme.typography.displayMedium,
                styleName = "Display Medium",
            )
            TypographyStyle(
                style = MaterialTheme.typography.displaySmall,
                styleName = "Display Small",
            )
        }
    }
}

@Composable
@Preview
private fun TypographyHeadlinePreview() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypographyStyle(
                style = MaterialTheme.typography.headlineLarge,
                styleName = "Headline Large",
            )
            TypographyStyle(
                style = MaterialTheme.typography.headlineMedium,
                styleName = "Headline Medium",
            )
            TypographyStyle(
                style = MaterialTheme.typography.headlineSmall,
                styleName = "Headline Small",
            )
        }
    }
}

@Composable
@Preview
private fun TypographyTitlePreview() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypographyStyle(
                style = MaterialTheme.typography.titleLarge,
                styleName = "Title Large",
            )
            TypographyStyle(
                style = MaterialTheme.typography.titleMedium,
                styleName = "Title Medium",
            )
            TypographyStyle(
                style = MaterialTheme.typography.titleSmall,
                styleName = "Title Small",
            )
        }
    }
}

@Composable
@Preview
private fun TypographyLabelPreview() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypographyStyle(
                style = MaterialTheme.typography.labelLarge,
                styleName = "Label Large",
            )
            TypographyStyle(
                style = MaterialTheme.typography.labelMedium,
                styleName = "Label Medium",
            )
            TypographyStyle(
                style = MaterialTheme.typography.labelSmall,
                styleName = "Label Small",
            )
        }
    }
}

@Composable
@Preview
private fun TypographyBodyPreview() {
    PreviewTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypographyStyle(
                style = MaterialTheme.typography.bodyLarge,
                styleName = "Body Large",
            )
            TypographyStyle(
                style = MaterialTheme.typography.bodyMedium,
                styleName = "Body Medium",
            )
            TypographyStyle(
                style = MaterialTheme.typography.bodySmall,
                styleName = "Body Small",
            )
        }
    }
}

@Composable
private fun TypographyStyle(
    style: TextStyle,
    styleName: String,
    displayText: String = "The quick brown fox jumps over the lazy dog.",
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = styleName,
        )

        Text(
            text = displayText,
            style = style,
        )
    }
}