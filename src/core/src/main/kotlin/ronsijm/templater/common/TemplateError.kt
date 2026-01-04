package ronsijm.templater.common


sealed interface TemplateError {
    val message: String
    val suggestion: String?
    val code: ErrorCode
    val severity: ErrorSeverity
        get() = ErrorSeverity.ERROR

    fun format(): String = buildString {
        append("[${code.name}] $message")
        suggestion?.let { append("\n  Suggestion: $it") }
    }

    fun toHtmlComment(): String = "<!-- Error: $message -->"
    fun toInline(): String = "[Error: $message]"
}





sealed interface ParseError : TemplateError {
    val location: ErrorLocation?

    override fun format(): String = buildString {
        location?.toPrefix()?.let { append(it) }
        append("[${code.name}] $message")
        suggestion?.let { append("\n  Suggestion: $it") }
    }
}

data class SyntaxError(
    override val message: String,
    override val location: ErrorLocation? = null,
    override val suggestion: String? = null
) : ParseError {
    override val code: ErrorCode = ErrorCode.PARSE_SYNTAX_ERROR
}

data class UnclosedTagError(
    val tagType: String,
    override val location: ErrorLocation? = null,
    override val suggestion: String? = "Check for matching closing tag"
) : ParseError {
    override val message: String = "Unclosed $tagType tag"
    override val code: ErrorCode = ErrorCode.PARSE_UNCLOSED_TAG
}

data class InvalidExpressionError(
    val expression: String,
    override val location: ErrorLocation? = null,
    override val suggestion: String? = null
) : ParseError {
    override val message: String = "Invalid expression: $expression"
    override val code: ErrorCode = ErrorCode.PARSE_INVALID_EXPRESSION
}

data class UnexpectedTokenError(
    val token: String,
    val expected: String? = null,
    override val location: ErrorLocation? = null
) : ParseError {
    override val message: String = buildString {
        append("Unexpected token: $token")
        expected?.let { append(", expected: $it") }
    }
    override val suggestion: String? = expected?.let { "Replace '$token' with '$it'" }
    override val code: ErrorCode = ErrorCode.PARSE_UNEXPECTED_TOKEN
}





sealed interface ValidationError : TemplateError {
    val location: ErrorLocation?

    override fun format(): String = buildString {
        location?.toPrefix()?.let { append(it) }
        append("[${code.name}] $message")
        suggestion?.let { append("\n  Suggestion: $it") }
    }
}

data class MissingParenthesesError(
    val functionName: String,
    override val location: ErrorLocation? = null
) : ValidationError {
    override val message: String = "Function call missing parentheses"
    override val suggestion: String = "Add () after the function name: $functionName()"
    override val code: ErrorCode = ErrorCode.VALIDATION_MISSING_PARENTHESES
}

data class UnknownModuleError(
    val moduleName: String,
    val availableModules: List<String> = emptyList(),
    override val location: ErrorLocation? = null
) : ValidationError {
    override val message: String = "Unknown module: $moduleName"
    override val suggestion: String? = when {
        moduleName.startsWith("dat") -> "Did you mean 'tp.date'?"
        moduleName.startsWith("fil") -> "Did you mean 'tp.file'?"
        moduleName.startsWith("front") -> "Did you mean 'tp.frontmatter'?"
        moduleName.startsWith("sys") -> "Did you mean 'tp.system'?"
        availableModules.isNotEmpty() -> "Available modules: ${availableModules.joinToString(", ")}"
        else -> null
    }
    override val code: ErrorCode = ErrorCode.VALIDATION_UNKNOWN_MODULE
}

data class UnknownFunctionError(
    val moduleName: String,
    val functionName: String,
    val availableFunctions: List<String> = emptyList(),
    override val location: ErrorLocation? = null
) : ValidationError {
    override val message: String = "Unknown function '$functionName' in module '$moduleName'"
    override val suggestion: String? = if (availableFunctions.isNotEmpty()) {
        "Available functions: ${availableFunctions.joinToString(", ")}"
    } else null
    override val code: ErrorCode = ErrorCode.VALIDATION_UNKNOWN_FUNCTION
}

data class InvalidArgumentError(
    val argumentName: String,
    val expectedType: String,
    val actualValue: String? = null,
    override val location: ErrorLocation? = null
) : ValidationError {
    override val message: String = buildString {
        append("Invalid argument '$argumentName': expected $expectedType")
        actualValue?.let { append(", got '$it'") }
    }
    override val suggestion: String? = "Provide a valid $expectedType value"
    override val code: ErrorCode = ErrorCode.VALIDATION_INVALID_ARGUMENT
}

data class TypeMismatchError(
    val expectedType: String,
    val actualType: String,
    val context: String? = null,
    override val location: ErrorLocation? = null
) : ValidationError {
    override val message: String = buildString {
        append("Type mismatch: expected $expectedType, got $actualType")
        context?.let { append(" in $it") }
    }
    override val suggestion: String? = "Convert the value to $expectedType"
    override val code: ErrorCode = ErrorCode.VALIDATION_TYPE_MISMATCH
}





sealed interface ExecutionError : TemplateError {
    val command: String?
}

data class CommandFailedError(
    override val command: String,
    override val message: String,
    override val suggestion: String? = null,
    val cause: Throwable? = null
) : ExecutionError {
    override val code: ErrorCode = ErrorCode.EXECUTION_COMMAND_FAILED
}

data class TimeoutError(
    override val command: String?,
    val timeoutMs: Long
) : ExecutionError {
    override val message: String = "Operation timed out after ${timeoutMs}ms"
    override val suggestion: String = "Consider breaking the operation into smaller parts"
    override val code: ErrorCode = ErrorCode.EXECUTION_TIMEOUT
}

data class CancellationError(
    override val command: String? = null,
    val reason: String = "Operation was cancelled by user"
) : ExecutionError {
    override val message: String = reason
    override val suggestion: String? = null
    override val code: ErrorCode = ErrorCode.EXECUTION_CANCELLED
    override val severity: ErrorSeverity = ErrorSeverity.INFO
}

data class DivisionByZeroError(
    val expression: String,
    override val command: String? = null
) : ExecutionError {
    override val message: String = "Division by zero in expression: $expression"
    override val suggestion: String = "Add a check for zero before dividing"
    override val code: ErrorCode = ErrorCode.EXECUTION_DIVISION_BY_ZERO
}

data class NullReferenceError(
    val variableName: String,
    override val command: String? = null
) : ExecutionError {
    override val message: String = "Null reference: '$variableName' is null"
    override val suggestion: String = "Check if '$variableName' is null before accessing it"
    override val code: ErrorCode = ErrorCode.EXECUTION_NULL_REFERENCE
}

data class IndexOutOfBoundsError(
    val index: Int,
    val size: Int,
    override val command: String? = null
) : ExecutionError {
    override val message: String = "Index $index out of bounds for size $size"
    override val suggestion: String = "Ensure index is between 0 and ${size - 1}"
    override val code: ErrorCode = ErrorCode.EXECUTION_INDEX_OUT_OF_BOUNDS
}





sealed interface IoError : TemplateError

data class FileNotFoundError(
    val filePath: String,
    override val suggestion: String? = "Check if the file path is correct"
) : IoError {
    override val message: String = "File not found: $filePath"
    override val code: ErrorCode = ErrorCode.IO_FILE_NOT_FOUND
}

data class PermissionDeniedError(
    val resource: String,
    val operation: String
) : IoError {
    override val message: String = "Permission denied: cannot $operation '$resource'"
    override val suggestion: String = "Check file permissions or run with elevated privileges"
    override val code: ErrorCode = ErrorCode.IO_PERMISSION_DENIED
}

data class NetworkError(
    val url: String,
    val details: String? = null,
    val cause: Throwable? = null
) : IoError {
    override val message: String = buildString {
        append("Network error accessing: $url")
        details?.let { append(" - $it") }
    }
    override val suggestion: String = "Check your network connection and the URL"
    override val code: ErrorCode = ErrorCode.IO_NETWORK_ERROR
}

data class ClipboardError(
    val operation: String,
    val details: String? = null
) : IoError {
    override val message: String = buildString {
        append("Clipboard error during $operation")
        details?.let { append(": $it") }
    }
    override val suggestion: String = "Try the operation again or check clipboard access"
    override val code: ErrorCode = ErrorCode.IO_CLIPBOARD_ERROR
}





sealed interface ConfigError : TemplateError

data class InvalidSettingError(
    val settingName: String,
    val invalidValue: String,
    val validValues: List<String>? = null
) : ConfigError {
    override val message: String = "Invalid value '$invalidValue' for setting '$settingName'"
    override val suggestion: String? = validValues?.let {
        "Valid values: ${it.joinToString(", ")}"
    }
    override val code: ErrorCode = ErrorCode.CONFIG_INVALID_SETTING
}

data class MissingRequiredSettingError(
    val settingName: String,
    val defaultValue: String? = null
) : ConfigError {
    override val message: String = "Missing required setting: $settingName"
    override val suggestion: String? = defaultValue?.let {
        "Consider using default value: $it"
    }
    override val code: ErrorCode = ErrorCode.CONFIG_MISSING_REQUIRED
}





sealed interface InternalError : TemplateError {
    override val severity: ErrorSeverity
        get() = ErrorSeverity.FATAL
}

data class UnexpectedInternalError(
    override val message: String,
    val cause: Throwable? = null
) : InternalError {
    override val suggestion: String = "This is a bug. Please report it with the error details."
    override val code: ErrorCode = ErrorCode.INTERNAL_ERROR
}

data class NotImplementedError(
    val feature: String
) : InternalError {
    override val message: String = "Feature not implemented: $feature"
    override val suggestion: String = "This feature is planned for a future release"
    override val code: ErrorCode = ErrorCode.INTERNAL_NOT_IMPLEMENTED
}





fun TemplateError.toErrorResult(): ErrorResult = ErrorResult(this.message)

fun TemplateError.toException(): Exception = when (this) {
    is ExecutionError -> RuntimeException(format(), (this as? CommandFailedError)?.cause)
    is ParseError -> IllegalArgumentException(format())
    is ValidationError -> IllegalArgumentException(format())
    is IoError -> java.io.IOException(format())
    is ConfigError -> IllegalStateException(format())
    is InternalError -> RuntimeException(format(), (this as? UnexpectedInternalError)?.cause)
}

