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

package eu.europa.ec.euidi.verifier.presentation.component.wrap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews
import eu.europa.ec.euidi.verifier.presentation.component.utils.ALPHA_DISABLED
import eu.europa.ec.euidi.verifier.presentation.component.utils.SIZE_100
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_LARGE

object ButtonConfigDefaults {
    /** Rounded shape for buttons. */
    val shape: Shape = RoundedCornerShape(SIZE_100.dp)

    /** Default padding inside buttons. */
    val contentPadding: PaddingValues = PaddingValues(
        vertical = 10.dp,
        horizontal = SPACING_LARGE.dp
    )

    /** Default colors; if null, [ButtonDefaults] colors will be used. */
    val buttonColors: ButtonColors? = null
}

/**
 * Types of buttons determining their style.
 */
enum class ButtonType {
    PRIMARY,
    SECONDARY,
}

/**
 * Configuration holder for buttons, marked @Immutable for Compose stability.
 *
 * @property type          Primary vs Secondary style.
 * @property enabled       Whether the button is interactive.
 * @property isWarning     Use warning/error color scheme.
 * @property shape         Corner shape of the button.
 * @property contentPadding Padding inside the button label.
 * @property buttonColors  Custom colors, or null to use defaults.
 * @property onClick       Click callback.
 * @property content       Slot for the button's content.
 */
@Immutable
data class ButtonConfig(
    val type: ButtonType,
    val enabled: Boolean = true,
    val isWarning: Boolean = false,
    val shape: Shape = ButtonConfigDefaults.shape,
    val contentPadding: PaddingValues = ButtonConfigDefaults.contentPadding,
    val buttonColors: ButtonColors? = ButtonConfigDefaults.buttonColors,
    val onClick: () -> Unit,
    val content: @Composable RowScope.() -> Unit,
)

/**
 * Creates a stable [ButtonConfig] instance with up-to-date lambdas for Compose recomposition.
 *
 * @param type           The visual style of the button (primary vs. secondary).
 * @param enabled        Whether the button is enabled.
 * @param isWarning      Whether to use the warning color scheme.
 * @param shape          Rounded corner shape for the button (default from [ButtonConfigDefaults]).
 * @param contentPadding Padding inside the button (default from [ButtonConfigDefaults]).
 * @param buttonColors   Optional custom colors for the button (default from [ButtonConfigDefaults]).
 * @param onClick        Lambda to invoke when the button is clicked.
 * @param content        Composable slot for the button's content (e.g. Text, Icon).
 *
 * This helper keeps a single [ButtonConfig] allocation across recompositions
 * (via [remember]), and uses [rememberUpdatedState] to avoid stale lambda
 * captures while still respecting identity-based recomposition.
 */
@Composable
fun rememberButtonConfig(
    type: ButtonType,
    enabled: Boolean = true,
    isWarning: Boolean = false,
    shape: Shape = ButtonConfigDefaults.shape,
    contentPadding: PaddingValues = ButtonConfigDefaults.contentPadding,
    buttonColors: ButtonColors? = ButtonConfigDefaults.buttonColors,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
): ButtonConfig {
    // Keep the latest onClick lambda without invalidating the whole config
    val currentOnClick by rememberUpdatedState(onClick)
    // Keep the latest content lambda in a stable reference
    val currentContent by rememberUpdatedState(content)

    // Remember the ButtonConfig instance so we don't reallocate it every recomposition.
    // Only re-create when structural parameters change (type, enabled, colors, etc.).
    return remember(
        type,
        enabled,
        isWarning,
        shape,
        contentPadding,
        buttonColors
    ) {
        ButtonConfig(
            type = type,
            enabled = enabled,
            // Wrap the state-backed lambdas in new lambdas so they always call the latest version
            onClick = { currentOnClick() },
            isWarning = isWarning,
            shape = shape,
            contentPadding = contentPadding,
            buttonColors = buttonColors,
            content = { currentContent() }
        )
    }
}

@Composable
fun WrapButton(
    modifier: Modifier = Modifier,
    buttonConfig: ButtonConfig,
) {
    when (buttonConfig.type) {
        ButtonType.PRIMARY -> WrapPrimaryButton(
            modifier = modifier,
            buttonConfig = buttonConfig,
        )

        ButtonType.SECONDARY -> WrapSecondaryButton(
            modifier = modifier,
            buttonConfig = buttonConfig,
        )
    }
}

@Composable
private fun WrapPrimaryButton(
    modifier: Modifier = Modifier,
    buttonConfig: ButtonConfig,
) {
    val (containerColor, contentColor) = if (buttonConfig.isWarning) {
        MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    }

    val disabledContentColor = contentColor.copy(
        alpha = ALPHA_DISABLED
    )
    val disabledContainerColor = containerColor.copy(
        alpha = ALPHA_DISABLED
    )

    val colors = buttonConfig.buttonColors ?: ButtonDefaults.buttonColors(
        containerColor = containerColor,
        disabledContainerColor = disabledContainerColor,
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
    )

    Button(
        modifier = modifier,
        enabled = buttonConfig.enabled,
        onClick = buttonConfig.onClick,
        shape = buttonConfig.shape,
        colors = colors,
        contentPadding = buttonConfig.contentPadding,
        content = buttonConfig.content
    )
}

@Composable
private fun WrapSecondaryButton(
    modifier: Modifier = Modifier,
    buttonConfig: ButtonConfig,
) {
    val (contentColor, borderColor) = if (buttonConfig.isWarning) {
        MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary
    }

    val disabledContentColor = contentColor.copy(
        alpha = ALPHA_DISABLED
    )
    val disabledBorderColor = borderColor.copy(
        alpha = ALPHA_DISABLED
    )

    val colors = buttonConfig.buttonColors ?: ButtonDefaults.outlinedButtonColors(
        contentColor = contentColor,
        disabledContentColor = disabledContentColor,
    )

    OutlinedButton(
        modifier = modifier,
        enabled = buttonConfig.enabled,
        onClick = buttonConfig.onClick,
        shape = buttonConfig.shape,
        colors = colors,
        border = BorderStroke(
            width = 1.dp,
            color = if (buttonConfig.enabled) {
                borderColor
            } else {
                disabledBorderColor
            },
        ),
        contentPadding = buttonConfig.contentPadding,
        content = buttonConfig.content
    )
}

@ThemeModePreviews
@Composable
private fun WrapPrimaryButtonEnabledPreview() {
    PreviewTheme {
        WrapButton(
            buttonConfig = ButtonConfig(
                type = ButtonType.PRIMARY,
                enabled = true,
                onClick = { },
                content = {
                    Text("Enabled Primary Button")
                },
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapPrimaryButtonDisabledPreview() {
    PreviewTheme {
        WrapButton(
            buttonConfig = ButtonConfig(
                type = ButtonType.PRIMARY,
                enabled = false,
                onClick = { },
                content = {
                    Text("Disabled Primary Button")
                },
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapPrimaryButtonEnabledWarningPreview() {
    PreviewTheme {
        WrapButton(
            buttonConfig = ButtonConfig(
                type = ButtonType.PRIMARY,
                enabled = true,
                isWarning = true,
                onClick = { },
                content = {
                    Text("Enabled Warning Primary Button")
                },
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapPrimaryButtonDisabledWarningPreview() {
    PreviewTheme {
        WrapButton(
            buttonConfig = ButtonConfig(
                type = ButtonType.PRIMARY,
                enabled = false,
                isWarning = true,
                onClick = { },
                content = {
                    Text("Disabled Warning Primary Button")
                },
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapSecondaryButtonEnabledPreview() {
    PreviewTheme {
        WrapButton(
            buttonConfig = ButtonConfig(
                type = ButtonType.SECONDARY,
                enabled = true,
                onClick = { },
                content = {
                    Text("Enabled Secondary Button")
                },
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapSecondaryButtonDisabledPreview() {
    PreviewTheme {
        WrapButton(
            buttonConfig = ButtonConfig(
                type = ButtonType.SECONDARY,
                enabled = false,
                onClick = { },
                content = {
                    Text("Disabled Secondary Button")
                },
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapSecondaryButtonEnabledWarningPreview() {
    PreviewTheme {
        WrapButton(
            buttonConfig = ButtonConfig(
                type = ButtonType.SECONDARY,
                enabled = true,
                isWarning = true,
                onClick = { },
                content = {
                    Text("Enabled Warning Secondary Button")
                },
            )
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapSecondaryButtonDisabledWarningPreview() {
    PreviewTheme {
        WrapButton(
            buttonConfig = ButtonConfig(
                type = ButtonType.SECONDARY,
                enabled = false,
                isWarning = true,
                onClick = { },
                content = {
                    Text("Disabled Warning Secondary Button")
                }
            )
        )
    }
}