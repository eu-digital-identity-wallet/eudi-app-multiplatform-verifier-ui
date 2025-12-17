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

package eu.europa.ec.euidi.verifier.core.provider

import android.content.Context
import eu.europa.ec.euidi.verifier.core.controller.model.BuildType
import eu.europa.ec.euidi.verifier.core.controller.model.FlavorType
import eu.europa.ec.euidi.verifier.core.di.initKoin
import org.koin.android.ext.koin.androidContext

object RuntimeProvider {

    lateinit var appContext: Context
        private set

    lateinit var versionName: String
        private set

    lateinit var buildType: BuildType
        private set

    lateinit var flavor: FlavorType
        private set

    private var initialized = false

    val isInitialized: Boolean
        get() = initialized

    fun init(
        context: Context,
        versionName: String,
        buildType: String,
        flavor: String
    ) {
        check(!initialized) { "RuntimeProvider already initialized" }

        appContext = context.applicationContext

        this.versionName = versionName
        this.buildType = BuildType.from(buildType)
        this.flavor = FlavorType.from(flavor)

        initKoin {
            androidContext(appContext)
        }

        initialized = true
    }
}