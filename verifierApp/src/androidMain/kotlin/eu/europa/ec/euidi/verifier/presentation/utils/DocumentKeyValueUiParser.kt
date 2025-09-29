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

package eu.europa.ec.euidi.verifier.presentation.utils

import eu.europa.ec.euidi.verifier.core.extension.decodeBase64ToUtf8OrNull
import eu.europa.ec.euidi.verifier.core.extension.encodeToBase64String
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.utils.safeLet
import eu.europa.ec.euidi.verifier.domain.transformer.UiTransformer.keyIsGender
import eu.europa.ec.euidi.verifier.domain.transformer.UiTransformer.keyIsUserPseudonym
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_boolean_false_readable_value
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_boolean_true_readable_value
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_gender_female
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_gender_male
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_gender_not_applicable
import eudiverifier.verifierapp.generated.resources.show_documents_screen_item_gender_not_known

fun parseKeyValueUi(
    item: Any,
    groupIdentifier: String,
    keyIdentifier: String = "",
    resourceProvider: ResourceProvider,
    allItems: StringBuilder
) {
    when (item) {

        is Map<*, *> -> {
            item.forEach { (key, value) ->
                safeLet(key as? String, value) { key, value ->
                    parseKeyValueUi(
                        item = value,
                        groupIdentifier = groupIdentifier,
                        keyIdentifier = key,
                        resourceProvider = resourceProvider,
                        allItems = allItems
                    )
                }
            }
        }

        is Collection<*> -> {
            item.forEachIndexed { index, value ->
                value?.let {
                    val key = if (item.size == 1) {
                        ""
                    } else {
                        (index + 1).toString()
                    }
                    parseKeyValueUi(
                        item = it,
                        groupIdentifier = groupIdentifier,
                        keyIdentifier = key,
                        resourceProvider = resourceProvider,
                        allItems = allItems
                    )
                }
            }
        }

        is Boolean -> {
            allItems.append(
                resourceProvider.getSharedString(
                    if (item) {
                        Res.string.show_documents_screen_item_boolean_true_readable_value
                    } else {
                        Res.string.show_documents_screen_item_boolean_false_readable_value
                    }
                )
            )
        }

        else -> {

            val base64String = (item as? ByteArray)?.encodeToBase64String()

            allItems.append(
                when {
                    keyIsGender(groupIdentifier) -> {
                        getGenderValue(item.toString(), resourceProvider)
                    }

                    base64String != null -> {
                        if (keyIsUserPseudonym(groupIdentifier)) {
                            base64String.decodeBase64ToUtf8OrNull()
                        } else {
                            base64String
                        }
                    }

                    else -> {
                        val itemString = item.toString()
                        if (keyIdentifier.isEmpty()) {
                            itemString
                        } else {
                            val lineChange = if (allItems.isNotEmpty()) "\n" else ""
                            val value = itemString
                            "$lineChange$keyIdentifier: $value"
                        }
                    }
                }
            )
        }
    }
}

private fun getGenderValue(value: String, resourceProvider: ResourceProvider): String =
    when (value) {
        "0" -> {
            resourceProvider.getSharedString(Res.string.show_documents_screen_item_gender_not_known)
        }

        "1" -> {
            resourceProvider.getSharedString(Res.string.show_documents_screen_item_gender_male)
        }

        "2" -> {
            resourceProvider.getSharedString(Res.string.show_documents_screen_item_gender_female)
        }

        "9" -> {
            resourceProvider.getSharedString(Res.string.show_documents_screen_item_gender_not_applicable)
        }

        else -> {
            value
        }
    }