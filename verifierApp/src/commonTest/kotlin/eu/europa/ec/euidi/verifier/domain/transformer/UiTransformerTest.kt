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

package eu.europa.ec.euidi.verifier.domain.transformer

import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.domain.transformer.UiTransformer.toListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemLeadingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.testutil.TestData
import org.jetbrains.compose.resources.StringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UiTransformerTest {

    // Resolves any string resource to a constant, so claim-translation lookups never fail.
    private val resourceProvider: ResourceProvider = mock {
        every { getSharedString(any()) } calls { (_: StringResource) -> "label" }
    }

    private val document = TestData.pidReceivedDocument

    //region transformToUiItems

    @Test
    fun `transformToUiItems returns an empty list for empty fields`() {
        val result = UiTransformer.transformToUiItems(
            fields = emptyList(),
            attestationType = AttestationType.Pid,
            resourceProvider = resourceProvider,
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `transformToUiItems maps each claim to a checked checkbox item`() {
        val claims = listOf(ClaimItem("given_name"), ClaimItem("family_name"))

        val result = UiTransformer.transformToUiItems(
            fields = claims,
            attestationType = AttestationType.Pid,
            resourceProvider = resourceProvider,
        )

        assertEquals(2, result.size)
        result.forEachIndexed { index, item ->
            assertEquals(claims[index].label, item.itemId)
            assertIs<ListItemMainContentDataUi.Text>(item.mainContentData)
            val trailing = item.trailingContentData
            assertIs<ListItemTrailingContentDataUi.Checkbox>(trailing)
            assertTrue(trailing.checkboxData.isChecked)
        }
    }

    //endregion

    //region toListItemDataUi content types

    @Test
    fun `toListItemDataUi renders a portrait claim as a user image with empty main text`() {
        val item = document.toListItemDataUi(
            itemId = "id",
            claimKey = ClaimItem("portrait"),
            claimValue = "base64-portrait",
            resourceProvider = resourceProvider,
        )

        val main = item.mainContentData
        assertIs<ListItemMainContentDataUi.Text>(main)
        assertEquals("", main.text)

        val leading = item.leadingContentData
        assertIs<ListItemLeadingContentDataUi.UserImage>(leading)
        assertEquals("base64-portrait", leading.userBase64Image)
    }

    @Test
    fun `toListItemDataUi renders a signature claim as an image`() {
        val item = document.toListItemDataUi(
            itemId = "id",
            claimKey = ClaimItem("signature_usual_mark"),
            claimValue = "base64-signature",
            resourceProvider = resourceProvider,
        )

        val main = item.mainContentData
        assertIs<ListItemMainContentDataUi.Image>(main)
        assertEquals("base64-signature", main.base64Image)
        assertNull(item.leadingContentData)
    }

    @Test
    fun `toListItemDataUi renders a regular claim as text`() {
        val item = document.toListItemDataUi(
            itemId = "id",
            claimKey = ClaimItem("given_name"),
            claimValue = "John",
            resourceProvider = resourceProvider,
        )

        val main = item.mainContentData
        assertIs<ListItemMainContentDataUi.Text>(main)
        assertEquals("John", main.text)
        assertNull(item.leadingContentData)
    }

    //endregion

    //region getClaimTranslation

    @Test
    fun `getClaimTranslation resolves a matching string resource when one exists`() {
        // "home" + "screen_title" -> resource key "home_screen_title", which exists.
        val result = UiTransformer.getClaimTranslation(
            attestationType = "home",
            claimLabel = "screen_title",
            resourceProvider = resourceProvider,
        )

        assertEquals("label", result)
    }

    @Test
    fun `getClaimTranslation falls back to the claim label when no resource matches`() {
        val result = UiTransformer.getClaimTranslation(
            attestationType = "PID",
            claimLabel = "no_such_claim_key",
            resourceProvider = resourceProvider,
        )

        assertEquals("no_such_claim_key", result)
    }

    //endregion

    //region key predicates

    @Test
    fun `key predicates classify document keys`() {
        assertTrue(UiTransformer.keyIsPortrait("portrait"))
        assertFalse(UiTransformer.keyIsPortrait("given_name"))

        assertTrue(UiTransformer.keyIsSignature("signature_usual_mark"))
        assertFalse(UiTransformer.keyIsSignature("given_name"))

        assertTrue(UiTransformer.keyIsUserPseudonym("user_pseudonym"))
        assertFalse(UiTransformer.keyIsUserPseudonym("given_name"))

        assertTrue(UiTransformer.keyIsGender("gender"))
        assertTrue(UiTransformer.keyIsGender("sex"))
        assertFalse(UiTransformer.keyIsGender("given_name"))
    }

    //endregion
}
