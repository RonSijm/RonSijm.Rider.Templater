package ronsijm.templater.script.compiler


object OpCode {
    const val NOP = 0


    const val PUSH_CONST = 1
    const val PUSH_INT = 2
    const val PUSH_TRUE = 3
    const val PUSH_FALSE = 4
    const val PUSH_NULL = 5
    const val PUSH_UNDEFINED = 6
    const val POP = 7
    const val DUP = 8
    const val PUSH_STRING = 9


    const val LOAD_VAR = 10
    const val STORE_VAR = 11


    const val ADD = 20
    const val SUB = 21
    const val MUL = 22
    const val DIV = 23
    const val MOD = 24
    const val NEG = 25


    const val USHR = 30
    const val SHR = 31
    const val SHL = 32
    const val BAND = 33
    const val BOR = 34
    const val BXOR = 35
    const val BNOT = 36


    const val EQ = 40
    const val SEQ = 41
    const val NE = 42
    const val SNE = 43
    const val LT = 44
    const val LE = 45
    const val GT = 46
    const val GE = 47


    const val NOT = 50
    const val AND = 51
    const val OR = 52


    const val JMP = 60
    const val JMP_IF_FALSE = 61
    const val JMP_IF_TRUE = 62


    const val CALL = 70
    const val CALL_METHOD = 71
    const val NEW = 72


    const val GET_PROP = 80
    const val SET_PROP = 81
    const val GET_INDEX = 82
    const val SET_INDEX = 83


    const val TYPEOF = 90
    const val TERNARY = 91


    const val MAKE_ARRAY = 100
    const val MAKE_OBJECT = 101


    const val LOAD_VAR_INT = 110
    const val MUL_INT_INT = 111
    const val ADD_INT_INT = 112
    const val ARRAY_GET_INT = 113
    const val MUL_CONST_ARRAY = 114
    const val ADD_MUL = 115


    const val SUB_INT_INT = 116
    const val DIV_INT_INT = 117
    const val MOD_INT_INT = 118
    const val NEG_INT = 119


    const val LT_INT_INT = 120
    const val LE_INT_INT = 121
    const val GT_INT_INT = 122
    const val GE_INT_INT = 123
    const val EQ_INT_INT = 124
    const val NE_INT_INT = 125

    const val RETURN = 255
}
