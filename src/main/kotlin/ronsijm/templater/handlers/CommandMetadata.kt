package ronsijm.templater.handlers

data class CommandMetadata(
    val name: String,
    val description: String,
    val example: String,
    val parameters: String = "",
    /** Whether this command can be cancelled by the user (e.g., dialogs) */
    val cancellable: Boolean = false,
    /** Whether this command is pure (no side effects, can be parallelized) */
    val pure: Boolean = false,
    /** Whether this command is a barrier (has side effects, requires sequential execution) */
    val barrier: Boolean = false
)

