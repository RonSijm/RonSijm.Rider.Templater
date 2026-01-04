package ronsijm.templater.expression


enum class BinaryOperator(val symbol: String, val precedence: Int) {

    ADD("+", 12),
    SUBTRACT("-", 12),
    MULTIPLY("*", 13),
    DIVIDE("/", 13),
    MODULO("%", 13),
    POWER("**", 14),


    EQUAL("==", 9),
    NOT_EQUAL("!=", 9),
    STRICT_EQUAL("===", 9),
    STRICT_NOT_EQUAL("!==", 9),
    LESS_THAN("<", 10),
    LESS_THAN_OR_EQUAL("<=", 10),
    GREATER_THAN(">", 10),
    GREATER_THAN_OR_EQUAL(">=", 10),


    BITWISE_AND("&", 8),
    BITWISE_OR("|", 6),
    BITWISE_XOR("^", 7),
    LEFT_SHIFT("<<", 11),
    RIGHT_SHIFT(">>", 11),
    UNSIGNED_RIGHT_SHIFT(">>>", 11),


    CONCAT("++", 12),


    RANGE("..", 10),
    RANGE_INCLUSIVE("..=", 10);

    companion object {
        private val symbolMap = entries.associateBy { it.symbol }

        fun fromSymbol(symbol: String): BinaryOperator? = symbolMap[symbol]
    }
}


enum class UnaryOperator(val symbol: String, val isPrefix: Boolean = true) {

    NEGATE("-", true),
    PLUS("+", true),


    NOT("!", true),


    BITWISE_NOT("~", true),


    PRE_INCREMENT("++", true),
    PRE_DECREMENT("--", true),
    POST_INCREMENT("++", false),
    POST_DECREMENT("--", false),


    SPREAD("...", true);

    companion object {
        fun fromSymbol(symbol: String, isPrefix: Boolean): UnaryOperator? =
            entries.find { it.symbol == symbol && it.isPrefix == isPrefix }
    }
}


enum class AssignmentOperator(val symbol: String) {
    ASSIGN("="),
    ADD_ASSIGN("+="),
    SUBTRACT_ASSIGN("-="),
    MULTIPLY_ASSIGN("*="),
    DIVIDE_ASSIGN("/="),
    MODULO_ASSIGN("%="),
    POWER_ASSIGN("**="),
    BITWISE_AND_ASSIGN("&="),
    BITWISE_OR_ASSIGN("|="),
    BITWISE_XOR_ASSIGN("^="),
    LEFT_SHIFT_ASSIGN("<<="),
    RIGHT_SHIFT_ASSIGN(">>="),
    UNSIGNED_RIGHT_SHIFT_ASSIGN(">>>="),
    NULLISH_COALESCING_ASSIGN("??="),
    LOGICAL_AND_ASSIGN("&&="),
    LOGICAL_OR_ASSIGN("||=");

    companion object {
        private val symbolMap = entries.associateBy { it.symbol }

        fun fromSymbol(symbol: String): AssignmentOperator? = symbolMap[symbol]
    }
}

