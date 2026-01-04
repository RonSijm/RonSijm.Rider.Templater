package ronsijm.templater.script.compiler

import ronsijm.templater.script.ScriptContext


class CompiledExpressionEvaluator(
    private val context: ScriptContext,
    private val functionCaller: (name: String, args: List<Any?>) -> Any?,
    private val methodCaller: (obj: Any?, method: String, args: List<Any?>) -> Any?,
    private val propertyGetter: (obj: Any?, prop: String) -> Any?,
    private val indexGetter: (obj: Any?, index: Any?) -> Any?
) {
    private val cache = ExpressionCache()
    private val vm = BytecodeVM(context, functionCaller, methodCaller, propertyGetter, indexGetter)


    fun evaluate(expression: String): Any? {
        return try {
            val compiled = cache.getOrCompile(expression)
            vm.execute(compiled)
        } catch (e: Exception) {

            null
        }
    }


    fun tryEvaluate(expression: String): Result<Any?> {
        return try {
            val compiled = cache.getOrCompile(expression)
            Result.success(vm.execute(compiled))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun canCompile(expression: String): Boolean {
        return try {
            cache.getOrCompile(expression)
            true
        } catch (e: Exception) {
            false
        }
    }


    fun clearCache() {
        cache.clear()
    }
}
