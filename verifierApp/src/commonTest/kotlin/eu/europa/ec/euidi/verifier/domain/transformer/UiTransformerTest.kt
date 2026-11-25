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
    fun `Given no fields When they are transformed to UI items Then an empty list is returned`() {
        // When
        val result = UiTransformer.transformToUiItems(
            fields = emptyList(),
            attestationType = AttestationType.Pid,
            resourceProvider = resourceProvider,
        )

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Given two claims When they are transformed to UI items Then each is a checked checkbox item`() {
        // Given
        val claims = listOf(ClaimItem("given_name"), ClaimItem("family_name"))

        // When
        val result = UiTransformer.transformToUiItems(
            fields = claims,
            attestationType = AttestationType.Pid,
            resourceProvider = resourceProvider,
        )

        // Then
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
    fun `Given a portrait claim When it is converted to a list item Then a user image with empty main text is rendered`() {
        // When
        val item = document.toListItemDataUi(
            itemId = "id",
            claimKey = ClaimItem("portrait"),
            claimValue = "base64-portrait",
            resourceProvider = resourceProvider,
        )

        // Then
        val main = item.mainContentData
        assertIs<ListItemMainContentDataUi.Text>(main)
        assertEquals("", main.text)

        val leading = item.leadingContentData
        assertIs<ListItemLeadingContentDataUi.UserImage>(leading)
        assertEquals("base64-portrait", leading.userBase64Image)
    }

    @Test
    fun `Given a signature claim When it is converted to a list item Then an image is rendered`() {
        // When
        val item = document.toListItemDataUi(
            itemId = "id",
            claimKey = ClaimItem("signature_usual_mark"),
            claimValue = "base64-signature",
            resourceProvider = resourceProvider,
        )

        // Then
        val main = item.mainContentData
        assertIs<ListItemMainContentDataUi.Image>(main)
        assertEquals("base64-signature", main.base64Image)
        assertNull(item.leadingContentData)
    }

    @Test
    fun `Given a regular claim When it is converted to a list item Then text is rendered`() {
        // When
        val item = document.toListItemDataUi(
            itemId = "id",
            claimKey = ClaimItem("given_name"),
            claimValue = "John",
            resourceProvider = resourceProvider,
        )

        // Then
        val main = item.mainContentData
        assertIs<ListItemMainContentDataUi.Text>(main)
        assertEquals("John", main.text)
        assertNull(item.leadingContentData)
    }

    //endregion

    //region getClaimTranslation

    /**
     * "home" + "screen_title" -> resource key "home_screen_title", which exists.
     */
    @Test
    fun `Given a claim whose string resource exists When its translation is resolved Then the resource text is returned`() {
        // When
        val result = UiTransformer.getClaimTranslation(
            attestationType = "home",
            claimLabel = "screen_title",
            resourceProvider = resourceProvider,
        )

        // Then
        assertEquals("label", result)
    }

    @Test
    fun `Given a claim without a matching string resource When its translation is resolved Then the claim label is returned`() {
        // When
        val result = UiTransformer.getClaimTranslation(
            attestationType = "PID",
            claimLabel = "no_such_claim_key",
            resourceProvider = resourceProvider,
        )

        // Then
        assertEquals("no_such_claim_key", result)
    }

    //endregion

    //region key predicates

    @Test
    fun `Given document keys When the key predicates are applied Then each key is classified correctly`() {
        // Then
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
