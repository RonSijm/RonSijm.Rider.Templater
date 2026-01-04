package ronsijm.templater.script


data class ArrowFunction(
    val parameters: List<String>,
    val body: String,
    val isExpression: Boolean = true
)


data class UserFunction(
    val name: String,
    val parameters: List<String>,
    val body: String,
    val executor: (String) -> Unit
)
