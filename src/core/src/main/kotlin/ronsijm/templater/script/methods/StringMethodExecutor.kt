package ronsijm.templater.script.methods


object StringMethodExecutor {


    fun execute(str: Any, methodName: String, args: List<Any?>): Any? {

        val strValue = when (str) {
            is StringBuilder -> str.toString()
            is String -> str
            else -> str.toString()
        }
        return when (methodName) {
            "split" -> {
                val delimiter = args.getOrNull(0)?.toString() ?: ""
                if (delimiter.isEmpty()) {

                    strValue.toList().map { it.toString() }
                } else {
                    strValue.split(delimiter)
                }
            }
            "trim" -> strValue.trim()
            "trimStart", "trimLeft" -> strValue.trimStart()
            "trimEnd", "trimRight" -> strValue.trimEnd()
            "toLowerCase", "toLocaleLowerCase" -> strValue.lowercase()
            "toUpperCase", "toLocaleUpperCase" -> strValue.uppercase()
            "substring" -> {
                val start = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val end = (args.getOrNull(1) as? Number)?.toInt() ?: strValue.length
                strValue.substring(start.coerceIn(0, strValue.length), end.coerceIn(0, strValue.length))
            }
            "slice" -> {
                val start = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                val end = (args.getOrNull(1) as? Number)?.toInt() ?: strValue.length
                val actualStart = if (start < 0) (strValue.length + start).coerceAtLeast(0) else start
                val actualEnd = if (end < 0) (strValue.length + end).coerceAtLeast(0) else end
                strValue.substring(actualStart.coerceIn(0, strValue.length), actualEnd.coerceIn(0, strValue.length))
            }
            "charAt" -> {
                val index = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                if (index in strValue.indices) strValue[index].toString() else ""
            }
            "charCodeAt" -> {
                val index = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                if (index in strValue.indices) strValue[index].code else Double.NaN
            }
            "indexOf" -> {
                val searchStr = args.getOrNull(0)?.toString() ?: ""
                strValue.indexOf(searchStr)
            }
            "lastIndexOf" -> {
                val searchStr = args.getOrNull(0)?.toString() ?: ""
                strValue.lastIndexOf(searchStr)
            }
            "includes", "contains" -> {
                val searchStr = args.getOrNull(0)?.toString() ?: ""
                strValue.contains(searchStr)
            }
            "startsWith" -> {
                val prefix = args.getOrNull(0)?.toString() ?: ""
                strValue.startsWith(prefix)
            }
            "endsWith" -> {
                val suffix = args.getOrNull(0)?.toString() ?: ""
                strValue.endsWith(suffix)
            }
            "replace" -> {
                val search = args.getOrNull(0)?.toString() ?: ""
                val replacement = args.getOrNull(1)?.toString() ?: ""
                strValue.replaceFirst(search, replacement)
            }
            "replaceAll" -> {
                val search = args.getOrNull(0)?.toString() ?: ""
                val replacement = args.getOrNull(1)?.toString() ?: ""
                strValue.replace(search, replacement)
            }
            "padStart" -> {
                val targetLength = (args.getOrNull(0) as? Number)?.toInt() ?: strValue.length
                val padString = args.getOrNull(1)?.toString() ?: " "
                strValue.padStart(targetLength, padString.firstOrNull() ?: ' ')
            }
            "padEnd" -> {
                val targetLength = (args.getOrNull(0) as? Number)?.toInt() ?: strValue.length
                val padString = args.getOrNull(1)?.toString() ?: " "
                strValue.padEnd(targetLength, padString.firstOrNull() ?: ' ')
            }
            "repeat" -> {
                val count = (args.getOrNull(0) as? Number)?.toInt() ?: 1
                strValue.repeat(count.coerceAtLeast(0))
            }
            "length" -> strValue.length
            else -> null
        }
    }


    fun isSupported(methodName: String): Boolean {
        return methodName in SUPPORTED_METHODS
    }

    private val SUPPORTED_METHODS = setOf(
        "split", "trim", "trimStart", "trimLeft", "trimEnd", "trimRight",
        "toLowerCase", "toLocaleLowerCase", "toUpperCase", "toLocaleUpperCase",
        "substring", "slice", "charAt", "charCodeAt", "indexOf", "lastIndexOf",
        "includes", "contains", "startsWith", "endsWith",
        "replace", "replaceAll", "padStart", "padEnd", "repeat", "length"
    )
}
