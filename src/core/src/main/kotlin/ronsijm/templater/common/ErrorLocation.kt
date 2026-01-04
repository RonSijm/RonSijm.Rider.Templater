package ronsijm.templater.common

data class ErrorLocation(
    val lineNumber: Int? = null,
    val columnNumber: Int? = null,
    val fileName: String? = null
) {
    override fun toString(): String = buildString {
        fileName?.let { append("$it:") }
        lineNumber?.let {
            append("$it")
            columnNumber?.let { append(":$it") }
        }
    }

    fun toPrefix(): String = if (lineNumber != null) {
        if (columnNumber != null) "[Line $lineNumber, Column $columnNumber] "
        else "[Line $lineNumber] "
    } else ""
}

