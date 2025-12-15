package ronsijm.templater.script

import ronsijm.templater.script.methods.ArrayMethodExecutor
import ronsijm.templater.script.methods.StringMethodExecutor
import ronsijm.templater.utils.Logging

/**
 * Evaluator for script expressions
 * Handles expression evaluation, template literals, concatenation, and function calls
 */
class ScriptEvaluator(
    private val scriptContext: ScriptContext,
    private val moduleRegistry: ModuleRegistry
) {

    companion object {
        private val LOG = Logging.getLogger<ScriptEvaluator>()
    }

    /**
     * Evaluate an expression and return its value
     */
    fun evaluateExpression(expression: String): Any? {
        val expr = expression.trim()

        // Remove 'await' keyword if present
        val cleanExpr = if (expr.startsWith("await ")) {
            expr.substring(6).trim()
        } else {
            expr
        }

        // Check for arrow functions FIRST (before arithmetic, since arrow body may contain operators)
        // Only match if => is at the top level (not inside parentheses like in function calls)
        if (isTopLevelArrowFunction(cleanExpr)) {
            return parseArrowFunction(cleanExpr)
        }

        // Check for comparison expressions (>, <, >=, <=, ==, !=)
        val comparisonOp = findComparisonOperator(cleanExpr)
        if (comparisonOp != null) {
            return evaluateComparison(cleanExpr, comparisonOp)
        }

        // Check for arithmetic expressions (*, /, -, + with numbers)
        // This handles expressions like "x * 2", "counter + 1", "y - x"
        val arithmeticOp = findArithmeticOperator(cleanExpr)
        if (arithmeticOp != null) {
            return evaluateArithmetic(cleanExpr, arithmeticOp)
        }

        // Check if it's a template literal (backticks)
        if (cleanExpr.startsWith("`") && cleanExpr.endsWith("`")) {
            return evaluateTemplateLiteral(cleanExpr.substring(1, cleanExpr.length - 1))
        }

        // Check if it's a string literal
        if (cleanExpr.startsWith("\"") && cleanExpr.endsWith("\"")) {
            return cleanExpr.substring(1, cleanExpr.length - 1)
        }

        // Check if it's a single-quote string literal
        if (cleanExpr.startsWith("'") && cleanExpr.endsWith("'")) {
            return cleanExpr.substring(1, cleanExpr.length - 1)
        }

        // Check if it's the tR variable
        if (cleanExpr == "tR") {
            return scriptContext.getResultAccumulator()
        }

        // Check if it's a boolean literal (must come before variable check)
        if (cleanExpr == "true") return true
        if (cleanExpr == "false") return false

        // Check if it's an array literal: [1, 2, 3] or ["a", "b"]
        if (cleanExpr.startsWith("[") && cleanExpr.endsWith("]")) {
            return parseArrayLiteral(cleanExpr)
        }

        // Check if it's an object literal: { key: value, ... }
        if (cleanExpr.startsWith("{") && cleanExpr.endsWith("}")) {
            return parseObjectLiteral(cleanExpr)
        }

        // Check if it's a variable reference
        if (cleanExpr.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*"))) {
            return scriptContext.getVariable(cleanExpr)
        }

        // Check if it's array indexing: arr[0] or arr[i]
        if (cleanExpr.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*\\[.+\\]"))) {
            return evaluateArrayAccess(cleanExpr)
        }

        // Check if it's a property access on a variable (e.g., arr.length, str.length)
        if (cleanExpr.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*\\.[a-zA-Z_][a-zA-Z0-9_]*")) && !cleanExpr.startsWith("tp.")) {
            return evaluateVariableProperty(cleanExpr)
        }

        // Check if it's a new Date() call
        if (cleanExpr.startsWith("new Date(")) {
            return createDateObject(cleanExpr)
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

    /**
     * Find an arithmetic operator outside of quotes/parentheses
     * Returns the operator and its position, or null if not found
     * Checks operators in order of precedence (lowest first): +, -, *, /
     */
    private fun findArithmeticOperator(expression: String): Pair<Char, Int>? {
        var inQuotes = false
        var quoteChar = ' '
        var parenDepth = 0

        // First pass: look for + or - (lowest precedence)
        for (i in expression.indices) {
            val char = expression[i]
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
                (char == '+' || char == '-') && !inQuotes && parenDepth == 0 && i > 0 -> {
                    // Make sure it's not a unary operator (check previous non-space char)
                    val prevNonSpace = expression.substring(0, i).trimEnd().lastOrNull()
                    if (prevNonSpace != null && prevNonSpace !in listOf('(', ',', '=', '+', '-', '*', '/')) {
                        return Pair(char, i)
                    }
                }
            }
        }

        // Reset for second pass
        inQuotes = false
        parenDepth = 0

        // Second pass: look for * or / (higher precedence)
        for (i in expression.indices) {
            val char = expression[i]
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
                (char == '*' || char == '/') && !inQuotes && parenDepth == 0 -> {
                    return Pair(char, i)
                }
            }
        }

        return null
    }

    /**
     * Evaluate an arithmetic expression
     * Handles +, -, *, / operations
     */
    private fun evaluateArithmetic(expression: String, operatorInfo: Pair<Char, Int>): Any? {
        val (operator, position) = operatorInfo
        val leftExpr = expression.substring(0, position).trim()
        val rightExpr = expression.substring(position + 1).trim()

        val leftValue = evaluateExpression(leftExpr)
        val rightValue = evaluateExpression(rightExpr)

        // If both are numbers, do arithmetic
        val leftNum = toNumber(leftValue)
        val rightNum = toNumber(rightValue)

        if (leftNum != null && rightNum != null) {
            return when (operator) {
                '+' -> if (leftNum is Int && rightNum is Int) leftNum + rightNum else leftNum.toDouble() + rightNum.toDouble()
                '-' -> if (leftNum is Int && rightNum is Int) leftNum - rightNum else leftNum.toDouble() - rightNum.toDouble()
                '*' -> if (leftNum is Int && rightNum is Int) leftNum * rightNum else leftNum.toDouble() * rightNum.toDouble()
                '/' -> if (leftNum is Int && rightNum is Int && rightNum != 0 && leftNum % rightNum == 0) {
                    leftNum / rightNum
                } else {
                    leftNum.toDouble() / rightNum.toDouble()
                }
                else -> null
            }
        }

        // If + and at least one is a string, do concatenation
        if (operator == '+') {
            return (leftValue?.toString() ?: "") + (rightValue?.toString() ?: "")
        }

        // Can't do arithmetic on non-numbers for -, *, /
        return null
    }

    /**
     * Find a comparison operator outside of quotes/parentheses
     * Returns the operator string and its position, or null if not found
     * Checks: >=, <=, ==, !=, >, <
     */
    private fun findComparisonOperator(expression: String): Pair<String, Int>? {
        var inQuotes = false
        var quoteChar = ' '
        var parenDepth = 0

        // Check for two-character operators first: >=, <=, ==, !=
        for (i in 0 until expression.length - 1) {
            val char = expression[i]
            val nextChar = expression[i + 1]
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
                !inQuotes && parenDepth == 0 -> {
                    val twoChar = "$char$nextChar"
                    if (twoChar in listOf(">=", "<=", "==", "!=")) {
                        return Pair(twoChar, i)
                    }
                }
            }
        }

        // Check for single-character operators: >, <
        inQuotes = false
        parenDepth = 0
        for (i in expression.indices) {
            val char = expression[i]
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
                (char == '>' || char == '<') && !inQuotes && parenDepth == 0 -> {
                    // Make sure it's not part of => or >= or <=
                    val prevChar = if (i > 0) expression[i - 1] else ' '
                    val nextChar = if (i < expression.length - 1) expression[i + 1] else ' '
                    if (prevChar != '=' && nextChar != '=') {
                        return Pair(char.toString(), i)
                    }
                }
            }
        }

        return null
    }

    /**
     * Evaluate a comparison expression
     * Handles >, <, >=, <=, ==, != operations
     */
    private fun evaluateComparison(expression: String, operatorInfo: Pair<String, Int>): Boolean {
        val (operator, position) = operatorInfo
        val leftExpr = expression.substring(0, position).trim()
        val rightExpr = expression.substring(position + operator.length).trim()

        val leftValue = evaluateExpression(leftExpr)
        val rightValue = evaluateExpression(rightExpr)

        // Try numeric comparison first
        val leftNum = toNumber(leftValue)
        val rightNum = toNumber(rightValue)

        if (leftNum != null && rightNum != null) {
            val leftDouble = leftNum.toDouble()
            val rightDouble = rightNum.toDouble()
            return when (operator) {
                ">" -> leftDouble > rightDouble
                "<" -> leftDouble < rightDouble
                ">=" -> leftDouble >= rightDouble
                "<=" -> leftDouble <= rightDouble
                "==" -> leftDouble == rightDouble
                "!=" -> leftDouble != rightDouble
                else -> false
            }
        }

        // String/object comparison
        return when (operator) {
            "==" -> leftValue == rightValue || leftValue?.toString() == rightValue?.toString()
            "!=" -> leftValue != rightValue && leftValue?.toString() != rightValue?.toString()
            else -> false
        }
    }

    /**
     * Convert a value to a Number if possible
     */
    private fun toNumber(value: Any?): Number? {
        return when (value) {
            is Number -> value
            is String -> value.toIntOrNull() ?: value.toDoubleOrNull()
            else -> null
        }
    }

    /**
     * Create a JavaScript Date object wrapper
     */
    private fun createDateObject(expression: String): DateObject {
        // For now, just create with current date/time
        // Could parse arguments later if needed
        return DateObject()
    }

    /** Evaluate array access like arr[0] or arr[i] */
    private fun evaluateArrayAccess(expression: String): Any? {
        val varName = expression.substringBefore("[")
        val indexExpr = expression.substringAfter("[").substringBeforeLast("]")
        val obj = scriptContext.getVariable(varName)
        val index = evaluateExpression(indexExpr)

        return when (obj) {
            is List<*> -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx] else null
            }
            is String -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx].toString() else null
            }
            else -> null
        }
    }

    /** Parse an array literal like [1, 2, 3] or ["a", "b", "c"] */
    private fun parseArrayLiteral(expression: String): List<Any?> {
        val content = expression.substring(1, expression.length - 1).trim()
        if (content.isEmpty()) return emptyList()

        val elements = mutableListOf<Any?>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '
        var depth = 0

        for (char in content) {
            when {
                (char == '"' || char == '\'') && depth == 0 -> {
                    if (!inQuotes) {
                        inQuotes = true
                        quoteChar = char
                    } else if (char == quoteChar) {
                        inQuotes = false
                    }
                    current.append(char)
                }
                char == '[' && !inQuotes -> {
                    depth++
                    current.append(char)
                }
                char == ']' && !inQuotes -> {
                    depth--
                    current.append(char)
                }
                char == ',' && !inQuotes && depth == 0 -> {
                    elements.add(evaluateExpression(current.toString().trim()))
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }

        // Don't forget the last element
        if (current.isNotEmpty()) {
            elements.add(evaluateExpression(current.toString().trim()))
        }

        return elements
    }

    /** Parse an object literal like { url: "http://...", method: "GET" } */
    private fun parseObjectLiteral(expression: String): Map<String, Any?> {
        val content = expression.substring(1, expression.length - 1).trim()
        if (content.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, Any?>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '
        var depth = 0

        for (char in content) {
            when {
                (char == '"' || char == '\'') && depth == 0 -> {
                    if (!inQuotes) {
                        inQuotes = true
                        quoteChar = char
                    } else if (char == quoteChar) {
                        inQuotes = false
                    }
                    current.append(char)
                }
                (char == '{' || char == '[') && !inQuotes -> {
                    depth++
                    current.append(char)
                }
                (char == '}' || char == ']') && !inQuotes -> {
                    depth--
                    current.append(char)
                }
                char == ',' && !inQuotes && depth == 0 -> {
                    parseObjectProperty(current.toString().trim())?.let { (key, value) ->
                        result[key] = value
                    }
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }

        // Don't forget the last property
        if (current.isNotEmpty()) {
            parseObjectProperty(current.toString().trim())?.let { (key, value) ->
                result[key] = value
            }
        }

        return result
    }

    /** Parse a single object property like "url: link" or "method: 'GET'" */
    private fun parseObjectProperty(property: String): Pair<String, Any?>? {
        val colonIndex = property.indexOf(':')
        if (colonIndex == -1) return null

        val key = property.substring(0, colonIndex).trim()
        val valueExpr = property.substring(colonIndex + 1).trim()
        val value = evaluateExpression(valueExpr)

        return key to value
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
     * Simple JavaScript Date object wrapper
     */
    class DateObject {
        private val dateTime = java.time.LocalDateTime.now()

        fun getHours(): Int = dateTime.hour
        fun getMinutes(): Int = dateTime.minute
        fun getSeconds(): Int = dateTime.second
        fun getDate(): Int = dateTime.dayOfMonth
        fun getMonth(): Int = dateTime.monthValue - 1 // JavaScript months are 0-indexed
        fun getFullYear(): Int = dateTime.year
        fun getDay(): Int = dateTime.dayOfWeek.value % 7 // JavaScript: 0=Sunday, 1=Monday, etc.
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
        return executeFunctionCall(expr, emptyList())
    }

    /**
     * Evaluate function call
     * Example: tp.system.prompt("Enter name")
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
        val functionPath = cleanExpr.substringBefore("(")
        val argsString = cleanExpr.substringAfter("(").substringBeforeLast(")")

        // Parse arguments
        val args = parseArguments(argsString)

        // Execute the function
        return executeFunctionCall(functionPath, args)
    }

    /**
     * Parse function arguments
     */
    private fun parseArguments(argsString: String): List<Any?> {
        if (argsString.isBlank()) return emptyList()

        val args = mutableListOf<Any?>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '
        var depth = 0

        for (char in argsString) {
            when {
                (char == '"' || char == '\'') && depth == 0 -> {
                    if (!inQuotes) {
                        inQuotes = true
                        quoteChar = char
                        current.append(char) // Keep the opening quote
                    } else if (char == quoteChar) {
                        inQuotes = false
                        current.append(char) // Keep the closing quote
                    } else {
                        current.append(char) // Different quote inside string
                    }
                }
                char == '(' && !inQuotes -> {
                    depth++
                    current.append(char)
                }
                char == ')' && !inQuotes -> {
                    depth--
                    current.append(char)
                }
                char == ',' && !inQuotes && depth == 0 -> {
                    args.add(evaluateExpression(current.toString().trim()))
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            args.add(evaluateExpression(current.toString().trim()))
        }

        return args
    }

    /**
     * Execute a function call
     * Delegates to ModuleRegistry or handles method calls on variables
     */
    private fun executeFunctionCall(functionPath: String, args: List<Any?>): Any? {
        // Check if it's a simple function name (could be an arrow function variable)
        if (!functionPath.contains(".")) {
            val fn = scriptContext.getVariable(functionPath)
            if (fn is ArrowFunction) {
                return executeArrowFunction(fn, args)
            }
        }

        // Check if it's a method call on a variable (e.g., hour.getHours(), str.split(","))
        val parts = functionPath.split(".")
        if (parts.size == 2) {
            val varName = parts[0]
            val methodName = parts[1]
            val obj = scriptContext.getVariable(varName)

            if (obj is DateObject) {
                return when (methodName) {
                    "getHours" -> obj.getHours()
                    "getMinutes" -> obj.getMinutes()
                    "getSeconds" -> obj.getSeconds()
                    "getDate" -> obj.getDate()
                    "getMonth" -> obj.getMonth()
                    "getFullYear" -> obj.getFullYear()
                    "getDay" -> obj.getDay()
                    else -> null
                }
            }

            // String methods
            if (obj is String) {
                return StringMethodExecutor.execute(obj, methodName, args)
            }

            // Array methods
            if (obj is List<*>) {
                return ArrayMethodExecutor.execute(obj, methodName, args) { fn, fnArgs ->
                    executeArrowFunction(fn, fnArgs)
                }
            }
        }

        // Delegate to ModuleRegistry
        return moduleRegistry.executeFunction(functionPath, args)
    }

    /** Check if expression is a top-level arrow function (=> not inside parentheses) */
    private fun isTopLevelArrowFunction(expression: String): Boolean {
        if (!expression.contains("=>")) return false

        var parenDepth = 0
        var bracketDepth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in 0 until expression.length - 1) {
            val char = expression[i]
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
                char == '=' && expression[i + 1] == '>' && !inQuotes && parenDepth == 0 && bracketDepth == 0 -> {
                    return true
                }
            }
        }
        return false
    }

    /** Parse an arrow function expression: (x) => x + 1 or x => x + 1 */
    private fun parseArrowFunction(expression: String): ArrowFunction? {
        val arrowIndex = expression.indexOf("=>")
        if (arrowIndex == -1) return null

        val paramsPart = expression.substring(0, arrowIndex).trim()
        val bodyPart = expression.substring(arrowIndex + 2).trim()

        // Parse parameters
        val parameters = if (paramsPart.startsWith("(") && paramsPart.endsWith(")")) {
            // (x, y) => ...
            paramsPart.substring(1, paramsPart.length - 1)
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } else {
            // x => ...
            listOf(paramsPart)
        }

        // Check if body is a block or expression
        val isExpression = !bodyPart.startsWith("{")
        val body = if (isExpression) {
            bodyPart
        } else {
            // Extract body from block, handle return statement
            bodyPart.removeSurrounding("{", "}").trim()
        }

        return ArrowFunction(parameters, body, isExpression)
    }

    /** Execute an arrow function with given arguments */
    fun executeArrowFunction(fn: ArrowFunction, args: List<Any?>): Any? {
        LOG?.debug("executeArrowFunction: fn=$fn, args=$args")

        // Save current variable values for parameters (to restore later)
        val savedValues = fn.parameters.map { it to scriptContext.getVariable(it) }

        // Bind arguments to parameters
        fn.parameters.forEachIndexed { index, param ->
            val argValue = args.getOrNull(index)
            LOG?.debug("  Setting param '$param' = $argValue (type: ${argValue?.javaClass})")
            scriptContext.setVariable(param, argValue)
        }

        LOG?.debug("  Evaluating body: '${fn.body}' (isExpression=${fn.isExpression})")
        val result = if (fn.isExpression) {
            // Expression body: evaluate directly
            val r = evaluateExpression(fn.body)
            LOG?.debug("  Expression result: $r (type: ${r?.javaClass})")
            r
        } else {
            // Block body: look for return statement
            val returnMatch = Regex("return\\s+(.+)").find(fn.body)
            if (returnMatch != null) {
                evaluateExpression(returnMatch.groupValues[1].trim())
            } else {
                evaluateExpression(fn.body)
            }
        }

        // Restore previous variable values
        savedValues.forEach { (param, value) ->
            if (value != null) {
                scriptContext.setVariable(param, value)
            } else {
                scriptContext.removeVariable(param)
            }
        }

        return result
    }
}
