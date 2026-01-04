package ronsijm.templater.parser


class TemplateExecutionException(
    message: String,
    val command: String,
    val suggestion: String? = null,
    cause: Throwable? = null
) : Exception(buildMessage(message, command, suggestion), cause) {

    companion object {
        private fun buildMessage(
            message: String,
            command: String,
            suggestion: String?
        ): String {
            val parts = mutableListOf<String>()
            parts.add("Error executing command: $command")
            parts.add("\n$message")

            if (suggestion != null) {
                parts.add("\nSuggestion: $suggestion")
            }

            return parts.joinToString("")
        }
    }
}
