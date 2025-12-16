package ronsijm.templater.script.evaluators

import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.script.ModuleRegistry
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.methods.ArrayMethodExecutor
import ronsijm.templater.script.methods.StringMethodExecutor

/**
 * Executes function calls including method calls on objects.
 * Single Responsibility: Only handles function/method execution.
 */
class FunctionCallExecutor(
    private val scriptContext: ScriptContext,
    private val moduleRegistry: ModuleRegistry,
    private val expressionEvaluator: (String) -> Any?,
    private val arrowFunctionExecutor: (ArrowFunction, List<Any?>) -> Any?
) {

    /**
     * Parse function arguments from a string.
     */
    fun parseArguments(argsString: String): List<Any?> {
        if (argsString.isBlank()) return emptyList()

        val args = mutableListOf<Any?>()
        var current = StringBuilder()
        val state = ParserState()

        for (char in argsString) {
            state.updateForChar(char)
            when {
                char == ',' && state.isAtTopLevel() -> {
                    args.add(expressionEvaluator(current.toString().trim()))
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            args.add(expressionEvaluator(current.toString().trim()))
        }

        return args
    }

    /**
     * Execute a function call by path and arguments.
     */
    fun executeFunctionCall(functionPath: String, args: List<Any?>): Any? {
        // Check if it's a simple function name (could be an arrow function variable)
        if (!functionPath.contains(".")) {
            val fn = scriptContext.getVariable(functionPath)
            if (fn is ArrowFunction) {
                return arrowFunctionExecutor(fn, args)
            }
        }

        val parts = functionPath.split(".")
        if (parts.size == 2) {
            val varName = parts[0]
            val methodName = parts[1]

            // Handle JavaScript Object.keys(), Object.values(), Object.entries()
            if (varName == "Object") {
                return executeObjectMethod(methodName, args)
            }

            val obj = scriptContext.getVariable(varName)

            // DateObject methods
            if (obj is DateObject) {
                return executeDateMethod(obj, methodName)
            }

            // String methods
            if (obj is String) {
                return StringMethodExecutor.execute(obj, methodName, args)
            }

            // Array methods
            if (obj is List<*>) {
                return ArrayMethodExecutor.execute(obj, methodName, args) { fn, fnArgs ->
                    arrowFunctionExecutor(fn, fnArgs)
                }
            }

            // Map property access
            if (obj is Map<*, *>) {
                return obj[methodName]
            }
        }

        // Delegate to ModuleRegistry
        return moduleRegistry.executeFunction(functionPath, args)
    }

    private fun executeObjectMethod(methodName: String, args: List<Any?>): Any? {
        val arg = args.getOrNull(0)
        return when (methodName) {
            "keys" -> when (arg) {
                is Map<*, *> -> arg.keys.map { it?.toString() ?: "" }
                else -> emptyList<String>()
            }
            "values" -> when (arg) {
                is Map<*, *> -> arg.values.toList()
                else -> emptyList<Any?>()
            }
            "entries" -> when (arg) {
                is Map<*, *> -> arg.entries.map { listOf(it.key?.toString() ?: "", it.value) }
                else -> emptyList<List<Any?>>()
            }
            else -> null
        }
    }

    private fun executeDateMethod(obj: DateObject, methodName: String): Any? {
        return when (methodName) {
            "getHours" -> obj.getHours()
            "getMinutes" -> obj.getMinutes()
            "getSeconds" -> obj.getSeconds()
            "getDate" -> obj.getDate()
            "getMonth" -> obj.getMonth()
            "getFullYear" -> obj.getFullYear()
            "getDay" -> obj.getDay()
            else -> null
        }
    }
}

/**
 * Simple JavaScript Date object wrapper.
 */
class DateObject {
    private val dateTime = java.time.LocalDateTime.now()

    fun getHours(): Int = dateTime.hour
    fun getMinutes(): Int = dateTime.minute
    fun getSeconds(): Int = dateTime.second
    fun getDate(): Int = dateTime.dayOfMonth
    fun getMonth(): Int = dateTime.monthValue - 1 // JavaScript months are 0-indexed
    fun getFullYear(): Int = dateTime.year
    fun getDay(): Int = dateTime.dayOfWeek.value % 7 // JavaScript: 0=Sunday
}

