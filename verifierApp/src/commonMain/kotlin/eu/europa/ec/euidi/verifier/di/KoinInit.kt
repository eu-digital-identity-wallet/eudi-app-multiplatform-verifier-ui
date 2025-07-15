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

package eu.europa.ec.euidi.verifier.di

import eu.europa.ec.euidi.verifier.di.modules.InteractorModule
import eu.europa.ec.euidi.verifier.di.modules.LoggerModule
import eu.europa.ec.euidi.verifier.di.modules.PreferencesModule
import eu.europa.ec.euidi.verifier.di.modules.ProviderModule
import eu.europa.ec.euidi.verifier.platform.platformModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module


fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()

        modules(
            platformModule(),
            PreferencesModule().module,
            LoggerModule().module,
            ProviderModule().module,
            InteractorModule().module,
            defaultModule // needed for generated viewModels
        )
    }

fun initKoin() = initKoin {} // called by iOS