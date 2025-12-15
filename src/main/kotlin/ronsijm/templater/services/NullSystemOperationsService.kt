package ronsijm.templater.services

/** Default no-op implementation - returns null for all dialogs */
object NullSystemOperationsService : SystemOperationsService {
    override fun prompt(promptText: String, defaultValue: String?, multiLine: Boolean, password: Boolean) = null
    override fun suggester(textItems: List<*>, values: List<*>, throwOnCancel: Boolean, placeholder: String, limit: Int?) = null
    override fun multiSuggester(textItems: List<*>, values: List<*>, throwOnCancel: Boolean, placeholder: String, limit: Int?) = null
}

