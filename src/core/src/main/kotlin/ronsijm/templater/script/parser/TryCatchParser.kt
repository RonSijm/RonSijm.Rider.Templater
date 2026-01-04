package ronsijm.templater.script.parser


object TryCatchParser {
    data class TryCatchInfo(
        val tryBody: List<String>,
        val catchVarName: String?,
        val catchBody: List<String>,
        val finallyBody: List<String>
    )


    fun extractTryCatch(statements: List<String>, startIndex: Int): Pair<TryCatchInfo, Int>? {
        val tryBody = mutableListOf<String>()
        var catchVarName: String? = null
        val catchBody = mutableListOf<String>()
        val finallyBody = mutableListOf<String>()
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
                tryBody.add(stmt.trim())
            }
            i++
        }


        while (i < statements.size) {
            var stmt = statements[i].trim()


            if (stmt == "}" && i + 1 < statements.size) {
                val nextStmt = statements[i + 1].trim()
                if (nextStmt.startsWith("catch")) {
                    i++
                    stmt = nextStmt
                }
            }

            if (stmt.startsWith("} catch") || stmt.startsWith("catch")) {

                val catchPart = stmt.substringAfter("catch").trim()
                if (catchPart.contains("(") && catchPart.contains(")")) {
                    catchVarName = catchPart.substringAfter("(").substringBefore(")").trim()
                }


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
                        catchBody.add(s.trim())
                    }
                    j++
                }

                i = j
                break
            } else {

                break
            }
        }


        while (i < statements.size) {
            var stmt = statements[i].trim()

            if (stmt == "}" && i + 1 < statements.size) {
                val nextStmt = statements[i + 1].trim()
                if (nextStmt.startsWith("finally")) {
                    i++
                    stmt = nextStmt
                }
            }

            if (stmt.startsWith("} finally") || stmt.startsWith("finally")) {
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
                        finallyBody.add(s.trim())
                    }
                    j++
                }

                i = j
                break
            } else {
                if (i < statements.size && statements[i].trim().startsWith("}")) {
                    i++
                }
                break
            }
        }

        return Pair(
            TryCatchInfo(
                tryBody.filter { it.isNotEmpty() },
                catchVarName,
                catchBody.filter { it.isNotEmpty() },
                finallyBody.filter { it.isNotEmpty() }
            ),
            i
        )
    }
}
