package ronsijm.templater.script.evaluators


class LiteralParser(
    private val expressionEvaluator: (String) -> Any?
) {


    private fun isQuoteEscaped(str: String, i: Int): Boolean {
        if (i == 0) return false
        var backslashCount = 0
        var j = i - 1
        while (j >= 0 && str[j] == '\\') {
            backslashCount++
            j--
        }
        return backslashCount % 2 == 1
    }


    fun parseArrayLiteral(expression: String): MutableList<Any?> {
        val content = expression.substring(1, expression.length - 1).trim()
        if (content.isEmpty()) return mutableListOf()

        val elements = mutableListOf<Any?>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '
        var depth = 0
        var i = 0

        while (i < content.length) {
            val char = content[i]

            when {
                (char == '"' || char == '\'') && depth == 0 && !isQuoteEscaped(content, i) -> {
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
            i++
        }

        if (current.isNotEmpty()) {
            elements.add(expressionEvaluator(current.toString().trim()))
        }

        return elements
    }


    fun parseObjectLiteral(expression: String): Map<String, Any?> {
        val content = expression.substring(1, expression.length - 1).trim()
        if (content.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, Any?>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '
        var depth = 0
        var i = 0

        while (i < content.length) {
            val char = content[i]

            when {
                (char == '"' || char == '\'') && depth == 0 && !isQuoteEscaped(content, i) -> {
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
            i++
        }

        if (current.isNotEmpty()) {
            parseObjectProperty(current.toString().trim())?.let { (key, value) ->
                result[key] = value
            }
        }

        return result
    }


    private fun parseObjectProperty(property: String): Pair<String, Any?>? {

        val colonIndex = findTopLevelColon(property)
        if (colonIndex == -1) return null

        var key = property.substring(0, colonIndex).trim()

        if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
            key = key.substring(1, key.length - 1)
        }
        val valueExpr = property.substring(colonIndex + 1).trim()
        val value = expressionEvaluator(valueExpr)

        return key to value
    }


    private fun findTopLevelColon(str: String): Int {
        var depth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in str.indices) {
            val char = str[i]

            when {
                (char == '"' || char == '\'') && depth == 0 && !isQuoteEscaped(str, i) -> {
                    if (!inQuotes) {
                        inQuotes = true
                        quoteChar = char
                    } else if (char == quoteChar) {
                        inQuotes = false
                    }
                }
                (char == '{' || char == '[') && !inQuotes -> depth++
                (char == '}' || char == ']') && !inQuotes -> depth--
                char == ':' && !inQuotes && depth == 0 -> return i
            }
        }
        return -1
    }


    fun parseStringLiteral(expression: String): String {
        val content = expression.substring(1, expression.length - 1)
        val result = StringBuilder()
        var i = 0

        while (i < content.length) {
            if (content[i] == '\\' && i + 1 < content.length) {

                when (content[i + 1]) {
                    '\'' -> result.append('\'')
                    '"' -> result.append('"')
                    '\\' -> result.append('\\')
                    'n' -> result.append('\n')
                    't' -> result.append('\t')
                    'r' -> result.append('\r')
                    else -> {

                        result.append(content[i])
                        result.append(content[i + 1])
                    }
                }
                i += 2
            } else {
                result.append(content[i])
                i++
            }
        }

        return result.toString()
    }


    fun parseNumber(expression: String): Number? {
        return expression.toIntOrNull() ?: expression.toDoubleOrNull()
    }


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
