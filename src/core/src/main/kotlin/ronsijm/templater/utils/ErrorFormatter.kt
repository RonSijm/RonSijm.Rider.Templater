package ronsijm.templater.utils


object ErrorFormatter {

    fun formatError(
        message: String,
        content: String,
        position: Int,
        contextLines: Int = 2
    ): String {
        val lineNumber = TextUtils.calculateLineNumber(content, position)
        val lines = content.lines()

        val startLine = maxOf(0, lineNumber - 1 - contextLines)
        val endLine = minOf(lines.size - 1, lineNumber - 1 + contextLines)

        val context = buildString {
            appendLine("Error at line $lineNumber: $message")
            appendLine()
            for (i in startLine..endLine) {
                val marker = if (i == lineNumber - 1) ">>> " else "    "
                appendLine("$marker${i + 1}: ${lines[i]}")
            }
        }

        return context
    }


    fun formatErrorSimple(
        message: String,
        content: String,
        position: Int
    ): String {
        val lineNumber = TextUtils.calculateLineNumber(content, position)
        return "Line $lineNumber: $message"
    }
}

