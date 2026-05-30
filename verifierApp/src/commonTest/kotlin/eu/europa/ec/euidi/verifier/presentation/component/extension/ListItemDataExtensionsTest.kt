/*
 * Copyright (c) 2026 European Commission
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

package eu.europa.ec.euidi.verifier.presentation.component.extension

import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListItemDataExtensionsTest {

    private fun item(id: String, trailing: ListItemTrailingContentDataUi? = null) = ListItemDataUi(
        itemId = id,
        mainContentData = ListItemMainContentDataUi.Text(id),
        trailingContentData = trailing,
    )

    private fun checkbox(checked: Boolean) =
        ListItemTrailingContentDataUi.Checkbox(CheckboxDataUi(isChecked = checked))

    @Test
    fun `hasAnyCheckedCheckbox is true when at least one checkbox is checked`() {
        val items = listOf(
            item("a", checkbox(checked = false)),
            item("b", checkbox(checked = true)),
        )

        assertTrue(items.hasAnyCheckedCheckbox())
    }

    @Test
    fun `hasAnyCheckedCheckbox is false when all checkboxes are unchecked`() {
        val items = listOf(item("a", checkbox(checked = false)))

        assertFalse(items.hasAnyCheckedCheckbox())
    }

    @Test
    fun `hasAnyCheckedCheckbox is false when there are no checkbox items`() {
        val items = listOf(item("a"))

        assertFalse(items.hasAnyCheckedCheckbox())
    }

    @Test
    fun `hasAnyCheckedCheckbox is false for an empty list`() {
        assertFalse(emptyList<ListItemDataUi>().hasAnyCheckedCheckbox())
    }
}
