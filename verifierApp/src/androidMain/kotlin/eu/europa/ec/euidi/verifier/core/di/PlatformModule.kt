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

package eu.europa.ec.euidi.verifier.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import eu.europa.ec.euidi.verifier.core.controller.AndroidPlatformController
import eu.europa.ec.euidi.verifier.core.controller.AndroidTransferController
import eu.europa.ec.euidi.verifier.core.controller.DataStoreControllerImpl.Companion.DATASTORE_FILENAME
import eu.europa.ec.euidi.verifier.core.controller.DataStoreControllerImpl.Companion.createDataStore
import eu.europa.ec.euidi.verifier.core.controller.PlatformController
import eu.europa.ec.euidi.verifier.core.controller.TransferController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {
    single<DataStore<Preferences>> {
        createDataStore {
            androidContext().filesDir.resolve(DATASTORE_FILENAME).absolutePath
        }
    }

    single<PlatformController> {
        AndroidPlatformController(androidContext())
    }

    single<TransferController> {
        AndroidTransferController(
            context = androidContext(),
            resourceProvider = get()
        )
    }
}