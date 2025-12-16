package ronsijm.templater.script.evaluators

/**
 * Evaluates arithmetic and comparison expressions.
 * Single Responsibility: Only handles mathematical operations and comparisons.
 */
class ArithmeticEvaluator(
    private val expressionEvaluator: (String) -> Any?,
    private val literalParser: LiteralParser
) {

    /**
     * Find an arithmetic operator outside of quotes/parentheses.
     * Returns the operator and its position, or null if not found.
     * Checks operators in order of precedence (lowest first): +, -, *, /
     */
    fun findArithmeticOperator(expression: String): Pair<Char, Int>? {
        val state = ParserState()

        // First pass: look for + or - (lowest precedence)
        for (i in expression.indices) {
            val char = expression[i]
            state.updateForChar(char)
            if ((char == '+' || char == '-') && state.isAtTopLevel() && i > 0) {
                val prevNonSpace = expression.substring(0, i).trimEnd().lastOrNull()
                if (prevNonSpace != null && prevNonSpace !in listOf('(', ',', '=', '+', '-', '*', '/')) {
                    return Pair(char, i)
                }
            }
        }

        // Reset for second pass
        state.reset()

        // Second pass: look for * or / (higher precedence)
        for (i in expression.indices) {
            val char = expression[i]
            state.updateForChar(char)
            if ((char == '*' || char == '/') && state.isAtTopLevel()) {
                return Pair(char, i)
            }
        }

        return null
    }

    /**
     * Evaluate an arithmetic expression. Handles +, -, *, / operations.
     */
    fun evaluateArithmetic(expression: String, operatorInfo: Pair<Char, Int>): Any? {
        val (operator, position) = operatorInfo
        val leftExpr = expression.substring(0, position).trim()
        val rightExpr = expression.substring(position + 1).trim()

        val leftValue = expressionEvaluator(leftExpr)
        val rightValue = expressionEvaluator(rightExpr)

        val leftNum = literalParser.toNumber(leftValue)
        val rightNum = literalParser.toNumber(rightValue)

        if (leftNum != null && rightNum != null) {
            return when (operator) {
                '+' -> if (leftNum is Int && rightNum is Int) leftNum + rightNum
                       else leftNum.toDouble() + rightNum.toDouble()
                '-' -> if (leftNum is Int && rightNum is Int) leftNum - rightNum
                       else leftNum.toDouble() - rightNum.toDouble()
                '*' -> if (leftNum is Int && rightNum is Int) leftNum * rightNum
                       else leftNum.toDouble() * rightNum.toDouble()
                '/' -> if (leftNum is Int && rightNum is Int && rightNum != 0 && leftNum % rightNum == 0) {
                    leftNum / rightNum
                } else {
                    leftNum.toDouble() / rightNum.toDouble()
                }
                else -> null
            }
        }

        // If + and at least one is a string, do concatenation
        if (operator == '+') {
            return (leftValue?.toString() ?: "") + (rightValue?.toString() ?: "")
        }

        return null
    }

    /**
     * Find a comparison operator outside of quotes/parentheses.
     */
    fun findComparisonOperator(expression: String): Pair<String, Int>? {
        val state = ParserState()

        // Check for two-character operators first: >=, <=, ==, !=
        for (i in 0 until expression.length - 1) {
            val char = expression[i]
            val nextChar = expression[i + 1]
            state.updateForChar(char)
            if (state.isAtTopLevel()) {
                val twoChar = "$char$nextChar"
                if (twoChar in listOf(">=", "<=", "==", "!=")) {
                    return Pair(twoChar, i)
                }
            }
        }

        // Reset and check for single-character operators: >, <
        state.reset()
        for (i in expression.indices) {
            val char = expression[i]
            state.updateForChar(char)
            if ((char == '>' || char == '<') && state.isAtTopLevel()) {
                val prevChar = if (i > 0) expression[i - 1] else ' '
                val nextChar = if (i < expression.length - 1) expression[i + 1] else ' '
                if (prevChar != '=' && nextChar != '=') {
                    return Pair(char.toString(), i)
                }
            }
        }

        return null
    }

    /**
     * Evaluate a comparison expression. Handles >, <, >=, <=, ==, != operations.
     */
    fun evaluateComparison(expression: String, operatorInfo: Pair<String, Int>): Boolean {
        val (operator, position) = operatorInfo
        val leftExpr = expression.substring(0, position).trim()
        val rightExpr = expression.substring(position + operator.length).trim()

        val leftValue = expressionEvaluator(leftExpr)
        val rightValue = expressionEvaluator(rightExpr)

        val leftNum = literalParser.toNumber(leftValue)
        val rightNum = literalParser.toNumber(rightValue)

        if (leftNum != null && rightNum != null) {
            val leftDouble = leftNum.toDouble()
            val rightDouble = rightNum.toDouble()
            return when (operator) {
                ">" -> leftDouble > rightDouble
                "<" -> leftDouble < rightDouble
                ">=" -> leftDouble >= rightDouble
                "<=" -> leftDouble <= rightDouble
                "==" -> leftDouble == rightDouble
                "!=" -> leftDouble != rightDouble
                else -> false
            }
        }

        // String/object comparison
        return when (operator) {
            "==" -> leftValue == rightValue || leftValue?.toString() == rightValue?.toString()
            "!=" -> leftValue != rightValue && leftValue?.toString() != rightValue?.toString()
            else -> false
        }
    }
}

