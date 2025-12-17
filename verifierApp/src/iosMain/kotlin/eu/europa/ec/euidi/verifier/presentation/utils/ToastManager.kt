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

package eu.europa.ec.euidi.verifier.presentation.utils

import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.endEditing
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

actual open class ToastManager {
    actual fun showToast(message: String) {

        // Dismiss the keyboard if it's open
        dismissKeyboard()

        val alert = UIAlertController.alertControllerWithTitle(
            title = null,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )

        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(alert, animated = true, completion = null)

        // Dismiss the alert after 2 seconds to mimic a toast
        val delay = dispatch_time(DISPATCH_TIME_NOW, 120)
        dispatch_after(delay, dispatch_get_main_queue()) {
            alert.dismissViewControllerAnimated(true, completion = null)
        }
    }

    //Function to dismiss the keyboard
    private fun dismissKeyboard() {
        val keyWindow = UIApplication.sharedApplication.keyWindow
        keyWindow?.endEditing(true)
    }
}