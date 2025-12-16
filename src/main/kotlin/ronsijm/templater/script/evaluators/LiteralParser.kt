package ronsijm.templater.script.evaluators

/**
 * Parses JavaScript-like literals: arrays, objects, strings, numbers, booleans.
 * Single Responsibility: Only handles literal parsing, not evaluation of complex expressions.
 */
class LiteralParser(
    private val expressionEvaluator: (String) -> Any?
) {

    /**
     * Parse an array literal like [1, 2, 3] or ["a", "b", "c"]
     */
    fun parseArrayLiteral(expression: String): MutableList<Any?> {
        val content = expression.substring(1, expression.length - 1).trim()
        if (content.isEmpty()) return mutableListOf()

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
                    elements.add(expressionEvaluator(current.toString().trim()))
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            elements.add(expressionEvaluator(current.toString().trim()))
        }

        return elements
    }

    /**
     * Parse an object literal like { url: "http://...", method: "GET" }
     */
    fun parseObjectLiteral(expression: String): Map<String, Any?> {
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

        if (current.isNotEmpty()) {
            parseObjectProperty(current.toString().trim())?.let { (key, value) ->
                result[key] = value
            }
        }

        return result
    }

    /**
     * Parse a single object property like "url: link" or "method: 'GET'"
     */
    private fun parseObjectProperty(property: String): Pair<String, Any?>? {
        val colonIndex = property.indexOf(':')
        if (colonIndex == -1) return null

        var key = property.substring(0, colonIndex).trim()
        // Strip quotes from key if present (e.g., "bug" -> bug, 'bug' -> bug)
        if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
            key = key.substring(1, key.length - 1)
        }
        val valueExpr = property.substring(colonIndex + 1).trim()
        val value = expressionEvaluator(valueExpr)

        return key to value
    }

    /**
     * Parse a string literal (double or single quoted)
     */
    fun parseStringLiteral(expression: String): String {
        return expression.substring(1, expression.length - 1)
    }

    /**
     * Try to parse as a number (Int or Double)
     */
    fun parseNumber(expression: String): Number? {
        return expression.toIntOrNull() ?: expression.toDoubleOrNull()
    }

    /**
     * Convert a value to a Number if possible
     */
    fun toNumber(value: Any?): Number? {
        return when (value) {
            is Number -> value
            is String -> value.toIntOrNull() ?: value.toDoubleOrNull()
            else -> null
        }
    }

    companion object {
        fun isArrayLiteral(expr: String) = expr.startsWith("[") && expr.endsWith("]")
        fun isObjectLiteral(expr: String) = expr.startsWith("{") && expr.endsWith("}")
        fun isDoubleQuotedString(expr: String) = expr.startsWith("\"") && expr.endsWith("\"")
        fun isSingleQuotedString(expr: String) = expr.startsWith("'") && expr.endsWith("'")
        fun isTemplateLiteral(expr: String) = expr.startsWith("`") && expr.endsWith("`")
    }
}

