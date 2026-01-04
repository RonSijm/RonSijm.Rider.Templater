package ronsijm.templater.ast.visitors

import ronsijm.templater.ast.*
import ronsijm.templater.utils.TypeConverter

class ConstantFoldingVisitor : TransformingVisitor() {

    override fun visitBinaryExpr(node: BinaryExpr): Expression {
        val left = node.left.accept(this)
        val right = node.right.accept(this)


        val leftValue = (left as? LiteralExpr)?.value
        val rightValue = (right as? LiteralExpr)?.value

        if (leftValue != null && rightValue != null) {
            val folded = foldBinary(leftValue, node.operator, rightValue, node.location)
            if (folded != null) return folded
        }


        val identity = applyIdentityOptimizations(left, node.operator, right, node.location)
        if (identity != null) return identity


        return if (left !== node.left || right !== node.right) {
            node.copy(left = left, right = right)
        } else {
            node
        }
    }

    private fun foldBinary(left: Any?, op: BinaryOperator, right: Any?, loc: SourceLocation): Expression? {
        return when (op) {

            BinaryOperator.ADD -> foldAdd(left, right, loc)
            BinaryOperator.SUBTRACT -> foldArithmetic(left, right, loc) { a, b -> a - b }
            BinaryOperator.MULTIPLY -> foldArithmetic(left, right, loc) { a, b -> a * b }
            BinaryOperator.DIVIDE -> foldDivide(left, right, loc)
            BinaryOperator.MODULO -> foldArithmetic(left, right, loc) { a, b -> a % b }


            BinaryOperator.EQUAL, BinaryOperator.STRICT_EQUAL -> BooleanLiteral(left == right, loc)
            BinaryOperator.NOT_EQUAL, BinaryOperator.STRICT_NOT_EQUAL -> BooleanLiteral(left != right, loc)
            BinaryOperator.LESS_THAN -> foldComparison(left, right, loc) { a, b -> a < b }
            BinaryOperator.GREATER_THAN -> foldComparison(left, right, loc) { a, b -> a > b }
            BinaryOperator.LESS_EQUAL -> foldComparison(left, right, loc) { a, b -> a <= b }
            BinaryOperator.GREATER_EQUAL -> foldComparison(left, right, loc) { a, b -> a >= b }


            BinaryOperator.AND -> foldLogicalAnd(left, right, loc)
            BinaryOperator.OR -> foldLogicalOr(left, right, loc)


            BinaryOperator.BITWISE_AND -> foldBitwise(left, right, loc) { a, b -> a and b }
            BinaryOperator.BITWISE_OR -> foldBitwise(left, right, loc) { a, b -> a or b }
            BinaryOperator.BITWISE_XOR -> foldBitwise(left, right, loc) { a, b -> a xor b }
            BinaryOperator.LEFT_SHIFT -> foldBitwise(left, right, loc) { a, b -> a shl b }
            BinaryOperator.RIGHT_SHIFT -> foldBitwise(left, right, loc) { a, b -> a shr b }
            BinaryOperator.UNSIGNED_RIGHT_SHIFT -> foldBitwise(left, right, loc) { a, b -> a ushr b }


            BinaryOperator.NULLISH_COALESCE -> if (left == null) toLiteral(right, loc) else toLiteral(left, loc)
        }
    }

    private fun foldAdd(left: Any?, right: Any?, loc: SourceLocation): Expression? {

        if (left is String || right is String) {
            return StringLiteral(left.toString() + right.toString(), loc)
        }

        return foldArithmetic(left, right, loc) { a, b -> a + b }
    }

    private fun foldArithmetic(left: Any?, right: Any?, loc: SourceLocation, op: (Double, Double) -> Double): Expression? {
        val l = toDouble(left) ?: return null
        val r = toDouble(right) ?: return null
        val result = op(l, r)
        return if (result == result.toLong().toDouble()) {
            NumberLiteral(result.toLong(), loc)
        } else {
            NumberLiteral(result, loc)
        }
    }

    private fun foldDivide(left: Any?, right: Any?, loc: SourceLocation): Expression? {
        val l = toDouble(left) ?: return null
        val r = toDouble(right) ?: return null
        if (r == 0.0) return NumberLiteral(Double.POSITIVE_INFINITY, loc)
        val result = l / r
        return if (result == result.toLong().toDouble()) {
            NumberLiteral(result.toLong(), loc)
        } else {
            NumberLiteral(result, loc)
        }
    }

    private fun foldComparison(left: Any?, right: Any?, loc: SourceLocation, op: (Double, Double) -> Boolean): Expression? {
        val l = toDouble(left) ?: return null
        val r = toDouble(right) ?: return null
        return BooleanLiteral(op(l, r), loc)
    }

    private fun foldLogicalAnd(left: Any?, right: Any?, loc: SourceLocation): Expression? {
        val l = toBoolean(left)
        val r = toBoolean(right)
        return BooleanLiteral(l && r, loc)
    }

    private fun foldLogicalOr(left: Any?, right: Any?, loc: SourceLocation): Expression? {
        val l = toBoolean(left)
        val r = toBoolean(right)
        return BooleanLiteral(l || r, loc)
    }

    private fun foldBitwise(left: Any?, right: Any?, loc: SourceLocation, op: (Int, Int) -> Int): Expression? {
        val l = toInt(left) ?: return null
        val r = toInt(right) ?: return null
        return NumberLiteral(op(l, r), loc)
    }

    private fun applyIdentityOptimizations(left: Expression, op: BinaryOperator, right: Expression, loc: SourceLocation): Expression? {
        val leftValue = (left as? NumberLiteral)?.number?.toDouble()
        val rightValue = (right as? NumberLiteral)?.number?.toDouble()

        return when (op) {
            BinaryOperator.ADD -> when {
                leftValue == 0.0 -> right
                rightValue == 0.0 -> left
                else -> null
            }
            BinaryOperator.SUBTRACT -> when {
                rightValue == 0.0 -> left
                else -> null
            }
            BinaryOperator.MULTIPLY -> when {
                leftValue == 1.0 -> right
                rightValue == 1.0 -> left
                leftValue == 0.0 -> NumberLiteral(0, loc)
                rightValue == 0.0 -> NumberLiteral(0, loc)
                else -> null
            }
            BinaryOperator.DIVIDE -> when {
                rightValue == 1.0 -> left
                else -> null
            }
            else -> null
        }
    }

    override fun visitUnaryExpr(node: UnaryExpr): Expression {
        val operand = node.operand.accept(this)
        val value = (operand as? LiteralExpr)?.value

        if (value != null) {
            val folded = foldUnary(node.operator, value, node.location)
            if (folded != null) return folded
        }

        return if (operand !== node.operand) {
            node.copy(operand = operand)
        } else {
            node
        }
    }

    private fun foldUnary(op: UnaryOperator, value: Any?, loc: SourceLocation): Expression? {
        return when (op) {
            UnaryOperator.NOT -> BooleanLiteral(!toBoolean(value), loc)
            UnaryOperator.NEGATE -> {
                val num = toDouble(value) ?: return null
                if (num == num.toLong().toDouble()) {
                    NumberLiteral(-num.toLong(), loc)
                } else {
                    NumberLiteral(-num, loc)
                }
            }
            UnaryOperator.PLUS -> {
                val num = toDouble(value) ?: return null
                if (num == num.toLong().toDouble()) {
                    NumberLiteral(num.toLong(), loc)
                } else {
                    NumberLiteral(num, loc)
                }
            }
            UnaryOperator.TYPEOF -> StringLiteral(typeofValue(value), loc)
            else -> null
        }
    }

    override fun visitConditionalExpr(node: ConditionalExpr): Expression {
        val condition = node.condition.accept(this)
        val condValue = (condition as? LiteralExpr)?.value


        if (condValue != null) {
            return if (toBoolean(condValue)) {
                node.consequent.accept(this)
            } else {
                node.alternate.accept(this)
            }
        }

        val consequent = node.consequent.accept(this)
        val alternate = node.alternate.accept(this)

        return if (condition !== node.condition || consequent !== node.consequent || alternate !== node.alternate) {
            node.copy(condition = condition, consequent = consequent, alternate = alternate)
        } else {
            node
        }
    }


    private fun toDouble(value: Any?): Double? = TypeConverter.toDoubleOrNull(value)

    private fun toInt(value: Any?): Int? = TypeConverter.toIntOrNull(value)

    private fun toBoolean(value: Any?): Boolean = TypeConverter.toBoolean(value)

    private fun toLiteral(value: Any?, loc: SourceLocation): Expression = when (value) {
        null -> NullLiteral(loc)
        is Number -> NumberLiteral(value, loc)
        is String -> StringLiteral(value, loc)
        is Boolean -> BooleanLiteral(value, loc)
        else -> StringLiteral(value.toString(), loc)
    }

    private fun typeofValue(value: Any?): String = when (value) {
        null -> "object"
        is Number -> "number"
        is String -> "string"
        is Boolean -> "boolean"
        is List<*> -> "object"
        is Map<*, *> -> "object"
        else -> "object"
    }
}

