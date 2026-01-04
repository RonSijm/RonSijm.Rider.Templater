package ronsijm.templater.utils


object FilePathValidator {


    private val INVALID_FILENAME_CHARS = setOf('<', '>', ':', '"', '/', '\\', '|', '?', '*')


    private val WINDOWS_RESERVED_NAMES = setOf(
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    )


    private const val MAX_PATH_LENGTH = 260
    private const val MAX_FILENAME_LENGTH = 255


    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun valid() = ValidationResult(true)
            fun invalid(message: String) = ValidationResult(false, message)
        }
    }


    fun validateFileName(fileName: String): ValidationResult {
        if (fileName.isBlank()) {
            return ValidationResult.invalid("File name cannot be empty or blank")
        }

        if (fileName.length > MAX_FILENAME_LENGTH) {
            return ValidationResult.invalid("File name exceeds maximum length of $MAX_FILENAME_LENGTH characters")
        }


        val invalidChars = fileName.filter { it in INVALID_FILENAME_CHARS }
        if (invalidChars.isNotEmpty()) {
            return ValidationResult.invalid("File name contains invalid characters: ${invalidChars.toSet().joinToString(", ") { "'$it'" }}")
        }


        if (fileName.any { it.code < 32 }) {
            return ValidationResult.invalid("File name contains control characters")
        }


        if (fileName.startsWith(" ") || fileName.endsWith(" ")) {
            return ValidationResult.invalid("File name cannot start or end with spaces")
        }
        if (fileName.endsWith(".")) {
            return ValidationResult.invalid("File name cannot end with a period")
        }


        val nameWithoutExtension = fileName.substringBeforeLast(".")
        if (nameWithoutExtension.uppercase() in WINDOWS_RESERVED_NAMES) {
            return ValidationResult.invalid("'$nameWithoutExtension' is a reserved name on Windows")
        }

        return ValidationResult.valid()
    }


    fun validatePath(path: String): ValidationResult {
        if (path.isBlank()) {
            return ValidationResult.invalid("Path cannot be empty or blank")
        }

        if (path.length > MAX_PATH_LENGTH) {
            return ValidationResult.invalid("Path exceeds maximum length of $MAX_PATH_LENGTH characters")
        }


        if (path.contains("..")) {
            return ValidationResult.invalid("Path cannot contain '..' (parent directory references)")
        }


        if (path.contains('\u0000')) {
            return ValidationResult.invalid("Path cannot contain null characters")
        }


        val segments = path.split('/', '\\').filter { it.isNotEmpty() }
        for (segment in segments) {
            val segmentResult = validatePathSegment(segment)
            if (!segmentResult.isValid) {
                return segmentResult
            }
        }

        return ValidationResult.valid()
    }


    private fun validatePathSegment(segment: String): ValidationResult {
        if (segment.isBlank()) {
            return ValidationResult.valid()
        }


        val invalidChars = segment.filter { it in INVALID_FILENAME_CHARS && it != '/' && it != '\\' }
        if (invalidChars.isNotEmpty()) {
            return ValidationResult.invalid("Path segment '$segment' contains invalid characters: ${invalidChars.toSet().joinToString(", ") { "'$it'" }}")
        }


        if (segment.any { it.code < 32 }) {
            return ValidationResult.invalid("Path segment '$segment' contains control characters")
        }


        val nameWithoutExtension = segment.substringBeforeLast(".")
        if (nameWithoutExtension.uppercase() in WINDOWS_RESERVED_NAMES) {
            return ValidationResult.invalid("'$nameWithoutExtension' is a reserved name on Windows")
        }

        return ValidationResult.valid()
    }


    fun validateRename(newName: String): ValidationResult {
        return validateFileName(newName)
    }


    fun validateMove(targetPath: String): ValidationResult {
        return validatePath(targetPath)
    }


    fun validateCreateNew(filename: String?, folder: String?): ValidationResult {
        if (filename != null) {
            val filenameResult = validateFileName(filename)
            if (!filenameResult.isValid) {
                return filenameResult
            }
        }

        if (folder != null) {
            val folderResult = validatePath(folder)
            if (!folderResult.isValid) {
                return folderResult
            }
        }

        return ValidationResult.valid()
    }
}
