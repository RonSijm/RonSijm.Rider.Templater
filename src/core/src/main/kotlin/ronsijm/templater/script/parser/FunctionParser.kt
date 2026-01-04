package ronsijm.templater.script.parser

import ronsijm.templater.script.ScriptParser


object FunctionParser {


    data class FunctionInfo(
        val name: String,
        val parameters: List<String>,
        val body: List<String>,
        val fullDeclaration: String
    )


    fun extractFunction(statements: List<String>, startIndex: Int, scriptParser: ScriptParser): Pair<FunctionInfo, Int>? {
        val firstStmt = statements[startIndex]

        if (firstStmt.contains("{") && firstStmt.contains("}")) {
            return extractFunctionFromSingleStatement(firstStmt, startIndex, scriptParser)
        }

        return extractFunctionFromMultipleStatements(statements, startIndex, scriptParser)
    }
    private fun extractFunctionFromSingleStatement(
        stmt: String,
        startIndex: Int,
        scriptParser: ScriptParser
    ): Pair<FunctionInfo, Int>? {
        val funcStart = stmt.indexOf("function ")
        if (funcStart == -1) return null

        val afterFunction = stmt.substring(funcStart + 9).trim()
        val nameEnd = afterFunction.indexOf('(')
        if (nameEnd == -1) return null

        val name = afterFunction.substring(0, nameEnd).trim()

        val paramsStart = stmt.indexOf('(', funcStart)
        if (paramsStart == -1) return null

        val paramsEnd = BraceMatchingUtils.findMatchingParen(stmt, paramsStart)
        if (paramsEnd == -1) return null

        val paramsStr = stmt.substring(paramsStart + 1, paramsEnd).trim()
        val parameters = if (paramsStr.isEmpty()) {
            emptyList()
        } else {
            paramsStr.split(",").map { it.trim() }
        }

        val braceStart = stmt.indexOf('{', paramsEnd)
        if (braceStart == -1) return null

        val braceEnd = BraceMatchingUtils.findMatchingBrace(stmt, braceStart)
        if (braceEnd == -1) return null

        val bodyContent = stmt.substring(braceStart + 1, braceEnd).trim()
        val body = if (bodyContent.isEmpty()) {
            emptyList()
        } else {
            scriptParser.splitStatements(bodyContent)
        }

        return Pair(
            FunctionInfo(name, parameters, body, stmt),
            startIndex + 1
        )
    }


    private fun extractFunctionFromMultipleStatements(
        statements: List<String>,
        startIndex: Int,
        scriptParser: ScriptParser
    ): Pair<FunctionInfo, Int>? {
        val firstStmt = statements[startIndex]

        val funcStart = firstStmt.indexOf("function ")
        if (funcStart == -1) return null

        val afterFunction = firstStmt.substring(funcStart + 9).trim()
        val nameEnd = afterFunction.indexOf('(')
        if (nameEnd == -1) return null

        val name = afterFunction.substring(0, nameEnd).trim()

        val paramsStart = firstStmt.indexOf('(', funcStart)
        if (paramsStart == -1) return null

        val paramsEnd = BraceMatchingUtils.findMatchingParen(firstStmt, paramsStart)
        if (paramsEnd == -1) return null

        val paramsStr = firstStmt.substring(paramsStart + 1, paramsEnd).trim()
        val parameters = if (paramsStr.isEmpty()) {
            emptyList()
        } else {
            paramsStr.split(",").map { it.trim() }
        }


        val funcBody = mutableListOf<String>()
        var braceDepth = 0
        var foundOpenBrace = false
        var i = startIndex
        val fullDeclaration = StringBuilder()

        while (i < statements.size) {
            val stmt = statements[i]
            fullDeclaration.append(stmt).append("\n")

            for (char in stmt) {
                if (char == '{') {
                    foundOpenBrace = true
                    braceDepth++
                } else if (char == '}') {
                    braceDepth--
                }
            }

            if (foundOpenBrace && braceDepth == 0) {

                val bodyStart = fullDeclaration.indexOf("{") + 1
                val bodyEnd = fullDeclaration.lastIndexOf("}")
                if (bodyStart > 0 && bodyEnd > bodyStart) {
                    val bodyContent = fullDeclaration.substring(bodyStart, bodyEnd).trim()
                    if (bodyContent.isNotEmpty()) {
                        funcBody.addAll(scriptParser.splitStatements(bodyContent))
                    }
                }
                return Pair(
                    FunctionInfo(name, parameters, funcBody, fullDeclaration.toString().trim()),
                    i + 1
                )
            }
            i++
        }

        return null
    }
}

