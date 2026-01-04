package ronsijm.templater.script.evaluators

import ronsijm.templater.common.ParserConstants
import ronsijm.templater.script.builtins.GlobalFunctions


class NewExpressionEvaluator(
    private val propertyAccessEvaluator: PropertyAccessEvaluator,
    private val parseArguments: (String) -> List<Any?>
) {


    fun tryEvaluate(expression: String): Any? {
        return when {
            expression.startsWith(ParserConstants.NEW_DATE_PREFIX) -> evaluateNewDate(expression)
            expression.startsWith(ParserConstants.NEW_ARRAY_PREFIX) -> evaluateNewArray(expression)
            else -> null
        }
    }


    fun canEvaluate(expression: String): Boolean {
        return expression.startsWith(ParserConstants.NEW_DATE_PREFIX) ||
               expression.startsWith(ParserConstants.NEW_ARRAY_PREFIX)
    }

    private fun evaluateNewDate(expression: String): Any? {
        val dateEndIndex = findClosingParen(expression, ParserConstants.NEW_DATE_PREFIX_LENGTH)
        var result: Any? = DateObject()

        if (dateEndIndex + 1 < expression.length && expression[dateEndIndex + 1] == '.') {
            result = propertyAccessEvaluator.evaluateChainedMethodCall(result, expression.substring(dateEndIndex + 1))
        }

        return result
    }

    private fun evaluateNewArray(expression: String): Any? {
        val arrayEndIndex = findClosingParen(expression, ParserConstants.NEW_ARRAY_PREFIX_LENGTH)

        val argsString = expression.substring(ParserConstants.NEW_ARRAY_PREFIX_LENGTH, arrayEndIndex)
        val args = if (argsString.isBlank()) emptyList() else parseArguments(argsString)
        var result: Any? = GlobalFunctions.execute("Array", args)

        if (arrayEndIndex + 1 < expression.length && expression[arrayEndIndex + 1] == '.') {
            result = propertyAccessEvaluator.evaluateChainedMethodCall(result, expression.substring(arrayEndIndex + 1))
        }

        return result
    }


    private fun findClosingParen(expression: String, startIndex: Int): Int {
        var parenDepth = 0
        var endIndex = startIndex

        for (i in startIndex until expression.length) {
            when (expression[i]) {
                '(' -> parenDepth++
                ')' -> {
                    if (parenDepth == 0) {
                        endIndex = i
                        break
                    }
                    parenDepth--
                }
            }
        }

        return endIndex
    }
}
