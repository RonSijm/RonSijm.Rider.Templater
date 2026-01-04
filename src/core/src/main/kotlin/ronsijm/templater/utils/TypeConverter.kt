package ronsijm.templater.utils


object TypeConverter {

    fun toInt(value: Any?): Int = when (value) {
        null -> 0
        is Number -> value.toInt()
        is Boolean -> if (value) 1 else 0
        is String -> value.trim().toIntOrNull() ?: 0
        else -> 0
    }


    fun toDouble(value: Any?): Double = when (value) {
        null -> 0.0
        is Number -> value.toDouble()
        is Boolean -> if (value) 1.0 else 0.0
        is String -> {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) 0.0
            else trimmed.toDoubleOrNull() ?: Double.NaN
        }
        else -> Double.NaN
    }


    fun toBoolean(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        is Number -> value.toDouble() != 0.0 && !value.toDouble().isNaN()
        is String -> value.isNotEmpty()
        else -> true
    }


    fun isIntLike(value: Any?): Boolean = when (value) {
        is Int, is Long, is Short, is Byte -> true
        is Double -> value.isFinite() && value == value.toLong().toDouble()
        is Float -> value.isFinite() && value == value.toLong().toFloat()
        else -> false
    }


    fun toIntOrNull(value: Any?): Int? = when (value) {
        is Number -> value.toInt()
        is String -> value.trim().toIntOrNull()
        else -> null
    }


    fun toDoubleOrNull(value: Any?): Double? = when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }
}

