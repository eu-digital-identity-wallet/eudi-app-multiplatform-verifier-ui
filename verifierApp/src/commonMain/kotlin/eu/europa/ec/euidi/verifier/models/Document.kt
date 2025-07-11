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

package eu.europa.ec.euidi.verifier.models

interface Document {
    val name: String
//    val type: DocumentType
    val data: DocumentData
}

data class DocumentData(
//    val format: DocumentFormat,
    val claims: Map<String, Boolean>
)

data class MsoMdoc(
    override val name: String,
    override val data: DocumentData,
//    override val type: AttestationType
) : Document

data class SdJwt(
    override val name: String,
    override val data: DocumentData,
//    override val type: AttestationType
) : Document

interface DocumentClaims {
    val familyName: String
    val givenName: String
    val birthDate: String
    val issuanceDate: String?
    val expiryDate: String
    val issuingAuthority: String
    val issuingCountry: String
    val documentNumber: String?
    val portrait: ByteArray?
    val ageInYears: Int?
    val ageBirthYear: Int?
    val ageOver18: Boolean?
    val sex: Int?
    val nationality: String?
    val issuingJurisdiction: String?
    val residentAddress: String?
    val residentCity: String?
    val residentState: String?
    val residentPostalCode: String?
}

data class PidClaims(
    override val familyName: String,
    override val givenName: String,
    override val birthDate: String,
    override val issuanceDate: String?,
    override val expiryDate: String,
    override val issuingAuthority: String,
    override val issuingCountry: String,
    override val documentNumber: String?,
    override val portrait: ByteArray?,
    override val ageInYears: Int?,
    override val ageBirthYear: Int?,
    override val ageOver18: Boolean?,
    override val sex: Int?,
    override val nationality: String?,
    override val issuingJurisdiction: String?,
    override val residentAddress: String?,
    override val residentCity: String?,
    override val residentState: String?,
    override val residentPostalCode: String?,
    val placeOfBirth: Any,
    val familyNameBirth: String?,
    val givenNameBirth: String?,
    val residentStreet: String?,
    val residentHouseNumber: String?,
    val personalAdministrativeNumber: String?,
    val emailAddress: String?,
    val mobilePhoneNumber: String?,
    val trustAnchor: String?
) : DocumentClaims {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PidClaims

        if (ageInYears != other.ageInYears) return false
        if (ageBirthYear != other.ageBirthYear) return false
        if (ageOver18 != other.ageOver18) return false
        if (sex != other.sex) return false
        if (familyName != other.familyName) return false
        if (givenName != other.givenName) return false
        if (birthDate != other.birthDate) return false
        if (issuanceDate != other.issuanceDate) return false
        if (expiryDate != other.expiryDate) return false
        if (issuingAuthority != other.issuingAuthority) return false
        if (issuingCountry != other.issuingCountry) return false
        if (documentNumber != other.documentNumber) return false
        if (!portrait.contentEquals(other.portrait)) return false
        if (nationality != other.nationality) return false
        if (issuingJurisdiction != other.issuingJurisdiction) return false
        if (residentAddress != other.residentAddress) return false
        if (residentCity != other.residentCity) return false
        if (residentState != other.residentState) return false
        if (residentPostalCode != other.residentPostalCode) return false
        if (placeOfBirth != other.placeOfBirth) return false
        if (familyNameBirth != other.familyNameBirth) return false
        if (givenNameBirth != other.givenNameBirth) return false
        if (residentStreet != other.residentStreet) return false
        if (residentHouseNumber != other.residentHouseNumber) return false
        if (personalAdministrativeNumber != other.personalAdministrativeNumber) return false
        if (emailAddress != other.emailAddress) return false
        if (mobilePhoneNumber != other.mobilePhoneNumber) return false
        if (trustAnchor != other.trustAnchor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ageInYears ?: 0
        result = 31 * result + (ageBirthYear ?: 0)
        result = 31 * result + (ageOver18?.hashCode() ?: 0)
        result = 31 * result + (sex ?: 0)
        result = 31 * result + familyName.hashCode()
        result = 31 * result + givenName.hashCode()
        result = 31 * result + birthDate.hashCode()
        result = 31 * result + (issuanceDate?.hashCode() ?: 0)
        result = 31 * result + expiryDate.hashCode()
        result = 31 * result + issuingAuthority.hashCode()
        result = 31 * result + issuingCountry.hashCode()
        result = 31 * result + (documentNumber?.hashCode() ?: 0)
        result = 31 * result + (portrait?.contentHashCode() ?: 0)
        result = 31 * result + (nationality?.hashCode() ?: 0)
        result = 31 * result + (issuingJurisdiction?.hashCode() ?: 0)
        result = 31 * result + (residentAddress?.hashCode() ?: 0)
        result = 31 * result + (residentCity?.hashCode() ?: 0)
        result = 31 * result + (residentState?.hashCode() ?: 0)
        result = 31 * result + (residentPostalCode?.hashCode() ?: 0)
        result = 31 * result + placeOfBirth.hashCode()
        result = 31 * result + (familyNameBirth?.hashCode() ?: 0)
        result = 31 * result + (givenNameBirth?.hashCode() ?: 0)
        result = 31 * result + (residentStreet?.hashCode() ?: 0)
        result = 31 * result + (residentHouseNumber?.hashCode() ?: 0)
        result = 31 * result + (personalAdministrativeNumber?.hashCode() ?: 0)
        result = 31 * result + (emailAddress?.hashCode() ?: 0)
        result = 31 * result + (mobilePhoneNumber?.hashCode() ?: 0)
        result = 31 * result + (trustAnchor?.hashCode() ?: 0)
        return result
    }
}