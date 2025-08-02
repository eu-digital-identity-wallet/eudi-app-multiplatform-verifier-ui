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

package eu.europa.ec.euidi.verifier.presentation.component

import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Data class to be used when we want to display an Icon.
 * @param resourceId The [DrawableResource] of the icon.
 * @param contentDescriptionId The [StringResource] of its content description.
 */
@Immutable
data class IconDataUi(
    val resourceId: DrawableResource,
    val contentDescriptionId: StringResource,
)