package ronsijm.templater.handlers

data class CommandMetadata(
    val name: String,
    val description: String,
    val example: String,
    val parameters: String = ""
)

