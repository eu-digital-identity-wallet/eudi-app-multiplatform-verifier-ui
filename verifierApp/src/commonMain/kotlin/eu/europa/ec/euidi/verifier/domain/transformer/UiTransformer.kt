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

package eu.europa.ec.euidi.verifier.domain.transformer

import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.utils.Constants
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getDisplayName
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemLeadingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eu.europa.ec.euidi.verifier.presentation.model.ClaimValue
import eu.europa.ec.euidi.verifier.presentation.model.ReceivedDocumentUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.allStringResources
import org.jetbrains.compose.resources.StringResource

object UiTransformer {

    fun transformToUiItems(
        fields: List<ClaimItem>,
        attestationType: AttestationType,
        resourceProvider: ResourceProvider
    ): List<ListItemDataUi> {
        return when (fields.isEmpty()) {
            true -> emptyList()
            false -> {
                fields.map { claimItem ->
                    val translation = getClaimTranslation(
                        attestationType = attestationType.getDisplayName(resourceProvider),
                        claimLabel = claimItem.label,
                        resourceProvider = resourceProvider
                    )

                    ListItemDataUi(
                        itemId = claimItem.label,
                        mainContentData = ListItemMainContentDataUi.Text(
                            text = translation
                        ),
                        trailingContentData = ListItemTrailingContentDataUi.Checkbox(
                            checkboxData = CheckboxDataUi(
                                isChecked = true
                            )
                        )
                    )
                }
            }
        }
    }

    fun ReceivedDocumentUi.toListItemDataUi(
        itemId: String,
        claimKey: ClaimItem,
        claimValue: ClaimValue,
        resourceProvider: ResourceProvider,
    ): ListItemDataUi {
        return ListItemDataUi(
            itemId = itemId,
            overlineText = getClaimTranslation(
                attestationType = this.documentType.getDisplayName(resourceProvider),
                claimLabel = claimKey.label,
                resourceProvider = resourceProvider
            ),
            mainContentData = calculateMainContent(
                key = claimKey,
                value = claimValue
            ),
            leadingContentData = calculateLeadingContent(
                key = claimKey,
                value = claimValue
            )
        )
    }

    private fun calculateMainContent(
        key: ClaimItem,
        value: String,
    ): ListItemMainContentDataUi {
        return when {
            keyIsPortrait(key = key.label) -> {
                ListItemMainContentDataUi.Text(text = "")
            }

            keyIsSignature(key = key.label) -> {
                ListItemMainContentDataUi.Image(base64Image = value)
            }

            else -> {
                ListItemMainContentDataUi.Text(text = value)
            }
        }
    }

    private fun calculateLeadingContent(
        key: ClaimItem,
        value: String,
    ): ListItemLeadingContentDataUi? {
        return if (keyIsPortrait(key = key.label)) {
            ListItemLeadingContentDataUi.UserImage(userBase64Image = value)
        } else {
            null
        }
    }

    fun getClaimTranslation(
        attestationType: String,
        claimLabel: String,
        resourceProvider: ResourceProvider
    ): String {
        val allStringResources: Map<String, StringResource> = Res.allStringResources
        val resourceKey = "${attestationType.replace(" ", "_")}_${claimLabel}".lowercase()

        return allStringResources[resourceKey]?.let {
            resourceProvider.getSharedString(it)
        } ?: claimLabel
    }

    fun keyIsPortrait(key: String): Boolean {
        return key == Constants.DocumentKeys.PORTRAIT
    }

    fun keyIsSignature(key: String): Boolean {
        return key == Constants.DocumentKeys.SIGNATURE
    }

    fun keyIsUserPseudonym(key: String): Boolean {
        return key == Constants.DocumentKeys.USER_PSEUDONYM
    }

    fun keyIsGender(key: String): Boolean {
        val listOfGenderKeys = Constants.DocumentKeys.GENDER_KEYS
        return listOfGenderKeys.contains(key)
    }
}