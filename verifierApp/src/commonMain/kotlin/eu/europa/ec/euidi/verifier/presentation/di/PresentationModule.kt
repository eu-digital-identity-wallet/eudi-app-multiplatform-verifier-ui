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

package eu.europa.ec.euidi.verifier.presentation.di

import eu.europa.ec.euidi.verifier.presentation.ui.custom_request.CustomRequestViewModel
import eu.europa.ec.euidi.verifier.presentation.ui.doc_to_request.DocumentsToRequestViewModel
import eu.europa.ec.euidi.verifier.presentation.ui.home.HomeViewModel
import eu.europa.ec.euidi.verifier.presentation.ui.menu.MenuViewModel
import eu.europa.ec.euidi.verifier.presentation.ui.qr_scan.QrScanViewModel
import eu.europa.ec.euidi.verifier.presentation.ui.settings.SettingsViewModel
import eu.europa.ec.euidi.verifier.presentation.ui.show_document.ShowDocumentsViewModel
import eu.europa.ec.euidi.verifier.presentation.ui.transfer_status.TransferStatusViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {

    viewModelOf(::CustomRequestViewModel)
    viewModelOf(::DocumentsToRequestViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MenuViewModel)
    viewModelOf(::QrScanViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ShowDocumentsViewModel)

    viewModel { params ->
        TransferStatusViewModel(
            transferStatusInteractor = get(),
            resourceProvider = get(),
            qrCode = params.get(),
        )
    }
}