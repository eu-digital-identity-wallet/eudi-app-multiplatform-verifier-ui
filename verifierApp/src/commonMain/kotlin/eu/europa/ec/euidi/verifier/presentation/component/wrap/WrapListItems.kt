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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.ClickableArea
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
fun WrapListItems(
    modifier: Modifier = Modifier,
    items: List<ListItemDataUi>,
    onItemClick: ((item: ListItemDataUi) -> Unit)?,
    hideSensitiveContent: Boolean = false,
    mainContentVerticalPadding: Dp? = null,
    clickableAreas: List<ClickableArea>? = null,
    throttleClicks: Boolean = true,
    addDivider: Boolean = true,
    shape: Shape? = null,
    colors: CardColors? = null,
    overlineTextStyle: TextStyle? = null,
) {
    WrapCard(
        modifier = modifier,
        shape = shape,
        colors = colors,
    ) {
        items.forEachIndexed { index, item ->
            WrapListItem(
                modifier = Modifier
                    .fillMaxWidth(),
                item = item,
                onItemClick = onItemClick,
                throttleClicks = throttleClicks,
                hideSensitiveContent = hideSensitiveContent,
                mainContentVerticalPadding = mainContentVerticalPadding,
                clickableAreas = clickableAreas,
                overlineTextStyle = overlineTextStyle,
                shape = RectangleShape
            )


            if (addDivider && index < items.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = SPACING_MEDIUM.dp))
            }
        }
    }
}

@ThemeModePreviews
@Composable
private fun WrapListItemsPreview(
    @PreviewParameter(TextLengthPreviewProvider::class) text: String
) {
    PreviewTheme {
        val trailingIcon = AppIcons.ChevronRight
        val leadingIcon = AppIcons.Home
        val items = listOf(
            ListItemDataUi(
                itemId = "1",
                mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
            ),
            ListItemDataUi(
                itemId = "2",
                mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                overlineText = "",
                supportingText = "",
            ),
            ListItemDataUi(
                itemId = "3",
                mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                overlineText = "Overline text $text",
                supportingText = "Supporting text $text",
                leadingContentData = ListItemLeadingContentDataUi.Icon(iconData = leadingIcon),
                trailingContentData = ListItemTrailingContentDataUi.Icon(
                    iconData = trailingIcon,
                ),
            ),
            ListItemDataUi(
                itemId = "4",
                mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                overlineText = "Overline text $text",
                supportingText = "Supporting text $text",
                leadingContentData = ListItemLeadingContentDataUi.Icon(iconData = leadingIcon),
                trailingContentData = ListItemTrailingContentDataUi.Checkbox(
                    checkboxData = CheckboxDataUi(
                        isChecked = true,
                        enabled = true,
                    ),
                ),
            ),
            ListItemDataUi(
                itemId = "5",
                mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                supportingText = "Supporting text $text",
                trailingContentData = ListItemTrailingContentDataUi.Icon(
                    iconData = trailingIcon,
                ),
            ),
            ListItemDataUi(
                itemId = "6",
                mainContentData = ListItemMainContentDataUi.Text(text = "Main text $text"),
                supportingText = "Supporting text $text",
                trailingContentData = ListItemTrailingContentDataUi.Checkbox(
                    checkboxData = CheckboxDataUi(
                        isChecked = true,
                        enabled = true,
                    ),
                ),
            ),
        )

        WrapListItems(
            items = items,
            onItemClick = {},
        )
    }
}