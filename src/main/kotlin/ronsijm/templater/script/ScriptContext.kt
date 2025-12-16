package ronsijm.templater.script

import ronsijm.templater.modules.FrontmatterModule
import ronsijm.templater.parser.TemplateContext

/**
 * Mutable state during script execution: variables and the result accumulator (tR).
 */
class ScriptContext(private val templateContext: TemplateContext) {
    private val variables = mutableMapOf<String, Any?>()
    private var resultAccumulator = StringBuilder()

    /** Flag to indicate early return from script execution */
    private var returnRequested = false

    val frontmatterModule by lazy { FrontmatterModule(templateContext) }

    fun initializeResultAccumulator(currentOutput: String) {
        resultAccumulator = StringBuilder(currentOutput)
        returnRequested = false // Reset return flag for new execution
    }

    fun getResultAccumulator() = resultAccumulator.toString()
    fun appendToResult(value: String) { resultAccumulator.append(value) }
    fun setResult(value: String) { resultAccumulator = StringBuilder(value) }

    fun getVariable(name: String) = variables[name]
    fun setVariable(name: String, value: Any?) { variables[name] = value }
    fun removeVariable(name: String) { variables.remove(name) }
    fun hasVariable(name: String) = variables.containsKey(name)

    fun getTemplateContext() = templateContext

    /** Request early return from script execution */
    fun requestReturn() { returnRequested = true }

    /** Check if return has been requested */
    fun isReturnRequested() = returnRequested

    /** Reset the return flag */
    fun resetReturn() { returnRequested = false }
}