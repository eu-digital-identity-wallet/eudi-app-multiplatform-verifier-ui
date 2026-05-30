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

package eu.europa.ec.euidi.verifier.domain.config.model

import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getAttestationTypeFromDocType
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getDisplayName
import eu.europa.ec.euidi.verifier.testutil.documentTypeResourceProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AttestationTypeTest {

    private val resourceProvider = documentTypeResourceProvider()

    @Test
    fun `namespace and docType are exposed for each attestation type`() {
        assertEquals("eu.europa.ec.eudi.pid.1", AttestationType.Pid.namespace)
        assertEquals("eu.europa.ec.eudi.pid.1", AttestationType.Pid.docType)

        assertEquals("org.iso.18013.5.1", AttestationType.Mdl.namespace)
        assertEquals("org.iso.18013.5.1.mDL", AttestationType.Mdl.docType)

        assertEquals("eu.europa.ec.eudi.employee.1", AttestationType.EmployeeId.namespace)
        assertEquals("eu.europa.ec.eudi.employee.1", AttestationType.EmployeeId.docType)
    }

    @Test
    fun `getDisplayName maps each type to its localized label`() {
        assertEquals("PID", AttestationType.Pid.getDisplayName(resourceProvider))
        assertEquals("MDL", AttestationType.Mdl.getDisplayName(resourceProvider))
        assertEquals("Employee ID", AttestationType.EmployeeId.getDisplayName(resourceProvider))
    }

    @Test
    fun `getAttestationTypeFromDocType resolves each known docType`() {
        assertEquals(
            AttestationType.Pid,
            getAttestationTypeFromDocType(AttestationType.Pid.docType)
        )
        assertEquals(
            AttestationType.Mdl,
            getAttestationTypeFromDocType(AttestationType.Mdl.docType)
        )
        assertEquals(
            AttestationType.EmployeeId,
            getAttestationTypeFromDocType(AttestationType.EmployeeId.docType)
        )
    }

    @Test
    fun `getAttestationTypeFromDocType throws for an unknown docType`() {
        assertFailsWith<IllegalArgumentException> {
            getAttestationTypeFromDocType("unknown.doctype")
        }
    }
}
