package ronsijm.templater.expression.visitors

import ronsijm.templater.expression.*


class ExpressionPrinter : ExpressionVisitor<String> {
    override fun visitIntLiteral(expr: LiteralExpression.IntLiteral): String = expr.value.toString()

    override fun visitDoubleLiteral(expr: LiteralExpression.DoubleLiteral): String = expr.value.toString()

    override fun visitStringLiteral(expr: LiteralExpression.StringLiteral): String = "\"${escapeString(expr.value)}\""

    override fun visitBooleanLiteral(expr: LiteralExpression.BooleanLiteral): String = expr.value.toString()

    override fun visitNullLiteral(expr: LiteralExpression.NullLiteral): String = "null"

    override fun visitArrayLiteral(expr: LiteralExpression.ArrayLiteral): String =
        expr.elements.joinToString(", ", "[", "]") { it.accept(this) }

    override fun visitObjectLiteral(expr: LiteralExpression.ObjectLiteral): String =
        expr.properties.entries.joinToString(", ", "{", "}") { (k, v) -> "$k: ${v.accept(this)}" }

    override fun visitVariable(expr: VariableExpression): String = expr.name

    override fun visitPropertyAccess(expr: PropertyAccessExpression): String =
        "${expr.target.accept(this)}.${expr.property}"

    override fun visitIndexAccess(expr: IndexAccessExpression): String =
        "${expr.target.accept(this)}[${expr.index.accept(this)}]"

    override fun visitBinary(expr: BinaryExpression): String =
        "(${expr.left.accept(this)} ${expr.operator.symbol} ${expr.right.accept(this)})"

    override fun visitUnary(expr: UnaryExpression): String =
        if (expr.operator.isPrefix) {
            "${expr.operator.symbol}${expr.operand.accept(this)}"
        } else {
            "${expr.operand.accept(this)}${expr.operator.symbol}"
        }

    override fun visitCall(expr: CallExpression): String =
        "${expr.callee.accept(this)}(${expr.arguments.joinToString(", ") { it.accept(this) }})"

    override fun visitMethodCall(expr: MethodCallExpression): String =
        "${expr.target.accept(this)}.${expr.method}(${expr.arguments.joinToString(", ") { it.accept(this) }})"

    override fun visitNew(expr: NewExpression): String =
        "new ${expr.constructor.accept(this)}(${expr.arguments.joinToString(", ") { it.accept(this) }})"

    override fun visitConditional(expr: ConditionalExpression): String =
        "(${expr.condition.accept(this)} ? ${expr.thenBranch.accept(this)} : ${expr.elseBranch.accept(this)})"

    override fun visitLogicalAnd(expr: LogicalAndExpression): String =
        "(${expr.left.accept(this)} && ${expr.right.accept(this)})"

    override fun visitLogicalOr(expr: LogicalOrExpression): String =
        "(${expr.left.accept(this)} || ${expr.right.accept(this)})"

    override fun visitNullishCoalescing(expr: NullishCoalescingExpression): String =
        "(${expr.left.accept(this)} ?? ${expr.right.accept(this)})"

    override fun visitArrowFunction(expr: ArrowFunctionExpression): String {
        val params = if (expr.parameters.size == 1) expr.parameters[0] else "(${expr.parameters.joinToString(", ")})"
        return "$params => ${expr.body.accept(this)}"
    }

    override fun visitTemplateLiteral(expr: TemplateLiteralExpression): String {
        val content = expr.parts.joinToString("") { part ->
            when (part) {
                is TemplatePart.StringPart -> part.value
                is TemplatePart.ExpressionPart -> "\${${part.expression.accept(this)}}"
            }
        }
        return "`$content`"
    }

    override fun visitAssignment(expr: AssignmentExpression): String =
        "${expr.target.accept(this)} ${expr.operator.symbol} ${expr.value.accept(this)}"

    override fun visitTypeof(expr: TypeofExpression): String =
        "typeof ${expr.operand.accept(this)}"

    override fun visitInstanceof(expr: InstanceofExpression): String =
        "(${expr.left.accept(this)} instanceof ${expr.right.accept(this)})"

    private fun escapeString(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

    companion object {
        private val instance = ExpressionPrinter()


        fun print(expr: Expression): String = expr.accept(instance)
    }
}

