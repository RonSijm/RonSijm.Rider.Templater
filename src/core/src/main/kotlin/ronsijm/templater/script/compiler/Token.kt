package ronsijm.templater.script.compiler


class Token(
    var type: Int = TokenType.EOF,
    var start: Int = 0,
    var end: Int = 0,
    var numValue: Double = 0.0
) {
    fun reset(type: Int, start: Int, end: Int, numValue: Double = 0.0) {
        this.type = type
        this.start = start
        this.end = end
        this.numValue = numValue
    }

    fun isOperator(): Boolean = type in 10..49
    fun isLiteral(): Boolean = type in 1..7
}
