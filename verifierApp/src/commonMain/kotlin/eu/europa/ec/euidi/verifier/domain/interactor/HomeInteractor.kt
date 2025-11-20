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

package eu.europa.ec.euidi.verifier.domain.interactor

import eu.europa.ec.euidi.verifier.core.controller.PlatformController
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.model.AttestationType.Companion.getDisplayName
import eu.europa.ec.euidi.verifier.presentation.component.AppIcons
import eu.europa.ec.euidi.verifier.presentation.component.ListItemDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemMainContentDataUi
import eu.europa.ec.euidi.verifier.presentation.component.ListItemTrailingContentDataUi
import eu.europa.ec.euidi.verifier.presentation.model.RequestedDocumentUi
import eudiverifier.verifierapp.generated.resources.Res
import eudiverifier.verifierapp.generated.resources.home_screen_main_button_text_default
import eudiverifier.verifierapp.generated.resources.home_screen_main_button_text_separator
import eudiverifier.verifierapp.generated.resources.home_screen_title
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface HomeInteractor {
    suspend fun getScreenTitle(): String
    suspend fun getDefaultMainButtonData(): ListItemDataUi
    suspend fun formatMainButtonData(
        requestedDocs: List<RequestedDocumentUi>,
        existingMainButtonData: ListItemDataUi
    ): ListItemDataUi

    fun closeApp()
}

class HomeInteractorImpl(
    private val platformController: PlatformController,
    private val dataStoreController: DataStoreController,
    private val uuidProvider: UuidProvider,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : HomeInteractor {

    override suspend fun getScreenTitle(): String {
        return withContext(dispatcher) {
            resourceProvider.getSharedString(Res.string.home_screen_title)
        }
    }

    override suspend fun getDefaultMainButtonData(): ListItemDataUi {
        return withContext(dispatcher) {
            ListItemDataUi(
                itemId = uuidProvider.provideUuid(),
                mainContentData = ListItemMainContentDataUi.Text(
                    text = resourceProvider.getSharedString(Res.string.home_screen_main_button_text_default)
                ),
                trailingContentData = ListItemTrailingContentDataUi.Icon(
                    iconData = AppIcons.ChevronRight
                )
            )
        }
    }

    override suspend fun formatMainButtonData(
        requestedDocs: List<RequestedDocumentUi>,
        existingMainButtonData: ListItemDataUi
    ): ListItemDataUi {
        return withContext(dispatcher) {
            val separator =
                resourceProvider.getSharedString(Res.string.home_screen_main_button_text_separator)

            val displayText = requestedDocs.joinToString(separator = separator) { doc ->
                val displayName = doc.documentType.getDisplayName(resourceProvider)
                "${doc.mode.displayName} $displayName"
            }

            existingMainButtonData.copy(
                mainContentData = ListItemMainContentDataUi.Text(text = displayText)
            )
        }
    }

    override fun closeApp() {
        platformController.closeApp()
    }
}