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

package eu.europa.ec.euidi.verifier.core.helper

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * An implementation of [AppCloser] for Android applications.
 *
 * This class allows for the programmatic closing of the current Android application.
 * It requires an [Activity] to be registered with it before the [closeApp] method can
 * successfully finish the activity.
 */
class AndroidAppCloser : AppCloser {

    /**
     * A [WeakReference] to the current [Activity].
     * This is used to allow the [Activity] to be garbage collected if it is no longer needed.
     */
    private var activityRef: WeakReference<Activity>? = null

    /**
     * Registers the activity to be closed.
     *
     * This method should be called when the activity is created or resumed.
     * The activity is stored as a [WeakReference] to avoid memory leaks.
     *
     * @param activity The activity to be registered.
     */
    fun registerActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    /**
     * Closes the application by finishing the current activity.
     *
     * This method attempts to close the application by calling the `finish()` method on the
     * currently registered activity. If no activity is registered or if the activity has been
     * garbage collected, this method will have no effect.
     */
    override fun closeApp() {
        activityRef?.get()?.finish()
    }
}