package ronsijm.templater.script.ast


sealed interface AstNode {
    val location: SourceLocation
}


data class SourceLocation(
    val line: Int,
    val column: Int,
    val length: Int = 0
) {
    companion object {
        val UNKNOWN = SourceLocation(0, 0, 0)
    }
}





sealed interface Expression : AstNode



data class NumberLiteral(
    val value: Number,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class StringLiteral(
    val value: String,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class BooleanLiteral(
    val value: Boolean,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class NullLiteral(
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class ArrayLiteral(
    val elements: List<Expression>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class ObjectLiteral(
    val properties: List<Pair<String, Expression>>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class TemplateLiteral(
    val parts: List<TemplatePart>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

sealed interface TemplatePart
data class TemplateString(val value: String) : TemplatePart
data class TemplateExpression(val expression: Expression) : TemplatePart



data class Identifier(
    val name: String,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class MemberAccess(
    val obj: Expression,
    val property: String,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class IndexAccess(
    val obj: Expression,
    val index: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression



enum class BinaryOperator {

    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,

    EQUALS, NOT_EQUALS, STRICT_EQUALS, STRICT_NOT_EQUALS,
    LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL,

    AND, OR,

    BITWISE_AND, BITWISE_OR, BITWISE_XOR,
    LEFT_SHIFT, RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT
}

enum class UnaryOperator {
    NEGATE, NOT, TYPEOF, BITWISE_NOT
}

data class BinaryExpression(
    val left: Expression,
    val operator: BinaryOperator,
    val right: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class UnaryExpression(
    val operator: UnaryOperator,
    val operand: Expression,
    val prefix: Boolean = true,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class TernaryExpression(
    val condition: Expression,
    val thenBranch: Expression,
    val elseBranch: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression



data class CallExpression(
    val callee: Expression,
    val arguments: List<Expression>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class ArrowFunction(
    val parameters: List<String>,
    val body: AstNode,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

data class NewExpression(
    val callee: Expression,
    val arguments: List<Expression>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression



data class AssignmentExpression(
    val target: Expression,
    val operator: AssignmentOperator,
    val value: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression

enum class AssignmentOperator {
    ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN, MODULO_ASSIGN
}

