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

package eu.europa.ec.euidi.verifier.core.controller

import co.touchlab.kermit.Logger

interface LoggerController {
    /**
     * Send a DEBUG log message.
     *
     * @param message The message you would like logged.
     */
    fun d(message: String)

    /**
     * Send an ERROR log message.
     *
     * @param message The message you would like logged.
     */
    fun e(message: String)

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param message   The message you would like logged.
     * @param throwable An exception to log
     */
    fun e(message: String, throwable: Throwable?)

    /**
     * Send an WARNING log message.
     *
     * @param message The message you would like logged.
     */
    fun w(message: String)
}

class LoggerControllerImpl : LoggerController {
    override fun d(message: String) {
        Logger.d { message }
    }

    override fun e(message: String) {
        Logger.e { message }
    }

    override fun e(message: String, throwable: Throwable?) {
        Logger.e(throwable) { message }
    }

    override fun w(message: String) {
        Logger.w { message }
    }
}