package ronsijm.templater.script.ast


class Lexer(private val source: String) {
    private var start = 0
    private var current = 0
    private var line = 1
    private var column = 1
    private var lineStart = 0

    private val tokens = mutableListOf<Token>()

    class LexerError(message: String, val location: SourceLocation) : RuntimeException(message)

    fun tokenize(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, currentLocation()))
        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {

            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            '[' -> addToken(TokenType.LEFT_BRACKET)
            ']' -> addToken(TokenType.RIGHT_BRACKET)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            ';' -> addToken(TokenType.SEMICOLON)
            ':' -> addToken(TokenType.COLON)
            '?' -> addToken(TokenType.QUESTION)
            '~' -> addToken(TokenType.TILDE)


            '+' -> addToken(when {
                match('+') -> TokenType.PLUS_PLUS
                match('=') -> TokenType.PLUS_EQUALS
                else -> TokenType.PLUS
            })
            '-' -> addToken(when {
                match('-') -> TokenType.MINUS_MINUS
                match('=') -> TokenType.MINUS_EQUALS
                else -> TokenType.MINUS
            })
            '*' -> addToken(if (match('=')) TokenType.STAR_EQUALS else TokenType.STAR)
            '/' -> {
                when {
                    match('/') -> skipLineComment()
                    match('*') -> skipBlockComment()
                    match('=') -> addToken(TokenType.SLASH_EQUALS)
                    else -> addToken(TokenType.SLASH)
                }
            }
            '%' -> addToken(if (match('=')) TokenType.PERCENT_EQUALS else TokenType.PERCENT)

            '=' -> addToken(when {
                match('=') -> if (match('=')) TokenType.STRICT_EQUALS else TokenType.EQUALS_EQUALS
                match('>') -> TokenType.ARROW
                else -> TokenType.EQUALS
            })
            '!' -> addToken(when {
                match('=') -> if (match('=')) TokenType.STRICT_NOT_EQUALS else TokenType.NOT_EQUALS
                else -> TokenType.NOT
            })
            '<' -> addToken(when {
                match('<') -> TokenType.LEFT_SHIFT
                match('=') -> TokenType.LESS_THAN_EQUALS
                else -> TokenType.LESS_THAN
            })
            '>' -> addToken(when {
                match('>') -> if (match('>')) TokenType.UNSIGNED_RIGHT_SHIFT else TokenType.RIGHT_SHIFT
                match('=') -> TokenType.GREATER_THAN_EQUALS
                else -> TokenType.GREATER_THAN
            })
            '&' -> addToken(if (match('&')) TokenType.AND else TokenType.AMPERSAND)
            '|' -> addToken(if (match('|')) TokenType.OR else TokenType.PIPE)
            '^' -> addToken(TokenType.CARET)


            ' ', '\r', '\t' -> {  }
            '\n' -> newLine()


            '"' -> string('"')
            '\'' -> string('\'')
            '`' -> templateLiteral()

            else -> when {
                c.isDigit() -> number()
                c.isIdentifierStart() -> identifier()
                else -> addErrorToken("Unexpected character: $c")
            }
        }
    }

    private fun string(quote: Char) {
        val sb = StringBuilder()
        while (!isAtEnd() && peek() != quote) {
            if (peek() == '\n') newLine()
            if (peek() == '\\' && peekNext() != '\u0000') {
                advance()
                sb.append(escapeChar(advance()))
            } else {
                sb.append(advance())
            }
        }
        if (isAtEnd()) {
            addErrorToken("Unterminated string")
            return
        }
        advance()
        addToken(TokenType.STRING, sb.toString())
    }

    private fun escapeChar(c: Char): Char = when (c) {
        'n' -> '\n'
        't' -> '\t'
        'r' -> '\r'
        '\\' -> '\\'
        '"' -> '"'
        '\'' -> '\''
        '`' -> '`'
        else -> c
    }

    private fun templateLiteral() {

        val sb = StringBuilder()
        while (!isAtEnd() && peek() != '`') {
            if (peek() == '\n') newLine()
            if (peek() == '\\' && peekNext() != '\u0000') {
                advance()
                sb.append(escapeChar(advance()))
            } else {
                sb.append(advance())
            }
        }
        if (isAtEnd()) {
            addErrorToken("Unterminated template literal")
            return
        }
        advance()
        addToken(TokenType.TEMPLATE_LITERAL, sb.toString())
    }

    private fun number() {
        while (peek().isDigit()) advance()
        if (peek() == '.' && peekNext().isDigit()) {
            advance()
            while (peek().isDigit()) advance()
        }
        val text = source.substring(start, current)
        val value: Number = if ('.' in text) text.toDouble() else text.toIntOrNull() ?: text.toLong()
        addToken(TokenType.NUMBER, value)
    }

    private fun identifier() {
        while (peek().isIdentifierPart()) advance()
        val text = source.substring(start, current)
        val type = Keywords.get(text) ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun skipLineComment() {
        while (peek() != '\n' && !isAtEnd()) advance()
    }

    private fun skipBlockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance()
                advance()
                return
            }
            if (peek() == '\n') newLine()
            advance()
        }
        addErrorToken("Unterminated block comment")
    }



    private fun isAtEnd(): Boolean = current >= source.length

    private fun advance(): Char {
        val c = source[current++]
        column++
        return c
    }

    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[current]

    private fun peekNext(): Char = if (current + 1 >= source.length) '\u0000' else source[current + 1]

    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected) return false
        current++
        column++
        return true
    }

    private fun newLine() {
        line++
        lineStart = current
        column = 1
    }

    private fun currentLocation(): SourceLocation {
        val startColumn = start - lineStart + 1
        return SourceLocation(line, startColumn, current - start)
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, currentLocation()))
    }

    private fun addErrorToken(message: String) {
        tokens.add(Token(TokenType.ERROR, message, null, currentLocation()))
    }

    private fun Char.isIdentifierStart(): Boolean = this.isLetter() || this == '_' || this == '$'
    private fun Char.isIdentifierPart(): Boolean = this.isLetterOrDigit() || this == '_' || this == '$'
}

