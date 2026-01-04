package ronsijm.templater.script.executors


class ConditionEvaluator(private val expressionEvaluator: (String) -> Any?) {


    fun evaluate(condition: String): Boolean {
        val trimmed = condition.trim()



        for (op in ComparisonOperator.entries) {
            if (trimmed.contains(op.symbol)) {
                val parts = trimmed.split(op.symbol, limit = 2).map { it.trim() }
                if (parts.size == 2) {
                    val left = expressionEvaluator(parts[0])
                    val right = expressionEvaluator(parts[1])
                    return op.evaluate(left, right)
                }
            }
        }


        return isTruthy(expressionEvaluator(trimmed))
    }


    private fun isTruthy(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        is Number -> value.toDouble() != 0.0 && !value.toDouble().isNaN()
        is String -> value.isNotEmpty()
        else -> true
    }


    private fun compareValues(left: Any?, right: Any?): Int {
        return when {
            left is Number && right is Number -> {
                left.toDouble().compareTo(right.toDouble())
            }
            left is String && right is String -> {
                left.compareTo(right)
            }
            left is String && right is Number -> {
                left.toDoubleOrNull()?.compareTo(right.toDouble()) ?: left.compareTo(right.toString())
            }
            left is Number && right is String -> {
                left.toDouble().compareTo(right.toDoubleOrNull() ?: return left.toString().compareTo(right))
            }
            left == null && right == null -> 0
            left == null -> -1
            right == null -> 1
            else -> left.toString().compareTo(right.toString())
        }
    }
}


enum class ComparisonOperator(val symbol: String) {
    STRICT_EQUAL("==="),
    STRICT_NOT_EQUAL("!=="),
    LESS_EQUAL("<="),
    GREATER_EQUAL(">="),
    EQUAL("=="),
    NOT_EQUAL("!="),
    LESS("<"),
    GREATER(">");


    fun evaluate(left: Any?, right: Any?): Boolean {
        return when (this) {
            STRICT_EQUAL -> left == right
            STRICT_NOT_EQUAL -> left != right
            LESS_EQUAL -> compareValues(left, right) <= 0
            GREATER_EQUAL -> compareValues(left, right) >= 0
            EQUAL -> compareValues(left, right) == 0
            NOT_EQUAL -> compareValues(left, right) != 0
            LESS -> compareValues(left, right) < 0
            GREATER -> compareValues(left, right) > 0
        }
    }

    private fun compareValues(left: Any?, right: Any?): Int {
        return when {
            left is Number && right is Number -> {
                left.toDouble().compareTo(right.toDouble())
            }
            left is String && right is String -> {
                left.compareTo(right)
            }
            left is String && right is Number -> {
                left.toDoubleOrNull()?.compareTo(right.toDouble()) ?: left.compareTo(right.toString())
            }
            left is Number && right is String -> {
                left.toDouble().compareTo(right.toDoubleOrNull() ?: return left.toString().compareTo(right))
            }
            left == null && right == null -> 0
            left == null -> -1
            right == null -> 1
            else -> left.toString().compareTo(right.toString())
        }
    }
}
