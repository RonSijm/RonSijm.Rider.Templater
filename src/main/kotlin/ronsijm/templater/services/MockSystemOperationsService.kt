package ronsijm.templater.services

class MockSystemOperationsService(
    private val promptResponse: String? = "mock prompt response",
    private val suggesterResponse: Any? = "mock suggester response",
    private val multiSuggesterResponse: List<*>? = listOf("mock", "multi", "suggester")
) : SystemOperationsService {
    
    // Track calls for verification in tests
    var promptCalls = mutableListOf<PromptCall>()
    var suggesterCalls = mutableListOf<SuggesterCall>()
    var multiSuggesterCalls = mutableListOf<MultiSuggesterCall>()
    
    data class PromptCall(
        val promptText: String,
        val defaultValue: String?,
        val multiLine: Boolean,
        val password: Boolean
    )
    
    data class SuggesterCall(
        val textItems: List<*>,
        val values: List<*>,
        val throwOnCancel: Boolean,
        val placeholder: String,
        val limit: Int?
    )
    
    data class MultiSuggesterCall(
        val textItems: List<*>,
        val values: List<*>,
        val throwOnCancel: Boolean,
        val placeholder: String,
        val limit: Int?
    )
    
    override fun prompt(
        promptText: String,
        defaultValue: String?,
        multiLine: Boolean,
        password: Boolean
    ): String? {
        promptCalls.add(PromptCall(promptText, defaultValue, multiLine, password))
        return promptResponse
    }
    
    override fun suggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): Any? {
        suggesterCalls.add(SuggesterCall(textItems, values, throwOnCancel, placeholder, limit))
        return suggesterResponse
    }
    
    override fun multiSuggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): List<*>? {
        multiSuggesterCalls.add(MultiSuggesterCall(textItems, values, throwOnCancel, placeholder, limit))
        return multiSuggesterResponse
    }
}

