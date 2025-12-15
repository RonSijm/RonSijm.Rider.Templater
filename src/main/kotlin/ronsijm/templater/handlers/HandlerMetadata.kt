package ronsijm.templater.handlers

/** Info for docs and autocomplete: tp.{module}.{name}() */
data class HandlerMetadata(
    val module: String,
    val name: String,
    val description: String,
    val example: String,
    val parameters: String = ""
)

interface HandlerMetadataProvider {
    val metadata: HandlerMetadata
}



