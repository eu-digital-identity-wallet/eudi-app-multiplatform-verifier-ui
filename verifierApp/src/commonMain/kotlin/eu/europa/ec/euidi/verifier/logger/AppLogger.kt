package eu.europa.ec.euidi.verifier.logger
import co.touchlab.kermit.Logger

interface AppLogger {
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

class AppLoggerImpl : AppLogger {
    override fun d(message: String) {
        Logger.d { message }
    }

    override fun e(message: String) {
        Logger.e { message }
    }

    override fun e(message: String, throwable: Throwable?) {
        Logger.e(throwable){ message }
    }

    override fun w(message: String) {
        Logger.w { message }
    }
}