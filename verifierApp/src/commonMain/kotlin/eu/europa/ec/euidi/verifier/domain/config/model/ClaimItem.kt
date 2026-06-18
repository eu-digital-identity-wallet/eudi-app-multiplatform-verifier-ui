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

package eu.europa.ec.euidi.verifier.domain.config.model

import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelable
import eu.europa.ec.euidi.verifier.presentation.utils.CommonParcelize

/**
 * A single requestable attribute of a document.
 *
 * [label] is the underlying mdoc element identifier (e.g. `birth_date`, `nationality`) and never
 * changes regardless of how the attribute is requested — it tells the prover which attribute the
 * request is about. [kind] decides *how* it is requested: a plain [ClaimKind.Disclosure] asks the
 * wallet to reveal the value, while a [ClaimKind.Zk] asks for a zero-knowledge predicate over it.
 */
@CommonParcelize
data class ClaimItem(
    val label: String,
    val kind: ClaimKind = ClaimKind.Disclosure,
) : CommonParcelable {

    /**
     * Stable, unique identifier used to key this claim in the selection list and to pair a UI
     * selection back to the claim it came from. A [ClaimKind.Disclosure] claim's id is simply its
     * [label] (so existing behaviour and ids are unchanged), while a [ClaimKind.Zk] predicate over
     * the same attribute gets a distinct id — that is what lets a disclosure and a ZK request over,
     * say, `birth_date` coexist in the same list without colliding.
     */
    val id: String
        get() = when (kind) {
            ClaimKind.Disclosure -> label
            is ClaimKind.Zk -> "$label$ZK_ID_SUFFIX"
        }

    companion object {
        const val ZK_ID_SUFFIX = "::zk"
    }
}

/**
 * How a [ClaimItem] is requested.
 *
 * [Disclosure] is the ordinary case — the wallet reveals the attribute's value. [Zk] asks instead
 * for a zero-knowledge proof of a predicate over the attribute, so the wallet can answer without
 * revealing the underlying value. The associated [Zk.value] carries the predicate parameter the
 * verifier operator supplies (the age threshold, or the accepted set of nationalities); it stays
 * null until the operator fills it in.
 */
@CommonParcelize
sealed interface ClaimKind : CommonParcelable {

    data object Disclosure : ClaimKind

    data class Zk(val value: ZkPredicateValue? = null) : ClaimKind
}

/**
 * The operator-supplied parameter for a [ClaimKind.Zk] request — the "input" of the predicate.
 */
@CommonParcelize
sealed interface ZkPredicateValue : CommonParcelable {

    /** Prove the holder's age is at least [years], without revealing the date of birth. */
    data class AgeOver(val years: Int) : ZkPredicateValue

    /**
     * Prove the holder's nationality is one of [countries] (ISO 3166-1 alpha-2 codes), without
     * revealing which one.
     */
    data class NationalityIn(val countries: List<String>) : ZkPredicateValue
}
