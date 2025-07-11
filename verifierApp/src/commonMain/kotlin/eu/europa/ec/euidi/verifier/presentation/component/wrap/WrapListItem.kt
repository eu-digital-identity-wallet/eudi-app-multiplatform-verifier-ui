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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.ClickableArea
import eu.europa.ec.euidi.verifier.presentation.component.ListItem
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemLeadingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.preview.PreviewTheme
import eu.europa.ec.euidi.verifier.presentation.component.preview.TextLengthPreviewProvider
import eu.europa.ec.euidi.verifier.presentation.component.preview.ThemeModePreviews
import eu.europa.ec.euidi.verifier.presentation.component.utils.SPACING_MEDIUM
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Composable
fun WrapListItem(
    modifier: Modifier = Modifier,
    item: ListItemDataUi,
    onItemClick: ((item: ListItemDataUi) -> Unit)?,
    throttleClicks: Boolean = true,
    hideSensitiveContent: Boolean = false,
    mainContentVerticalPadding: Dp? = null,
    mainContentTextStyle: TextStyle? = null,
    overlineTextStyle: TextStyle? = null,
    supportingTextColor: Color? = null,
    clickableAreas: List<ClickableArea>? = null,
    shape: Shape? = null,
    colors: CardColors? = null,
) {
    WrapCard(
        modifier = modifier,
        throttleClicks = throttleClicks,
        shape = shape,
        colors = colors,
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            item = item,
            onItemClick = onItemClick,
            hideSensitiveContent = hideSensitiveContent,
            mainContentVerticalPadding = mainContentVerticalPadding,
            mainContentTextStyle = mainContentTextStyle,
            overlineTextStyle = overlineTextStyle ?: MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            supportingTextColor = supportingTextColor,
            clickableAreas = clickableAreas ?: listOf(ClickableArea.ENTIRE_ROW),
        )
    }
}

@ThemeModePreviews
@Composable
private fun WrapListItemPreview(
    @PreviewParameter(TextLengthPreviewProvider::class) text: String
) {
    PreviewTheme {
        val trailingIcon = AppIcons.ChevronRight
        val leadingIcon = AppIcons.Home
        Column(
            verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp)
        ) {
            WrapListItem(
                modifier = Modifier.fillMaxWidth(),
                item = ListItemDataUi(
                    itemId = "1",
                    mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                ),
                onItemClick = {},
            )
            WrapListItem(
                modifier = Modifier.fillMaxWidth(),
                item = ListItemDataUi(
                    itemId = "2",
                    mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                    overlineText = "",
                    supportingText = "",
                ),
                onItemClick = {},
            )
            WrapListItem(
                modifier = Modifier.fillMaxWidth(),
                item = ListItemDataUi(
                    itemId = "3",
                    mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                    overlineText = "Overline text $text",
                    supportingText = "Supporting text $text",
                    leadingContentData = ListItemLeadingContentDataUi.Icon(iconData = leadingIcon),
                    trailingContentData = ListItemTrailingContentDataUi.Icon(
                        iconData = trailingIcon,
                    ),
                ),
                onItemClick = {},
            )
            WrapListItem(
                modifier = Modifier.fillMaxWidth(),
                item = ListItemDataUi(
                    itemId = "4",
                    mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                    supportingText = "Supporting text $text",
                    trailingContentData = ListItemTrailingContentDataUi.Icon(
                        iconData = trailingIcon,
                    ),
                ),
                onItemClick = {},
            )
        }
    }
}