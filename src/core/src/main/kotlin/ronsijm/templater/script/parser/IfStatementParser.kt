package ronsijm.templater.script.parser

import ronsijm.templater.script.ScriptParser
import ronsijm.templater.utils.Quadruple


object IfStatementParser {

    fun extractIfStatement(
        statements: List<String>,
        startIndex: Int,
        scriptParser: ScriptParser
    ): Quadruple<String, List<String>, List<Pair<String?, List<String>>>, Int>? {
        val firstStmt = statements[startIndex]


        if (firstStmt.contains("{") && firstStmt.contains("}")) {
            return extractIfStatementFromSingleStatement(firstStmt, startIndex, scriptParser)
        }

        val ifHeader = firstStmt
        val ifBody = mutableListOf<String>()
        val elseBranches = mutableListOf<Pair<String?, List<String>>>()
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


            val closingBraces = stmt.count { it == '}' }
            val openingBraces = stmt.count { it == '{' }

            if (stmt.trim().startsWith("}") && braceDepth - closingBraces == 0) {

                braceDepth = 0

                break
            }

            braceDepth += openingBraces
            braceDepth -= closingBraces

            if (braceDepth > 0 || !stmt.trim().endsWith("}")) {

                ifBody.add(stmt.trim())
            }
            i++
        }


        while (i < statements.size) {
            var stmt = statements[i].trim()


            if (stmt == "}" && i + 1 < statements.size) {
                val nextStmt = statements[i + 1].trim()
                if (nextStmt.startsWith("else")) {
                    i++
                    stmt = nextStmt
                }
            }

            if (stmt.startsWith("} else if") || stmt.startsWith("else if")) {

                val conditionPart = stmt.substringAfter("else if").trim()
                val condition = if (conditionPart.contains("(") && conditionPart.contains(")")) {
                    conditionPart.substringAfter("(").substringBefore(")")
                } else {
                    conditionPart.removeSurrounding("(", ")")
                }
                val elseIfBody = mutableListOf<String>()


                var j = i
                braceDepth = 0
                foundOpenBrace = false
                while (j < statements.size) {
                    val s = statements[j]
                    if (s.contains("{")) {
                        foundOpenBrace = true


                        braceDepth = 1
                        j++
                        break
                    }
                    j++
                }

                if (!foundOpenBrace) break


                while (j < statements.size && braceDepth > 0) {
                    val s = statements[j]


                    val closingBraces = s.count { it == '}' }
                    val openingBraces = s.count { it == '{' }

                    if (s.trim().startsWith("}") && braceDepth - closingBraces == 0) {


                        braceDepth = 0
                        break
                    }

                    braceDepth += openingBraces
                    braceDepth -= closingBraces

                    if (braceDepth > 0 || !s.trim().endsWith("}")) {

                        elseIfBody.add(s.trim())
                    }
                    j++
                }

                elseBranches.add(Pair(condition, elseIfBody.filter { it.isNotEmpty() }))
                i = j
            }
            else if (stmt.startsWith("} else") || stmt == "else") {

                val elseBody = mutableListOf<String>()


                var j = i
                braceDepth = 0
                foundOpenBrace = false
                while (j < statements.size) {
                    val s = statements[j]
                    if (s.contains("{")) {
                        foundOpenBrace = true


                        braceDepth = 1
                        j++
                        break
                    }
                    j++
                }

                if (!foundOpenBrace) break


                while (j < statements.size && braceDepth > 0) {
                    val s = statements[j]
                    braceDepth += s.count { it == '{' }
                    braceDepth -= s.count { it == '}' }

                    if (braceDepth > 0 || !s.trim().endsWith("}")) {

                        elseBody.add(s.trim())
                    }
                    j++
                }

                elseBranches.add(Pair(null, elseBody.filter { it.isNotEmpty() }))
                i = j
                break
            }
            else {


                if (i < statements.size && statements[i].trim().startsWith("}")) {
                    i++
                }
                break
            }
        }

        return Quadruple(ifHeader, ifBody.filter { it.isNotEmpty() }, elseBranches, i)
    }


    fun extractIfStatementFromSingleStatement(
        stmt: String,
        startIndex: Int = 0,
        scriptParser: ScriptParser
    ): Quadruple<String, List<String>, List<Pair<String?, List<String>>>, Int>? {

        val ifStart = stmt.indexOf("if")
        if (ifStart == -1) return null

        val condStart = stmt.indexOf('(', ifStart)
        if (condStart == -1) return null

        val condEnd = BraceMatchingUtils.findMatchingParen(stmt, condStart)
        if (condEnd == -1) return null

        val ifCondition = stmt.substring(condStart + 1, condEnd).trim()


        val ifBraceStart = stmt.indexOf('{', condEnd)
        if (ifBraceStart == -1) return null

        val ifBraceEnd = BraceMatchingUtils.findMatchingBrace(stmt, ifBraceStart)
        if (ifBraceEnd == -1) return null

        val ifBodyContent = stmt.substring(ifBraceStart + 1, ifBraceEnd).trim()
        val ifBody = if (ifBodyContent.isEmpty()) emptyList() else scriptParser.splitStatements(ifBodyContent)


        val elseBranches = mutableListOf<Pair<String?, List<String>>>()
        var remaining = stmt.substring(ifBraceEnd + 1).trim()

        while (remaining.startsWith("else")) {
            remaining = remaining.removePrefix("else").trim()

            if (remaining.startsWith("if")) {

                remaining = remaining.removePrefix("if").trim()
                val elseIfCondStart = remaining.indexOf('(')
                if (elseIfCondStart == -1) break

                val elseIfCondEnd = BraceMatchingUtils.findMatchingParen(remaining, elseIfCondStart)
                if (elseIfCondEnd == -1) break

                val elseIfCondition = remaining.substring(elseIfCondStart + 1, elseIfCondEnd).trim()

                val elseIfBraceStart = remaining.indexOf('{', elseIfCondEnd)
                if (elseIfBraceStart == -1) break

                val elseIfBraceEnd = BraceMatchingUtils.findMatchingBrace(remaining, elseIfBraceStart)
                if (elseIfBraceEnd == -1) break

                val elseIfBodyContent = remaining.substring(elseIfBraceStart + 1, elseIfBraceEnd).trim()
                val elseIfBody = if (elseIfBodyContent.isEmpty()) emptyList() else scriptParser.splitStatements(elseIfBodyContent)

                elseBranches.add(Pair(elseIfCondition, elseIfBody))
                remaining = remaining.substring(elseIfBraceEnd + 1).trim()
            } else if (remaining.startsWith("{")) {

                val elseBraceEnd = BraceMatchingUtils.findMatchingBrace(remaining, 0)
                if (elseBraceEnd == -1) break

                val elseBodyContent = remaining.substring(1, elseBraceEnd).trim()
                val elseBody = if (elseBodyContent.isEmpty()) emptyList() else scriptParser.splitStatements(elseBodyContent)

                elseBranches.add(Pair(null, elseBody))
                break
            } else {
                break
            }
        }

        return Quadruple("if ($ifCondition)", ifBody, elseBranches, startIndex + 1)
    }
}
