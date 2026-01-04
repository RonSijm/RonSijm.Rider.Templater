package ronsijm.templater.script.evaluators

import ronsijm.templater.script.profiling.ProfilingContext


class ArithmeticEvaluator(
    private val expressionEvaluator: (String) -> Any?,
    private val literalParser: LiteralParser
) {
    companion object {

        val profiler get() = ProfilingContext.arithmeticProfiler

        fun resetProfiling() {
            ProfilingContext.arithmeticProfiler.reset()
        }

        fun getProfilingReport(): String {
            return ProfilingContext.arithmeticProfiler.getReport()
        }
    }


    fun findArithmeticOperator(expression: String): Pair<Char, Int>? {
        val state = ParserState()


        var addSubOp: Pair<Char, Int>? = null
        var mulDivOp: Pair<Char, Int>? = null
        var bitwiseOp: Pair<Char, Int>? = null

        var i = 0
        while (i < expression.length) {
            val char = expression[i]
            state.updateForChar(char)

            if (state.isAtTopLevel()) {

                if (char == '>' && i + 2 < expression.length && expression[i + 1] == '>' && expression[i + 2] == '>') {
                    bitwiseOp = Pair('U', i)
                    i += 3
                    continue
                }
                if (char == '>' && i + 1 < expression.length && expression[i + 1] == '>' &&
                    (i + 2 >= expression.length || expression[i + 2] != '>')) {
                    bitwiseOp = Pair('R', i)
                    i += 2
                    continue
                }
                if (char == '<' && i + 1 < expression.length && expression[i + 1] == '<') {
                    bitwiseOp = Pair('L', i)
                    i += 2
                    continue
                }


                if ((char == '+' || char == '-') && i > 0) {
                    val prevNonSpace = expression.substring(0, i).trimEnd().lastOrNull()
                    if (prevNonSpace != null && prevNonSpace !in listOf('(', ',', '=', '+', '-', '*', '/', '%')) {
                        addSubOp = Pair(char, i)
                    }
                }


                if (char == '*' || char == '/' || char == '%') {
                    mulDivOp = Pair(char, i)
                }
            }
            i++
        }


        return bitwiseOp ?: addSubOp ?: mulDivOp
    }


    fun evaluateArithmetic(expression: String, operatorInfo: Pair<Char, Int>): Any? {
        val (operator, position) = operatorInfo


        val (leftExpr, rightExpr) = when (operator) {
            'U' -> {
                expression.substring(0, position).trim() to expression.substring(position + 3).trim()
            }
            'R', 'L' -> {
                expression.substring(0, position).trim() to expression.substring(position + 2).trim()
            }
            else -> {
                expression.substring(0, position).trim() to expression.substring(position + 1).trim()
            }
        }

        val leftValue = ProfilingContext.profiled(profiler::recursiveLeftEvalTime, profiler::recursiveLeftEvalCount) {
            expressionEvaluator(leftExpr)
        }

        val rightValue = ProfilingContext.profiled(profiler::recursiveRightEvalTime, profiler::recursiveRightEvalCount) {
            expressionEvaluator(rightExpr)
        }



        if (operator == '+' && (leftValue is String || rightValue is String)) {
            return (leftValue?.toString() ?: "") + (rightValue?.toString() ?: "")
        }

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
                '%' -> if (leftNum is Int && rightNum is Int) leftNum % rightNum
                       else leftNum.toDouble() % rightNum.toDouble()
                'U' -> (leftNum.toLong() and 0xFFFFFFFFL).ushr(rightNum.toInt()).toInt()
                'R' -> leftNum.toInt() shr rightNum.toInt()
                'L' -> leftNum.toInt() shl rightNum.toInt()
                else -> null
            }
        }


        if (operator == '+') {
            return (leftValue?.toString() ?: "") + (rightValue?.toString() ?: "")
        }

        return null
    }


    fun findComparisonOperator(expression: String): Pair<String, Int>? {
        val state = ParserState()

        var i = 0
        while (i < expression.length) {
            val char = expression[i]
            state.updateForChar(char)

            if (state.isAtTopLevel()) {
                val nextChar = if (i + 1 < expression.length) expression[i + 1] else ' '
                val nextNextChar = if (i + 2 < expression.length) expression[i + 2] else ' '
                val prevChar = if (i > 0) expression[i - 1] else ' '


                if ((char == '=' || char == '!') && nextChar == '=' && nextNextChar == '=') {
                    return Pair("$char$nextChar$nextNextChar", i)
                }


                if (char == '>' && nextChar == '=') {
                    return Pair(">=", i)
                }
                if (char == '<' && nextChar == '=') {
                    return Pair("<=", i)
                }
                if (char == '=' && nextChar == '=' && nextNextChar != '=') {
                    return Pair("==", i)
                }
                if (char == '!' && nextChar == '=' && nextNextChar != '=') {
                    return Pair("!=", i)
                }



                if (char == '>' && nextChar != '=' && nextChar != '>' && prevChar != '>') {
                    return Pair(">", i)
                }
                if (char == '<' && nextChar != '=' && nextChar != '<') {
                    return Pair("<", i)
                }
            }
            i++
        }

        return null
    }


    fun evaluateComparison(expression: String, operatorInfo: Pair<String, Int>): Boolean {
        val (operator, position) = operatorInfo
        val leftExpr = expression.substring(0, position).trim()
        val rightExpr = expression.substring(position + operator.length).trim()

        val leftValue = expressionEvaluator(leftExpr)
        val rightValue = expressionEvaluator(rightExpr)


        if (operator == "===" || operator == "!==") {
            val strictEqual = when {
                leftValue == null && rightValue == null -> true
                leftValue == null || rightValue == null -> false
                leftValue is Number && rightValue is Number -> leftValue.toDouble() == rightValue.toDouble()
                leftValue::class == rightValue::class -> leftValue == rightValue
                else -> false
            }
            return if (operator == "===") strictEqual else !strictEqual
        }

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


        val leftStr = stripQuotes(leftValue?.toString())
        val rightStr = stripQuotes(rightValue?.toString())

        return when (operator) {
            "==" -> leftStr == rightStr
            "!=" -> leftStr != rightStr
            else -> false
        }
    }


    private fun stripQuotes(value: String?): String? {
        if (value == null) return null
        val trimmed = value.trim()
        if (trimmed.length >= 2) {
            if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
                (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
                return trimmed.substring(1, trimmed.length - 1)
            }
        }
        return trimmed
    }
}
