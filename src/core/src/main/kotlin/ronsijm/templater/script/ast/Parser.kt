package ronsijm.templater.script.ast


class Parser(private val tokens: List<Token>) {
    private var current = 0

    class ParseError(message: String, val location: SourceLocation) : RuntimeException(message)

    fun parse(): Program {
        val statements = mutableListOf<Statement>()
        while (!isAtEnd()) {
            try {
                declaration()?.let { statements.add(it) }
            } catch (e: ParseError) {
                synchronize()
            }
        }
        return Program(statements, SourceLocation(1, 1))
    }



    private fun declaration(): Statement? {
        return when {
            check(TokenType.LET) || check(TokenType.CONST) || check(TokenType.VAR) -> variableDeclaration()
            check(TokenType.FUNCTION) -> functionDeclaration()
            else -> statement()
        }
    }

    private fun variableDeclaration(): Statement {
        val kindToken = advance()
        val kind = when (kindToken.type) {
            TokenType.LET -> VariableKind.LET
            TokenType.CONST -> VariableKind.CONST
            TokenType.VAR -> VariableKind.VAR
            else -> throw error(kindToken, "Expected variable declaration keyword")
        }
        val name = consume(TokenType.IDENTIFIER, "Expected variable name").lexeme
        val initializer = if (match(TokenType.EQUALS)) expression() else null
        consumeOptionalSemicolon()
        return VariableDeclaration(kind, name, initializer, kindToken.location)
    }

    private fun functionDeclaration(): Statement {
        val funcToken = advance()
        val name = consume(TokenType.IDENTIFIER, "Expected function name").lexeme
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name")
        val params = mutableListOf<String>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                params.add(consume(TokenType.IDENTIFIER, "Expected parameter name").lexeme)
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters")
        consume(TokenType.LEFT_BRACE, "Expected '{' before function body")
        val body = blockStatement()
        return FunctionDeclaration(name, params, body, funcToken.location)
    }



    private fun statement(): Statement {
        return when {
            match(TokenType.IF) -> ifStatement()
            match(TokenType.FOR) -> forStatement()
            match(TokenType.WHILE) -> whileStatement()
            match(TokenType.RETURN) -> returnStatement()
            match(TokenType.BREAK) -> BreakStatement(previous().location).also { consumeOptionalSemicolon() }
            match(TokenType.CONTINUE) -> ContinueStatement(previous().location).also { consumeOptionalSemicolon() }
            match(TokenType.TRY) -> tryStatement()
            match(TokenType.THROW) -> throwStatement()
            match(TokenType.LEFT_BRACE) -> blockStatement()
            match(TokenType.SEMICOLON) -> EmptyStatement(previous().location)
            else -> expressionStatement()
        }
    }

    private fun blockStatement(): BlockStatement {
        val location = previous().location
        val statements = mutableListOf<Statement>()
        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            declaration()?.let { statements.add(it) }
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after block")
        return BlockStatement(statements, location)
    }

    private fun ifStatement(): Statement {
        val location = previous().location
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after if condition")
        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE)) statement() else null
        return IfStatement(condition, thenBranch, elseBranch, location)
    }

    private fun forStatement(): Statement {
        val location = previous().location
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'")


        if (check(TokenType.LET) || check(TokenType.CONST) || check(TokenType.VAR)) {
            val kindToken = advance()
            val varName = consume(TokenType.IDENTIFIER, "Expected variable name").lexeme
            if (match(TokenType.OF)) {
                val iterable = expression()
                consume(TokenType.RIGHT_PAREN, "Expected ')' after for-of")
                val body = statement()
                return ForOfStatement(varName, iterable, body, location)
            }

            current -= 2
        }


        val init: Statement? = when {
            match(TokenType.SEMICOLON) -> null
            check(TokenType.LET) || check(TokenType.CONST) || check(TokenType.VAR) -> variableDeclaration()
            else -> expressionStatement()
        }
        val condition = if (!check(TokenType.SEMICOLON)) expression() else null
        consume(TokenType.SEMICOLON, "Expected ';' after loop condition")
        val update = if (!check(TokenType.RIGHT_PAREN)) expression() else null
        consume(TokenType.RIGHT_PAREN, "Expected ')' after for clauses")
        val body = statement()
        return ForStatement(init, condition, update, body, location)
    }

    private fun whileStatement(): Statement {
        val location = previous().location
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after condition")
        val body = statement()
        return WhileStatement(condition, body, location)
    }

    private fun returnStatement(): Statement {
        val location = previous().location
        val value = if (!check(TokenType.SEMICOLON) && !check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            expression()
        } else null
        consumeOptionalSemicolon()
        return ReturnStatement(value, location)
    }

    private fun tryStatement(): Statement {
        val location = previous().location
        consume(TokenType.LEFT_BRACE, "Expected '{' after 'try'")
        val tryBlock = blockStatement()
        var catchParam: String? = null
        var catchBlock: BlockStatement? = null
        var finallyBlock: BlockStatement? = null
        if (match(TokenType.CATCH)) {
            consume(TokenType.LEFT_PAREN, "Expected '(' after 'catch'")
            catchParam = consume(TokenType.IDENTIFIER, "Expected catch parameter").lexeme
            consume(TokenType.RIGHT_PAREN, "Expected ')' after catch parameter")
            consume(TokenType.LEFT_BRACE, "Expected '{' after catch")
            catchBlock = blockStatement()
        }
        if (match(TokenType.FINALLY)) {
            consume(TokenType.LEFT_BRACE, "Expected '{' after 'finally'")
            finallyBlock = blockStatement()
        }
        return TryStatement(tryBlock, catchParam, catchBlock, finallyBlock, location)
    }

    private fun throwStatement(): Statement {
        val location = previous().location
        val expr = expression()
        consumeOptionalSemicolon()
        return ThrowStatement(expr, location)
    }

    private fun expressionStatement(): Statement {
        val location = peek().location
        val expr = expression()


        if (expr is Identifier && expr.name == "tR") {
            if (check(TokenType.EQUALS) || check(TokenType.PLUS_EQUALS)) {
                val op = if (match(TokenType.PLUS_EQUALS)) AssignmentOperator.PLUS_ASSIGN
                else { advance(); AssignmentOperator.ASSIGN }
                val value = expression()
                consumeOptionalSemicolon()
                return ResultAccumulatorStatement(op, value, location)
            }
        }

        consumeOptionalSemicolon()
        return ExpressionStatement(expr, location)
    }



    private fun expression(): Expression = assignment()

    private fun assignment(): Expression {
        val expr = ternary()

        if (match(TokenType.EQUALS, TokenType.PLUS_EQUALS, TokenType.MINUS_EQUALS,
                TokenType.STAR_EQUALS, TokenType.SLASH_EQUALS, TokenType.PERCENT_EQUALS)) {
            val operator = when (previous().type) {
                TokenType.EQUALS -> AssignmentOperator.ASSIGN
                TokenType.PLUS_EQUALS -> AssignmentOperator.PLUS_ASSIGN
                TokenType.MINUS_EQUALS -> AssignmentOperator.MINUS_ASSIGN
                TokenType.STAR_EQUALS -> AssignmentOperator.MULTIPLY_ASSIGN
                TokenType.SLASH_EQUALS -> AssignmentOperator.DIVIDE_ASSIGN
                TokenType.PERCENT_EQUALS -> AssignmentOperator.MODULO_ASSIGN
                else -> throw error(previous(), "Unknown assignment operator")
            }
            val value = assignment()
            return AssignmentExpression(expr, operator, value, expr.location)
        }
        return expr
    }

    private fun ternary(): Expression {
        var expr = or()
        if (match(TokenType.QUESTION)) {
            val thenBranch = expression()
            consume(TokenType.COLON, "Expected ':' in ternary expression")
            val elseBranch = ternary()
            expr = TernaryExpression(expr, thenBranch, elseBranch, expr.location)
        }
        return expr
    }

    private fun or(): Expression {
        var expr = and()
        while (match(TokenType.OR)) {
            val right = and()
            expr = BinaryExpression(expr, BinaryOperator.OR, right, expr.location)
        }
        return expr
    }

    private fun and(): Expression {
        var expr = bitwiseOr()
        while (match(TokenType.AND)) {
            val right = bitwiseOr()
            expr = BinaryExpression(expr, BinaryOperator.AND, right, expr.location)
        }
        return expr
    }

    private fun bitwiseOr(): Expression {
        var expr = bitwiseXor()
        while (match(TokenType.PIPE)) {
            val right = bitwiseXor()
            expr = BinaryExpression(expr, BinaryOperator.BITWISE_OR, right, expr.location)
        }
        return expr
    }

    private fun bitwiseXor(): Expression {
        var expr = bitwiseAnd()
        while (match(TokenType.CARET)) {
            val right = bitwiseAnd()
            expr = BinaryExpression(expr, BinaryOperator.BITWISE_XOR, right, expr.location)
        }
        return expr
    }

    private fun bitwiseAnd(): Expression {
        var expr = equality()
        while (match(TokenType.AMPERSAND)) {
            val right = equality()
            expr = BinaryExpression(expr, BinaryOperator.BITWISE_AND, right, expr.location)
        }
        return expr
    }

    private fun equality(): Expression {
        var expr = comparison()
        while (match(TokenType.EQUALS_EQUALS, TokenType.NOT_EQUALS,
                TokenType.STRICT_EQUALS, TokenType.STRICT_NOT_EQUALS)) {
            val operator = when (previous().type) {
                TokenType.EQUALS_EQUALS -> BinaryOperator.EQUALS
                TokenType.NOT_EQUALS -> BinaryOperator.NOT_EQUALS
                TokenType.STRICT_EQUALS -> BinaryOperator.STRICT_EQUALS
                TokenType.STRICT_NOT_EQUALS -> BinaryOperator.STRICT_NOT_EQUALS
                else -> throw error(previous(), "Unknown equality operator")
            }
            val right = comparison()
            expr = BinaryExpression(expr, operator, right, expr.location)
        }
        return expr
    }

    private fun comparison(): Expression {
        var expr = shift()
        while (match(TokenType.LESS_THAN, TokenType.LESS_THAN_EQUALS,
                TokenType.GREATER_THAN, TokenType.GREATER_THAN_EQUALS)) {
            val operator = when (previous().type) {
                TokenType.LESS_THAN -> BinaryOperator.LESS_THAN
                TokenType.LESS_THAN_EQUALS -> BinaryOperator.LESS_THAN_OR_EQUAL
                TokenType.GREATER_THAN -> BinaryOperator.GREATER_THAN
                TokenType.GREATER_THAN_EQUALS -> BinaryOperator.GREATER_THAN_OR_EQUAL
                else -> throw error(previous(), "Unknown comparison operator")
            }
            val right = shift()
            expr = BinaryExpression(expr, operator, right, expr.location)
        }
        return expr
    }

    private fun shift(): Expression {
        var expr = term()
        while (match(TokenType.LEFT_SHIFT, TokenType.RIGHT_SHIFT, TokenType.UNSIGNED_RIGHT_SHIFT)) {
            val operator = when (previous().type) {
                TokenType.LEFT_SHIFT -> BinaryOperator.LEFT_SHIFT
                TokenType.RIGHT_SHIFT -> BinaryOperator.RIGHT_SHIFT
                TokenType.UNSIGNED_RIGHT_SHIFT -> BinaryOperator.UNSIGNED_RIGHT_SHIFT
                else -> throw error(previous(), "Unknown shift operator")
            }
            val right = term()
            expr = BinaryExpression(expr, operator, right, expr.location)
        }
        return expr
    }

    private fun term(): Expression {
        var expr = factor()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operator = if (previous().type == TokenType.PLUS) BinaryOperator.PLUS else BinaryOperator.MINUS
            val right = factor()
            expr = BinaryExpression(expr, operator, right, expr.location)
        }
        return expr
    }

    private fun factor(): Expression {
        var expr = unary()
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            val operator = when (previous().type) {
                TokenType.STAR -> BinaryOperator.MULTIPLY
                TokenType.SLASH -> BinaryOperator.DIVIDE
                TokenType.PERCENT -> BinaryOperator.MODULO
                else -> throw error(previous(), "Unknown factor operator")
            }
            val right = unary()
            expr = BinaryExpression(expr, operator, right, expr.location)
        }
        return expr
    }

    private fun unary(): Expression {
        if (match(TokenType.NOT, TokenType.MINUS, TokenType.TILDE, TokenType.TYPEOF)) {
            val operator = when (previous().type) {
                TokenType.NOT -> UnaryOperator.NOT
                TokenType.MINUS -> UnaryOperator.NEGATE
                TokenType.TILDE -> UnaryOperator.BITWISE_NOT
                TokenType.TYPEOF -> UnaryOperator.TYPEOF
                else -> throw error(previous(), "Unknown unary operator")
            }
            val operand = unary()
            return UnaryExpression(operator, operand, true, previous().location)
        }
        return postfix()
    }

    private fun postfix(): Expression {
        var expr = call()
        while (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {


            expr = UnaryExpression(
                if (previous().type == TokenType.PLUS_PLUS) UnaryOperator.NEGATE else UnaryOperator.NEGATE,
                expr, false, expr.location
            )
        }
        return expr
    }

    private fun call(): Expression {
        var expr = primary()
        while (true) {
            expr = when {
                match(TokenType.LEFT_PAREN) -> finishCall(expr)
                match(TokenType.DOT) -> {
                    val name = consume(TokenType.IDENTIFIER, "Expected property name after '.'").lexeme
                    MemberAccess(expr, name, expr.location)
                }
                match(TokenType.LEFT_BRACKET) -> {
                    val index = expression()
                    consume(TokenType.RIGHT_BRACKET, "Expected ']' after index")
                    IndexAccess(expr, index, expr.location)
                }
                else -> break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expression): Expression {
        val args = mutableListOf<Expression>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                args.add(expression())
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments")
        return CallExpression(callee, args, callee.location)
    }

    private fun primary(): Expression {
        val token = peek()
        return when {
            match(TokenType.NUMBER) -> NumberLiteral(previous().literal as Number, previous().location)
            match(TokenType.STRING) -> StringLiteral(previous().literal as String, previous().location)
            match(TokenType.TEMPLATE_LITERAL) -> {

                StringLiteral(previous().literal as String, previous().location)
            }
            match(TokenType.TRUE) -> BooleanLiteral(true, previous().location)
            match(TokenType.FALSE) -> BooleanLiteral(false, previous().location)
            match(TokenType.NULL, TokenType.UNDEFINED) -> NullLiteral(previous().location)
            match(TokenType.IDENTIFIER) -> {
                val ident = Identifier(previous().lexeme, previous().location)

                if (check(TokenType.ARROW)) {
                    advance()
                    val body = if (check(TokenType.LEFT_BRACE)) {
                        advance()
                        blockStatement()
                    } else {
                        expression()
                    }
                    ArrowFunction(listOf(ident.name), body, ident.location)
                } else ident
            }
            match(TokenType.LEFT_PAREN) -> {

                val startLoc = previous().location
                if (check(TokenType.RIGHT_PAREN)) {
                    advance()
                    if (match(TokenType.ARROW)) {
                        val body = if (check(TokenType.LEFT_BRACE)) { advance(); blockStatement() } else expression()
                        return ArrowFunction(emptyList(), body, startLoc)
                    }
                }

                val params = mutableListOf<String>()
                if (check(TokenType.IDENTIFIER)) {
                    params.add(advance().lexeme)
                    while (match(TokenType.COMMA)) {
                        params.add(consume(TokenType.IDENTIFIER, "Expected parameter").lexeme)
                    }
                    if (match(TokenType.RIGHT_PAREN) && match(TokenType.ARROW)) {
                        val body = if (check(TokenType.LEFT_BRACE)) { advance(); blockStatement() } else expression()
                        return ArrowFunction(params, body, startLoc)
                    }

                    current -= params.size * 2
                }
                val expr = expression()
                consume(TokenType.RIGHT_PAREN, "Expected ')' after expression")
                expr
            }
            match(TokenType.LEFT_BRACKET) -> arrayLiteral()
            match(TokenType.LEFT_BRACE) -> objectLiteral()
            match(TokenType.NEW) -> newExpression()
            else -> throw error(token, "Expected expression, got ${token.type}")
        }
    }

    private fun arrayLiteral(): Expression {
        val location = previous().location
        val elements = mutableListOf<Expression>()
        if (!check(TokenType.RIGHT_BRACKET)) {
            do {
                if (check(TokenType.RIGHT_BRACKET)) break
                elements.add(expression())
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_BRACKET, "Expected ']' after array elements")
        return ArrayLiteral(elements, location)
    }

    private fun objectLiteral(): Expression {
        val location = previous().location
        val properties = mutableListOf<Pair<String, Expression>>()
        if (!check(TokenType.RIGHT_BRACE)) {
            do {
                if (check(TokenType.RIGHT_BRACE)) break
                val key = when {
                    check(TokenType.IDENTIFIER) -> advance().lexeme
                    check(TokenType.STRING) -> (advance().literal as String)
                    else -> throw error(peek(), "Expected property name")
                }
                consume(TokenType.COLON, "Expected ':' after property name")
                val value = expression()
                properties.add(key to value)
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_BRACE, "Expected '}' after object literal")
        return ObjectLiteral(properties, location)
    }

    private fun newExpression(): Expression {
        val location = previous().location
        val callee = call()
        return NewExpression(callee, (callee as? CallExpression)?.arguments ?: emptyList(), location)
    }



    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean = !isAtEnd() && peek().type == type

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun consumeOptionalSemicolon() {
        match(TokenType.SEMICOLON)
    }

    private fun error(token: Token, message: String): ParseError {
        return ParseError("$message at ${token.location}", token.location)
    }

    private fun synchronize() {
        advance()
        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return
            when (peek().type) {
                TokenType.FUNCTION, TokenType.VAR, TokenType.LET, TokenType.CONST,
                TokenType.FOR, TokenType.IF, TokenType.WHILE, TokenType.RETURN,
                TokenType.TRY -> return
                else -> advance()
            }
        }
    }
}

