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

package eu.europa.ec.euidi.verifier.domain.di

import eu.europa.ec.euidi.verifier.core.controller.DataStoreController
import eu.europa.ec.euidi.verifier.core.provider.ResourceProvider
import eu.europa.ec.euidi.verifier.core.provider.UuidProvider
import eu.europa.ec.euidi.verifier.domain.interactor.HomeInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.HomeInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.MenuInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.MenuInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.QrScanInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.QrScanInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.ReverseEngagementInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.ReverseEngagementInteractorImpl
import eu.europa.ec.euidi.verifier.domain.interactor.SettingsInteractor
import eu.europa.ec.euidi.verifier.domain.interactor.SettingsInteractorImpl
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
class InteractorModule {

    @Factory
    fun provideHomeInteractor(
        uuidProvider: UuidProvider,
        resourceProvider: ResourceProvider,
    ): HomeInteractor = HomeInteractorImpl(
        uuidProvider,
        resourceProvider,
    )

    @Factory
    fun provideMenuInteractor(
        uuidProvider: UuidProvider,
        resourceProvider: ResourceProvider,
    ): MenuInteractor = MenuInteractorImpl(
        uuidProvider,
        resourceProvider,
    )

    @Factory
    fun provideSettingsInteractor(
        uuidProvider: UuidProvider,
        resourceProvider: ResourceProvider,
        dataStoreController: DataStoreController,
    ): SettingsInteractor = SettingsInteractorImpl(
        uuidProvider,
        resourceProvider,
        dataStoreController,
    )

    @Factory
    fun provideReverseEngagementInteractor(
        resourceProvider: ResourceProvider,
    ): ReverseEngagementInteractor = ReverseEngagementInteractorImpl(
        resourceProvider,
    )

    @Factory
    fun provideQrScanInteractor(
        resourceProvider: ResourceProvider,
    ): QrScanInteractor = QrScanInteractorImpl(
        resourceProvider,
    )

}