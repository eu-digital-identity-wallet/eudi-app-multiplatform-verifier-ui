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

    @Composable
    fun defaultColors(
        type: ButtonType,
        isWarning: Boolean
    ): ButtonColors {
        return when (type) {
            ButtonType.PRIMARY -> {
                val (containerColor, contentColor) = if (isWarning) {
                    MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
                } else {
                    MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                }

                val disabledContainerColor = containerColor.copy(alpha = ALPHA_DISABLED)
                val disabledContentColor = contentColor.copy(alpha = ALPHA_DISABLED)

                ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    disabledContainerColor = disabledContainerColor,
                    contentColor = contentColor,
                    disabledContentColor = disabledContentColor
                )
            }

            ButtonType.SECONDARY -> {
                val contentColor = if (isWarning) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }

                val disabledContentColor = contentColor.copy(
                    alpha = ALPHA_DISABLED
                )

                ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = disabledContentColor
                )
            }
        }
    }

    @Composable
    fun defaultBorder(
        type: ButtonType,
        isWarning: Boolean,
        enabled: Boolean
    ): BorderStroke? = when (type) {
        ButtonType.PRIMARY -> null

        ButtonType.SECONDARY -> {
            val borderColor = if (isWarning) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }

            val disabledBorderColor = borderColor.copy(
                alpha = ALPHA_DISABLED
            )

            BorderStroke(
                width = 1.dp,
                color = if (enabled) {
                    borderColor
                } else {
                    disabledBorderColor
                }
            )
        }
    }
}

/**
 * Types of buttons determining their style.
 */
enum class ButtonType {
    PRIMARY,
    SECONDARY,
}

@Immutable
data class ButtonConfig(
    val type: ButtonType,
    val enabled: Boolean = true,
    val isWarning: Boolean = false,
    val shape: Shape = ButtonConfigDefaults.shape,
    val contentPadding: PaddingValues = ButtonConfigDefaults.contentPadding,
    val buttonColors: ButtonColors? = null,
    val border: BorderStroke? = null,
    val onClick: () -> Unit,
    val content: @Composable RowScope.() -> Unit,
)

@Composable
fun rememberButtonConfig(
    type: ButtonType,
    enabled: Boolean = true,
    isWarning: Boolean = false,
    shape: Shape = ButtonConfigDefaults.shape,
    contentPadding: PaddingValues = ButtonConfigDefaults.contentPadding,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
): ButtonConfig {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentContent by rememberUpdatedState(content)

    val colors = ButtonConfigDefaults.defaultColors(type, isWarning)
    val border = ButtonConfigDefaults.defaultBorder(type, isWarning, enabled)

    return remember(
        type, enabled, isWarning, shape, contentPadding, colors, border
    ) {
        ButtonConfig(
            type = type,
            enabled = enabled,
            isWarning = isWarning,
            shape = shape,
            contentPadding = contentPadding,
            buttonColors = colors,
            border = border,
            onClick = { currentOnClick() },
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
    val colors = buttonConfig.buttonColors ?: ButtonConfigDefaults.defaultColors(
        type = buttonConfig.type,
        isWarning = buttonConfig.isWarning
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
    val colors = buttonConfig.buttonColors ?: ButtonConfigDefaults.defaultColors(
        type = buttonConfig.type,
        isWarning = buttonConfig.isWarning
    )

    val border = buttonConfig.border ?: ButtonConfigDefaults.defaultBorder(
        type = buttonConfig.type,
        isWarning = buttonConfig.isWarning,
        enabled = buttonConfig.enabled
    )

    OutlinedButton(
        modifier = modifier,
        enabled = buttonConfig.enabled,
        onClick = buttonConfig.onClick,
        shape = buttonConfig.shape,
        colors = colors,
        border = border,
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