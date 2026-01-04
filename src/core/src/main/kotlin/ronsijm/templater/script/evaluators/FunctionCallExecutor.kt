package ronsijm.templater.script.evaluators

import ronsijm.templater.common.BuiltinObjects
import ronsijm.templater.common.CacheConfig
import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.script.ModuleRegistry
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.UserFunction
import ronsijm.templater.script.builtins.GlobalFunctions
import ronsijm.templater.script.builtins.MathObject
import ronsijm.templater.script.methods.ArrayMethodExecutor
import ronsijm.templater.script.methods.DateMethodExecutor
import ronsijm.templater.script.methods.NumberMethodExecutor
import ronsijm.templater.script.methods.StringMethodExecutor

class FunctionCallExecutor(
    private val scriptContext: ScriptContext,
    private val moduleRegistry: ModuleRegistry,
    private val expressionEvaluator: (String) -> Any?,
    private val arrowFunctionExecutor: (ArrowFunction, List<Any?>) -> Any?
) {
    companion object {
        private val TOKEN_SPLIT_REGEX = Regex("[^a-zA-Z0-9_]")
    }

    private val memoCache = LinkedHashMap<String, Any?>(CacheConfig.FUNCTION_CACHE_SIZE, CacheConfig.CACHE_LOAD_FACTOR, true)
    private val maxCacheSize = CacheConfig.FUNCTION_CACHE_SIZE

    private data class InlinedFunction(
        val parameters: List<String>,
        val bodyExpression: String,
        val isSimpleReturn: Boolean
    )
    private val inlineCache = mutableMapOf<String, InlinedFunction>()

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


    fun executeFunctionCall(functionPath: String, args: List<Any?>): Any? {
        if (!functionPath.contains(".")) {
            val fn = scriptContext.getVariable(functionPath)
            when (fn) {
                is ArrowFunction -> return arrowFunctionExecutor(fn, args)
                is UserFunction -> return executeUserFunction(fn, args)
            }

            if (GlobalFunctions.isSupported(functionPath)) {
                return GlobalFunctions.execute(functionPath, args)
            }
        }

        val parts = functionPath.split(".")
        if (parts.size == 2) {
            val varName = parts[0]
            val methodName = parts[1]

            if (varName == BuiltinObjects.MATH) {
                return MathObject.execute(methodName, args)
            }

            if (varName == BuiltinObjects.OBJECT) {
                return executeObjectMethod(methodName, args)
            }

            if (varName == BuiltinObjects.DATE && methodName == "now") {
                return System.currentTimeMillis()
            }

            if (varName == "performance" && methodName == "now") {
                return System.nanoTime() / 1_000_000.0
            }

            if (varName == BuiltinObjects.CONSOLE && methodName == "log") {
                println(args.joinToString(" ") { it?.toString() ?: "undefined" })
                return null
            }

            if (varName == BuiltinObjects.ARRAY && methodName == "from") {
                return executeArrayFrom(args)
            }

            if ((varName.startsWith("\"") && varName.endsWith("\"")) ||
                (varName.startsWith("'") && varName.endsWith("'"))) {
                val strValue = varName.substring(1, varName.length - 1)
                return StringMethodExecutor.execute(strValue, methodName, args)
            }

            val obj = scriptContext.getVariable(varName)

            if (obj is DateObject) {
                return DateMethodExecutor.execute(obj, methodName)
            }

            if (obj is String || obj is StringBuilder) {
                return StringMethodExecutor.execute(obj, methodName, args)
            }

            if (obj is Number) {
                return NumberMethodExecutor.execute(obj, methodName, args)
            }

            if (obj is List<*>) {
                return ArrayMethodExecutor.execute(obj, methodName, args) { fn, fnArgs ->
                    arrowFunctionExecutor(fn, fnArgs)
                }
            }

            if (obj is Map<*, *>) {
                return obj[methodName]
            }
        }

        return moduleRegistry.executeFunction(functionPath, args)
    }

    private fun executeUserFunction(fn: UserFunction, args: List<Any?>): Any? {
        val inlined = tryInlineFunction(fn, args)
        if (inlined != null) {
            return inlined
        }

        val canMemoize = !fn.body.contains("tR") && !fn.body.contains("console.")

        val cacheKey = if (canMemoize) buildCacheKey(fn.name, args) else null
        if (cacheKey != null) {
            val cached = memoCache[cacheKey]
            if (cached != null || memoCache.containsKey(cacheKey)) {
                return cached
            }
        }

        val savedValues = fn.parameters.map { it to scriptContext.getVariable(it) }

        try {
            fn.parameters.forEachIndexed { index, param ->
                scriptContext.setVariable(param, args.getOrNull(index))
            }

            fn.executor(fn.body)

            val result = scriptContext.getReturnValue()

            if (cacheKey != null) {
                if (memoCache.size >= maxCacheSize) {
                    val oldest = memoCache.keys.first()
                    memoCache.remove(oldest)
                }
                memoCache[cacheKey] = result
            }

            return result
        } finally {
            savedValues.forEach { (name, value) ->
                if (value != null) {
                    scriptContext.setVariable(name, value)
                } else {
                    scriptContext.clearVariable(name)
                }
            }
            scriptContext.clearReturnState()
        }
    }

    private fun buildCacheKey(functionName: String, args: List<Any?>): String {
        val sb = StringBuilder(functionName)
        sb.append('(')
        args.forEachIndexed { index, arg ->
            if (index > 0) sb.append(',')
            sb.append(serializeArg(arg))
        }
        sb.append(')')
        return sb.toString()
    }

    private fun serializeArg(arg: Any?): String {
        return when (arg) {
            null -> "null"
            is Number -> arg.toString()
            is String -> "\"$arg\""
            is Boolean -> arg.toString()
            is List<*> -> "[${arg.joinToString(",") { serializeArg(it) }}]"
            else -> arg.hashCode().toString()
        }
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

    private fun tryInlineFunction(fn: UserFunction, args: List<Any?>): Any? {
        val cached = inlineCache[fn.name]
        if (cached != null) {
            return evaluateInlinedFunction(cached, args)
        }

        val body = fn.body.trim()

        if (body.contains("tR") || body.contains("console.") || body.contains("let ") ||
            body.contains("const ") || body.contains("var ")) {
            return null
        }

        if (!body.startsWith("return ")) {
            return null
        }

        val returnExpr = body.substring(7).trim().removeSuffix(";")

        val hasExternalVars = returnExpr.split(TOKEN_SPLIT_REGEX)
            .filter { it.isNotEmpty() && it[0].isLetter() }
            .any { token ->
                token !in fn.parameters &&
                token !in setOf("true", "false", "null", "Math", "Array")
            }

        if (hasExternalVars) {
            return null
        }

        val inlined = InlinedFunction(fn.parameters, returnExpr, true)
        inlineCache[fn.name] = inlined

        return evaluateInlinedFunction(inlined, args)
    }

    private fun evaluateInlinedFunction(inlined: InlinedFunction, args: List<Any?>): Any? {
        val savedValues = mutableListOf<Pair<String, Any?>>()
        try {
            inlined.parameters.forEachIndexed { index, param ->
                savedValues.add(param to scriptContext.getVariable(param))
                scriptContext.setVariable(param, args.getOrNull(index))
            }

            return expressionEvaluator(inlined.bodyExpression)
        } finally {
            savedValues.forEach { (name, value) ->
                if (value != null) {
                    scriptContext.setVariable(name, value)
                } else {
                    scriptContext.clearVariable(name)
                }
            }
        }
    }

    private fun executeArrayFrom(args: List<Any?>): Any? {
        val arrayLike = args.getOrNull(0)
        val mapFn = args.getOrNull(1) as? ArrowFunction

        if (arrayLike is Map<*, *>) {
            val length = (arrayLike["length"] as? Number)?.toInt() ?: 0
            val result = ArrayList<Any?>(length)

            for (i in 0 until length) {
                val value = if (mapFn != null) {
                    arrowFunctionExecutor(mapFn, listOf(null, i))
                } else {
                    null
                }
                result.add(value)
            }

            return result
        }

        if (arrayLike is List<*>) {
            return if (mapFn != null) {
                arrayLike.mapIndexed { index, item ->
                    arrowFunctionExecutor(mapFn, listOf(item, index))
                }
            } else {
                ArrayList(arrayLike)
            }
        }

        return emptyList<Any?>()
    }
}
