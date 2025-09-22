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

package eu.europa.ec.euidi.verifier.core.extension

import eu.europa.ec.eudi.verifier.core.Namespace
import eu.europa.ec.eudi.verifier.core.response.DocumentClaims
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.domain.config.model.ClaimItem
import eu.europa.ec.euidi.verifier.presentation.model.ClaimValue
import eu.europa.ec.euidi.verifier.presentation.utils.parseKeyValueUi

/**
 * Flattens all claims across namespaces into a single map.
 * Note that: If multiple namespaces contain the same claim key, later ones overwrite earlier ones.
 */
fun DocumentClaims.flattenedClaims(resourceProvider: ResourceProvider): Map<ClaimItem, ClaimValue> {
    return this.claims.flatMap { (_, claimsMap) ->
        claimsMap.map { (claimKey, claimValue) ->
            val formattedClaimValue = buildString {
                claimValue?.let { safeClaimValue ->
                    parseKeyValueUi(
                        item = safeClaimValue,
                        groupIdentifier = claimKey,
                        resourceProvider = resourceProvider,
                        allItems = this@buildString
                    )
                }
            }
            ClaimItem(label = claimKey) to formattedClaimValue
        }
    }.toMap()
}

/**
 * Groups claims by their namespace to avoid collisions when claim keys repeat across namespaces.
 */
fun DocumentClaims.groupedClaimsByNamespace(
    resourceProvider: ResourceProvider
): Map<Namespace, Map<ClaimItem, ClaimValue>> {
    return this.claims.mapValues { (_, claimsMap) ->
        claimsMap.map { (claimKey, claimValue) ->
            val formattedClaimValue = buildString {
                claimValue?.let { safeClaimValue ->
                    parseKeyValueUi(
                        item = safeClaimValue,
                        groupIdentifier = claimKey,
                        resourceProvider = resourceProvider,
                        allItems = this@buildString
                    )
                }
            }
            ClaimItem(label = claimKey) to formattedClaimValue
        }.toMap()
    }
}