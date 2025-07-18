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

package eu.europa.ec.euidi.verifier.domain.transformer

import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.config.AttestationType
import eu.europa.ec.euidi.verifier.domain.config.AttestationType.Companion.getDisplayName
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.wrap.CheckboxDataUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.allStringResources
import org.jetbrains.compose.resources.StringResource

object UiTransformer {

    suspend fun transformToUiItems(
        fields: List<ClaimItem>,
        attestationType: AttestationType,
        resourceProvider: ResourceProvider
    ) : List<ListItemDataUi> {
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

    suspend fun getClaimTranslation(
        attestationType: String,
        claimLabel: String,
        resourceProvider: ResourceProvider
    ): String {
        val allStringResources: Map<String, StringResource> = Res.allStringResources
        val resourceKey = "${attestationType.replace(" ", "_")}_${claimLabel}".lowercase()

       return resourceProvider.getSharedString(
            resource = allStringResources[resourceKey] ?: allStringResources.getValue("pid_family_name")
        )
    }
}