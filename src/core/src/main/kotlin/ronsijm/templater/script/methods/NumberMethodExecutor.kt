package ronsijm.templater.script.methods


object NumberMethodExecutor {


    fun execute(num: Number, methodName: String, args: List<Any?>): Any? {
        return when (methodName) {
            "toFixed" -> {
                val digits = (args.getOrNull(0) as? Number)?.toInt() ?: 0
                String.format("%.${digits}f", num.toDouble())
            }
            "toString" -> {
                val radix = (args.getOrNull(0) as? Number)?.toInt()
                if (radix != null && radix in 2..36) {
                    num.toLong().toString(radix)
                } else {
                    num.toString()
                }
            }
            "toExponential" -> {
                val digits = (args.getOrNull(0) as? Number)?.toInt() ?: 6
                String.format("%.${digits}e", num.toDouble())
            }
            "toPrecision" -> {
                val precision = (args.getOrNull(0) as? Number)?.toInt() ?: 6
                String.format("%.${precision}g", num.toDouble())
            }
            "valueOf" -> num
            else -> null
        }
    }


    fun isSupported(methodName: String): Boolean {
        return methodName in SUPPORTED_METHODS
    }

    private val SUPPORTED_METHODS = setOf(
        "toFixed", "toString", "toExponential", "toPrecision", "valueOf"
    )
}
