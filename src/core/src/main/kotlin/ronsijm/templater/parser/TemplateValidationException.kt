package ronsijm.templater.parser


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


            if (suggestion != null) {
                parts.add("\nSuggestion: $suggestion")
            }

            return parts.joinToString("")
        }
    }
}
