package ronsijm.templater.debug

data class FunctionScope(
    val id: String,
    val name: String,
    val nodeIds: List<String>,
    val parentScopeId: String? = null
)

