package ronsijm.templater.script

/** Parses for loops and if/else statements from script blocks */
class ScriptParser {

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    /** Returns (loopHeader, loopBody, nextStatementIndex) or null */
    fun extractForLoop(statements: List<String>, startIndex: Int): Triple<String, List<String>, Int>? {
        val loopHeader = statements[startIndex]
        val loopBody = mutableListOf<String>()
        var braceDepth = 0
        var foundOpenBrace = false
        var i = startIndex

        // Find the opening brace
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

        // Collect loop body until closing brace
        while (i < statements.size && braceDepth > 0) {
            val stmt = statements[i]
            braceDepth += stmt.count { it == '{' }
            braceDepth -= stmt.count { it == '}' }

            if (braceDepth > 0 || !stmt.trim().endsWith("}")) {
                // Don't remove braces - they might be part of template literals like ${i}
                loopBody.add(stmt.trim())
            }
            i++
        }

        return Triple(loopHeader, loopBody.filter { it.isNotEmpty() }, i)
    }

    /** Parses "for (let i = 1; i <= 5; i++)" into its components */
    fun parseForLoopHeader(loopHeader: String): ForLoopInfo? {
        val forContent = loopHeader.substringAfter("(").substringBeforeLast(")")
        val parts = forContent.split(";").map { it.trim() }

        if (parts.size != 3) return null

        // Parse initialization: let i = 1
        val initPart = parts[0].replace(Regex("^(let|const|var)\\s+"), "")
        val varName = initPart.substringBefore("=").trim()
        val startValue = initPart.substringAfter("=").trim().toIntOrNull() ?: return null

        // Parse condition: i <= 5
        val conditionPart = parts[1]
        val conditionOperator = when {
            conditionPart.contains("<=") -> "<="
            conditionPart.contains(">=") -> ">="
            conditionPart.contains("<") -> "<"
            conditionPart.contains(">") -> ">"
            else -> return null
        }
        val endValue = conditionPart.substringAfter(conditionOperator).trim().toIntOrNull() ?: return null

        // Parse increment: i++ or i--
        val incrementPart = parts[2]
        val isIncrement = incrementPart.contains("++")

        return ForLoopInfo(varName, startValue, endValue, conditionOperator, isIncrement)
    }

    /** Parses "for (const item of array)" or "for (let item of items)" */
    fun parseForOfLoopHeader(loopHeader: String): ForOfLoopInfo? {
        val forContent = loopHeader.substringAfter("(").substringBeforeLast(")")

        // Check if it's a for-of loop
        if (!forContent.contains(" of ")) return null

        val parts = forContent.split(" of ", limit = 2).map { it.trim() }
        if (parts.size != 2) return null

        // Parse variable declaration: const item, let item, or just item
        val varDecl = parts[0].replace(Regex("^(let|const|var)\\s+"), "").trim()
        val arrayExpr = parts[1].trim()

        return ForOfLoopInfo(varDecl, arrayExpr)
    }

    /** Check if a for loop header is a for-of loop */
    fun isForOfLoop(loopHeader: String): Boolean {
        val forContent = loopHeader.substringAfter("(").substringBeforeLast(")")
        return forContent.contains(" of ")
    }

    fun checkCondition(current: Int, operator: String, target: Int) = when (operator) {
        "<=" -> current <= target
        ">=" -> current >= target
        "<" -> current < target
        ">" -> current > target
        else -> false
    }

    data class ForLoopInfo(
        val varName: String,
        val startValue: Int,
        val endValue: Int,
        val conditionOperator: String,
        val isIncrement: Boolean
    )

    data class ForOfLoopInfo(
        val varName: String,
        val arrayExpression: String
    )

    /** Returns (ifCondition, ifBody, elseBranches, nextStatementIndex). elseBranches has null condition for final else. */
    fun extractIfStatement(statements: List<String>, startIndex: Int): Quadruple<String, List<String>, List<Pair<String?, List<String>>>, Int>? {
        val ifHeader = statements[startIndex]
        val ifBody = mutableListOf<String>()
        val elseBranches = mutableListOf<Pair<String?, List<String>>>()
        var braceDepth = 0
        var foundOpenBrace = false
        var i = startIndex

        // Find the opening brace for if block
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

        // Collect if body until closing brace
        while (i < statements.size && braceDepth > 0) {
            val stmt = statements[i]

            // Check if this statement closes the if block (before counting opening braces)
            val closingBraces = stmt.count { it == '}' }
            val openingBraces = stmt.count { it == '{' }

            if (stmt.trim().startsWith("}") && braceDepth - closingBraces == 0) {
                // This statement closes the if block, don't add it to the body
                braceDepth = 0
                // Don't increment i yet - we need to check for else/else if
                break
            }

            braceDepth += openingBraces
            braceDepth -= closingBraces

            if (braceDepth > 0 || !stmt.trim().endsWith("}")) {
                // Don't remove braces - they might be part of template literals like ${i}
                ifBody.add(stmt.trim())
            }
            i++
        }

        // Check for else if / else
        while (i < statements.size) {
            var stmt = statements[i].trim()

            // If current statement is just "}", check if next statement is "else" or "else if"
            if (stmt == "}" && i + 1 < statements.size) {
                val nextStmt = statements[i + 1].trim()
                if (nextStmt.startsWith("else")) {
                    i++ // Move to the else/else if statement
                    stmt = nextStmt
                }
            }

            if (stmt.startsWith("} else if") || stmt.startsWith("else if")) {
                // Extract else if condition
                val conditionPart = stmt.substringAfter("else if").trim()
                val condition = if (conditionPart.contains("(") && conditionPart.contains(")")) {
                    conditionPart.substringAfter("(").substringBefore(")")
                } else {
                    conditionPart.removeSurrounding("(", ")")
                }
                val elseIfBody = mutableListOf<String>()

                // Find opening brace
                var j = i
                braceDepth = 0
                foundOpenBrace = false
                while (j < statements.size) {
                    val s = statements[j]
                    if (s.contains("{")) {
                        foundOpenBrace = true
                        // Only count the opening brace for the else if block
                        // (the statement might have a closing brace from the previous block)
                        braceDepth = 1
                        j++
                        break
                    }
                    j++
                }

                if (!foundOpenBrace) break

                // Collect else if body
                while (j < statements.size && braceDepth > 0) {
                    val s = statements[j]

                    // Check if this statement closes the else if block (before counting opening braces)
                    val closingBraces = s.count { it == '}' }
                    val openingBraces = s.count { it == '{' }

                    if (s.trim().startsWith("}") && braceDepth - closingBraces == 0) {
                        // This statement closes the else if block
                        // Don't increment j - let the next else/else if parser handle this statement
                        braceDepth = 0
                        break
                    }

                    braceDepth += openingBraces
                    braceDepth -= closingBraces

                    if (braceDepth > 0 || !s.trim().endsWith("}")) {
                        // Don't remove braces - they might be part of template literals like ${i}
                        elseIfBody.add(s.trim())
                    }
                    j++
                }

                elseBranches.add(Pair(condition, elseIfBody.filter { it.isNotEmpty() }))
                i = j
            }
            else if (stmt.startsWith("} else") || stmt == "else") {
                // Extract else body
                val elseBody = mutableListOf<String>()

                // Find opening brace
                var j = i
                braceDepth = 0
                foundOpenBrace = false
                while (j < statements.size) {
                    val s = statements[j]
                    if (s.contains("{")) {
                        foundOpenBrace = true
                        // Only count the opening brace for the else block
                        // (the statement might have a closing brace from the previous block)
                        braceDepth = 1
                        j++
                        break
                    }
                    j++
                }

                if (!foundOpenBrace) break

                // Collect else body
                while (j < statements.size && braceDepth > 0) {
                    val s = statements[j]
                    braceDepth += s.count { it == '{' }
                    braceDepth -= s.count { it == '}' }

                    if (braceDepth > 0 || !s.trim().endsWith("}")) {
                        // Don't remove braces - they might be part of template literals like ${i}
                        elseBody.add(s.trim())
                    }
                    j++
                }

                elseBranches.add(Pair(null, elseBody.filter { it.isNotEmpty() }))
                i = j
                break // Final else, no more branches
            }
            else {
                // No more else/else if branches
                // If we're still at the closing brace, skip past it
                if (i < statements.size && statements[i].trim().startsWith("}")) {
                    i++
                }
                break
            }
        }

        return Quadruple(ifHeader, ifBody.filter { it.isNotEmpty() }, elseBranches, i)
    }

    data class TryCatchInfo(
        val tryBody: List<String>,
        val catchVarName: String?,
        val catchBody: List<String>,
        val finallyBody: List<String>
    )

    /** Returns (tryBody, catchVarName, catchBody, finallyBody, nextStatementIndex) or null */
    fun extractTryCatch(statements: List<String>, startIndex: Int): Pair<TryCatchInfo, Int>? {
        val tryBody = mutableListOf<String>()
        var catchVarName: String? = null
        val catchBody = mutableListOf<String>()
        val finallyBody = mutableListOf<String>()
        var braceDepth = 0
        var foundOpenBrace = false
        var i = startIndex

        // Find the opening brace for try block
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

        // Collect try body until closing brace
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

        // Look for catch block
        while (i < statements.size) {
            var stmt = statements[i].trim()

            // If current statement is just "}", check if next statement is "catch"
            if (stmt == "}" && i + 1 < statements.size) {
                val nextStmt = statements[i + 1].trim()
                if (nextStmt.startsWith("catch")) {
                    i++
                    stmt = nextStmt
                }
            }

            if (stmt.startsWith("} catch") || stmt.startsWith("catch")) {
                // Extract catch variable name if present: catch (e) or catch (error)
                val catchPart = stmt.substringAfter("catch").trim()
                if (catchPart.contains("(") && catchPart.contains(")")) {
                    catchVarName = catchPart.substringAfter("(").substringBefore(")").trim()
                }

                // Find opening brace for catch block
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

                // Collect catch body
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
                // No catch block found
                break
            }
        }

        // Look for finally block (optional)
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