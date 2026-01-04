package ronsijm.templater.script.compiler


object TokenType {
    const val EOF = 0
    const val NUMBER = 1
    const val STRING = 2
    const val IDENTIFIER = 3
    const val TRUE = 4
    const val FALSE = 5
    const val NULL = 6
    const val UNDEFINED = 7


    const val PLUS = 10
    const val MINUS = 11
    const val STAR = 12
    const val SLASH = 13
    const val PERCENT = 14


    const val UNSIGNED_RIGHT_SHIFT = 20
    const val RIGHT_SHIFT = 21
    const val LEFT_SHIFT = 22
    const val BITWISE_AND = 23
    const val BITWISE_OR = 24
    const val BITWISE_XOR = 25


    const val STRICT_EQUAL = 30
    const val STRICT_NOT_EQUAL = 31
    const val EQUAL = 32
    const val NOT_EQUAL = 33
    const val GREATER_EQUAL = 34
    const val LESS_EQUAL = 35
    const val GREATER = 36
    const val LESS = 37


    const val AND = 40
    const val OR = 41
    const val NOT = 42


    const val LPAREN = 50
    const val RPAREN = 51
    const val LBRACKET = 52
    const val RBRACKET = 53
    const val LBRACE = 54
    const val RBRACE = 55
    const val COMMA = 56
    const val DOT = 57
    const val COLON = 58
    const val SEMICOLON = 59
    const val QUESTION = 60


    const val ASSIGN = 70
    const val PLUS_ASSIGN = 71
    const val MINUS_ASSIGN = 72
    const val STAR_ASSIGN = 73
    const val SLASH_ASSIGN = 74


    const val TYPEOF = 80
    const val NEW = 81
    const val AWAIT = 82


    const val ARROW = 90
}

