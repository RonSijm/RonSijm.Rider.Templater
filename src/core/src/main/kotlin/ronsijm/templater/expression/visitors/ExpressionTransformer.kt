package ronsijm.templater.expression.visitors

import ronsijm.templater.expression.*


open class ExpressionTransformer : ExpressionVisitor<Expression> {
    override fun visitIntLiteral(expr: LiteralExpression.IntLiteral): Expression = expr
    override fun visitDoubleLiteral(expr: LiteralExpression.DoubleLiteral): Expression = expr
    override fun visitStringLiteral(expr: LiteralExpression.StringLiteral): Expression = expr
    override fun visitBooleanLiteral(expr: LiteralExpression.BooleanLiteral): Expression = expr
    override fun visitNullLiteral(expr: LiteralExpression.NullLiteral): Expression = expr

    override fun visitArrayLiteral(expr: LiteralExpression.ArrayLiteral): Expression =
        expr.copy(elements = expr.elements.map { it.accept(this) })

    override fun visitObjectLiteral(expr: LiteralExpression.ObjectLiteral): Expression =
        expr.copy(properties = expr.properties.mapValues { it.value.accept(this) })

    override fun visitVariable(expr: VariableExpression): Expression = expr

    override fun visitPropertyAccess(expr: PropertyAccessExpression): Expression =
        expr.copy(target = expr.target.accept(this))

    override fun visitIndexAccess(expr: IndexAccessExpression): Expression =
        expr.copy(target = expr.target.accept(this), index = expr.index.accept(this))

    override fun visitBinary(expr: BinaryExpression): Expression =
        expr.copy(left = expr.left.accept(this), right = expr.right.accept(this))

    override fun visitUnary(expr: UnaryExpression): Expression =
        expr.copy(operand = expr.operand.accept(this))

    override fun visitCall(expr: CallExpression): Expression =
        expr.copy(callee = expr.callee.accept(this), arguments = expr.arguments.map { it.accept(this) })

    override fun visitMethodCall(expr: MethodCallExpression): Expression =
        expr.copy(target = expr.target.accept(this), arguments = expr.arguments.map { it.accept(this) })

    override fun visitNew(expr: NewExpression): Expression =
        expr.copy(constructor = expr.constructor.accept(this), arguments = expr.arguments.map { it.accept(this) })

    override fun visitConditional(expr: ConditionalExpression): Expression =
        expr.copy(
            condition = expr.condition.accept(this),
            thenBranch = expr.thenBranch.accept(this),
            elseBranch = expr.elseBranch.accept(this)
        )

    override fun visitLogicalAnd(expr: LogicalAndExpression): Expression =
        expr.copy(left = expr.left.accept(this), right = expr.right.accept(this))

    override fun visitLogicalOr(expr: LogicalOrExpression): Expression =
        expr.copy(left = expr.left.accept(this), right = expr.right.accept(this))

    override fun visitNullishCoalescing(expr: NullishCoalescingExpression): Expression =
        expr.copy(left = expr.left.accept(this), right = expr.right.accept(this))

    override fun visitArrowFunction(expr: ArrowFunctionExpression): Expression =
        expr.copy(body = expr.body.accept(this))

    override fun visitTemplateLiteral(expr: TemplateLiteralExpression): Expression =
        expr.copy(parts = expr.parts.map { part ->
            when (part) {
                is TemplatePart.StringPart -> part
                is TemplatePart.ExpressionPart -> TemplatePart.ExpressionPart(part.expression.accept(this))
            }
        })

    override fun visitAssignment(expr: AssignmentExpression): Expression =
        expr.copy(target = expr.target.accept(this), value = expr.value.accept(this))

    override fun visitTypeof(expr: TypeofExpression): Expression =
        expr.copy(operand = expr.operand.accept(this))

    override fun visitInstanceof(expr: InstanceofExpression): Expression =
        expr.copy(left = expr.left.accept(this), right = expr.right.accept(this))
}


class ConstantFoldingTransformer : ExpressionTransformer() {
    override fun visitBinary(expr: BinaryExpression): Expression {
        val left = expr.left.accept(this)
        val right = expr.right.accept(this)


        if (left is LiteralExpression && right is LiteralExpression) {
            return foldBinary(left, expr.operator, right, expr.sourceLocation)
                ?: expr.copy(left = left, right = right)
        }

        return expr.copy(left = left, right = right)
    }

    override fun visitUnary(expr: UnaryExpression): Expression {
        val operand = expr.operand.accept(this)

        if (operand is LiteralExpression) {
            return foldUnary(expr.operator, operand, expr.sourceLocation)
                ?: expr.copy(operand = operand)
        }

        return expr.copy(operand = operand)
    }

    private fun foldBinary(left: LiteralExpression, op: BinaryOperator, right: LiteralExpression, loc: SourceLocation?): LiteralExpression? {
        return when {
            left is LiteralExpression.IntLiteral && right is LiteralExpression.IntLiteral -> {
                when (op) {
                    BinaryOperator.ADD -> LiteralExpression.IntLiteral(left.value + right.value, loc)
                    BinaryOperator.SUBTRACT -> LiteralExpression.IntLiteral(left.value - right.value, loc)
                    BinaryOperator.MULTIPLY -> LiteralExpression.IntLiteral(left.value * right.value, loc)
                    BinaryOperator.DIVIDE -> LiteralExpression.IntLiteral(left.value / right.value, loc)
                    BinaryOperator.MODULO -> LiteralExpression.IntLiteral(left.value % right.value, loc)
                    else -> null
                }
            }
            left is LiteralExpression.DoubleLiteral || right is LiteralExpression.DoubleLiteral -> {
                val l = (left.value as? Number)?.toDouble() ?: return null
                val r = (right.value as? Number)?.toDouble() ?: return null
                when (op) {
                    BinaryOperator.ADD -> LiteralExpression.DoubleLiteral(l + r, loc)
                    BinaryOperator.SUBTRACT -> LiteralExpression.DoubleLiteral(l - r, loc)
                    BinaryOperator.MULTIPLY -> LiteralExpression.DoubleLiteral(l * r, loc)
                    BinaryOperator.DIVIDE -> LiteralExpression.DoubleLiteral(l / r, loc)
                    else -> null
                }
            }
            left is LiteralExpression.StringLiteral && right is LiteralExpression.StringLiteral -> {
                when (op) {
                    BinaryOperator.ADD, BinaryOperator.CONCAT -> LiteralExpression.StringLiteral(left.value + right.value, loc)
                    else -> null
                }
            }
            left is LiteralExpression.BooleanLiteral && right is LiteralExpression.BooleanLiteral -> {
                when (op) {
                    BinaryOperator.EQUAL, BinaryOperator.STRICT_EQUAL -> LiteralExpression.BooleanLiteral(left.value == right.value, loc)
                    BinaryOperator.NOT_EQUAL, BinaryOperator.STRICT_NOT_EQUAL -> LiteralExpression.BooleanLiteral(left.value != right.value, loc)
                    else -> null
                }
            }
            else -> null
        }
    }

    private fun foldUnary(op: UnaryOperator, operand: LiteralExpression, loc: SourceLocation?): LiteralExpression? {
        return when {
            operand is LiteralExpression.IntLiteral && op == UnaryOperator.NEGATE ->
                LiteralExpression.IntLiteral(-operand.value, loc)
            operand is LiteralExpression.DoubleLiteral && op == UnaryOperator.NEGATE ->
                LiteralExpression.DoubleLiteral(-operand.value, loc)
            operand is LiteralExpression.BooleanLiteral && op == UnaryOperator.NOT ->
                LiteralExpression.BooleanLiteral(!operand.value, loc)
            else -> null
        }
    }
}

