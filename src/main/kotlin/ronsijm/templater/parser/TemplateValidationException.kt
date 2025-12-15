package ronsijm.templater.parser

/**
 * Exception thrown when template validation fails
 * Provides detailed error information including line numbers and suggestions
 */
class TemplateValidationException(
    message: String,
    val lineNumber: Int? = null,
    val columnNumber: Int? = null,
    val suggestion: String? = null,
    cause: Throwable? = null
) : Exception(buildMessage(message, lineNumber, columnNumber, suggestion), cause) {
    
    companion object {
        private fun buildMessage(
            message: String,
            lineNumber: Int?,
            columnNumber: Int?,
            suggestion: String?
        ): String {
            val parts = mutableListOf<String>()
            
            // Add location if available
            if (lineNumber != null) {
                val location = if (columnNumber != null) {
                    "Line $lineNumber, Column $columnNumber"
                } else {
                    "Line $lineNumber"
                }
                parts.add("[$location] $message")
            } else {
                parts.add(message)
            }
            
            // Add suggestion if available
            if (suggestion != null) {
                parts.add("\nSuggestion: $suggestion")
            }
            
            return parts.joinToString("")
        }
    }
}