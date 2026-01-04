package ronsijm.templater.script.evaluators


class TernaryOperatorEvaluator(
    private val expressionEvaluator: (String) -> Any?,
    private val isTruthyChecker: (Any?) -> Boolean
) {

    data class TernaryResult(val value: Any?)


    fun evaluateTernaryOperator(expression: String): TernaryResult? {

        var depth = 0
        var inString = false
        var stringChar = ' '
        var questionIndex = -1

        for (i in expression.indices) {
            val char = expression[i]


            if ((char == '"' || char == '\'' || char == '`') && !isQuoteEscaped(expression, i)) {
                if (!inString) {
                    inString = true
                    stringChar = char
                } else if (char == stringChar) {
                    inString = false
                }
            }

            if (!inString) {
                when (char) {
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                    '?' -> if (depth == 0) {
                        questionIndex = i
                        break
                    }
                }
            }
        }

        if (questionIndex == -1) return null


        depth = 0
        inString = false
        var colonIndex = -1

        for (i in (questionIndex + 1) until expression.length) {
            val char = expression[i]


            if ((char == '"' || char == '\'' || char == '`') && !isQuoteEscaped(expression, i)) {
                if (!inString) {
                    inString = true
                    stringChar = char
                } else if (char == stringChar) {
                    inString = false
                }
            }

            if (!inString) {
                when (char) {
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                    ':' -> if (depth == 0) {
                        colonIndex = i
                        break
                    }
                }
            }
        }

        if (colonIndex == -1) return null

        val condition = expression.substring(0, questionIndex).trim()
        val trueExpr = expression.substring(questionIndex + 1, colonIndex).trim()
        val falseExpr = expression.substring(colonIndex + 1).trim()

        val conditionValue = expressionEvaluator(condition)
        val isTruthy = isTruthyChecker(conditionValue)

        return TernaryResult(if (isTruthy) expressionEvaluator(trueExpr) else expressionEvaluator(falseExpr))
    }


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
}
