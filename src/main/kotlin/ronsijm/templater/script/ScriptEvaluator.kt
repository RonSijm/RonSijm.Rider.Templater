package ronsijm.templater.script

import ronsijm.templater.script.evaluators.*
import ronsijm.templater.utils.Logging

/**
 * Evaluator for script expressions.
 * Delegates to specialized evaluators for different expression types (SOLID: Single Responsibility).
 */
class ScriptEvaluator(
    private val scriptContext: ScriptContext,
    private val moduleRegistry: ModuleRegistry
) {

    companion object {
        private val LOG = Logging.getLogger<ScriptEvaluator>()
    }

    // Statement executor for multi-statement arrow function bodies
    // Set via setStatementExecutor() after ScriptExecutor is created
    private var statementExecutor: ((String) -> Unit)? = null

    // Lazy-initialized evaluators to avoid circular dependencies
    private val literalParser: LiteralParser by lazy {
        LiteralParser { evaluateExpression(it) }
    }

    private val arithmeticEvaluator: ArithmeticEvaluator by lazy {
        ArithmeticEvaluator({ evaluateExpression(it) }, literalParser)
    }

    private val arrowFunctionHandler: ArrowFunctionHandler by lazy {
        ArrowFunctionHandler(scriptContext, { evaluateExpression(it) }) { statementExecutor }
    }

    private val templateLiteralEvaluator: TemplateLiteralEvaluator by lazy {
        TemplateLiteralEvaluator { evaluateExpression(it) }
    }

    private val functionCallExecutor: FunctionCallExecutor by lazy {
        FunctionCallExecutor(
            scriptContext,
            moduleRegistry,
            { evaluateExpression(it) },
            { fn, args -> executeArrowFunction(fn, args) }
        )
    }

    /**
     * Set the statement executor for multi-statement arrow function bodies.
     * Must be called before any arrow functions with block bodies are executed.
     */
    fun setStatementExecutor(executor: (String) -> Unit) {
        this.statementExecutor = executor
    }

    /**
     * Evaluate an expression and return its value.
     * Delegates to specialized evaluators based on expression type.
     */
    fun evaluateExpression(expression: String): Any? {
        val expr = expression.trim()

        // Remove 'await' keyword if present
        val cleanExpr = if (expr.startsWith("await ")) {
            expr.substring(6).trim()
        } else {
            expr
        }

        // Handle logical NOT operator (!)
        // Must be before other checks since !variable should negate the variable's value
        if (cleanExpr.startsWith("!") && cleanExpr.length > 1 && cleanExpr[1] != '=') {
            val innerExpr = cleanExpr.substring(1).trim()
            val innerValue = evaluateExpression(innerExpr)
            return !isTruthy(innerValue)
        }

        // Check for arrow functions FIRST (before arithmetic, since arrow body may contain operators)
        if (arrowFunctionHandler.isTopLevelArrowFunction(cleanExpr)) {
            return arrowFunctionHandler.parseArrowFunction(cleanExpr)
        }

        // Check for logical operators (&&, ||) - must be before comparison/arithmetic
        val logicalOpIndex = findTopLevelLogicalOperator(cleanExpr)
        if (logicalOpIndex != null) {
            return evaluateLogicalExpression(cleanExpr, logicalOpIndex)
        }

        // Check for comparison expressions (>, <, >=, <=, ==, !=)
        val comparisonOp = arithmeticEvaluator.findComparisonOperator(cleanExpr)
        if (comparisonOp != null) {
            return arithmeticEvaluator.evaluateComparison(cleanExpr, comparisonOp)
        }

        // Check for arithmetic expressions (*, /, -, + with numbers)
        val arithmeticOp = arithmeticEvaluator.findArithmeticOperator(cleanExpr)
        if (arithmeticOp != null) {
            return arithmeticEvaluator.evaluateArithmetic(cleanExpr, arithmeticOp)
        }

        // Check if it's a template literal (backticks)
        if (LiteralParser.isTemplateLiteral(cleanExpr)) {
            return evaluateTemplateLiteral(cleanExpr.substring(1, cleanExpr.length - 1))
        }

        // Check if it's a string literal
        if (LiteralParser.isDoubleQuotedString(cleanExpr)) {
            return literalParser.parseStringLiteral(cleanExpr)
        }

        // Check if it's a single-quote string literal
        if (LiteralParser.isSingleQuotedString(cleanExpr)) {
            return literalParser.parseStringLiteral(cleanExpr)
        }

        // Check if it's the tR variable
        if (cleanExpr == "tR") {
            return scriptContext.getResultAccumulator()
        }

        // Check if it's a boolean literal (must come before variable check)
        if (cleanExpr == "true") return true
        if (cleanExpr == "false") return false
        if (cleanExpr == "null") return null

        // Check if it's an array literal: [1, 2, 3] or ["a", "b"]
        if (LiteralParser.isArrayLiteral(cleanExpr)) {
            return literalParser.parseArrayLiteral(cleanExpr)
        }

        // Check if it's an object literal: { key: value, ... }
        if (LiteralParser.isObjectLiteral(cleanExpr)) {
            return literalParser.parseObjectLiteral(cleanExpr)
        }

        // Check if it's a variable reference
        if (cleanExpr.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) {
            return scriptContext.getVariable(cleanExpr)
        }

        // Check if it's array indexing: arr[0] or arr[i] or arr[key].method()
        // Match variable name followed by [...] and optionally more stuff after
        if (cleanExpr.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*\\[.+\\].*"))) {
            return evaluateArrayAccessWithChain(cleanExpr)
        }

        // Check if it's a property access on a variable (e.g., arr.length, str.length)
        if (cleanExpr.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*\\.[a-zA-Z_][a-zA-Z0-9_]*")) && !cleanExpr.startsWith("tp.")) {
            return evaluateVariableProperty(cleanExpr)
        }

        // Check if it's a new Date() call
        if (cleanExpr.startsWith("new Date(")) {
            return DateObject()
        }

        // Check if it's a function call (contains parentheses)
        if (cleanExpr.contains("(")) {
            return evaluateFunctionCall(cleanExpr)
        }

        // Check if it's a property access (e.g., tp.frontmatter.title)
        if (cleanExpr.startsWith("tp.") && cleanExpr.contains(".")) {
            return evaluatePropertyAccess(cleanExpr)
        }

        // Try to parse as number
        cleanExpr.toIntOrNull()?.let { return it }
        cleanExpr.toDoubleOrNull()?.let { return it }

        // Return as-is (might be a number or other literal)
        return cleanExpr
    }

    /** Evaluate array access like arr[0] or arr[i] or arr[key].method().chain() */
    private fun evaluateArrayAccessWithChain(expression: String): Any? {
        val varName = expression.substringBefore("[")

        // Find the matching closing bracket for the first [
        val openBracketIndex = expression.indexOf('[')
        val closeBracketIndex = findMatchingBracket(expression, openBracketIndex)
        if (closeBracketIndex == -1) return null

        val indexExpr = expression.substring(openBracketIndex + 1, closeBracketIndex)
        val obj = scriptContext.getVariable(varName)
        val index = evaluateExpression(indexExpr)

        var result: Any? = when (obj) {
            is List<*> -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx] else null
            }
            is String -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx].toString() else null
            }
            is Map<*, *> -> {
                // Map access: obj[key] where key is a string
                val key = index?.toString() ?: return null
                obj[key]
            }
            else -> null
        }

        // Check if there's more after the closing bracket (method chain or more array access)
        if (closeBracketIndex < expression.length - 1) {
            val remaining = expression.substring(closeBracketIndex + 1).trim()
            if (remaining.isNotEmpty() && (remaining.startsWith(".") || remaining.startsWith("["))) {
                result = evaluateChainedMethodCall(result, remaining)
            }
        }

        return result
    }

    /** Evaluate property access on a variable like arr.length, str.length, or obj.key */
    private fun evaluateVariableProperty(expression: String): Any? {
        val parts = expression.split(".", limit = 2)
        if (parts.size != 2) return null

        val varName = parts[0]
        val propName = parts[1]
        val obj = scriptContext.getVariable(varName)

        return when (obj) {
            is List<*> -> when (propName) {
                "length" -> obj.size
                else -> null
            }
            is String -> when (propName) {
                "length" -> obj.length
                else -> null
            }
            is Map<*, *> -> obj[propName]
            else -> null
        }
    }

    /**
     * Evaluate template literal with ${} interpolation
     * Example: `${i}. Item number ${i}\n`
     */
    private fun evaluateTemplateLiteral(template: String): String {
        val result = StringBuilder()
        var i = 0

        while (i < template.length) {
            if (i < template.length - 1 && template[i] == '$' && template[i + 1] == '{') {
                // Find the closing brace
                var braceDepth = 1
                var j = i + 2
                while (j < template.length && braceDepth > 0) {
                    when (template[j]) {
                        '{' -> braceDepth++
                        '}' -> braceDepth--
                    }
                    j++
                }

                // Extract and evaluate the expression
                val expression = template.substring(i + 2, j - 1)
                val value = evaluateExpression(expression)
                result.append(value?.toString() ?: "")

                i = j
            } else if (template[i] == '\\' && i < template.length - 1 && template[i + 1] == 'n') {
                // Handle \n escape sequence
                result.append('\n')
                i += 2
            } else if (template[i] == '\\' && i < template.length - 1 && template[i + 1] == 't') {
                // Handle \t escape sequence
                result.append('\t')
                i += 2
            } else {
                result.append(template[i])
                i++
            }
        }

        return result.toString()
    }

    /**
     * Evaluate property access
     * Example: tp.frontmatter.title
     */
    private fun evaluatePropertyAccess(expression: String): Any? {
        val expr = expression.trim()

        // For now, treat property access as a function call with no arguments
        // This allows tp.frontmatter.title to work like tp.frontmatter.title()
        return functionCallExecutor.executeFunctionCall(expr, emptyList())
    }

    /**
     * Evaluate function call
     * Example: tp.system.prompt("Enter name")
     * Also handles chained calls: Object.keys(obj).forEach(fn)
     */
    private fun evaluateFunctionCall(expression: String): Any? {
        val expr = expression.trim()

        // Remove 'await' keyword if present
        val cleanExpr = if (expr.startsWith("await ")) {
            expr.substring(6).trim()
        } else {
            expr
        }

        // Extract function path and arguments
        // Need to find the matching opening parenthesis for the function call
        val firstParenIndex = cleanExpr.indexOf('(')
        if (firstParenIndex == -1) return null

        val functionPath = cleanExpr.substring(0, firstParenIndex)

        // Find the matching closing parenthesis and get the end index
        val (argsString, endIndex) = extractMatchingParenContentWithIndex(cleanExpr, firstParenIndex)

        // Parse arguments using the FunctionCallExecutor
        val args = functionCallExecutor.parseArguments(argsString)

        // Execute the function using the FunctionCallExecutor
        var result = functionCallExecutor.executeFunctionCall(functionPath, args)

        // Check if there's a chained method call or array access after the closing paren
        // e.g., Object.keys(obj).forEach(fn) - after Object.keys(obj) there's .forEach(fn)
        // e.g., str.split('#')[0] - after split('#') there's [0]
        if (endIndex < cleanExpr.length - 1) {
            val remaining = cleanExpr.substring(endIndex + 1).trim()
            if (remaining.startsWith(".") || remaining.startsWith("[")) {
                // There's a chained method call or array access
                result = evaluateChainedMethodCall(result, remaining)
            }
        }

        return result
    }

    /**
     * Evaluate a chained method call on a result.
     * e.g., ".forEach(fn)" on a list result
     * Also handles array index access like [0] after method calls
     */
    private fun evaluateChainedMethodCall(obj: Any?, chainExpr: String): Any? {
        if (obj == null) return null

        // chainExpr starts with "." or "[" - e.g., ".forEach(fn)" or "[0]" or ".split('#')[0]"
        var currentObj = obj
        var remaining = chainExpr

        while (remaining.isNotEmpty() && (remaining.startsWith(".") || remaining.startsWith("["))) {
            if (remaining.startsWith("[")) {
                // Array index access: [0] or [i]
                val closeBracket = findMatchingBracket(remaining, 0)
                if (closeBracket == -1) break

                val indexExpr = remaining.substring(1, closeBracket)
                val index = evaluateExpression(indexExpr)

                currentObj = when (currentObj) {
                    is List<*> -> {
                        val idx = (index as? Number)?.toInt() ?: return null
                        if (idx in currentObj.indices) currentObj[idx] else null
                    }
                    is String -> {
                        val idx = (index as? Number)?.toInt() ?: return null
                        if (idx in currentObj.indices) currentObj[idx].toString() else null
                    }
                    is Map<*, *> -> {
                        val key = index?.toString() ?: return null
                        currentObj[key]
                    }
                    else -> null
                }

                remaining = if (closeBracket < remaining.length - 1) {
                    remaining.substring(closeBracket + 1).trim()
                } else {
                    ""
                }
            } else {
                // Method call: .method(args)
                // Remove the leading dot
                remaining = remaining.substring(1)

                // Find the method name and arguments
                val parenIndex = remaining.indexOf('(')
                if (parenIndex == -1) {
                    // Property access, not a method call
                    val methodName = remaining.takeWhile { it.isLetterOrDigit() || it == '_' }
                    return when (currentObj) {
                        is List<*> -> when (methodName) {
                            "length" -> currentObj.size
                            else -> null
                        }
                        is String -> when (methodName) {
                            "length" -> currentObj.length
                            else -> null
                        }
                        is Map<*, *> -> currentObj[methodName]
                        else -> null
                    }
                }

                val methodName = remaining.substring(0, parenIndex)
                val (argsString, endIndex) = extractMatchingParenContentWithIndex(remaining, parenIndex)
                val args = functionCallExecutor.parseArguments(argsString)

                // Execute the method on the current object
                currentObj = when (currentObj) {
                    is List<*> -> {
                        ronsijm.templater.script.methods.ArrayMethodExecutor.execute(currentObj, methodName, args) { fn, fnArgs ->
                            executeArrowFunction(fn, fnArgs)
                        }
                    }
                    is String -> {
                        ronsijm.templater.script.methods.StringMethodExecutor.execute(currentObj, methodName, args)
                    }
                    else -> null
                }

                // Update remaining to check for more chained calls
                remaining = if (endIndex < remaining.length - 1) {
                    remaining.substring(endIndex + 1).trim()
                } else {
                    ""
                }
            }
        }

        return currentObj
    }

    /** Find the matching closing bracket for an opening bracket at the given index */
    private fun findMatchingBracket(str: String, openIndex: Int): Int {
        var depth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in openIndex until str.length) {
            val char = str[i]
            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                }
                char == '[' && !inQuotes -> depth++
                char == ']' && !inQuotes -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    /**
     * Extract content between matching parentheses starting at the given index.
     * Returns a pair of (content, endIndex) where endIndex is the position of the closing paren.
     * Handles nested parentheses correctly.
     */
    private fun extractMatchingParenContentWithIndex(str: String, openParenIndex: Int): Pair<String, Int> {
        var depth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in openParenIndex until str.length) {
            val char = str[i]
            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                }
                char == '(' && !inQuotes -> depth++
                char == ')' && !inQuotes -> {
                    depth--
                    if (depth == 0) {
                        // Found the matching closing paren
                        return Pair(str.substring(openParenIndex + 1, i), i)
                    }
                }
            }
        }
        // Fallback: return everything after the opening paren
        return Pair(str.substring(openParenIndex + 1).trimEnd(')'), str.length - 1)
    }

    /** Execute an arrow function with given arguments */
    fun executeArrowFunction(fn: ArrowFunction, args: List<Any?>): Any? {
        return arrowFunctionHandler.executeArrowFunction(fn, args)
    }

    /**
     * Find a top-level logical operator (&& or ||) in the expression.
     * Returns a Pair of (index, operator) or null if not found.
     * Top-level means not inside parentheses, brackets, quotes, or arrow function bodies.
     */
    private fun findTopLevelLogicalOperator(expr: String): Pair<Int, String>? {
        // First, find the position of any top-level => (arrow function)
        // Anything after => is inside the arrow body and should not be considered
        val arrowIndex = findTopLevelArrowIndex(expr)

        // Only search for logical operators BEFORE the arrow (if any)
        val searchEnd = arrowIndex ?: expr.length

        var parenDepth = 0
        var bracketDepth = 0
        var braceDepth = 0
        var inQuotes = false
        var quoteChar = ' '

        // Search from right to left (but only up to the arrow)
        // Start at searchEnd - 1 to include the last character before searchEnd
        var i = searchEnd - 1
        while (i >= 0) {
            val char = expr[i]
            val nextChar = if (i < expr.length - 1) expr[i + 1] else ' '

            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                }
                char == ')' && !inQuotes -> parenDepth++
                char == '(' && !inQuotes -> {
                    parenDepth--
                    if (parenDepth < 0) parenDepth = 0
                }
                char == ']' && !inQuotes -> bracketDepth++
                char == '[' && !inQuotes -> bracketDepth--
                char == '}' && !inQuotes -> braceDepth++
                char == '{' && !inQuotes -> braceDepth--
                !inQuotes && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0 -> {
                    // Check for || first (lower precedence)
                    if (char == '|' && nextChar == '|') {
                        return Pair(i, "||")
                    }
                    // Then check for &&
                    if (char == '&' && nextChar == '&') {
                        return Pair(i, "&&")
                    }
                }
            }
            i--
        }
        return null
    }

    /**
     * Find the index of a top-level => (arrow function) in the expression.
     * Returns null if no top-level arrow is found.
     */
    private fun findTopLevelArrowIndex(expr: String): Int? {
        var parenDepth = 0
        var bracketDepth = 0
        var braceDepth = 0
        var inQuotes = false
        var quoteChar = ' '

        var i = 0
        while (i < expr.length - 1) {
            val char = expr[i]
            val nextChar = expr[i + 1]

            when {
                (char == '"' || char == '\'' || char == '`') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                }
                char == '(' && !inQuotes -> parenDepth++
                char == ')' && !inQuotes -> parenDepth--
                char == '[' && !inQuotes -> bracketDepth++
                char == ']' && !inQuotes -> bracketDepth--
                char == '{' && !inQuotes -> braceDepth++
                char == '}' && !inQuotes -> braceDepth--
                char == '=' && nextChar == '>' && !inQuotes && parenDepth == 0 && bracketDepth == 0 && braceDepth == 0 -> {
                    return i
                }
            }
            i++
        }
        return null
    }

    /**
     * Evaluate a logical expression (&&, ||).
     */
    private fun evaluateLogicalExpression(expr: String, opInfo: Pair<Int, String>): Any? {
        val (index, op) = opInfo
        val left = expr.substring(0, index).trim()
        val right = expr.substring(index + 2).trim()

        val leftValue = evaluateExpression(left)

        return when (op) {
            "&&" -> {
                // Short-circuit: if left is falsy, return left; otherwise return right
                if (isTruthy(leftValue)) {
                    evaluateExpression(right)
                } else {
                    leftValue
                }
            }
            "||" -> {
                // Short-circuit: if left is truthy, return left; otherwise return right
                if (isTruthy(leftValue)) {
                    leftValue
                } else {
                    evaluateExpression(right)
                }
            }
            else -> null
        }
    }

    /**
     * Check if a value is truthy (JavaScript-like semantics).
     */
    private fun isTruthy(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            is Number -> value.toDouble() != 0.0
            is String -> value.isNotEmpty()
            is List<*> -> true // Arrays are always truthy in JS
            is Map<*, *> -> true // Objects are always truthy in JS
            else -> true
        }
    }
}
