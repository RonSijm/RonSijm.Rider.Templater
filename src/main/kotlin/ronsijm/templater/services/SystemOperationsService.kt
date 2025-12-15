package ronsijm.templater.services

/**
 * Abstraction for user interaction dialogs (prompts, selection lists, etc.)
 */
interface SystemOperationsService {
    fun prompt(
        promptText: String,
        defaultValue: String? = null,
        multiLine: Boolean = false,
        password: Boolean = false
    ): String?

    fun suggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean = false,
        placeholder: String = "",
        limit: Int? = null
    ): Any?

    fun multiSuggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean = false,
        placeholder: String = "",
        limit: Int? = null
    ): List<*>?
}

