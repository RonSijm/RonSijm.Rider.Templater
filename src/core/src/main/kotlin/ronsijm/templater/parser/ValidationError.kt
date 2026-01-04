package ronsijm.templater.parser


data class ValidationError(
    val message: String,
    val lineNumber: Int? = null,
    val columnNumber: Int? = null,
    val suggestion: String? = null
) {
    override fun toString(): String {
        val location = if (lineNumber != null) {
            if (columnNumber != null) {
                "[Line $lineNumber, Column $columnNumber] "
            } else {
                "[Line $lineNumber] "
            }
        } else {
            ""
        }

        val suggestionText = if (suggestion != null) {
            "\n  Suggestion: $suggestion"
        } else {
            ""
        }

        return "$location$message$suggestionText"
    }
}