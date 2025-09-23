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

package eu.europa.ec.euidi.verifier.core.provider

import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.generic_error_description
import eudiverifier.verifierapp.generated.resources.generic_network_error_message
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

interface ResourceProvider {
    fun getSharedString(resource: StringResource): String
    fun getSharedString(resource: StringResource, vararg formatArgs: Any): String
    fun genericErrorMessage(): String
    fun genericNetworkErrorMessage(): String
}

class ResourceProviderImpl : ResourceProvider {

    override fun getSharedString(resource: StringResource): String =
        runBlocking {
            getString(resource)
        }

    override fun getSharedString(
        resource: StringResource,
        vararg formatArgs: Any
    ): String = runBlocking {
        getString(resource, *formatArgs)
    }

    override fun genericErrorMessage(): String =
        getSharedString(Res.string.generic_error_description)

    override fun genericNetworkErrorMessage(): String =
        getSharedString(Res.string.generic_network_error_message)
}