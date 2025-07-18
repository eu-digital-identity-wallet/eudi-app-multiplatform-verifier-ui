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

package eu.europa.ec.euidi.verifier.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import eu.europa.ec.euidi.verifier.core.helper.AndroidAppCloser
import eu.europa.ec.euidi.verifier.core.helper.AppCloser
import eu.europa.ec.euidi.verifier.presentation.ui.container.ContainerView
import org.koin.android.ext.android.inject

class ContainerActivity : ComponentActivity() {

    private val appCloser: AppCloser by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        registerActivityWithAppCloser()

        setContent {
            ContainerView()
        }
    }

    private fun registerActivityWithAppCloser() {
        // Register activity after Koin is already started
        (appCloser as? AndroidAppCloser)?.registerActivity(activity = this)
    }
}