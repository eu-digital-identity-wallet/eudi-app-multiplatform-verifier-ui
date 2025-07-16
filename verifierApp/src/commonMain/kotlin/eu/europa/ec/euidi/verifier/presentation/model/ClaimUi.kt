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

package eu.europa.ec.euidi.verifier.presentation.model

import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelable
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelize

@CommonParcelize
sealed class ClaimUi(
    open val key: String,
    open val displayTitle: String
) : CommonParcelable {

    @CommonParcelize
    sealed class Common(
        override val key: String,
        override val displayTitle: String
    ) : ClaimUi(key, displayTitle), CommonParcelable {
        data object FamilyName : Common("family_name", "Family Name(s)")
        data object GivenName : Common("given_name", "Given Name(s)")
        data object BirthDate : Common("birth_date", "Date of Birth")
        data object ExpiryDate : Common("expiry_date", "Expiry Date")
        data object IssuingCountry : Common("issuing_country", "Issuing Country")
        data object IssuingAuthority : Common("issuing_authority", "Issuing Authority")
        data object DocumentNumber : Common("document_number", "Document Number")
        data object Portrait : Common("portrait", "Portrait Image")
        data object Sex : Common("sex", "Sex")
        data object Nationality : Common("nationality", "Nationality")
        data object IssuingJurisdiction : Common("issuing_jurisdiction", "Issuing Jurisdiction")
        data object ResidentAddress : Common("resident_address", "Resident Address")
        data object ResidentCountry : Common("resident_country", "Resident Country")
        data object ResidentState : Common("resident_state", "Resident State")
        data object ResidentCity : Common("resident_city", "Resident City")
        data object ResidentPostalCode : Common("resident_postal_code", "Resident Postal Code")
        data object AgeInYears : Common("age_in_years", "Age in Years")
        data object AgeBirthYear : Common("age_birth_year", "Age at Birth")
        data object AgeOver18 : Common("age_over_18", "Age Over 18")

        companion object {
            val allClaims: List<ClaimUi> = listOf(
                FamilyName,
                GivenName,
                BirthDate,
                ExpiryDate,
                IssuingCountry,
                IssuingAuthority,
                DocumentNumber,
                Portrait,
                Sex,
                Nationality,
                IssuingJurisdiction,
                ResidentAddress,
                ResidentCountry,
                ResidentState,
                ResidentCity,
                ResidentPostalCode,
                AgeInYears,
                AgeBirthYear,
                AgeOver18
            )
        }
    }

    @CommonParcelize
    sealed class PidClaim(
        override val key: String,
        override val displayTitle: String
    ) : ClaimUi(key, displayTitle), CommonParcelable {
        data object IssuanceDate : PidClaim("issuance_date", "Issuance Date")
        data object EmailAddress : PidClaim("email_address", "Email Address")
        data object ResidentStreet : PidClaim("resident_street", "Resident Street")
        data object ResidentHouseNumber : PidClaim("resident_house_number", "Resident House Number")
        data object PersonalAdministrativeNumber :
            PidClaim("personal_administrative_number", "Personal Administrative Number")

        data object MobilePhoneNumber : PidClaim("mobile_phone_number", "Mobile Phone Number")
        data object BirthFamilyName : PidClaim("birth_family_name", "Birth Family Name")
        data object BirthGivenName : PidClaim("birth_given_name", "Birth Given Name")
        data object PlaceOfBirth : PidClaim("place_of_birth", "Place of Birth")
        data object TrustAnchor : PidClaim("trust_anchor", "Trust Anchor")

        companion object {
            val allClaims: List<ClaimUi> = listOf(
                IssuanceDate,
                EmailAddress,
                ResidentStreet,
                ResidentHouseNumber,
                PersonalAdministrativeNumber,
                MobilePhoneNumber,
                BirthFamilyName,
                BirthGivenName,
                PlaceOfBirth,
                TrustAnchor
            )
        }
    }

    @CommonParcelize
    sealed class MdlClaim(
        override val key: String,
        override val displayTitle: String
    ) : ClaimUi(key, displayTitle), CommonParcelable {
        data object DrivingPrivileges : MdlClaim("driving_privileges", "Driving Privileges")
        data object UnDistinguishingSign :
            MdlClaim("un_distinguishing_sign", "UN distinguishing sign")

        data object AdministrativeNumber :
            MdlClaim("administrative_number", "Administrative number")

        data object Height : MdlClaim("height", "Height (cm)")
        data object Weight : MdlClaim("weight", "Weight (kg)")
        data object EyeColour : MdlClaim("eye_colour", "Eye colour")
        data object HairColour : MdlClaim("hair_colour", "Hair colour")
        data object BirthPlaceRaw : MdlClaim("birth_place", "Place of birth")
        data object PermanentResidence :
            MdlClaim("resident_address", "Permanent place of residence")

        data object PortraitCaptureDate :
            MdlClaim("portrait_capture_date", "Portrait image timestamp")

        data object BiometricTemplate :
            MdlClaim("biometric_template_xx", "Fingerprint biometric information")

        data object FamilyNameNationalCharacter :
            MdlClaim("family_name_national_character", "Family name in national characters")

        data object GivenNameNationalCharacter :
            MdlClaim("given_name_national_character", "Given name in national characters")

        data object SignatureUsualMark : MdlClaim("signature_usual_mark", "Signature / usual mark")

        companion object {
            val allClaims: List<ClaimUi> = listOf(
                DrivingPrivileges,
                UnDistinguishingSign,
                AdministrativeNumber,
                Height,
                Weight,
                EyeColour,
                HairColour,
                BirthPlaceRaw,
                PermanentResidence,
                PortraitCaptureDate,
                BiometricTemplate,
                FamilyNameNationalCharacter,
                GivenNameNationalCharacter,
                SignatureUsualMark
            )
        }
    }

    companion object {
        fun String.toClaim(): ClaimUi? = when (this) {
            "family_name" -> Common.FamilyName
            "given_name" -> Common.GivenName
            "birth_date" -> Common.BirthDate
            "expiry_date" -> Common.ExpiryDate
            "issuing_country" -> Common.IssuingCountry
            "issuing_authority" -> Common.IssuingAuthority
            "document_number" -> Common.DocumentNumber
            "portrait" -> Common.Portrait
            "sex" -> Common.Sex
            "nationality" -> Common.Nationality
            "issuing_jurisdiction" -> Common.IssuingJurisdiction
            "resident_address" -> Common.ResidentAddress
            "resident_country" -> Common.ResidentCountry
            "resident_state" -> Common.ResidentState
            "resident_city" -> Common.ResidentCity
            "resident_postal_code" -> Common.ResidentPostalCode
            "age_in_years" -> Common.AgeInYears
            "age_birth_year" -> Common.AgeBirthYear
            "age_over_18" -> Common.AgeOver18
            "issuance_date" -> PidClaim.IssuanceDate
            "email_address" -> PidClaim.EmailAddress
            "resident_street" -> PidClaim.ResidentStreet
            "resident_house_number" -> PidClaim.ResidentHouseNumber
            "personal_administrative_number" -> PidClaim.PersonalAdministrativeNumber
            "mobile_phone_number" -> PidClaim.MobilePhoneNumber
            "birth_family_name" -> PidClaim.BirthFamilyName
            "birth_given_name" -> PidClaim.BirthGivenName
            "place_of_birth" -> PidClaim.PlaceOfBirth
            "trust_anchor" -> PidClaim.TrustAnchor
            "driving_privileges" -> MdlClaim.DrivingPrivileges
            "un_distinguishing_sign" -> MdlClaim.UnDistinguishingSign
            "administrative_number" -> MdlClaim.AdministrativeNumber
            "height" -> MdlClaim.Height
            "weight" -> MdlClaim.Weight
            "eye_colour" -> MdlClaim.EyeColour
            "hair_colour" -> MdlClaim.HairColour
            "birth_place" -> MdlClaim.BirthPlaceRaw
            "portrait_capture_date" -> MdlClaim.PortraitCaptureDate
            "biometric_template_xx" -> MdlClaim.BiometricTemplate
            "family_name_national_character" -> MdlClaim.FamilyNameNationalCharacter
            "given_name_national_character" -> MdlClaim.GivenNameNationalCharacter
            "signature_usual_mark" -> MdlClaim.SignatureUsualMark
            else -> null
        }

        val common: List<ClaimUi> = Common.allClaims

        val pidClaims: List<ClaimUi> = common + PidClaim.allClaims

        val mdlClaims: List<ClaimUi> = common + MdlClaim.allClaims
    }
}