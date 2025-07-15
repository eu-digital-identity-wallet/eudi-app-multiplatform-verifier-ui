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

package eu.europa.ec.euidi.verifier.core.provider

import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.generic_error_description
import eudiverifier.verifierapp.generated.resources.generic_network_error_message
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

interface ResourceProvider {
    suspend fun getSharedString(resource: StringResource): String
    suspend fun getSharedString(resource: StringResource, vararg formatArgs: Any): String
    suspend fun genericErrorMessage(): String
    suspend fun genericNetworkErrorMessage(): String
}

class ResourceProviderImpl : ResourceProvider {

    override suspend fun getSharedString(resource: StringResource): String {
        return getString(resource)
    }

    override suspend fun getSharedString(
        resource: StringResource,
        vararg formatArgs: Any
    ): String {
        return getString(resource, *formatArgs)
    }

    override suspend fun genericErrorMessage(): String {
        return getSharedString(Res.string.generic_error_description)
    }

    override suspend fun genericNetworkErrorMessage(): String {
        return getSharedString(Res.string.generic_network_error_message)
    }
}