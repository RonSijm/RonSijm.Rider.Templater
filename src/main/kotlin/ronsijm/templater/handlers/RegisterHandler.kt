package ronsijm.templater.handlers

/**
 * Annotation to mark a handler for auto-registration.
 *
 * KSP will scan for classes annotated with @RegisterHandler and generate:
 * - HandlerRegistry with all handlers grouped by module
 * - HandlerMetadata for each handler (derived from annotation + request class)
 *
 * Naming conventions:
 * - Handler: {Name}Handler (e.g., NowHandler)
 * - Request: {Name}Request (e.g., NowRequest) - must be in same package
 * - Parser: {Name}RequestParser (e.g., NowRequestParser) - must be in same package
 *
 * The command name is derived from the handler class name:
 * - NowHandler → "now"
 * - DailyQuoteHandler → "daily_quote"
 *
 * @param module The module category (e.g., "date", "file", "system", "web")
 * @param description Brief description of what the handler does
 * @param example Example usage (e.g., "now(\"YYYY-MM-DD\", \"+7d\")")
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RegisterHandler(
    val module: String,
    val description: String = "",
    val example: String = ""
)

