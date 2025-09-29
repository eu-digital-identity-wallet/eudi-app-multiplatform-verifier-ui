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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private const val white: Long = 0xFFFFFFFF
private const val black: Long = 0xFF000000

// Light theme base colors palette.
private const val eudiw_theme_light_primary: Long = 0xFF2A5FD9
private const val eudiw_theme_light_onPrimary: Long = white
private const val eudiw_theme_light_primaryContainer: Long = 0xFFEADDFF
private const val eudiw_theme_light_onPrimaryContainer: Long = 0xFF21005D
private const val eudiw_theme_light_secondary: Long = 0xFFD6D9F9
private const val eudiw_theme_light_onSecondary: Long = 0xF1D192B
private const val eudiw_theme_light_secondaryContainer: Long = 0xFFE8DEF8
private const val eudiw_theme_light_onSecondaryContainer: Long = 0xFF1D192B
private const val eudiw_theme_light_tertiary: Long = 0xFFE4EEE7
private const val eudiw_theme_light_onTertiary: Long = 0xFF1D192B
private const val eudiw_theme_light_tertiaryContainer: Long = 0xFFFFD8E4
private const val eudiw_theme_light_onTertiaryContainer: Long = 0xFF31111D
private const val eudiw_theme_light_error: Long = 0xFFB3261E
private const val eudiw_theme_light_onError: Long = white
private const val eudiw_theme_light_errorContainer: Long = 0xFFF9DEDC
private const val eudiw_theme_light_onErrorContainer: Long = 0xFF410E0B
private const val eudiw_theme_light_surface: Long = 0xFFF7FAFF
private const val eudiw_theme_light_onSurface: Long = 0xFF1D1B20
private const val eudiw_theme_light_background: Long = eudiw_theme_light_surface
private const val eudiw_theme_light_onBackground: Long = eudiw_theme_light_onSurface
private const val eudiw_theme_light_surfaceVariant: Long = 0xFFF5DED8
private const val eudiw_theme_light_onSurfaceVariant: Long = 0xFF49454F
private const val eudiw_theme_light_outline: Long = 0xFF79747E
private const val eudiw_theme_light_outlineVariant: Long = 0xFFCAC4D0
private const val eudiw_theme_light_scrim: Long = black
private const val eudiw_theme_light_inverseSurface: Long = 0xFF322F35
private const val eudiw_theme_light_inverseOnSurface: Long = 0xFFF5EFF7
private const val eudiw_theme_light_inversePrimary: Long = 0xFFD0BCFF
private const val eudiw_theme_light_surfaceDim: Long = 0xFFE2E8F3
private const val eudiw_theme_light_surfaceBright: Long = 0xFFFEF7FF
private const val eudiw_theme_light_surfaceContainerLowest: Long = white
private const val eudiw_theme_light_surfaceContainerLow: Long = 0xFFF7F2FA
private const val eudiw_theme_light_surfaceContainer: Long = 0xFFEBF1FD
private const val eudiw_theme_light_surfaceContainerHigh: Long = 0xFFECE6F0
private const val eudiw_theme_light_surfaceContainerHighest: Long = 0xFFE6E0E9
private const val eudiw_theme_light_surfaceTint: Long = eudiw_theme_light_surface

// Light theme extra colors palette.
private const val eudiw_theme_light_success: Long = 0xFF55953B
private const val eudiw_theme_light_warning: Long = 0xFFF39626
private const val eudiw_theme_light_pending: Long = 0xFFAB5200
private const val eudiw_theme_light_divider: Long = 0xFFD9D9D9


// Dark theme base colors palette.
private const val eudiw_theme_dark_primary: Long = 0xFFB4C5FF
private const val eudiw_theme_dark_onPrimary: Long = 0xFF002A77
private const val eudiw_theme_dark_primaryContainer: Long = 0xFF1A55CF
private const val eudiw_theme_dark_onPrimaryContainer: Long = white
private const val eudiw_theme_dark_secondary: Long = white
private const val eudiw_theme_dark_onSecondary: Long = 0xFF2B2F47
private const val eudiw_theme_dark_secondaryContainer: Long = 0xFFCFD2F2
private const val eudiw_theme_dark_onSecondaryContainer: Long = 0xFF3A3E57
private const val eudiw_theme_dark_tertiary: Long = 0xFF1F2B25
private const val eudiw_theme_dark_onTertiary: Long = 0xFF29322E
private const val eudiw_theme_dark_tertiaryContainer: Long = 0xFFCDD7D0
private const val eudiw_theme_dark_onTertiaryContainer: Long = 0xFF38413D
private const val eudiw_theme_dark_error: Long = 0xFFFFB4AA
private const val eudiw_theme_dark_onError: Long = 0xFF690003
private const val eudiw_theme_dark_errorContainer: Long = 0xFFA61C16
private const val eudiw_theme_dark_onErrorContainer: Long = 0xFFFFF6F5
private const val eudiw_theme_dark_surface: Long = 0xFF131313
private const val eudiw_theme_dark_onSurface: Long = 0xFFE5E2E1
private const val eudiw_theme_dark_background: Long = eudiw_theme_dark_surface
private const val eudiw_theme_dark_onBackground: Long = eudiw_theme_dark_onSurface
private const val eudiw_theme_dark_surfaceVariant: Long = 0xFF45474B
private const val eudiw_theme_dark_onSurfaceVariant: Long = 0xFFC5C6CB
private const val eudiw_theme_dark_outline: Long = 0xFF8F9195
private const val eudiw_theme_dark_outlineVariant: Long = 0xFF45474B
private const val eudiw_theme_dark_scrim: Long = black
private const val eudiw_theme_dark_inverseSurface: Long = 0xFFE5E2E1
private const val eudiw_theme_dark_inverseOnSurface: Long = 0xFF313030
private const val eudiw_theme_dark_inversePrimary: Long = 0xFF1B55CF
private const val eudiw_theme_dark_surfaceDim: Long = 0xFF131313
private const val eudiw_theme_dark_surfaceBright: Long = 0xFF3A3939
private const val eudiw_theme_dark_surfaceContainerLowest: Long = 0xFF0E0E0E
private const val eudiw_theme_dark_surfaceContainerLow: Long = 0xFF1C1B1C
private const val eudiw_theme_dark_surfaceContainer: Long = 0xFF1C1E2E
private const val eudiw_theme_dark_surfaceContainerHigh: Long = 0xFF2A2A2A
private const val eudiw_theme_dark_surfaceContainerHighest: Long = 0xFF353535
private const val eudiw_theme_dark_surfaceTint: Long = eudiw_theme_dark_surface

// Dark theme extra colors palette.
private const val eudiw_theme_dark_success: Long = 0xFF93D875
private const val eudiw_theme_dark_warning: Long = 0xFFFFB689
private const val eudiw_theme_dark_pending: Long = 0xFFCC8B3F
private const val eudiw_theme_dark_divider: Long = 0xFFD9D9D9


internal const val eudiw_theme_light_background_preview: Long = eudiw_theme_light_surface
internal const val eudiw_theme_dark_background_preview: Long = eudiw_theme_dark_surface

internal val lightColors = lightColorScheme(
    primary = Color(eudiw_theme_light_primary),
    onPrimary = Color(eudiw_theme_light_onPrimary),
    primaryContainer = Color(eudiw_theme_light_primaryContainer),
    onPrimaryContainer = Color(eudiw_theme_light_onPrimaryContainer),
    secondary = Color(eudiw_theme_light_secondary),
    onSecondary = Color(eudiw_theme_light_onSecondary),
    secondaryContainer = Color(eudiw_theme_light_secondaryContainer),
    onSecondaryContainer = Color(eudiw_theme_light_onSecondaryContainer),
    tertiary = Color(eudiw_theme_light_tertiary),
    onTertiary = Color(eudiw_theme_light_onTertiary),
    tertiaryContainer = Color(eudiw_theme_light_tertiaryContainer),
    onTertiaryContainer = Color(eudiw_theme_light_onTertiaryContainer),
    error = Color(eudiw_theme_light_error),
    errorContainer = Color(eudiw_theme_light_errorContainer),
    onError = Color(eudiw_theme_light_onError),
    onErrorContainer = Color(eudiw_theme_light_onErrorContainer),
    background = Color(eudiw_theme_light_background),
    onBackground = Color(eudiw_theme_light_onBackground),
    surface = Color(eudiw_theme_light_surface),
    onSurface = Color(eudiw_theme_light_onSurface),
    surfaceVariant = Color(eudiw_theme_light_surfaceVariant),
    onSurfaceVariant = Color(eudiw_theme_light_onSurfaceVariant),
    outline = Color(eudiw_theme_light_outline),
    inverseOnSurface = Color(eudiw_theme_light_inverseOnSurface),
    inverseSurface = Color(eudiw_theme_light_inverseSurface),
    inversePrimary = Color(eudiw_theme_light_inversePrimary),
    surfaceTint = Color(eudiw_theme_light_surfaceTint),
    outlineVariant = Color(eudiw_theme_light_outlineVariant),
    scrim = Color(eudiw_theme_light_scrim),
    surfaceBright = Color(eudiw_theme_light_surfaceBright),
    surfaceDim = Color(eudiw_theme_light_surfaceDim),
    surfaceContainer = Color(eudiw_theme_light_surfaceContainer),
    surfaceContainerHigh = Color(eudiw_theme_light_surfaceContainerHigh),
    surfaceContainerHighest = Color(eudiw_theme_light_surfaceContainerHighest),
    surfaceContainerLow = Color(eudiw_theme_light_surfaceContainerLow),
    surfaceContainerLowest = Color(eudiw_theme_light_surfaceContainerLowest),
)

internal val darkColors = darkColorScheme(
    primary = Color(eudiw_theme_dark_primary),
    onPrimary = Color(eudiw_theme_dark_onPrimary),
    primaryContainer = Color(eudiw_theme_dark_primaryContainer),
    onPrimaryContainer = Color(eudiw_theme_dark_onPrimaryContainer),
    secondary = Color(eudiw_theme_dark_secondary),
    onSecondary = Color(eudiw_theme_dark_onSecondary),
    secondaryContainer = Color(eudiw_theme_dark_secondaryContainer),
    onSecondaryContainer = Color(eudiw_theme_dark_onSecondaryContainer),
    tertiary = Color(eudiw_theme_dark_tertiary),
    onTertiary = Color(eudiw_theme_dark_onTertiary),
    tertiaryContainer = Color(eudiw_theme_dark_tertiaryContainer),
    onTertiaryContainer = Color(eudiw_theme_dark_onTertiaryContainer),
    error = Color(eudiw_theme_dark_error),
    errorContainer = Color(eudiw_theme_dark_errorContainer),
    onError = Color(eudiw_theme_dark_onError),
    onErrorContainer = Color(eudiw_theme_dark_onErrorContainer),
    background = Color(eudiw_theme_dark_background),
    onBackground = Color(eudiw_theme_dark_onBackground),
    surface = Color(eudiw_theme_dark_surface),
    onSurface = Color(eudiw_theme_dark_onSurface),
    surfaceVariant = Color(eudiw_theme_dark_surfaceVariant),
    onSurfaceVariant = Color(eudiw_theme_dark_onSurfaceVariant),
    outline = Color(eudiw_theme_dark_outline),
    inverseOnSurface = Color(eudiw_theme_dark_inverseOnSurface),
    inverseSurface = Color(eudiw_theme_dark_inverseSurface),
    inversePrimary = Color(eudiw_theme_dark_inversePrimary),
    surfaceTint = Color(eudiw_theme_dark_surfaceTint),
    outlineVariant = Color(eudiw_theme_dark_outlineVariant),
    scrim = Color(eudiw_theme_dark_scrim),
    surfaceBright = Color(eudiw_theme_dark_surfaceBright),
    surfaceDim = Color(eudiw_theme_dark_surfaceDim),
    surfaceContainer = Color(eudiw_theme_dark_surfaceContainer),
    surfaceContainerHigh = Color(eudiw_theme_dark_surfaceContainerHigh),
    surfaceContainerHighest = Color(eudiw_theme_dark_surfaceContainerHighest),
    surfaceContainerLow = Color(eudiw_theme_dark_surfaceContainerLow),
    surfaceContainerLowest = Color(eudiw_theme_dark_surfaceContainerLowest),
)

val ColorScheme.divider: Color
    @Composable get() = if (isSystemInDarkTheme()) {
        Color(eudiw_theme_dark_divider)
    } else {
        Color(eudiw_theme_light_divider)
    }