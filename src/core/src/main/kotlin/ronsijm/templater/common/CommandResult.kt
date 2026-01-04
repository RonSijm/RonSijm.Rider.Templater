package ronsijm.templater.common

import ronsijm.templater.utils.ErrorMessages

sealed interface CommandResult {
    val value: Any?
        get() = when (this) {
            is OkValueResult<*> -> this.value
            is OkResult -> ""
            is CancelledResult -> null
            is ErrorResult -> null
        }

    val isSuccess: Boolean
        get() = this is OkResult || this is OkValueResult<*>

    val isCancelled: Boolean
        get() = this is CancelledResult

    val isError: Boolean
        get() = this is ErrorResult
}

object OkResult : CommandResult {
    override val value: Any? get() = ""
    override fun toString(): String = ""
}

data class OkValueResult<T>(private val _value: T) : CommandResult {
    override val value: T get() = _value
    override fun toString(): String = _value?.toString() ?: ""
}


object CancelledResult : CommandResult, BooleanConvertible {
    override val value: Any? get() = null
    override val booleanValue: Boolean get() = false
    override fun toString(): String = "[Cancelled]"
}

data class ErrorResult(val message: String) : CommandResult {
    override val value: Any? get() = null
    override fun toString(): String = ErrorMessages.inlineError(message)
}



@Suppress("UNCHECKED_CAST")
fun <T> CommandResult.valueAs(): T? = when (this) {
    is OkValueResult<*> -> this.value as? T
    is OkResult -> "" as? T
    is CancelledResult -> null
    is ErrorResult -> null
}

inline fun <T, R> CommandResult.map(transform: (T) -> R): CommandResult = when (this) {
    is OkValueResult<*> -> {


        @Suppress("UNCHECKED_CAST")
        OkValueResult(transform(this.value as T))
    }
    is OkResult -> this
    is CancelledResult -> this
    is ErrorResult -> this
}

inline fun <T> CommandResult.flatMap(transform: (T) -> CommandResult): CommandResult = when (this) {
    is OkValueResult<*> -> {


        @Suppress("UNCHECKED_CAST")
        transform(this.value as T)
    }
    is OkResult -> this
    is CancelledResult -> this
    is ErrorResult -> this
}

inline fun CommandResult.mapError(transform: (String) -> String): CommandResult = when (this) {
    is ErrorResult -> ErrorResult(transform(this.message))
    else -> this
}

inline fun <T> CommandResult.recover(recovery: (String) -> T): CommandResult = when (this) {
    is ErrorResult -> OkValueResult(recovery(this.message))
    else -> this
}

inline fun CommandResult.recoverWith(recovery: (String) -> CommandResult): CommandResult = when (this) {
    is ErrorResult -> recovery(this.message)
    else -> this
}

inline fun <T> CommandResult.onSuccess(action: (T) -> Unit): CommandResult {
    if (this is OkValueResult<*>) {


        @Suppress("UNCHECKED_CAST")
        action(this.value as T)
    }
    return this
}

inline fun CommandResult.onError(action: (String) -> Unit): CommandResult {
    if (this is ErrorResult) {
        action(this.message)
    }
    return this
}

inline fun CommandResult.onCancelled(action: () -> Unit): CommandResult {
    if (this is CancelledResult) {
        action()
    }
    return this
}

fun <T> CommandResult.getOrDefault(default: T): T = when (this) {
    is OkValueResult<*> -> {


        @Suppress("UNCHECKED_CAST")
        this.value as T
    }
    else -> default
}

inline fun <T> CommandResult.getOrElse(default: () -> T): T = when (this) {
    is OkValueResult<*> -> {


        @Suppress("UNCHECKED_CAST")
        this.value as T
    }
    else -> default()
}

fun <T> CommandResult.getOrThrow(): T = when (this) {
    is OkValueResult<*> -> {


        @Suppress("UNCHECKED_CAST")
        this.value as T
    }
    is ErrorResult -> throw IllegalStateException(this.message)
    is CancelledResult -> throw IllegalStateException("Operation was cancelled")
    is OkResult -> {


        @Suppress("UNCHECKED_CAST")
        "" as T
    }
}

inline fun <R> CommandResult.fold(
    onSuccess: (Any?) -> R,
    onError: (String) -> R,
    onCancelled: () -> R
): R = when (this) {
    is OkValueResult<*> -> onSuccess(this.value)
    is OkResult -> onSuccess("")
    is ErrorResult -> onError(this.message)
    is CancelledResult -> onCancelled()
}

inline fun <T, U, R> CommandResult.zip(
    other: CommandResult,
    combiner: (T, U) -> R
): CommandResult = when {
    this is CancelledResult -> this
    other is CancelledResult -> other
    this is ErrorResult -> this
    other is ErrorResult -> other
    this is OkValueResult<*> && other is OkValueResult<*> -> {


        @Suppress("UNCHECKED_CAST")
        OkValueResult(combiner(this.value as T, other.value as U))
    }
    else -> OkResult
}

inline fun <T> CommandResult.filter(
    errorMessage: String = "Value did not match filter",
    predicate: (T) -> Boolean
): CommandResult = when (this) {
    is OkValueResult<*> -> {


        @Suppress("UNCHECKED_CAST")
        if (predicate(this.value as T)) this else ErrorResult(errorMessage)
    }
    else -> this
}

fun <T : Any> T?.toResult(errorMessage: String = "Value was null"): CommandResult =
    if (this != null) OkValueResult(this) else ErrorResult(errorMessage)

inline fun <T> runCatching(block: () -> T): CommandResult = try {
    OkValueResult(block())
} catch (e: Exception) {
    ErrorResult(e.message ?: "Unknown error")
}

