package ronsijm.templater.script

import ronsijm.templater.modules.FrontmatterModule
import ronsijm.templater.parser.TemplateContext

/**
 * Mutable state during script execution: variables and the result accumulator (tR).
 */
class ScriptContext(private val templateContext: TemplateContext) {
    private val variables = mutableMapOf<String, Any?>()
    private var resultAccumulator = StringBuilder()

    val frontmatterModule by lazy { FrontmatterModule(templateContext) }

    fun initializeResultAccumulator(currentOutput: String) {
        resultAccumulator = StringBuilder(currentOutput)
    }

    fun getResultAccumulator() = resultAccumulator.toString()
    fun appendToResult(value: String) { resultAccumulator.append(value) }
    fun setResult(value: String) { resultAccumulator = StringBuilder(value) }

    fun getVariable(name: String) = variables[name]
    fun setVariable(name: String, value: Any?) { variables[name] = value }
    fun removeVariable(name: String) { variables.remove(name) }
    fun hasVariable(name: String) = variables.containsKey(name)

    fun getTemplateContext() = templateContext
}

/** Represents an arrow function: (params) => body */
data class ArrowFunction(
    val parameters: List<String>,
    val body: String,
    val isExpression: Boolean = true  // true for `x => x + 1`, false for `x => { return x + 1 }`
)