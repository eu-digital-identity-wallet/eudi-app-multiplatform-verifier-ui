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

package eu.europa.ec.euidi.verifier.presentation.component

import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.content_description_arrow_back_icon
import eudiverifier.verifierapp.generated.resources.content_description_check_icon
import eudiverifier.verifierapp.generated.resources.content_description_chevron_right_icon
import eudiverifier.verifierapp.generated.resources.content_description_close_icon
import eudiverifier.verifierapp.generated.resources.content_description_expand_less_icon
import eudiverifier.verifierapp.generated.resources.content_description_expand_more_icon
import eudiverifier.verifierapp.generated.resources.content_description_home_icon
import eudiverifier.verifierapp.generated.resources.content_description_logo_full_icon
import eudiverifier.verifierapp.generated.resources.content_description_logo_plain_icon
import eudiverifier.verifierapp.generated.resources.content_description_logo_text_icon
import eudiverifier.verifierapp.generated.resources.content_description_menu_icon
import eudiverifier.verifierapp.generated.resources.content_description_more_vert_icon
import eudiverifier.verifierapp.generated.resources.content_description_nfc_icon
import eudiverifier.verifierapp.generated.resources.content_description_reverse_engagement_icon
import eudiverifier.verifierapp.generated.resources.content_description_search_icon
import eudiverifier.verifierapp.generated.resources.content_description_settings_icon
import eudiverifier.verifierapp.generated.resources.content_description_user_icon
import eudiverifier.verifierapp.generated.resources.ic_arrow_back
import eudiverifier.verifierapp.generated.resources.ic_check
import eudiverifier.verifierapp.generated.resources.ic_chevron_right
import eudiverifier.verifierapp.generated.resources.ic_close
import eudiverifier.verifierapp.generated.resources.ic_expand_less
import eudiverifier.verifierapp.generated.resources.ic_expand_more
import eudiverifier.verifierapp.generated.resources.ic_home
import eudiverifier.verifierapp.generated.resources.ic_logo_full
import eudiverifier.verifierapp.generated.resources.ic_logo_plain
import eudiverifier.verifierapp.generated.resources.ic_logo_text
import eudiverifier.verifierapp.generated.resources.ic_menu
import eudiverifier.verifierapp.generated.resources.ic_nfc
import eudiverifier.verifierapp.generated.resources.ic_reverse_engagement
import eudiverifier.verifierapp.generated.resources.ic_search
import eudiverifier.verifierapp.generated.resources.ic_settings
import eudiverifier.verifierapp.generated.resources.ic_user
import eudiverifier.verifierapp.generated.resources.ic_vertical_more

/**
 * A Singleton object responsible for providing access to all the app's Icons.
 */
object AppIcons {

    val ArrowBack: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_arrow_back,
        contentDescriptionId = Res.string.content_description_arrow_back_icon,
    )

    val Close: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_close,
        contentDescriptionId = Res.string.content_description_close_icon,
    )

    val VerticalMore: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_vertical_more,
        contentDescriptionId = Res.string.content_description_more_vert_icon,
    )

    val Menu: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_menu,
        contentDescriptionId = Res.string.content_description_menu_icon,
    )

    val ChevronRight: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_chevron_right,
        contentDescriptionId = Res.string.content_description_chevron_right_icon,
    )

    val ExpandMoreIcon: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_expand_more,
        contentDescriptionId = Res.string.content_description_expand_more_icon,
    )

    val ExpandLessIcon: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_expand_less,
        contentDescriptionId = Res.string.content_description_expand_less_icon,
    )

    val LogoFull: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_logo_full,
        contentDescriptionId = Res.string.content_description_logo_full_icon,
    )

    val LogoPlain: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_logo_plain,
        contentDescriptionId = Res.string.content_description_logo_plain_icon,
    )

    val LogoText: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_logo_text,
        contentDescriptionId = Res.string.content_description_logo_text_icon,
    )

    val Nfc: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_nfc,
        contentDescriptionId = Res.string.content_description_nfc_icon,
    )

    val Search: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_search,
        contentDescriptionId = Res.string.content_description_search_icon,
    )

    val Check: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_check,
        contentDescriptionId = Res.string.content_description_check_icon,
    )

    val Home: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_home,
        contentDescriptionId = Res.string.content_description_home_icon,
    )

    val ReverseEngagement: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_reverse_engagement,
        contentDescriptionId = Res.string.content_description_reverse_engagement_icon,
    )

    val Settings: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_settings,
        contentDescriptionId = Res.string.content_description_settings_icon,
    )

    val User: IconDataUi = IconDataUi(
        resourceId = Res.drawable.ic_user,
        contentDescriptionId = Res.string.content_description_user_icon,
    )
}