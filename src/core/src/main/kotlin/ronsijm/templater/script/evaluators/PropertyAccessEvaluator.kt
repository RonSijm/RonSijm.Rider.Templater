package ronsijm.templater.script.evaluators

import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.methods.ArrayMethodExecutor
import ronsijm.templater.script.methods.DateMethodExecutor
import ronsijm.templater.script.methods.NumberMethodExecutor
import ronsijm.templater.script.methods.StringMethodExecutor


class PropertyAccessEvaluator(
    private val scriptContext: ScriptContext,
    private val evaluateExpression: (String) -> Any?,
    private val parseArguments: (String) -> List<Any?>,
    private val executeArrowFunction: (ArrowFunction, List<Any?>) -> Any?
) {


    fun evaluateArrayAccessWithChain(expression: String): Any? {
        val varName = expression.substringBefore("[")


        val openBracketIndex = expression.indexOf('[')
        val closeBracketIndex = findMatchingBracket(expression, openBracketIndex)
        if (closeBracketIndex == -1) return null

        val indexExpr = expression.substring(openBracketIndex + 1, closeBracketIndex)
        val obj = scriptContext.getVariable(varName)



        val index = if (indexExpr.all { it.isLetterOrDigit() || it == '_' } && !indexExpr.all { it.isDigit() }) {

            scriptContext.getVariable(indexExpr)
        } else {

            evaluateExpression(indexExpr)
        }

        var result: Any? = when (obj) {
            is List<*> -> {

                when (index) {
                    is Int -> if (index in obj.indices) obj[index] else null
                    is Double -> {
                        val idx = index.toInt()
                        if (idx in obj.indices) obj[idx] else null
                    }
                    else -> {
                        val idx = (index as? Number)?.toInt() ?: return null
                        if (idx in obj.indices) obj[idx] else null
                    }
                }
            }
            is String -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx].toString() else null
            }
            is Map<*, *> -> {
                val key = index?.toString() ?: return null
                obj[key]
            }
            else -> null
        }


        if (closeBracketIndex < expression.length - 1) {
            val remaining = expression.substring(closeBracketIndex + 1).trim()
            if (remaining.isNotEmpty() && (remaining.startsWith(".") || remaining.startsWith("["))) {
                result = evaluateChainedMethodCall(result, remaining)
            }
        }

        return result
    }


    fun evaluateVariableProperty(expression: String): Any? {
        val parts = expression.split(".", limit = 2)
        if (parts.size != 2) return null

        val varName = parts[0]
        val propName = parts[1]
        val obj = scriptContext.getVariable(varName)

        return when (obj) {
            is List<*> -> when (propName) {
                "length" -> obj.size
                else -> null
            }
            is String -> when (propName) {
                "length" -> obj.length
                else -> null
            }
            is Map<*, *> -> obj[propName]
            else -> null
        }
    }


    fun evaluateChainedMethodCall(obj: Any?, chainExpr: String): Any? {
        if (obj == null) return null

        var currentObj = obj
        var remaining = chainExpr

        while (remaining.isNotEmpty() && (remaining.startsWith(".") || remaining.startsWith("["))) {
            if (remaining.startsWith("[")) {
                currentObj = evaluateArrayIndexAccess(currentObj, remaining)
                val closeBracket = findMatchingBracket(remaining, 0)
                remaining = if (closeBracket != -1 && closeBracket < remaining.length - 1) {
                    remaining.substring(closeBracket + 1).trim()
                } else {
                    ""
                }
            } else {

                remaining = remaining.substring(1)
                val parenIndex = remaining.indexOf('(')
                if (parenIndex == -1) {
                    return evaluatePropertyOnObject(currentObj, remaining)
                }

                val methodName = remaining.substring(0, parenIndex)
                val (argsString, endIndex) = extractMatchingParenContentWithIndex(remaining, parenIndex)
                val args = parseArguments(argsString)

                currentObj = executeMethodOnObject(currentObj, methodName, args)
                remaining = if (endIndex < remaining.length - 1) {
                    remaining.substring(endIndex + 1).trim()
                } else {
                    ""
                }
            }
        }

        return currentObj
    }

    private fun evaluateArrayIndexAccess(obj: Any?, remaining: String): Any? {
        val closeBracket = findMatchingBracket(remaining, 0)
        if (closeBracket == -1) return null

        val indexExpr = remaining.substring(1, closeBracket)
        val index = evaluateExpression(indexExpr)

        return when (obj) {
            is List<*> -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx] else null
            }
            is String -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx].toString() else null
            }
            is Map<*, *> -> {
                val key = index?.toString() ?: return null
                obj[key]
            }
            else -> null
        }
    }

    private fun evaluatePropertyOnObject(obj: Any?, propName: String): Any? {
        val methodName = propName.takeWhile { it.isLetterOrDigit() || it == '_' }
        return when (obj) {
            is List<*> -> if (methodName == "length") obj.size else null
            is String -> if (methodName == "length") obj.length else null
            is Map<*, *> -> obj[methodName]
            else -> null
        }
    }

    private fun executeMethodOnObject(obj: Any?, methodName: String, args: List<Any?>): Any? {
        return when (obj) {
            is List<*> -> ArrayMethodExecutor.execute(obj, methodName, args, executeArrowFunction)
            is String -> StringMethodExecutor.execute(obj, methodName, args)
            is DateObject -> DateMethodExecutor.execute(obj, methodName, args)
            is Number -> NumberMethodExecutor.execute(obj, methodName, args)
            else -> null
        }
    }


    fun findMatchingBracket(str: String, openIndex: Int): Int {
        var depth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in openIndex until str.length) {
            val char = str[i]
            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                }
                char == '[' && !inQuotes -> depth++
                char == ']' && !inQuotes -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }


    fun extractMatchingParenContentWithIndex(str: String, openParenIndex: Int): Pair<String, Int> {
        var depth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in openParenIndex until str.length) {
            val char = str[i]
            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                }
                char == '(' && !inQuotes -> depth++
                char == ')' && !inQuotes -> {
                    depth--
                    if (depth == 0) {
                        return Pair(str.substring(openParenIndex + 1, i), i)
                    }
                }
            }
        }
        return Pair(str.substring(openParenIndex + 1).trimEnd(')'), str.length - 1)
    }


    fun findMatchingParen(str: String, openParenIndex: Int): Int {
        var depth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in openParenIndex until str.length) {
            val char = str[i]
            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                }
                char == '(' && !inQuotes -> depth++
                char == ')' && !inQuotes -> {
                    depth--
                    if (depth == 0) {
                        return i
                    }
                }
            }
        }
        return -1
    }
}
