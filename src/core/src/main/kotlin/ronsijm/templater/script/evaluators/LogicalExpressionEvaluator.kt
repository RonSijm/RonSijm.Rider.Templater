package ronsijm.templater.script.evaluators

import ronsijm.templater.common.BooleanConvertible


class LogicalExpressionEvaluator(
    private val evaluateExpression: (String) -> Any?
) {


    private fun isQuoteEscaped(str: String, i: Int): Boolean {
        if (i == 0) return false
        var backslashCount = 0
        var j = i - 1
        while (j >= 0 && str[j] == '\\') {
            backslashCount++
            j--
        }
        return backslashCount % 2 == 1
    }


    fun findTopLevelLogicalOperator(expr: String): Pair<Int, String>? {


        val arrowIndex = findTopLevelArrowIndex(expr)


        val searchEnd = arrowIndex ?: expr.length

        var parenDepth = 0
        var bracketDepth = 0
        var braceDepth = 0
        var inQuotes = false
        var quoteChar = ' '


        var i = searchEnd - 1
        while (i >= 0) {
            val char = expr[i]
            val nextChar = if (i < expr.length - 1) expr[i + 1] else ' '

            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes && !isQuoteEscaped(expr, i) -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes && !isQuoteEscaped(expr, i) -> {
                    inQuotes = false
                }
                char == ')' && !inQuotes -> parenDepth++
                char == '(' && !inQuotes -> {
                    parenDepth--
                    if (parenDepth < 0) parenDepth = 0
                }
                char == ']' && !inQuotes -> bracketDepth++
                char == '[' && !inQuotes -> bracketDepth--
                char == '}' && !inQuotes -> braceDepth++
                char == '{' && !inQuotes -> braceDepth--
                !inQuotes && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0 -> {

                    if (char == '|' && nextChar == '|') {
                        return Pair(i, "||")
                    }

                    if (char == '&' && nextChar == '&') {
                        return Pair(i, "&&")
                    }
                }
            }
            i--
        }
        return null
    }


    private fun findTopLevelArrowIndex(expr: String): Int? {
        var parenDepth = 0
        var bracketDepth = 0
        var braceDepth = 0
        var inQuotes = false
        var quoteChar = ' '

        var i = 0
        while (i < expr.length - 1) {
            val char = expr[i]
            val nextChar = expr[i + 1]

            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes && !isQuoteEscaped(expr, i) -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes && !isQuoteEscaped(expr, i) -> {
                    inQuotes = false
                }
                char == '(' && !inQuotes -> parenDepth++
                char == ')' && !inQuotes -> parenDepth--
                char == '[' && !inQuotes -> bracketDepth++
                char == ']' && !inQuotes -> bracketDepth--
                char == '{' && !inQuotes -> braceDepth++
                char == '}' && !inQuotes -> braceDepth--
                char == '=' && nextChar == '>' && !inQuotes && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0 -> {
                    return i
                }
            }
            i++
        }
        return null
    }


    fun evaluateLogicalExpression(expr: String, opInfo: Pair<Int, String>): Any? {
        val (index, op) = opInfo
        val left = expr.substring(0, index).trim()
        val right = expr.substring(index + 2).trim()

        val leftValue = evaluateExpression(left)

        return when (op) {
            "&&" -> {

                if (isTruthy(leftValue)) {
                    evaluateExpression(right)
                } else {
                    leftValue
                }
            }
            "||" -> {

                if (isTruthy(leftValue)) {
                    leftValue
                } else {
                    evaluateExpression(right)
                }
            }
            else -> null
        }
    }


    fun isTruthy(value: Any?): Boolean {
        return when (value) {
            null -> false
            is BooleanConvertible -> value.booleanValue
            is Boolean -> value
            is Number -> {
                val d = value.toDouble()
                !d.isNaN() && d != 0.0
            }
            is String -> value.isNotEmpty()
            is List<*> -> true
            is Map<*, *> -> true
            else -> true
        }
    }
}
