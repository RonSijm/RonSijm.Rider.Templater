package ronsijm.templater.handlers

data class CommandMetadata(
    val name: String,
    val description: String,
    val example: String,
    val parameters: String = "",
    val cancellable: Boolean = false,
    val pure: Boolean = false,
    val barrier: Boolean = false
)
