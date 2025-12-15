package ronsijm.templater.utils

import com.intellij.openapi.diagnostic.Logger

/**
 * Logging utility that safely creates loggers
 * Handles the case where IntelliJ Platform is not available (e.g., in tests)
 */
object Logging {
    /**
     * Get a logger for the specified class
     * Returns null if the IntelliJ Platform logging is not available
     * 
     * Usage:
     * ```
     * companion object {
     *     private val LOG = Logging.getLogger<MyClass>()
     * }
     * ```
     */
    inline fun <reified T> getLogger(): Logger? {
        return try {
            Logger.getInstance(T::class.java)
        } catch (e: Throwable) {
            null // Logger not available in test environment
        }
    }
    
    /**
     * Get a logger for the specified class
     * Returns null if the IntelliJ Platform logging is not available
     * 
     * Usage:
     * ```
     * companion object {
     *     private val LOG = Logging.getLogger(MyClass::class.java)
     * }
     * ```
     */
    fun getLogger(clazz: Class<*>): Logger? {
        return try {
            Logger.getInstance(clazz)
        } catch (e: Throwable) {
            null // Logger not available in test environment
        }
    }
}

