package ronsijm.templater.script

/** Represents an arrow function: (params) => body */
data class ArrowFunction(
    val parameters: List<String>,
    val body: String,
    val isExpression: Boolean = true  // true for `x => x + 1`, false for `x => { return x + 1 }`
)

