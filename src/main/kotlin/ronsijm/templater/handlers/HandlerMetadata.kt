package ronsijm.templater.handlers

/** Info for docs and autocomplete: tp.{module}.{name}() */
data class HandlerMetadata(
    val module: String,
    val name: String,
    val description: String,
    val example: String,
    val parameters: String = "",
    /** Whether this handler can be cancelled by the user (e.g., dialogs) */
    val cancellable: Boolean = false,
    /** Whether this handler is pure (no side effects, can be parallelized) */
    val pure: Boolean = false,
    /** Whether this handler is a barrier (has side effects, requires sequential execution) */
    val barrier: Boolean = false
)

