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

package eu.europa.ec.euidi.verifier.presentation.component.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import eu.europa.ec.euidi.verifier.presentation.theme.AppTypography
import eu.europa.ec.euidi.verifier.presentation.theme.darkColors
import eu.europa.ec.euidi.verifier.presentation.theme.lightColors

@Composable
fun PreviewTheme(
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Light theme
        MaterialTheme(
            colorScheme = lightColors,
            typography = AppTypography()
        ) {
            Surface(
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    Text(text = "Light")
                    content()
                }
            }
        }

        // Dark theme
        MaterialTheme(
            colorScheme = darkColors,
            typography = AppTypography()
        ) {
            Surface(
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    Text(text = "Dark")
                    content()
                }
            }
        }
    }
}