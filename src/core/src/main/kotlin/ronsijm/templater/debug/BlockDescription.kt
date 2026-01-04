package ronsijm.templater.debug

data class BlockDescription(
    val label: String,
    val variablesRead: Set<String>,
    val variablesWritten: Set<String>,
    val hasTrWrite: Boolean
)

