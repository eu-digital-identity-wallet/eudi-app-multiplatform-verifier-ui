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

package eu.europa.ec.euidi.verifier.domain.config.model

import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelable
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelize
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.document_type_age_verification
import eudiverifier.verifierapp.generated.resources.document_type_mdl
import eudiverifier.verifierapp.generated.resources.document_type_pid

@CommonParcelize
sealed interface AttestationType : CommonParcelable {
    val namespace: String
    val docType: String

    data object Pid : AttestationType {

        override val namespace: String
            get() = "eu.europa.ec.eudi.pid.1"

        override val docType: String
            get() = "eu.europa.ec.eudi.pid.1"
    }

    data object Mdl : AttestationType {

        override val namespace: String
            get() = "org.iso.18013.5.1.mDL"

        override val docType: String
            get() = "org.iso.18013.5.1"
    }

    data object AgeVerification : AttestationType {

        override val namespace: String
            get() = "eu.europa.ec.eudi.pseudonym.age_over_18.1"

        override val docType: String
            get() = "eu.europa.ec.eudi.pseudonym.age_over_18.1"
    }

    companion object {
        suspend fun AttestationType.getDisplayName(
            resourceProvider: ResourceProvider
        ): String {
            return when (this) {
                Pid -> resourceProvider.getSharedString(Res.string.document_type_pid)
                Mdl -> resourceProvider.getSharedString(Res.string.document_type_mdl)
                AgeVerification -> resourceProvider.getSharedString(Res.string.document_type_age_verification)
            }
        }
    }
}