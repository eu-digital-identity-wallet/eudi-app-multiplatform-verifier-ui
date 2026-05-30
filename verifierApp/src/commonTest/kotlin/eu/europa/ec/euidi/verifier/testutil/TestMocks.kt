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

package eu.europa.ec.euidi.verifier.testutil

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.document_type_employee_id
import eudiverifier.verifierapp.generated.resources.document_type_mdl
import eudiverifier.verifierapp.generated.resources.document_type_pid

/**
 * Shared Mokkery mock *factories* for the common tests.
 *
 * These are functions (not shared instances) on purpose: every call returns a fresh mock so that
 * recorded interactions never leak between tests.
 */

/**
 * A [UuidProvider] mock that returns deterministic, incrementing ids: "uuid-0", "uuid-1", ...
 * Each invocation of this factory starts a fresh counter.
 */
fun sequentialUuidProvider(): UuidProvider {
    var counter = 0
    return mock {
        every { provideUuid() } calls { "uuid-${counter++}" }
    }
}

/**
 * A [ResourceProvider] mock that maps the document-type display-name resources to short labels
 * ("PID", "MDL", "Employee ID"). Tests that need additional strings can stub them on the returned
 * mock, e.g. `documentTypeResourceProvider().apply { every { getSharedString(...) } returns ... }`.
 */
fun documentTypeResourceProvider(): ResourceProvider {
    val resourceProvider = mock<ResourceProvider>()
    every { resourceProvider.getSharedString(Res.string.document_type_pid) } returns "PID"
    every { resourceProvider.getSharedString(Res.string.document_type_mdl) } returns "MDL"
    every {
        resourceProvider.getSharedString(Res.string.document_type_employee_id)
    } returns "Employee ID"
    return resourceProvider
}
