package ronsijm.templater.script.ast


enum class TokenType {

    NUMBER,
    STRING,
    TEMPLATE_LITERAL,
    TRUE,
    FALSE,
    NULL,
    UNDEFINED,


    IDENTIFIER,
    LET,
    CONST,
    VAR,
    FUNCTION,
    RETURN,
    IF,
    ELSE,
    FOR,
    OF,
    IN,
    WHILE,
    BREAK,
    CONTINUE,
    TRY,
    CATCH,
    FINALLY,
    THROW,
    NEW,
    TYPEOF,


    PLUS,
    MINUS,
    STAR,
    SLASH,
    PERCENT,


    EQUALS_EQUALS,
    NOT_EQUALS,
    STRICT_EQUALS,
    STRICT_NOT_EQUALS,
    LESS_THAN,
    LESS_THAN_EQUALS,
    GREATER_THAN,
    GREATER_THAN_EQUALS,


    AND,
    OR,
    NOT,


    AMPERSAND,
    PIPE,
    CARET,
    TILDE,
    LEFT_SHIFT,
    RIGHT_SHIFT,
    UNSIGNED_RIGHT_SHIFT,


    EQUALS,
    PLUS_EQUALS,
    MINUS_EQUALS,
    STAR_EQUALS,
    SLASH_EQUALS,
    PERCENT_EQUALS,


    PLUS_PLUS,
    MINUS_MINUS,


    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    LEFT_BRACKET,
    RIGHT_BRACKET,
    COMMA,
    DOT,
    SEMICOLON,
    COLON,
    QUESTION,
    ARROW,


    EOF,
    ERROR
}


data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any?,
    val location: SourceLocation
) {
    override fun toString(): String = "$type '$lexeme' at ${location.line}:${location.column}"
}


@Suppress("MemberNameEqualsClassName")
object Keywords {
    private val keywords = mapOf(
        "let" to TokenType.LET,
        "const" to TokenType.CONST,
        "var" to TokenType.VAR,
        "function" to TokenType.FUNCTION,
        "return" to TokenType.RETURN,
        "if" to TokenType.IF,
        "else" to TokenType.ELSE,
        "for" to TokenType.FOR,
        "of" to TokenType.OF,
        "in" to TokenType.IN,
        "while" to TokenType.WHILE,
        "break" to TokenType.BREAK,
        "continue" to TokenType.CONTINUE,
        "try" to TokenType.TRY,
        "catch" to TokenType.CATCH,
        "finally" to TokenType.FINALLY,
        "throw" to TokenType.THROW,
        "new" to TokenType.NEW,
        "typeof" to TokenType.TYPEOF,
        "true" to TokenType.TRUE,
        "false" to TokenType.FALSE,
        "null" to TokenType.NULL,
        "undefined" to TokenType.UNDEFINED
    )

    fun get(text: String): TokenType? = keywords[text]
}

