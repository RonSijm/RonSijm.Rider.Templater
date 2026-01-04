package ronsijm.templater.script.evaluators

import ronsijm.templater.script.compiler.BytecodeVM
import ronsijm.templater.script.compiler.ExpressionCache


class BytecodeEvaluationHelper(
    private val expressionCache: ExpressionCache,
    private val bytecodeVM: BytecodeVM
) {

    data class BytecodeResult(val value: Any?)


    fun tryBytecodeEvaluation(expression: String): BytecodeResult? {

        if (expression.length < 3) return null


        var hasOperator = false
        var hasBracket = false
        var hasDot = false
        var hasParenAfterIdent = false
        var prevWasIdent = false
        var prevIdentStart = -1
        var prevIdentEnd = -1
        var i = 0

        while (i < expression.length) {
            val c = expression[i]
            when {
                c == '+' || c == '-' || c == '*' || c == '/' || c == '%' ||
                c == '>' || c == '<' || c == '=' || c == '!' ||
                c == '&' || c == '|' || c == '^' || c == '?' -> hasOperator = true
                c == '[' -> hasBracket = true
                c == '.' -> hasDot = true
                c == '(' -> {
                    if (prevWasIdent) {
                        hasParenAfterIdent = true

                        val funcName = expression.substring(prevIdentStart, prevIdentEnd)

                        if (funcName != "Math" && !expression.substring(0, prevIdentStart).endsWith("Math.")) {
                            return null
                        }
                    }
                }
                c == '"' || c == '\'' || c == '`' -> return null
            }
            if (c.isLetterOrDigit() || c == '_') {
                if (!prevWasIdent) {
                    prevIdentStart = i
                }
                prevIdentEnd = i + 1
                prevWasIdent = true
            } else {
                prevWasIdent = false
            }
            i++
        }


        if (hasDot && !hasParenAfterIdent) return null


        if (!hasOperator && !hasBracket && !hasParenAfterIdent) return null


        if (expression.contains("new ") || expression.contains("typeof ") ||
            expression.contains("=>") || expression.contains("instanceof")) {
            return null
        }

        return try {
            val compiled = expressionCache.getOrCompile(expression)
            BytecodeResult(bytecodeVM.execute(compiled))
        } catch (e: Exception) {

            null
        }
    }
}
