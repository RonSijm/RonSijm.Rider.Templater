package ronsijm.templater.script.executors

import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.UserFunction


class FunctionExecutor(
    private val scriptContext: ScriptContext,
    private val bodyExecutor: (String) -> Unit
) {


    fun executeFunctionDeclaration(statement: String) {
        val afterFunction = statement.removePrefix("function ").trim()
        val nameEnd = afterFunction.indexOf('(')
        if (nameEnd == -1) return

        val name = afterFunction.substring(0, nameEnd).trim()
        val paramsStart = nameEnd + 1
        val paramsEnd = afterFunction.indexOf(')', paramsStart)
        if (paramsEnd == -1) return

        val paramsStr = afterFunction.substring(paramsStart, paramsEnd).trim()
        val parameters = if (paramsStr.isEmpty()) {
            emptyList()
        } else {
            paramsStr.split(",").map { it.trim() }
        }

        val bodyStart = afterFunction.indexOf('{', paramsEnd)
        if (bodyStart == -1) return

        val bodyEnd = findMatchingBrace(afterFunction, bodyStart)
        if (bodyEnd == -1) return

        val body = afterFunction.substring(bodyStart + 1, bodyEnd).trim()

        val userFunction = UserFunction(
            name = name,
            parameters = parameters,
            body = body,
            executor = { bodyCode -> bodyExecutor(bodyCode) }
        )

        scriptContext.setVariable(name, userFunction)
    }


    private fun findMatchingBrace(str: String, openIndex: Int): Int {
        var depth = 1
        var i = openIndex + 1
        while (i < str.length && depth > 0) {
            when (str[i]) {
                '{' -> depth++
                '}' -> depth--
            }
            i++
        }
        return if (depth == 0) i - 1 else -1
    }
}

