package ronsijm.templater.handlers

/**
 * Marker interface for handlers that can be cancelled by the user.
 *
 * Cancellable handlers are typically those that show dialogs to the user
 * (prompts, suggesters, etc.) where the user can press Cancel/Escape.
 *
 * When a cancellable handler returns [CancelledResult], the template parser will check
 * the CancelBehavior setting to determine whether to:
 * - REMOVE_EXPRESSION: Replace the template expression with empty string
 * - KEEP_EXPRESSION: Keep the original template expression unchanged
 *
 * Handlers implementing this interface should:
 * - Return [CancelledResult] when the user cancels the operation
 * - Return [OkValueResult] when the operation completes successfully
 *
 * Example cancellable handlers:
 * - PromptHandler (tp.system.prompt)
 * - SuggesterHandler (tp.system.suggester)
 * - MultiSuggesterHandler (tp.system.multi_suggester)
 */
interface CancellableHandler

/**
 * Sealed interface representing the result of a command execution.
 */
sealed interface CommandResult {
    /**
     * Gets the underlying value of the result.
     * - For OkValueResult: returns the wrapped value
     * - For OkResult: returns empty string
     * - For CancelledResult: returns null
     * - For ErrorResult: returns null
     */
    val value: Any?
        get() = when (this) {
            is OkValueResult<*> -> this.value
            is OkResult -> ""
            is CancelledResult -> null
            is ErrorResult -> null
        }

    /**
     * Returns true if this result represents a successful operation.
     */
    val isSuccess: Boolean
        get() = this is OkResult || this is OkValueResult<*>

    /**
     * Returns true if this result was cancelled.
     */
    val isCancelled: Boolean
        get() = this is CancelledResult

    /**
     * Returns true if this result is an error.
     */
    val isError: Boolean
        get() = this is ErrorResult
}

/**
 * Represents a successful command execution with no output value.
 */
object OkResult : CommandResult {
    override val value: Any? get() = ""
    override fun toString(): String = ""
}

/**
 * Represents a successful command execution with an output value.
 */
data class OkValueResult<T>(private val _value: T) : CommandResult {
    override val value: T get() = _value
    override fun toString(): String = _value?.toString() ?: ""
}

/**
 * Represents a cancelled operation (user cancelled a dialog).
 */
object CancelledResult : CommandResult {
    override val value: Any? get() = null
    override fun toString(): String = "[Cancelled]"
}

/**
 * Represents an error during command execution.
 */
data class ErrorResult(val message: String) : CommandResult {
    override val value: Any? get() = null
    override fun toString(): String = "[Error: $message]"
}

/**
 * Extension to get the value as a specific type, or null if not matching.
 */
@Suppress("UNCHECKED_CAST")
fun <T> CommandResult.valueAs(): T? = when (this) {
    is OkValueResult<*> -> this.value as? T
    is OkResult -> "" as? T
    is CancelledResult -> null
    is ErrorResult -> null
}

