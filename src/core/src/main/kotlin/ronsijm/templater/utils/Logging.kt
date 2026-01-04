package ronsijm.templater.utils

import java.util.logging.Level
import java.util.logging.Logger as JulLogger


interface Logger {
    fun debug(message: String)
    fun debug(message: String, throwable: Throwable)
    fun info(message: String)
    fun warn(message: String)
    fun warn(message: String, throwable: Throwable)
    fun error(message: String)
    fun error(message: String, throwable: Throwable)
    val isDebugEnabled: Boolean
}


class JulLoggerWrapper(private val logger: JulLogger) : Logger {
    override fun debug(message: String) {
        logger.fine(message)
    }

    override fun debug(message: String, throwable: Throwable) {
        logger.log(Level.FINE, message, throwable)
    }

    override fun info(message: String) {
        logger.info(message)
    }

    override fun warn(message: String) {
        logger.warning(message)
    }

    override fun warn(message: String, throwable: Throwable) {
        logger.log(Level.WARNING, message, throwable)
    }

    override fun error(message: String) {
        logger.severe(message)
    }

    override fun error(message: String, throwable: Throwable) {
        logger.log(Level.SEVERE, message, throwable)
    }

    override val isDebugEnabled: Boolean get() = logger.isLoggable(Level.FINE)
}


object Logging {

    inline fun <reified T> getLogger(): Logger {
        return JulLoggerWrapper(JulLogger.getLogger(T::class.java.name))
    }


    fun getLogger(clazz: Class<*>): Logger {
        return JulLoggerWrapper(JulLogger.getLogger(clazz.name))
    }
}
