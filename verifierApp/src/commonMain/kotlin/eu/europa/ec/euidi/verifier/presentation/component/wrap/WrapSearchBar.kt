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

package eu.europa.ec.euidi.verifier.presentation.component.wrap

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews

@Composable
fun WrapSearchBar(
    modifier: Modifier = Modifier,
    text: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onClearClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WrapTextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            maxLines = 1,
            singleLine = true,
            onValueChange = {
                onValueChange(it)
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingIcon = {
                WrapIcon(
                    iconData = AppIcons.Search,
                    customTint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = if (text.isNotEmpty()) {
                {
                    WrapIconButton(
                        iconData = AppIcons.Close,
                        onClick = {
                            onClearClick()
                            focusManager.clearFocus()
                        }
                    )
                }
            } else null,
            shape = MaterialTheme.shapes.extraLarge,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            )
        )
    }
}

@Composable
@ThemeModePreviews
private fun WrapSearchBarPreview() {
    PreviewTheme {
        WrapSearchBar(
            modifier = Modifier.fillMaxWidth(),
            text = "",
            placeholder = "Search documents",
            onValueChange = { },
            onClearClick = {},
        )
    }
}