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

package eu.europa.ec.euidi.verifier.presentation.ui.container

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import eu.europa.ec.euidi.verifier.presentation.navigation.VerifierNavHost
import eu.europa.ec.euidi.verifier.presentation.theme.appTypography
import eu.europa.ec.euidi.verifier.presentation.theme.darkColors
import eu.europa.ec.euidi.verifier.presentation.theme.lightColors
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun ContainerView(
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {

    val colorScheme by remember(isDarkTheme) {
        mutableStateOf(if (isDarkTheme) darkColors else lightColors)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent) //TODO This is the color of the status bar. Do we want to change it?
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            VerifierNavHost()
        }
    }
}