package ronsijm.templater.ast

sealed interface AstNode {
    val location: SourceLocation
    fun <R> accept(visitor: AstVisitor<R>): R
}

data class SourceLocation(
    val startOffset: Int,
    val endOffset: Int,
    val line: Int = 0,
    val column: Int = 0
) {
    companion object {
        val UNKNOWN = SourceLocation(0, 0, 0, 0)
    }
}

sealed interface Expression : AstNode

sealed interface LiteralExpr : Expression {
    val value: Any?
}

data class NumberLiteral(
    val number: Number,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : LiteralExpr {
    override val value: Any? get() = number
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitNumberLiteral(this)
}

data class StringLiteral(
    val text: String,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : LiteralExpr {
    override val value: Any? get() = text
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitStringLiteral(this)
}

data class BooleanLiteral(
    val boolean: Boolean,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : LiteralExpr {
    override val value: Any? get() = boolean
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitBooleanLiteral(this)
}

data class NullLiteral(
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : LiteralExpr {
    override val value: Any? get() = null
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitNullLiteral(this)
}

data class ArrayLiteral(
    val elements: List<Expression>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitArrayLiteral(this)
}

data class ObjectLiteral(
    val properties: List<ObjectProperty>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitObjectLiteral(this)
}

data class ObjectProperty(
    val key: String,
    val value: Expression,
    val computed: Boolean = false
)

data class TemplateLiteral(
    val parts: List<TemplatePart>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitTemplateLiteral(this)
}

sealed interface TemplatePart
data class TemplateString(val text: String) : TemplatePart
data class TemplateInterpolation(val expression: Expression) : TemplatePart

data class Identifier(
    val name: String,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitIdentifier(this)
}

data class PropertyAccess(
    val obj: Expression,
    val property: String,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitPropertyAccess(this)
}

data class IndexAccess(
    val obj: Expression,
    val index: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitIndexAccess(this)
}

data class BinaryExpr(
    val left: Expression,
    val operator: BinaryOperator,
    val right: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitBinaryExpr(this)
}

enum class BinaryOperator(val symbol: String) {

    ADD("+"), SUBTRACT("-"), MULTIPLY("*"), DIVIDE("/"), MODULO("%"),

    EQUAL("=="), NOT_EQUAL("!="), STRICT_EQUAL("==="), STRICT_NOT_EQUAL("!=="),
    LESS_THAN("<"), GREATER_THAN(">"), LESS_EQUAL("<="), GREATER_EQUAL(">="),

    AND("&&"), OR("||"),

    BITWISE_AND("&"), BITWISE_OR("|"), BITWISE_XOR("^"),
    LEFT_SHIFT("<<"), RIGHT_SHIFT(">>"), UNSIGNED_RIGHT_SHIFT(">>>"),

    NULLISH_COALESCE("??");

    companion object {
        private val symbolMap = entries.associateBy { it.symbol }
        fun fromSymbol(symbol: String): BinaryOperator? = symbolMap[symbol]
    }
}

data class UnaryExpr(
    val operator: UnaryOperator,
    val operand: Expression,
    val prefix: Boolean = true,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitUnaryExpr(this)
}

enum class UnaryOperator(val symbol: String) {
    NOT("!"),
    NEGATE("-"),
    PLUS("+"),
    TYPEOF("typeof"),
    INCREMENT("++"),
    DECREMENT("--");

    companion object {
        private val symbolMap = entries.associateBy { it.symbol }
        fun fromSymbol(symbol: String): UnaryOperator? = symbolMap[symbol]
    }
}

data class ConditionalExpr(
    val condition: Expression,
    val consequent: Expression,
    val alternate: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitConditionalExpr(this)
}

data class CallExpr(
    val callee: Expression,
    val arguments: List<Expression>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitCallExpr(this)
}

data class MethodCallExpr(
    val obj: Expression,
    val method: String,
    val arguments: List<Expression>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitMethodCallExpr(this)
}

data class ArrowFunctionExpr(
    val params: List<String>,
    val body: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitArrowFunctionExpr(this)
}

data class NewExpr(
    val callee: Expression,
    val arguments: List<Expression>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitNewExpr(this)
}
data class AssignmentExpr(
    val target: Expression,
    val operator: AssignmentOperator,
    val value: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitAssignmentExpr(this)
}

enum class AssignmentOperator(val symbol: String) {
    ASSIGN("="),
    ADD_ASSIGN("+="),
    SUBTRACT_ASSIGN("-="),
    MULTIPLY_ASSIGN("*="),
    DIVIDE_ASSIGN("/="),
    MODULO_ASSIGN("%=");

    companion object {
        private val symbolMap = entries.associateBy { it.symbol }
        fun fromSymbol(symbol: String): AssignmentOperator? = symbolMap[symbol]
    }
}

data class GroupExpr(
    val expression: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Expression {
    override fun <R> accept(visitor: AstVisitor<R>): R = visitor.visitGroupExpr(this)
}

