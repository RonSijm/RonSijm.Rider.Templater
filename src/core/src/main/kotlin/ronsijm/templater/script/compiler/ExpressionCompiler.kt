package ronsijm.templater.script.compiler


class ExpressionCompiler {
    private val tokenizer = Tokenizer()
    private lateinit var tokens: TokenList
    private lateinit var source: String
    private var pos: Int = 0
    private lateinit var builder: BytecodeBuilder

    companion object {

        private const val PREC_NONE = 0
        private const val PREC_ASSIGNMENT = 1
        private const val PREC_TERNARY = 2
        private const val PREC_OR = 3
        private const val PREC_AND = 4
        private const val PREC_BIT_OR = 5
        private const val PREC_BIT_XOR = 6
        private const val PREC_BIT_AND = 7
        private const val PREC_EQUALITY = 8
        private const val PREC_COMPARISON = 9
        private const val PREC_SHIFT = 10
        private const val PREC_TERM = 11
        private const val PREC_FACTOR = 12
        private const val PREC_UNARY = 13
        private const val PREC_CALL = 14
        private const val PREC_PRIMARY = 15


        private val precedence = IntArray(100).apply {
            this[TokenType.OR] = PREC_OR
            this[TokenType.AND] = PREC_AND
            this[TokenType.BITWISE_OR] = PREC_BIT_OR
            this[TokenType.BITWISE_XOR] = PREC_BIT_XOR
            this[TokenType.BITWISE_AND] = PREC_BIT_AND
            this[TokenType.EQUAL] = PREC_EQUALITY
            this[TokenType.NOT_EQUAL] = PREC_EQUALITY
            this[TokenType.STRICT_EQUAL] = PREC_EQUALITY
            this[TokenType.STRICT_NOT_EQUAL] = PREC_EQUALITY
            this[TokenType.LESS] = PREC_COMPARISON
            this[TokenType.LESS_EQUAL] = PREC_COMPARISON
            this[TokenType.GREATER] = PREC_COMPARISON
            this[TokenType.GREATER_EQUAL] = PREC_COMPARISON
            this[TokenType.LEFT_SHIFT] = PREC_SHIFT
            this[TokenType.RIGHT_SHIFT] = PREC_SHIFT
            this[TokenType.UNSIGNED_RIGHT_SHIFT] = PREC_SHIFT
            this[TokenType.PLUS] = PREC_TERM
            this[TokenType.MINUS] = PREC_TERM
            this[TokenType.STAR] = PREC_FACTOR
            this[TokenType.SLASH] = PREC_FACTOR
            this[TokenType.PERCENT] = PREC_FACTOR
            this[TokenType.LPAREN] = PREC_CALL
            this[TokenType.LBRACKET] = PREC_CALL
            this[TokenType.DOT] = PREC_CALL
            this[TokenType.QUESTION] = PREC_TERNARY
        }
    }

    fun compile(expr: String): CompiledExpr {
        tokens = tokenizer.tokenize(expr)
        source = tokenizer.getSource()
        pos = 0
        builder = BytecodeBuilder()

        parseExpression(PREC_ASSIGNMENT)
        builder.emit(OpCode.RETURN)

        return builder.build(expr)
    }

    private fun current(): Token = tokens[pos]
    private fun peek(): Token = if (pos + 1 < tokens.size) tokens[pos + 1] else tokens[tokens.size - 1]

    private fun advance(): Token {
        val token = current()
        if (token.type != TokenType.EOF) pos++
        return token
    }

    private fun match(type: Int): Boolean {
        if (current().type != type) return false
        advance()
        return true
    }

    private fun expect(type: Int, message: String) {
        if (current().type != type) {
            throw RuntimeException("Expected $message at position ${current().start}")
        }
        advance()
    }

    private fun parseExpression(minPrecedence: Int) {

        parsePrefixExpression()


        while (precedence[current().type] >= minPrecedence) {
            parseInfixExpression()
        }
    }

    private fun parsePrefixExpression() {
        val token = current()
        when (token.type) {
            TokenType.NUMBER -> {
                advance()
                val num = token.numValue

                if (num == num.toLong().toDouble() && num >= Int.MIN_VALUE && num <= Int.MAX_VALUE) {
                    builder.emit(OpCode.PUSH_INT, num.toInt())
                } else {
                    val constIndex = builder.addConstant(num)
                    builder.emit(OpCode.PUSH_CONST, constIndex)
                }
            }
            TokenType.STRING -> {
                advance()

                val str = source.substring(token.start + 1, token.end - 1)
                val strIndex = builder.addString(str)
                builder.emit(OpCode.PUSH_STRING, strIndex)
            }
            TokenType.TRUE -> {
                advance()
                builder.emit(OpCode.PUSH_TRUE)
            }
            TokenType.FALSE -> {
                advance()
                builder.emit(OpCode.PUSH_FALSE)
            }
            TokenType.NULL -> {
                advance()
                builder.emit(OpCode.PUSH_NULL)
            }
            TokenType.UNDEFINED -> {
                advance()
                builder.emit(OpCode.PUSH_UNDEFINED)
            }
            TokenType.IDENTIFIER -> {
                val nameToken = advance()
                val name = source.substring(nameToken.start, nameToken.end)


                if (name == "Math" && current().type == TokenType.DOT) {
                    advance()
                    val methodToken = advance()
                    if (methodToken.type != TokenType.IDENTIFIER) {
                        throw RuntimeException("Expected method name after Math.")
                    }
                    val methodName = source.substring(methodToken.start, methodToken.end)
                    val fullName = "Math.$methodName"
                    val nameIndex = builder.addString(fullName)
                    builder.emit(OpCode.PUSH_STRING, nameIndex)
                } else {
                    val nameIndex = builder.addString(name)
                    builder.emit(OpCode.LOAD_VAR, nameIndex)
                }
            }
            TokenType.LPAREN -> {
                advance()
                parseExpression(PREC_ASSIGNMENT)
                expect(TokenType.RPAREN, ")")
            }
            TokenType.LBRACKET -> {
                advance()
                var count = 0
                if (current().type != TokenType.RBRACKET) {
                    do {
                        parseExpression(PREC_ASSIGNMENT)
                        count++
                    } while (match(TokenType.COMMA))
                }
                expect(TokenType.RBRACKET, "]")
                builder.emit(OpCode.MAKE_ARRAY, count)
            }
            TokenType.LBRACE -> parseObjectLiteral()
            TokenType.MINUS -> parseUnaryMinus()
            TokenType.NOT -> parseUnaryNot()
            TokenType.TYPEOF -> parseTypeof()
            TokenType.NEW -> parseNew()
            else -> throw RuntimeException("Unexpected token at position ${token.start}: type=${token.type}")
        }
    }

    private fun parseObjectLiteral() {
        advance()
        var count = 0
        if (current().type != TokenType.RBRACE) {
            do {

                val keyToken = advance()
                val key = when (keyToken.type) {
                    TokenType.IDENTIFIER -> source.substring(keyToken.start, keyToken.end)
                    TokenType.STRING -> source.substring(keyToken.start + 1, keyToken.end - 1)
                    else -> throw RuntimeException("Expected object key")
                }
                val keyIndex = builder.addString(key)
                builder.emit(OpCode.PUSH_CONST, keyIndex)

                expect(TokenType.COLON, ":")
                parseExpression(PREC_ASSIGNMENT)
                count++
            } while (match(TokenType.COMMA))
        }
        expect(TokenType.RBRACE, "}")
        builder.emit(OpCode.MAKE_OBJECT, count)
    }

    private fun parseUnaryMinus() {
        advance()
        parseExpression(PREC_UNARY)
        builder.emit(OpCode.NEG)
    }

    private fun parseUnaryNot() {
        advance()
        parseExpression(PREC_UNARY)
        builder.emit(OpCode.NOT)
    }

    private fun parseTypeof() {
        advance()
        parseExpression(PREC_UNARY)
        builder.emit(OpCode.TYPEOF)
    }

    private fun parseNew() {
        advance()
        parseExpression(PREC_CALL)

    }

    private fun parseInfixExpression() {
        val token = current()
        val prec = precedence[token.type]

        when (token.type) {

            TokenType.PLUS -> parseBinaryOp(OpCode.ADD, prec)
            TokenType.MINUS -> parseBinaryOp(OpCode.SUB, prec)
            TokenType.STAR -> parseBinaryOp(OpCode.MUL, prec)
            TokenType.SLASH -> parseBinaryOp(OpCode.DIV, prec)
            TokenType.PERCENT -> parseBinaryOp(OpCode.MOD, prec)


            TokenType.UNSIGNED_RIGHT_SHIFT -> parseBinaryOp(OpCode.USHR, prec)
            TokenType.RIGHT_SHIFT -> parseBinaryOp(OpCode.SHR, prec)
            TokenType.LEFT_SHIFT -> parseBinaryOp(OpCode.SHL, prec)
            TokenType.BITWISE_AND -> parseBinaryOp(OpCode.BAND, prec)
            TokenType.BITWISE_OR -> parseBinaryOp(OpCode.BOR, prec)
            TokenType.BITWISE_XOR -> parseBinaryOp(OpCode.BXOR, prec)


            TokenType.EQUAL -> parseBinaryOp(OpCode.EQ, prec)
            TokenType.NOT_EQUAL -> parseBinaryOp(OpCode.NE, prec)
            TokenType.STRICT_EQUAL -> parseBinaryOp(OpCode.SEQ, prec)
            TokenType.STRICT_NOT_EQUAL -> parseBinaryOp(OpCode.SNE, prec)
            TokenType.LESS -> parseBinaryOp(OpCode.LT, prec)
            TokenType.LESS_EQUAL -> parseBinaryOp(OpCode.LE, prec)
            TokenType.GREATER -> parseBinaryOp(OpCode.GT, prec)
            TokenType.GREATER_EQUAL -> parseBinaryOp(OpCode.GE, prec)


            TokenType.AND -> parseLogicalAnd()
            TokenType.OR -> parseLogicalOr()


            TokenType.QUESTION -> parseTernary()


            TokenType.LPAREN -> parseCall()
            TokenType.LBRACKET -> parseIndexAccess()
            TokenType.DOT -> parsePropertyAccess()

            else -> throw RuntimeException("Unexpected infix operator: ${token.type}")
        }
    }

    private fun parseBinaryOp(opcode: Int, prec: Int) {
        advance()
        parseExpression(prec + 1)
        builder.emit(opcode)
    }

    private fun parseLogicalAnd() {
        advance()
        val jumpPos = builder.currentPosition()
        builder.emit(OpCode.JMP_IF_FALSE, 0)
        builder.emit(OpCode.POP)
        parseExpression(PREC_AND + 1)
        builder.patchJump(jumpPos, builder.currentPosition())
    }

    private fun parseLogicalOr() {
        advance()
        val jumpPos = builder.currentPosition()
        builder.emit(OpCode.JMP_IF_TRUE, 0)
        builder.emit(OpCode.POP)
        parseExpression(PREC_OR + 1)
        builder.patchJump(jumpPos, builder.currentPosition())
    }

    private fun parseTernary() {
        advance()
        val jumpIfFalse = builder.currentPosition()
        builder.emit(OpCode.JMP_IF_FALSE, 0)
        builder.emit(OpCode.POP)

        parseExpression(PREC_TERNARY)

        val jumpToEnd = builder.currentPosition()
        builder.emit(OpCode.JMP, 0)

        expect(TokenType.COLON, ":")
        builder.patchJump(jumpIfFalse, builder.currentPosition())
        builder.emit(OpCode.POP)

        parseExpression(PREC_TERNARY)
        builder.patchJump(jumpToEnd, builder.currentPosition())
    }

    private fun parseCall() {
        advance()
        var argc = 0
        if (current().type != TokenType.RPAREN) {
            do {
                parseExpression(PREC_ASSIGNMENT)
                argc++
            } while (match(TokenType.COMMA))
        }
        expect(TokenType.RPAREN, ")")
        builder.emit(OpCode.CALL, argc)
    }

    private fun parseIndexAccess() {
        advance()
        parseExpression(PREC_ASSIGNMENT)
        expect(TokenType.RBRACKET, "]")
        builder.emit(OpCode.GET_INDEX)
    }

    private fun parsePropertyAccess() {
        advance()
        val nameToken = advance()
        if (nameToken.type != TokenType.IDENTIFIER) {
            throw RuntimeException("Expected property name")
        }
        val name = source.substring(nameToken.start, nameToken.end)
        val nameIndex = builder.addString(name)
        builder.emit(OpCode.GET_PROP, nameIndex)
    }
}
