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

package eu.europa.ec.euidi.verifier.domain.interactor

import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemLeadingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuItemUi
import eu.europa.ec.euidi.verifier.presentation.ui.menu.model.MenuTypeUi
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.menu_screen_item_home_name
import eudiverifier.verifierapp.generated.resources.menu_screen_item_reverse_engagement_name
import eudiverifier.verifierapp.generated.resources.menu_screen_item_settings_name
import eudiverifier.verifierapp.generated.resources.menu_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface MenuInteractor {
    suspend fun getScreenTitle(): String
    suspend fun getMenuItemsUi(): List<MenuItemUi>
}

class MenuInteractorImpl(
    private val uuidProvider: UuidProvider,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MenuInteractor {

    override suspend fun getScreenTitle(): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(Res.string.menu_screen_title)
        }
    }

    override suspend fun getMenuItemsUi(): List<MenuItemUi> {
        return withContext(dispatcher) {
            val chevronRightIcon = ListItemTrailingContentDataUi.Icon(
                iconData = AppIcons.ChevronRight
            )

            buildList {
                add(
                    MenuItemUi(
                        type = MenuTypeUi.HOME,
                        data = ListItemDataUi(
                            itemId = uuidProvider.provideUuid(),
                            mainContentData = ListItemMainContentDataUi.Text(
                                text = resourceProvider.getSharedString(
                                    Res.string.menu_screen_item_home_name
                                )
                            ),
                            leadingContentData = ListItemLeadingContentDataUi.Icon(
                                iconData = AppIcons.Home
                            ),
                            trailingContentData = chevronRightIcon
                        )
                    )
                )

                add(
                    MenuItemUi(
                        type = MenuTypeUi.REVERSE_ENGAGEMENT,
                        data = ListItemDataUi(
                            itemId = uuidProvider.provideUuid(),
                            mainContentData = ListItemMainContentDataUi.Text(
                                text = resourceProvider.getSharedString(
                                    Res.string.menu_screen_item_reverse_engagement_name
                                )
                            ),
                            leadingContentData = ListItemLeadingContentDataUi.Icon(
                                iconData = AppIcons.ReverseEngagement
                            ),
                            trailingContentData = chevronRightIcon
                        )
                    )
                )

                add(
                    MenuItemUi(
                        type = MenuTypeUi.SETTINGS,
                        data = ListItemDataUi(
                            itemId = uuidProvider.provideUuid(),
                            mainContentData = ListItemMainContentDataUi.Text(
                                text = resourceProvider.getSharedString(
                                    Res.string.menu_screen_item_settings_name
                                )
                            ),
                            leadingContentData = ListItemLeadingContentDataUi.Icon(
                                iconData = AppIcons.Settings
                            ),
                            trailingContentData = chevronRightIcon
                        )
                    )
                )
            }
        }
    }
}