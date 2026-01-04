package ronsijm.templater.utils


object ArgumentParser {

    fun getString(args: List<Any?>, index: Int, default: String = "") =
        args.getOrNull(index)?.toString() ?: default

    fun getInt(args: List<Any?>, index: Int, default: Int? = null) =
        args.getOrNull(index)?.toString()?.toIntOrNull() ?: default

    fun getBoolean(args: List<Any?>, index: Int, default: Boolean = false) =
        args.getOrNull(index)?.toString()?.toBoolean() ?: default

    @Suppress("UNCHECKED_CAST")
    fun <T> getList(args: List<Any?>, index: Int, default: List<T> = emptyList()) =
        (args.getOrNull(index) as? List<T>) ?: default

    fun getAny(args: List<Any?>, index: Int) = args.getOrNull(index)


    fun parseArgumentString(argsString: String): List<Any?> {
        if (argsString.isBlank()) return emptyList()

        val args = mutableListOf<Any?>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '
        var inArray = 0

        for (char in argsString) {
            when {
                (char == '"' || char == '\'') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                    current.append(char)
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                    current.append(char)
                }
                char == '[' && !inQuotes -> {
                    inArray++
                    current.append(char)
                }
                char == ']' && !inQuotes -> {
                    inArray--
                    current.append(char)
                }
                char == ',' && !inQuotes && inArray == 0 -> {
                    args.add(parseValue(current.toString().trim()))
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            args.add(parseValue(current.toString().trim()))
        }

        return args
    }

    fun parseValue(value: String): Any? {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> null
            trimmed == "null" -> null
            trimmed == "true" -> true
            trimmed == "false" -> false
            trimmed.startsWith("[") && trimmed.endsWith("]") -> parseArray(trimmed.substring(1, trimmed.length - 1))
            trimmed.startsWith("\"") && trimmed.endsWith("\"") -> trimmed.substring(1, trimmed.length - 1)
            trimmed.startsWith("'") && trimmed.endsWith("'") -> trimmed.substring(1, trimmed.length - 1)
            trimmed.toIntOrNull() != null -> trimmed.toInt()
            trimmed.toDoubleOrNull() != null -> trimmed.toDouble()
            else -> trimmed
        }
    }

    private fun parseArray(content: String): List<Any?> {
        if (content.isBlank()) return emptyList()

        val items = mutableListOf<Any?>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '
        var inNestedArray = 0

        for (char in content) {
            when {
                (char == '"' || char == '\'') && !inQuotes -> {
                    inQuotes = true
                    quoteChar = char
                    current.append(char)
                }
                char == quoteChar && inQuotes -> {
                    inQuotes = false
                    current.append(char)
                }
                char == '[' && !inQuotes -> {
                    inNestedArray++
                    current.append(char)
                }
                char == ']' && !inQuotes -> {
                    inNestedArray--
                    current.append(char)
                }
                char == ',' && !inQuotes && inNestedArray == 0 -> {
                    items.add(parseValue(current.toString().trim()))
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }

        if (current.isNotEmpty()) {
            items.add(parseValue(current.toString().trim()))
        }

        return items
    }
}
