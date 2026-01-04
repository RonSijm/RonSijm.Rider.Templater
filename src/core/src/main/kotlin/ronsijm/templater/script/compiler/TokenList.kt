package ronsijm.templater.script.compiler


class TokenList(initialCapacity: Int = 64) {
    private var tokens = Array(initialCapacity) { Token() }
    var size: Int = 0
        private set

    fun clear() {
        size = 0
    }

    fun add(type: Int, start: Int, end: Int, numValue: Double = 0.0): Token {
        if (size >= tokens.size) {
            val newSize = tokens.size * 2
            val newTokens = Array(newSize) { i ->
                if (i < tokens.size) tokens[i] else Token()
            }
            tokens = newTokens
        }
        val token = tokens[size++]
        token.reset(type, start, end, numValue)
        return token
    }

    operator fun get(index: Int): Token = tokens[index]
}

