package ronsijm.templater.script.compiler


class Tokenizer {
    private var source: CharArray = CharArray(0)
    private var sourceString: String = ""
    private var pos: Int = 0
    private var length: Int = 0

    private val tokens = TokenList(64)


    fun tokenize(expr: String): TokenList {
        sourceString = expr
        source = expr.toCharArray()
        pos = 0
        length = source.size
        tokens.clear()

        while (pos < length) {
            skipWhitespace()
            if (pos >= length) break

            val c = source[pos]
            when {
                c.isDigit() || (c == '.' && pos + 1 < length && source[pos + 1].isDigit()) -> scanNumber()
                c == '"' || c == '\'' || c == '`' -> scanString(c)
                c.isLetter() || c == '_' || c == '$' -> scanIdentifier()
                else -> scanOperator()
            }
        }

        tokens.add(TokenType.EOF, pos, pos)
        return tokens
    }

    private fun skipWhitespace() {
        while (pos < length && source[pos].isWhitespace()) {
            pos++
        }
    }

    private fun scanNumber() {
        val start = pos
        var value = 0.0
        var hasDecimal = false
        var decimalPlace = 0.1


        if (source[pos] == '.') {
            hasDecimal = true
            pos++
        }

        while (pos < length) {
            val c = source[pos]
            when {
                c.isDigit() -> {
                    if (hasDecimal) {
                        value += (c - '0') * decimalPlace
                        decimalPlace *= 0.1
                    } else {
                        value = value * 10 + (c - '0')
                    }
                    pos++
                }
                c == '.' && !hasDecimal -> {
                    hasDecimal = true
                    pos++
                }
                else -> break
            }
        }


        if (pos < length && (source[pos] == 'e' || source[pos] == 'E')) {
            pos++
            var expSign = 1
            if (pos < length && (source[pos] == '+' || source[pos] == '-')) {
                if (source[pos] == '-') expSign = -1
                pos++
            }
            var exp = 0
            while (pos < length && source[pos].isDigit()) {
                exp = exp * 10 + (source[pos] - '0')
                pos++
            }
            value *= Math.pow(10.0, (expSign * exp).toDouble())
        }

        tokens.add(TokenType.NUMBER, start, pos, value)
    }

    private fun scanString(quote: Char) {
        val start = pos
        pos++

        while (pos < length && source[pos] != quote) {
            if (source[pos] == '\\' && pos + 1 < length) {
                pos += 2
            } else {
                pos++
            }
        }

        if (pos < length) pos++

        tokens.add(TokenType.STRING, start, pos)
    }

    private fun scanIdentifier() {
        val start = pos
        while (pos < length && (source[pos].isLetterOrDigit() || source[pos] == '_' || source[pos] == '$')) {
            pos++
        }


        val type = when (val id = sourceString.substring(start, pos)) {
            "true" -> TokenType.TRUE
            "false" -> TokenType.FALSE
            "null" -> TokenType.NULL
            "undefined" -> TokenType.UNDEFINED
            "typeof" -> TokenType.TYPEOF
            "new" -> TokenType.NEW
            "await" -> TokenType.AWAIT
            else -> TokenType.IDENTIFIER
        }

        tokens.add(type, start, pos)
    }

    private fun scanOperator() {
        val start = pos
        val c = source[pos]
        val next = if (pos + 1 < length) source[pos + 1] else '\u0000'
        val next2 = if (pos + 2 < length) source[pos + 2] else '\u0000'

        val (type, len) = when {

            c == '>' && next == '>' && next2 == '>' -> TokenType.UNSIGNED_RIGHT_SHIFT to 3
            c == '=' && next == '=' && next2 == '=' -> TokenType.STRICT_EQUAL to 3
            c == '!' && next == '=' && next2 == '=' -> TokenType.STRICT_NOT_EQUAL to 3

            c == '>' && next == '>' -> TokenType.RIGHT_SHIFT to 2
            c == '<' && next == '<' -> TokenType.LEFT_SHIFT to 2
            c == '>' && next == '=' -> TokenType.GREATER_EQUAL to 2
            c == '<' && next == '=' -> TokenType.LESS_EQUAL to 2
            c == '=' && next == '=' -> TokenType.EQUAL to 2
            c == '!' && next == '=' -> TokenType.NOT_EQUAL to 2
            c == '&' && next == '&' -> TokenType.AND to 2
            c == '|' && next == '|' -> TokenType.OR to 2
            c == '=' && next == '>' -> TokenType.ARROW to 2
            c == '+' && next == '=' -> TokenType.PLUS_ASSIGN to 2
            c == '-' && next == '=' -> TokenType.MINUS_ASSIGN to 2
            c == '*' && next == '=' -> TokenType.STAR_ASSIGN to 2
            c == '/' && next == '=' -> TokenType.SLASH_ASSIGN to 2

            c == '+' -> TokenType.PLUS to 1
            c == '-' -> TokenType.MINUS to 1
            c == '*' -> TokenType.STAR to 1
            c == '/' -> TokenType.SLASH to 1
            c == '%' -> TokenType.PERCENT to 1
            c == '>' -> TokenType.GREATER to 1
            c == '<' -> TokenType.LESS to 1
            c == '!' -> TokenType.NOT to 1
            c == '&' -> TokenType.BITWISE_AND to 1
            c == '|' -> TokenType.BITWISE_OR to 1
            c == '^' -> TokenType.BITWISE_XOR to 1
            c == '(' -> TokenType.LPAREN to 1
            c == ')' -> TokenType.RPAREN to 1
            c == '[' -> TokenType.LBRACKET to 1
            c == ']' -> TokenType.RBRACKET to 1
            c == '{' -> TokenType.LBRACE to 1
            c == '}' -> TokenType.RBRACE to 1
            c == ',' -> TokenType.COMMA to 1
            c == '.' -> TokenType.DOT to 1
            c == ':' -> TokenType.COLON to 1
            c == ';' -> TokenType.SEMICOLON to 1
            c == '?' -> TokenType.QUESTION to 1
            c == '=' -> TokenType.ASSIGN to 1
            else -> {
                pos++
                return
            }
        }

        pos += len
        tokens.add(type, start, pos)
    }


    fun getSource(): String = sourceString
}
