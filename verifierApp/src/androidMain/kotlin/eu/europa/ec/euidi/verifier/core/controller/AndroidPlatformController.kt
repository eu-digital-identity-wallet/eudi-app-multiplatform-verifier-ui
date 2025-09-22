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

package eu.europa.ec.euidi.verifier.core.controller

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import eu.europa.ec.euidi.verifier.BuildConfig
import eu.europa.ec.euidi.verifier.core.controller.model.BuildType
import java.lang.ref.WeakReference

class AndroidPlatformController(
    private val context: Context
) : PlatformController {
    private var activityRef: WeakReference<Activity>? = null

    fun registerActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override val buildType: BuildType
        get() = BuildType.from(BuildConfig.BUILD_TYPE)

    override fun closeApp() {
        activityRef?.get()?.finish()
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
