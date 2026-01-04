package ronsijm.templater.handlers

data class HandlerMetadata(
    val module: String,
    val name: String,
    val description: String,
    val example: String,
    val parameters: String = "",
    val cancellable: Boolean = false,
    val pure: Boolean = false,
    val barrier: Boolean = false
)
