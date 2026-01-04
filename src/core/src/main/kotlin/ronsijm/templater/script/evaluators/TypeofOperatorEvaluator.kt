package ronsijm.templater.script.evaluators

import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.UserFunction


class TypeofOperatorEvaluator(
    private val scriptContext: ScriptContext,
    private val expressionEvaluator: (String) -> Any?
) {
    companion object {

        private val TYPEOF_VAR_REGEX = Regex("^([a-zA-Z_][a-zA-Z0-9_]*)")
    }


    fun evaluateTypeofOperator(expression: String): String? {
        if (!expression.startsWith("typeof ")) {
            return null
        }

        val afterTypeof = expression.substring(7).trim()

        val varNameMatch = TYPEOF_VAR_REGEX.find(afterTypeof)
        if (varNameMatch != null) {
            val varName = varNameMatch.groupValues[1]
            val restOfExpr = afterTypeof.substring(varName.length).trim()


            val typeofResult = if (!scriptContext.hasVariable(varName) && varName != "true" && varName != "false" && varName != "null") {
                "undefined"
            } else {
                val value = expressionEvaluator(varName)
                getJsTypeof(value)
            }



            if (restOfExpr.isNotEmpty()) {

                return expressionEvaluator("\"$typeofResult\"$restOfExpr") as? String
            }
            return typeofResult
        }

        val value = expressionEvaluator(afterTypeof)
        return getJsTypeof(value)
    }


    private fun getJsTypeof(value: Any?): String {
        return when (value) {
            null -> "object"
            is Boolean -> "boolean"
            is Number -> "number"
            is String -> "string"
            is ArrowFunction -> "function"
            is UserFunction -> "function"
            is List<*> -> "object"
            is Map<*, *> -> "object"
            else -> "undefined"
        }
    }
}
