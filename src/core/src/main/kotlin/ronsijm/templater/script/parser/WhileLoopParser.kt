package ronsijm.templater.script.parser

import ronsijm.templater.script.ScriptParser


object WhileLoopParser {

    fun extractWhileLoop(statements: List<String>, startIndex: Int, scriptParser: ScriptParser): Triple<String, List<String>, Int>? {
        val firstStmt = statements[startIndex]


        if (firstStmt.contains("{") && firstStmt.contains("}")) {
            val braceStart = firstStmt.indexOf('{')
            val braceEnd = BraceMatchingUtils.findMatchingBrace(firstStmt, braceStart)
            if (braceStart != -1 && braceEnd > braceStart) {

                val condStart = firstStmt.indexOf('(')
                val condEnd = BraceMatchingUtils.findMatchingParen(firstStmt, condStart)
                if (condStart != -1 && condEnd > condStart && condEnd < braceStart) {
                    val condition = firstStmt.substring(condStart + 1, condEnd).trim()
                    val bodyContent = firstStmt.substring(braceStart + 1, braceEnd).trim()
                    val body = if (bodyContent.isEmpty()) emptyList() else scriptParser.splitStatements(bodyContent)
                    return Triple(condition, body, startIndex + 1)
                }
            }
        }

        val whileHeader = firstStmt
        val loopBody = mutableListOf<String>()
        var braceDepth = 0
        var foundOpenBrace = false
        var i = startIndex


        val condition = whileHeader.substringAfter("(").substringBeforeLast(")").trim()
        if (condition.isEmpty()) return null


        while (i < statements.size) {
            val stmt = statements[i]
            if (stmt.contains("{")) {
                foundOpenBrace = true
                braceDepth += stmt.count { it == '{' }
                braceDepth -= stmt.count { it == '}' }
                i++
                break
            }
            i++
        }

        if (!foundOpenBrace) return null


        while (i < statements.size && braceDepth > 0) {
            val stmt = statements[i]
            braceDepth += stmt.count { it == '{' }
            braceDepth -= stmt.count { it == '}' }

            if (braceDepth > 0 || !stmt.trim().endsWith("}")) {
                loopBody.add(stmt.trim())
            }
            i++
        }

        return Triple(condition, loopBody.filter { it.isNotEmpty() }, i)
    }
}
