package ronsijm.templater.handlers

/**
 * Annotation to provide a description for a request parameter.
 * 
 * Used by KSP to generate parameter documentation in HandlerMetadata.
 * 
 * Example:
 * ```kotlin
 * data class NowRequest(
 *     @ParamDescription("Date/time format string")
 *     val format: String = "yyyy-MM-dd HH:mm",
 *     
 *     @ParamDescription("Offset string (e.g., '+7d', '-1M')")
 *     val offset: String = ""
 * ) : CommandRequest
 * ```
 * 
 * @param value The description of the parameter
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ParamDescription(val value: String)

