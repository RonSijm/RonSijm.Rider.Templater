package ronsijm.templater.ast.visitors

import ronsijm.templater.ast.*

class PrettyPrintVisitor : AstVisitor<String> {

    override fun visitNumberLiteral(node: NumberLiteral): String =
        node.number.toString()

    override fun visitStringLiteral(node: StringLiteral): String =
        "\"${escapeString(node.text)}\""

    override fun visitBooleanLiteral(node: BooleanLiteral): String =
        node.boolean.toString()

    override fun visitNullLiteral(node: NullLiteral): String = "null"

    override fun visitArrayLiteral(node: ArrayLiteral): String =
        "[${node.elements.joinToString(", ") { it.accept(this) }}]"

    override fun visitObjectLiteral(node: ObjectLiteral): String {
        if (node.properties.isEmpty()) return "{}"
        return "{ ${node.properties.joinToString(", ") { prop ->
            val key = if (needsQuotes(prop.key)) "\"${prop.key}\"" else prop.key
            "$key: ${prop.value.accept(this)}"
        }} }"
    }

    override fun visitTemplateLiteral(node: TemplateLiteral): String {
        val content = node.parts.joinToString("") { part ->
            when (part) {
                is TemplateString -> part.text
                is TemplateInterpolation -> "\${${part.expression.accept(this)}}"
            }
        }
        return "`$content`"
    }

    override fun visitIdentifier(node: Identifier): String = node.name

    override fun visitPropertyAccess(node: PropertyAccess): String =
        "${node.obj.accept(this)}.${node.property}"

    override fun visitIndexAccess(node: IndexAccess): String =
        "${node.obj.accept(this)}[${node.index.accept(this)}]"

    override fun visitBinaryExpr(node: BinaryExpr): String {
        val left = maybeParenthesize(node.left, node)
        val right = maybeParenthesize(node.right, node)
        return "$left ${node.operator.symbol} $right"
    }

    override fun visitUnaryExpr(node: UnaryExpr): String {
        val operand = node.operand.accept(this)
        return if (node.prefix) {
            when (node.operator) {
                UnaryOperator.TYPEOF -> "typeof $operand"
                else -> "${node.operator.symbol}$operand"
            }
        } else {
            "$operand${node.operator.symbol}"
        }
    }

    override fun visitConditionalExpr(node: ConditionalExpr): String {
        val condition = node.condition.accept(this)
        val consequent = node.consequent.accept(this)
        val alternate = node.alternate.accept(this)
        return "$condition ? $consequent : $alternate"
    }

    override fun visitCallExpr(node: CallExpr): String {
        val callee = node.callee.accept(this)
        val args = node.arguments.joinToString(", ") { it.accept(this) }
        return "$callee($args)"
    }

    override fun visitMethodCallExpr(node: MethodCallExpr): String {
        val obj = node.obj.accept(this)
        val args = node.arguments.joinToString(", ") { it.accept(this) }
        return "$obj.${node.method}($args)"
    }

    override fun visitArrowFunctionExpr(node: ArrowFunctionExpr): String {
        val params = when (node.params.size) {
            0 -> "()"
            1 -> node.params[0]
            else -> "(${node.params.joinToString(", ")})"
        }
        val body = node.body.accept(this)
        return "$params => $body"
    }

    override fun visitNewExpr(node: NewExpr): String {
        val callee = node.callee.accept(this)
        val args = node.arguments.joinToString(", ") { it.accept(this) }
        return "new $callee($args)"
    }

    override fun visitAssignmentExpr(node: AssignmentExpr): String {
        val target = node.target.accept(this)
        val value = node.value.accept(this)
        return "$target ${node.operator.symbol} $value"
    }

    override fun visitGroupExpr(node: GroupExpr): String =
        "(${node.expression.accept(this)})"

    // Helper functions
    private fun escapeString(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\t", "\\t")
        .replace("\r", "\\r")

    private fun needsQuotes(key: String): Boolean =
        !key.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))

    private fun maybeParenthesize(child: Expression, parent: BinaryExpr): String {
        val childStr = child.accept(this)
        return if (child is BinaryExpr && needsParentheses(child.operator, parent.operator)) {
            "($childStr)"
        } else {
            childStr
        }
    }

    private fun needsParentheses(childOp: BinaryOperator, parentOp: BinaryOperator): Boolean {
        val childPrec = precedence(childOp)
        val parentPrec = precedence(parentOp)
        return childPrec < parentPrec
    }

    private fun precedence(op: BinaryOperator): Int = when (op) {
        BinaryOperator.OR -> 1
        BinaryOperator.AND -> 2
        BinaryOperator.NULLISH_COALESCE -> 3
        BinaryOperator.BITWISE_OR -> 4
        BinaryOperator.BITWISE_XOR -> 5
        BinaryOperator.BITWISE_AND -> 6
        BinaryOperator.EQUAL, BinaryOperator.NOT_EQUAL, BinaryOperator.STRICT_EQUAL, BinaryOperator.STRICT_NOT_EQUAL -> 7
        BinaryOperator.LESS_THAN, BinaryOperator.GREATER_THAN, BinaryOperator.LESS_EQUAL, BinaryOperator.GREATER_EQUAL -> 8
        BinaryOperator.LEFT_SHIFT, BinaryOperator.RIGHT_SHIFT, BinaryOperator.UNSIGNED_RIGHT_SHIFT -> 9
        BinaryOperator.ADD, BinaryOperator.SUBTRACT -> 10
        BinaryOperator.MULTIPLY, BinaryOperator.DIVIDE, BinaryOperator.MODULO -> 11
    }
}

