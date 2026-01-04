package ronsijm.templater.script.builtins

import ronsijm.templater.utils.TypeConverter


object GlobalFunctions {


    fun execute(functionName: String, args: List<Any?>): Any? {
        return when (functionName) {
            "parseInt" -> {
                val value = args.getOrNull(0)
                val radix = (args.getOrNull(1) as? Number)?.toInt() ?: 10
                parseIntJs(value, radix)
            }
            "parseFloat" -> {
                val value = args.getOrNull(0)
                parseFloatJs(value)
            }
            "String" -> {
                args.getOrNull(0)?.toString() ?: "undefined"
            }
            "Number" -> {
                val value = args.getOrNull(0)
                toNumberJs(value)
            }
            "Boolean" -> {
                val value = args.getOrNull(0)
                toBooleanJs(value)
            }
            "isNaN" -> {
                val value = args.getOrNull(0)
                val num = toNumberJs(value)
                num is Double && num.isNaN()
            }
            "isFinite" -> {
                val value = args.getOrNull(0)
                val num = toNumberJs(value)
                num is Number && num.toDouble().isFinite()
            }
            "Array" -> {

                val length = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                MutableList<Any?>(length) { null }
            }
            "console.log" -> {

                println(args.joinToString(" ") { it?.toString() ?: "undefined" })
                null
            }
            else -> null
        }
    }


    fun isSupported(functionName: String): Boolean {
        return functionName in SUPPORTED_FUNCTIONS
    }


    private fun parseIntJs(value: Any?, radix: Int): Any {
        val str = value?.toString()?.trim() ?: return Double.NaN
        if (str.isEmpty()) return Double.NaN


        var i = 0
        var negative = false
        if (str[i] == '-') {
            negative = true
            i++
        } else if (str[i] == '+') {
            i++
        }


        var result = 0L
        var hasDigits = false
        while (i < str.length) {
            val c = str[i]
            val digit = when {
                c in '0'..'9' -> c - '0'
                c in 'a'..'z' -> c - 'a' + 10
                c in 'A'..'Z' -> c - 'A' + 10
                else -> break
            }
            if (digit >= radix) break
            result = result * radix + digit
            hasDigits = true
            i++
        }

        if (!hasDigits) return Double.NaN
        return if (negative) -result.toInt() else result.toInt()
    }


    private fun parseFloatJs(value: Any?): Any {
        val str = value?.toString()?.trim() ?: return Double.NaN
        return str.toDoubleOrNull() ?: Double.NaN
    }


    private fun toNumberJs(value: Any?): Any = TypeConverter.toDouble(value)


    private fun toBooleanJs(value: Any?): Boolean = TypeConverter.toBoolean(value)

    private val SUPPORTED_FUNCTIONS = setOf(
        "parseInt", "parseFloat", "String", "Number", "Boolean",
        "isNaN", "isFinite", "Array", "console.log"
    )
}
