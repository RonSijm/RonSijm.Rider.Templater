package ronsijm.templater.script.parser

import ronsijm.templater.script.ScriptParser


object ForLoopParser {
    private val VAR_DECL_REGEX = Regex("^(let|const|var)\\s+")


    fun extractForLoop(statements: List<String>, startIndex: Int, scriptParser: ScriptParser): Triple<String, List<String>, Int>? {
        val firstStmt = statements[startIndex]



        if (firstStmt.contains("{") && firstStmt.contains("}")) {
            val braceStart = firstStmt.indexOf('{')

            val braceEnd = BraceMatchingUtils.findMatchingBrace(firstStmt, braceStart)
            if (braceStart != -1 && braceEnd > braceStart) {
                val header = firstStmt.substring(0, braceStart).trim()
                val bodyContent = firstStmt.substring(braceStart + 1, braceEnd).trim()
                val body = if (bodyContent.isEmpty()) {
                    emptyList()
                } else {
                    scriptParser.splitStatements(bodyContent)
                }
                return Triple(header, body, startIndex + 1)
            }
        }



        if (!firstStmt.contains("{")) {
            val bracelessBody = extractBracelessForLoopBody(firstStmt)
            if (bracelessBody != null) {
                val (header, body) = bracelessBody
                return Triple(header, listOf(body), startIndex + 1)
            }
        }


        val loopHeader = firstStmt
        val loopBody = mutableListOf<String>()
        var braceDepth = 0
        var foundOpenBrace = false
        var i = startIndex


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

        return Triple(loopHeader, loopBody.filter { it.isNotEmpty() }, i)
    }


    private fun extractBracelessForLoopBody(stmt: String): Pair<String, String>? {

        val forStart = stmt.indexOf("for")
        if (forStart == -1) return null

        val parenStart = stmt.indexOf('(', forStart)
        if (parenStart == -1) return null


        val parenEnd = BraceMatchingUtils.findMatchingParen(stmt, parenStart)
        if (parenEnd == -1 || parenEnd >= stmt.length - 1) return null


        val header = stmt.substring(0, parenEnd + 1).trim()


        val body = stmt.substring(parenEnd + 1).trim().trimEnd(';')

        if (body.isEmpty()) return null

        return Pair(header, body)
    }


    fun parseForLoopHeader(loopHeader: String): ForLoopInfo? {
        val forContent = loopHeader.substringAfter("(").substringBeforeLast(")")
        val parts = forContent.split(";").map { it.trim() }

        if (parts.size != 3) return null


        val initPart = parts[0].replace(VAR_DECL_REGEX, "")
        val varName = initPart.substringBefore("=").trim()
        val startExpr = initPart.substringAfter("=").trim()


        val conditionPart = parts[1]
        val conditionOperator = when {
            conditionPart.contains("<=") -> "<="
            conditionPart.contains(">=") -> ">="
            conditionPart.contains("<") -> "<"
            conditionPart.contains(">") -> ">"
            else -> return null
        }
        val endExpr = conditionPart.substringAfter(conditionOperator).trim()


        val incrementPart = parts[2]
        val isIncrement = incrementPart.contains("++")

        return ForLoopInfo(varName, startExpr, endExpr, conditionOperator, isIncrement)
    }


    fun parseForOfLoopHeader(loopHeader: String): ForOfLoopInfo? {
        val forContent = loopHeader.substringAfter("(").substringBeforeLast(")")


        if (!forContent.contains(" of ")) return null

        val parts = forContent.split(" of ", limit = 2).map { it.trim() }
        if (parts.size != 2) return null


        val varDecl = parts[0].replace(VAR_DECL_REGEX, "").trim()
        val arrayExpr = parts[1].trim()

        return ForOfLoopInfo(varDecl, arrayExpr)
    }


    fun isForOfLoop(loopHeader: String): Boolean {
        val forContent = loopHeader.substringAfter("(").substringBeforeLast(")")
        return forContent.contains(" of ")
    }


    data class ForLoopInfo(
        val varName: String,
        val startExpr: String,
        val endExpr: String,
        val conditionOperator: String,
        val isIncrement: Boolean
    )


    data class ForOfLoopInfo(
        val varName: String,
        val arrayExpression: String
    )
}