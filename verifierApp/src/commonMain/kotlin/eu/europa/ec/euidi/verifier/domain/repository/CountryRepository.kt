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

package eu.europa.ec.euidi.verifier.domain.repository

/**
 * Source of the selectable countries. Returns the ISO 3166-1 alpha-2 codes only; the human-readable
 * names are resolved separately from string resources (`country_<code>`), so this list stays a pure,
 * localization-independent catalogue. Ordered alphabetically by English code.
 */
/**
 * A named, pre-defined group of countries (e.g. Schengen, EU) offered as a one-tap convenience for
 * selecting a common set. [id] is stable and used by the UI to identify the active set; the
 * human-readable label is resolved separately from string resources (`country_set_<id>`).
 */
data class CountrySet(
    val id: String,
    val countryCodes: List<String>,
)

interface CountryRepository {
    fun getAllCountryCodes(): List<String>

    /** The pre-defined country sets, in display order. */
    fun getCountrySets(): List<CountrySet>
}

class CountryRepositoryImpl : CountryRepository {

    override fun getAllCountryCodes(): List<String> = ALL_ISO_3166_1_ALPHA2

    override fun getCountrySets(): List<CountrySet> = COUNTRY_SETS

    private companion object {

        const val SET_SCHENGEN = "schengen"
        const val SET_EU = "eu"

        // Schengen Area (29 members). Note: Cyprus is in the EU but is NOT a Schengen member.
        val SCHENGEN: List<String> = listOf(
            "AT", "BE", "BG", "HR", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IS", "IT", "LV",
            "LI", "LT", "LU", "MT", "NL", "NO", "PL", "PT", "RO", "SK", "SI", "ES", "SE", "CH",
        )

        // European Union (27 members).
        val EU: List<String> = listOf(
            "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT",
            "LV", "LT", "LU", "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE",
        )

        val COUNTRY_SETS: List<CountrySet> = listOf(
            CountrySet(id = SET_SCHENGEN, countryCodes = SCHENGEN),
            CountrySet(id = SET_EU, countryCodes = EU),
        )

        // ISO 3166-1 alpha-2 codes, ordered by English country code.
        val ALL_ISO_3166_1_ALPHA2: List<String> = listOf(
            "AF", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ",
            "AG", "AR", "AM", "AW", "AU", "AT", "AZ", "BS",
            "BH", "BD", "BB", "BY", "BE", "BZ", "BJ", "BM",
            "BT", "BO", "BQ", "BA", "BW", "BV", "BR", "IO",
            "BN", "BG", "BF", "BI", "CV", "KH", "CM", "CA",
            "KY", "CF", "TD", "CL", "CN", "CX", "CC", "CO",
            "KM", "CG", "CD", "CK", "CR", "HR", "CU", "CW",
            "CY", "CZ", "CI", "DK", "DJ", "DM", "DO", "EC",
            "EG", "SV", "GQ", "ER", "EE", "SZ", "ET", "FK",
            "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA",
            "GM", "GE", "DE", "GH", "GI", "GR", "GL", "GD",
            "GP", "GU", "GT", "GG", "GN", "GW", "GY", "HT",
            "HM", "VA", "HN", "HK", "HU", "IS", "IN", "ID",
            "IR", "IQ", "IE", "IM", "IL", "IT", "JM", "JP",
            "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW",
            "KG", "LA", "LV", "LB", "LS", "LR", "LY", "LI",
            "LT", "LU", "MO", "MG", "MW", "MY", "MV", "ML",
            "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM",
            "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM",
            "NA", "NR", "NP", "NL", "NC", "NZ", "NI", "NE",
            "NG", "NU", "NF", "MK", "MP", "NO", "OM", "PK",
            "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN",
            "PL", "PT", "PR", "QA", "RO", "RU", "RW", "RE",
            "BL", "SH", "KN", "LC", "MF", "PM", "VC", "WS",
            "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG",
            "SX", "SK", "SI", "SB", "SO", "ZA", "GS", "SS",
            "ES", "LK", "SD", "SR", "SJ", "SE", "CH", "SY",
            "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO",
            "TT", "TN", "TM", "TC", "TV", "TR", "UG", "UA",
            "AE", "GB", "UM", "US", "UY", "UZ", "VU", "VE",
            "VN", "VG", "VI", "WF", "EH", "YE", "ZM", "ZW",
            "AX",
        )
    }
}
