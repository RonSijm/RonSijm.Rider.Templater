package ronsijm.templater.common

data class ErrorCollection(
    private val errors: MutableList<TemplateError> = mutableListOf()
) : Iterable<TemplateError> {

    val size: Int get() = errors.size
    val isEmpty: Boolean get() = errors.isEmpty()
    val isNotEmpty: Boolean get() = errors.isNotEmpty()

    fun add(error: TemplateError) {
        errors.add(error)
    }

    fun addAll(collection: Collection<TemplateError>) {
        errors.addAll(collection)
    }

    fun clear() {
        errors.clear()
    }

    override fun iterator(): Iterator<TemplateError> = errors.iterator()

    fun hasErrors(): Boolean = errors.any { it.severity >= ErrorSeverity.ERROR }
    fun hasFatalErrors(): Boolean = errors.any { it.severity == ErrorSeverity.FATAL }
    fun hasWarnings(): Boolean = errors.any { it.severity == ErrorSeverity.WARNING }

    fun filterBySeverity(severity: ErrorSeverity): List<TemplateError> =
        errors.filter { it.severity == severity }

    fun filterByCode(code: ErrorCode): List<TemplateError> =
        errors.filter { it.code == code }

    fun format(): String = errors.joinToString("\n") { it.format() }

    companion object {
        fun of(vararg errors: TemplateError): ErrorCollection =
            ErrorCollection(errors.toMutableList())
    }
}

