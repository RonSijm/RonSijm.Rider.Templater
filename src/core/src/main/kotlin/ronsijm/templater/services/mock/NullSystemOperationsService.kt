package ronsijm.templater.services.mock

import ronsijm.templater.services.SystemOperationsService


object NullSystemOperationsService : SystemOperationsService {
    override fun prompt(promptText: String, defaultValue: String?, multiLine: Boolean, password: Boolean) = null
    override fun suggester(textItems: List<*>, values: List<*>, throwOnCancel: Boolean, placeholder: String, limit: Int?) = null
    override fun multiSuggester(textItems: List<*>, values: List<*>, throwOnCancel: Boolean, placeholder: String, limit: Int?) = null
}
