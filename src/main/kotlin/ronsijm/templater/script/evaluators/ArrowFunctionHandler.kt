package ronsijm.templater.script.evaluators

import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.ScriptLexer
import ronsijm.templater.utils.Logging

/**
 * Handles parsing and execution of arrow functions.
 * Single Responsibility: Only handles arrow function operations.
 */
class ArrowFunctionHandler(
    private val scriptContext: ScriptContext,
    private val expressionEvaluator: (String) -> Any?,
    private val statementExecutorProvider: (() -> ((String) -> Unit)?)? = null
) {
    companion object {
        private val LOG = Logging.getLogger<ArrowFunctionHandler>()
    }

    private val lexer = ScriptLexer()

    /**
     * Check if expression is a top-level arrow function definition.
     * Returns true only if => is at the top level (not inside parentheses, brackets, or quotes).
     */
    fun isTopLevelArrowFunction(expression: String): Boolean {
        if (!expression.contains("=>")) return false

        var parenDepth = 0
        var bracketDepth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in 0 until expression.length - 1) {
            val char = expression[i]
            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                }
                char == '(' && !inQuotes -> parenDepth++
                char == ')' && !inQuotes -> parenDepth--
                char == '[' && !inQuotes -> bracketDepth++
                char == ']' && !inQuotes -> bracketDepth--
                char == '=' && expression[i + 1] == '>' && !inQuotes && parenDepth == 0 && bracketDepth == 0 -> {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Parse an arrow function expression into an ArrowFunction object.
     */
    fun parseArrowFunction(expression: String): ArrowFunction {
        val trimmed = expression.trim()

        // Pattern 1: (params) => body
        if (trimmed.startsWith("(")) {
            val closeParenIndex = findMatchingParen(trimmed, 0)
            val paramsStr = trimmed.substring(1, closeParenIndex)
            val params = if (paramsStr.isBlank()) emptyList()
                         else paramsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val afterParen = trimmed.substring(closeParenIndex + 1).trimStart()
            val bodyPart = afterParen.removePrefix("=>").trim()
            val isExpression = !bodyPart.startsWith("{")
            val body = if (isExpression) bodyPart else bodyPart.removeSurrounding("{", "}").trim()
            return ArrowFunction(params, body, isExpression)
        }

        // Pattern 2: param => body
        val arrowIndex = trimmed.indexOf("=>")
        val param = trimmed.substring(0, arrowIndex).trim()
        val bodyPart = trimmed.substring(arrowIndex + 2).trim()
        val isExpression = !bodyPart.startsWith("{")
        val body = if (isExpression) bodyPart else bodyPart.removeSurrounding("{", "}").trim()
        return ArrowFunction(listOf(param), body, isExpression)
    }

    /**
     * Execute an arrow function with the given arguments.
     */
    fun executeArrowFunction(fn: ArrowFunction, args: List<Any?>): Any? {
        LOG?.debug("executeArrowFunction: params=${fn.parameters}, args=$args, isExpression=${fn.isExpression}, body='${fn.body}'")

        // Save current variable values
        val savedValues = fn.parameters.map { it to scriptContext.getVariable(it) }

        try {
            // Bind parameters to arguments
            fn.parameters.forEachIndexed { index, param ->
                val argValue = args.getOrNull(index)
                LOG?.debug("executeArrowFunction: binding param '$param' = $argValue")
                scriptContext.setVariable(param, argValue)
            }

            // Evaluate the body
            val result = if (fn.isExpression) {
                LOG?.debug("executeArrowFunction: evaluating expression body")
                expressionEvaluator(fn.body)
            } else {
                // Block body: execute multiple statements
                LOG?.debug("executeArrowFunction: executing block body")
                executeBlockBody(fn.body)
            }
            LOG?.debug("executeArrowFunction: result=$result")
            return result
        } finally {
            // Restore previous variable values
            savedValues.forEach { (param, value) ->
                if (value != null) {
                    scriptContext.setVariable(param, value)
                } else {
                    scriptContext.removeVariable(param)
                }
            }
        }
    }

    /**
     * Execute a block body with multiple statements.
     * Returns the value from a return statement if present, otherwise null.
     */
    private fun executeBlockBody(body: String): Any? {
        LOG?.debug("executeBlockBody: body='$body'")

        // Get the statement executor dynamically
        val statementExecutor = statementExecutorProvider?.invoke()
        LOG?.debug("executeBlockBody: statementExecutor=${if (statementExecutor != null) "present" else "null"}")

        // If we have a statement executor, use it for proper multi-statement handling
        if (statementExecutor != null) {
            val statements = lexer.smartSplitStatements(body)
            LOG?.debug("executeBlockBody: split into ${statements.size} statements: $statements")
            for (statement in statements) {
                val trimmed = statement.trim()
                if (trimmed.isEmpty()) continue

                LOG?.debug("executeBlockBody: executing statement='$trimmed'")

                // Check for return statement
                if (trimmed.startsWith("return ") || trimmed.startsWith("return;") || trimmed == "return") {
                    val returnValue = trimmed.removePrefix("return").trim().removeSuffix(";").trim()
                    return if (returnValue.isEmpty()) null else expressionEvaluator(returnValue)
                }

                // Execute the statement
                statementExecutor.invoke(trimmed)
            }
            return null
        }

        // Fallback: look for return statement (legacy behavior)
        LOG?.debug("executeBlockBody: using fallback (no statement executor)")
        val returnMatch = Regex("return\\s+(.+)").find(body)
        if (returnMatch != null) {
            return expressionEvaluator(returnMatch.groupValues[1].trim())
        }
        return expressionEvaluator(body)
    }

    private fun findMatchingParen(str: String, openIndex: Int): Int {
        var depth = 0
        for (i in openIndex until str.length) {
            when (str[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }
}

