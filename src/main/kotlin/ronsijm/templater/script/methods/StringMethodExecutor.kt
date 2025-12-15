package ronsijm.templater.script.methods

/**
 * Executes JavaScript-like string methods on String values.
 * Extracted from ScriptEvaluator to improve code organization.
 */
object StringMethodExecutor {

    /**
     * Execute a method on a String value
     * @param str The string to operate on
     * @param methodName The method name (e.g., "split", "trim", "toLowerCase")
     * @param args The method arguments
     * @return The result of the method call, or null if method not supported
     */
    fun execute(str: String, methodName: String, args: List<Any?>): Any? {
        return when (methodName) {
            "split" -> {
                val delimiter = args.getOrNull(0)?.toString() ?: ""
                str.split(delimiter)
            }
            "trim" -> str.trim()
            "trimStart", "trimLeft" -> str.trimStart()
            "trimEnd", "trimRight" -> str.trimEnd()
            "toLowerCase", "toLocaleLowerCase" -> str.lowercase()
            "toUpperCase", "toLocaleUpperCase" -> str.uppercase()
            "substring" -> {
                val start = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val end = (args.getOrNull(1) as? Number)?.toInt() ?: str.length
                str.substring(start.coerceIn(0, str.length), end.coerceIn(0, str.length))
            }
            "slice" -> {
                val start = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val end = (args.getOrNull(1) as? Number)?.toInt() ?: str.length
                val actualStart = if (start < 0) (str.length + start).coerceAtLeast(0) else start
                val actualEnd = if (end < 0) (str.length + end).coerceAtLeast(0) else end
                str.substring(actualStart.coerceIn(0, str.length), actualEnd.coerceIn(0, str.length))
            }
            "charAt" -> {
                val index = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                if (index in str.indices) str[index].toString() else ""
            }
            "indexOf" -> {
                val searchStr = args.getOrNull(0)?.toString() ?: ""
                str.indexOf(searchStr)
            }
            "lastIndexOf" -> {
                val searchStr = args.getOrNull(0)?.toString() ?: ""
                str.lastIndexOf(searchStr)
            }
            "includes", "contains" -> {
                val searchStr = args.getOrNull(0)?.toString() ?: ""
                str.contains(searchStr)
            }
            "startsWith" -> {
                val prefix = args.getOrNull(0)?.toString() ?: ""
                str.startsWith(prefix)
            }
            "endsWith" -> {
                val suffix = args.getOrNull(0)?.toString() ?: ""
                str.endsWith(suffix)
            }
            "replace" -> {
                val search = args.getOrNull(0)?.toString() ?: ""
                val replacement = args.getOrNull(1)?.toString() ?: ""
                str.replaceFirst(search, replacement)
            }
            "replaceAll" -> {
                val search = args.getOrNull(0)?.toString() ?: ""
                val replacement = args.getOrNull(1)?.toString() ?: ""
                str.replace(search, replacement)
            }
            "padStart" -> {
                val targetLength = (args.getOrNull(0) as? Number)?.toInt() ?: str.length
                val padString = args.getOrNull(1)?.toString() ?: " "
                str.padStart(targetLength, padString.firstOrNull() ?: ' ')
            }
            "padEnd" -> {
                val targetLength = (args.getOrNull(0) as? Number)?.toInt() ?: str.length
                val padString = args.getOrNull(1)?.toString() ?: " "
                str.padEnd(targetLength, padString.firstOrNull() ?: ' ')
            }
            "repeat" -> {
                val count = (args.getOrNull(0) as? Number)?.toInt() ?: 1
                str.repeat(count.coerceAtLeast(0))
            }
            "length" -> str.length
            else -> null
        }
    }

    /**
     * Check if a method name is a supported string method
     */
    fun isSupported(methodName: String): Boolean {
        return methodName in SUPPORTED_METHODS
    }

    private val SUPPORTED_METHODS = setOf(
        "split", "trim", "trimStart", "trimLeft", "trimEnd", "trimRight",
        "toLowerCase", "toLocaleLowerCase", "toUpperCase", "toLocaleUpperCase",
        "substring", "slice", "charAt", "indexOf", "lastIndexOf",
        "includes", "contains", "startsWith", "endsWith",
        "replace", "replaceAll", "padStart", "padEnd", "repeat", "length"
    )
}

