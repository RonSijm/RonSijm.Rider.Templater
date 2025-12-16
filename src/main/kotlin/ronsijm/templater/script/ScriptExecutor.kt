package ronsijm.templater.script

import ronsijm.templater.utils.Logging

/**
 * Executor for script statements
 * Handles execution of for loops, if/else statements, variable assignments, and tR manipulation
 */
class ScriptExecutor(
    private val scriptContext: ScriptContext,
    private val evaluator: ScriptEvaluator,
    private val parser: ScriptParser
) {

    companion object {
        private val LOG = Logging.getLogger<ScriptExecutor>()
    }

    /**
     * Execute a for loop
     * Supports: for (let i = start; i <= end; i++) and for (const item of array)
     */
    fun executeForLoop(loopHeader: String, loopBody: List<String>) {
        // Check if it's a for-of loop
        if (parser.isForOfLoop(loopHeader)) {
            executeForOfLoop(loopHeader, loopBody)
            return
        }

        // Parse traditional for loop header
        val loopInfo = parser.parseForLoopHeader(loopHeader) ?: return

        // Execute loop
        var currentValue = loopInfo.startValue
        while (parser.checkCondition(currentValue, loopInfo.conditionOperator, loopInfo.endValue)) {
            // Set loop variable
            scriptContext.setVariable(loopInfo.varName, currentValue)

            // Execute loop body
            for (bodyStatement in loopBody) {
                executeStatement(bodyStatement)
            }

            // Increment/decrement
            currentValue = if (loopInfo.isIncrement) currentValue + 1 else currentValue - 1
        }

        // Clean up loop variable
        scriptContext.removeVariable(loopInfo.varName)
    }

    /** Execute a for-of loop: for (const item of array) */
    private fun executeForOfLoop(loopHeader: String, loopBody: List<String>) {
        val loopInfo = parser.parseForOfLoopHeader(loopHeader) ?: return

        // Evaluate the array expression
        val arrayValue = evaluator.evaluateExpression(loopInfo.arrayExpression)

        // Convert to iterable
        val items: List<Any?> = when (arrayValue) {
            is List<*> -> arrayValue
            is String -> arrayValue.toList().map { it.toString() }
            else -> return
        }

        // Execute loop for each item
        for (item in items) {
            scriptContext.setVariable(loopInfo.varName, item)

            for (bodyStatement in loopBody) {
                executeStatement(bodyStatement)
            }
        }

        // Clean up loop variable
        scriptContext.removeVariable(loopInfo.varName)
    }

    /**
     * Execute an if/else statement
     */
    fun executeIfStatement(
        ifHeader: String,
        ifBody: List<String>,
        elseBranches: List<Pair<String?, List<String>>>
    ) {
        LOG?.debug("executeIfStatement called")
        LOG?.debug("  ifHeader: $ifHeader")
        LOG?.debug("  ifBody: $ifBody")
        LOG?.debug("  elseBranches: $elseBranches")

        // Parse if condition - extract content between ( and )
        val afterIf = ifHeader.substringAfter("if").trim()
        val condition = if (afterIf.startsWith("(") && afterIf.contains(")")) {
            afterIf.substringAfter("(").substringBefore(")")
        } else {
            afterIf.removeSurrounding("(", ")").substringBefore("{").trim()
        }
        LOG?.debug("  condition: $condition")

        val conditionResult = evaluateCondition(condition)
        LOG?.debug("  condition evaluates to: $conditionResult")

        if (conditionResult) {
            LOG?.debug("  Executing if body with ${ifBody.size} statements")
            // Execute if body
            for (statement in ifBody) {
                executeStatement(statement)
            }
        } else {
            LOG?.debug("  Checking ${elseBranches.size} else branches")
            // Check else if / else branches
            var executed = false
            for ((elseCondition, elseBody) in elseBranches) {
                if (elseCondition == null) {
                    LOG?.debug("  Executing final else with ${elseBody.size} statements")
                    // Final else
                    for (statement in elseBody) {
                        executeStatement(statement)
                    }
                    executed = true
                    break
                } else if (evaluateCondition(elseCondition)) {
                    LOG?.debug("  Executing else if with ${elseBody.size} statements")
                    // else if condition is true
                    for (statement in elseBody) {
                        executeStatement(statement)
                    }
                    executed = true
                    break
                }
            }
        }
    }

    /**
     * Evaluate a boolean condition
     */
    private fun evaluateCondition(condition: String): Boolean {
        val trimmed = condition.trim()

        // Handle comparison operators - check longer operators first to avoid partial matches
        when {
            trimmed.contains("===") -> {
                val parts = trimmed.split("===").map { it.trim() }
                if (parts.size == 2) {
                    val left = evaluator.evaluateExpression(parts[0])
                    val right = evaluator.evaluateExpression(parts[1])
                    return left == right  // Strict equality
                }
            }
            trimmed.contains("!==") -> {
                val parts = trimmed.split("!==").map { it.trim() }
                if (parts.size == 2) {
                    val left = evaluator.evaluateExpression(parts[0])
                    val right = evaluator.evaluateExpression(parts[1])
                    return left != right  // Strict inequality
                }
            }
            trimmed.contains("<=") -> {
                val parts = trimmed.split("<=").map { it.trim() }
                if (parts.size == 2) {
                    val left = evaluator.evaluateExpression(parts[0])
                    val right = evaluator.evaluateExpression(parts[1])
                    return compareValues(left, right) <= 0
                }
            }
            trimmed.contains(">=") -> {
                val parts = trimmed.split(">=").map { it.trim() }
                if (parts.size == 2) {
                    val left = evaluator.evaluateExpression(parts[0])
                    val right = evaluator.evaluateExpression(parts[1])
                    return compareValues(left, right) >= 0
                }
            }
            trimmed.contains("==") -> {
                val parts = trimmed.split("==").map { it.trim() }
                if (parts.size == 2) {
                    val left = evaluator.evaluateExpression(parts[0])
                    val right = evaluator.evaluateExpression(parts[1])
                    return compareValues(left, right) == 0
                }
            }
            trimmed.contains("!=") -> {
                val parts = trimmed.split("!=").map { it.trim() }
                if (parts.size == 2) {
                    val left = evaluator.evaluateExpression(parts[0])
                    val right = evaluator.evaluateExpression(parts[1])
                    return compareValues(left, right) != 0
                }
            }
            trimmed.contains("<") -> {
                val parts = trimmed.split("<").map { it.trim() }
                if (parts.size == 2) {
                    val left = evaluator.evaluateExpression(parts[0])
                    val right = evaluator.evaluateExpression(parts[1])
                    return compareValues(left, right) < 0
                }
            }
            trimmed.contains(">") -> {
                val parts = trimmed.split(">").map { it.trim() }
                if (parts.size == 2) {
                    val left = evaluator.evaluateExpression(parts[0])
                    val right = evaluator.evaluateExpression(parts[1])
                    return compareValues(left, right) > 0
                }
            }
        }

        // If no comparison operator, evaluate as boolean expression
        val result = evaluator.evaluateExpression(trimmed)
        return when (result) {
            is Boolean -> result
            is Number -> result.toDouble() != 0.0
            is String -> result.isNotEmpty()
            null -> false
            else -> true
        }
    }

    /**
     * Compare two values
     */
    private fun compareValues(left: Any?, right: Any?): Int {
        // Handle null comparisons explicitly
        if (left == null && right == null) return 0
        if (left == null) return -1  // null is "less than" non-null
        if (right == null) return 1  // non-null is "greater than" null

        if (left is Number && right is Number) {
            return left.toDouble().compareTo(right.toDouble())
        }
        if (left is String && right is String) {
            return left.compareTo(right)
        }
        // For other types, compare string representations
        return left.toString().compareTo(right.toString())
    }

    /**
     * Execute a single statement
     */
    fun executeStatement(statement: String) {
        val stmt = statement.trim()
        LOG?.debug("executeStatement: $stmt")

        if (stmt.isEmpty()) return

        // Check if return was already requested
        if (scriptContext.isReturnRequested()) return

        // Skip braces
        if (stmt == "{" || stmt == "}") return

        // Return statement: return or return;
        if (stmt == "return" || stmt == "return;") {
            scriptContext.requestReturn()
            return
        }

        // Variable declaration: let x = value
        if (stmt.startsWith("let ") || stmt.startsWith("const ") || stmt.startsWith("var ")) {
            executeVariableDeclaration(stmt)
            return
        }

        // tR manipulation: tR += "text" (check BEFORE general assignment)
        if (stmt.startsWith("tR")) {
            executeResultAccumulator(stmt)
            return
        }

        // Assignment: x = value (but not arrow functions or comparisons)
        if (stmt.contains("=") && !stmt.contains("==") && !stmt.contains("=>")) {
            executeAssignment(stmt)
            return
        }

        // Function call (standalone)
        if (stmt.contains("(")) {
            evaluator.evaluateExpression(stmt)
            return
        }
    }

    /**
     * Execute variable declaration
     * Example: let x = tp.date.now()
     */
    private fun executeVariableDeclaration(statement: String) {
        // Remove 'let', 'const', or 'var' keyword
        val withoutKeyword = statement.replace(Regex("^(let|const|var)\\s+"), "")
        executeAssignment(withoutKeyword)
    }

    /**
     * Execute assignment
     * Example: x = "value" or fn = x => x + 1
     */
    private fun executeAssignment(statement: String) {
        // Find the first = that's not part of => or == or !=
        val assignmentIndex = findAssignmentOperator(statement)
        if (assignmentIndex == -1) return

        val varName = statement.substring(0, assignmentIndex).trim()
        val valueExpr = statement.substring(assignmentIndex + 1).trim()
        val value = evaluator.evaluateExpression(valueExpr)

        scriptContext.setVariable(varName, value)
    }

    /** Find the index of the assignment = operator (not ==, !=, >=, <=, =>) */
    private fun findAssignmentOperator(statement: String): Int {
        var i = 0
        while (i < statement.length) {
            if (statement[i] == '=') {
                // Check it's not ==, !=, >=, <=, or =>
                val prevChar = if (i > 0) statement[i - 1] else ' '
                val nextChar = if (i < statement.length - 1) statement[i + 1] else ' '

                if (prevChar != '!' && prevChar != '>' && prevChar != '<' && prevChar != '=' &&
                    nextChar != '=' && nextChar != '>') {
                    return i
                }
            }
            i++
        }
        return -1
    }

    /**
     * Execute tR manipulation
     * Example: tR += "text"
     */
    private fun executeResultAccumulator(statement: String) {
        LOG?.debug("executeResultAccumulator called with: $statement")
        when {
            statement.contains("+=") -> {
                val value = statement.substringAfter("+=").trim()
                LOG?.debug("tR += detected, value to add: $value")
                val evaluated = evaluator.evaluateExpression(value)
                LOG?.debug("Evaluated to: $evaluated")
                scriptContext.appendToResult(evaluated?.toString() ?: "")
                LOG?.debug("tR is now: ${scriptContext.getResultAccumulator()}")
            }
            statement.contains("=") -> {
                val value = statement.substringAfter("=").trim()
                val evaluated = evaluator.evaluateExpression(value)
                scriptContext.setResult(evaluated?.toString() ?: "")
            }
        }
    }

    /**
     * Execute a try/catch block
     */
    fun executeTryCatch(tryCatchInfo: ScriptParser.TryCatchInfo) {
        LOG?.debug("executeTryCatch called")
        LOG?.debug("  tryBody: ${tryCatchInfo.tryBody}")
        LOG?.debug("  catchVarName: ${tryCatchInfo.catchVarName}")
        LOG?.debug("  catchBody: ${tryCatchInfo.catchBody}")
        LOG?.debug("  finallyBody: ${tryCatchInfo.finallyBody}")

        var caughtException: Exception? = null

        try {
            // Execute try body
            for (statement in tryCatchInfo.tryBody) {
                executeStatement(statement)
            }
        } catch (e: Exception) {
            caughtException = e
            LOG?.debug("Exception caught: ${e.message}")

            // Set the error variable if specified
            if (tryCatchInfo.catchVarName != null) {
                scriptContext.setVariable(tryCatchInfo.catchVarName, e.message ?: "Unknown error")
            }

            // Execute catch body
            for (statement in tryCatchInfo.catchBody) {
                executeStatement(statement)
            }

            // Clean up error variable
            if (tryCatchInfo.catchVarName != null) {
                scriptContext.removeVariable(tryCatchInfo.catchVarName)
            }
        } finally {
            // Execute finally body (always runs)
            for (statement in tryCatchInfo.finallyBody) {
                executeStatement(statement)
            }
        }
    }
}