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

package eu.europa.ec.euidi.verifier.domain.di

import eu.europa.ec.euidi.verifier.core.controller.DataStoreController
import eu.europa.ec.euidi.verifier.core.controller.PlatformController
import eu.europa.ec.euidi.verifier.core.controller.TransferController
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.config.ConfigProvider
import eu.europa.ec.euidi.verifier.domain.config.ConfigProviderImpl
import eu.europa.ec.euidi.verifier.domain.interactor.CustomRequestInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.CustomRequestInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.DocumentsToRequestInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.DocumentsToRequestInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.HomeInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.HomeInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.MenuInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.MenuInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.QrScanInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.QrScanInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.SettingsInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.SettingsInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.ShowDocumentsInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.ShowDocumentsInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.TransferStatusInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.TransferStatusInteractorImpl
import org.koin.dsl.module

val domainModule = module {

    single<ConfigProvider> { ConfigProviderImpl(get<PlatformController>()) }

    factory<HomeInteractor> {
        HomeInteractorImpl(
            get<PlatformController>(),
            get<UuidProvider>(),
            get<ResourceProvider>()
        )
    }

    factory<DocumentsToRequestInteractor> {
        DocumentsToRequestInteractorImpl(
            get<ConfigProvider>(),
            get<ResourceProvider>()
        )
    }

    factory<CustomRequestInteractor> {
        CustomRequestInteractorImpl(
            resourceProvider = get<ResourceProvider>(),
            configProvider = get<ConfigProvider>()
        )
    }

    factory<ShowDocumentsInteractor> {
        ShowDocumentsInteractorImpl(
            get<ResourceProvider>(),
            get<UuidProvider>()
        )
    }

    factory<TransferStatusInteractor> {
        TransferStatusInteractorImpl(
            get<ResourceProvider>(),
            get<UuidProvider>(),
            get<TransferController>(),
            get<DataStoreController>(),
            get<ConfigProvider>()
        )
    }

    factory<MenuInteractor> {
        MenuInteractorImpl(
            get<UuidProvider>(),
            get<ResourceProvider>(),
            get<ConfigProvider>()
        )
    }

    factory<SettingsInteractor> {
        SettingsInteractorImpl(
            get<UuidProvider>(),
            get<ResourceProvider>(),
            get<DataStoreController>()
        )
    }

    factory<QrScanInteractor> {
        QrScanInteractorImpl(
            get<ResourceProvider>()
        )
    }
}