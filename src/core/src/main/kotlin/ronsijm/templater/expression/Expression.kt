package ronsijm.templater.expression


sealed interface Expression {

    val sourceLocation: SourceLocation?


    fun <R> accept(visitor: ExpressionVisitor<R>): R
}


data class SourceLocation(
    val line: Int,
    val column: Int,
    val length: Int = 0
)






sealed interface LiteralExpression : Expression {
    val value: Any?

    data class IntLiteral(
        override val value: Int,
        override val sourceLocation: SourceLocation? = null
    ) : LiteralExpression {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitIntLiteral(this)
    }

    data class DoubleLiteral(
        override val value: Double,
        override val sourceLocation: SourceLocation? = null
    ) : LiteralExpression {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitDoubleLiteral(this)
    }

    data class StringLiteral(
        override val value: String,
        override val sourceLocation: SourceLocation? = null
    ) : LiteralExpression {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitStringLiteral(this)
    }

    data class BooleanLiteral(
        override val value: Boolean,
        override val sourceLocation: SourceLocation? = null
    ) : LiteralExpression {
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitBooleanLiteral(this)
    }

    data class NullLiteral(
        override val sourceLocation: SourceLocation? = null
    ) : LiteralExpression {
        override val value: Nothing? = null
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitNullLiteral(this)
    }

    data class ArrayLiteral(
        val elements: List<Expression>,
        override val sourceLocation: SourceLocation? = null
    ) : LiteralExpression {
        override val value: List<Expression> get() = elements
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitArrayLiteral(this)
    }

    data class ObjectLiteral(
        val properties: Map<String, Expression>,
        override val sourceLocation: SourceLocation? = null
    ) : LiteralExpression {
        override val value: Map<String, Expression> get() = properties
        override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitObjectLiteral(this)
    }
}






data class VariableExpression(
    val name: String,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitVariable(this)
}


data class PropertyAccessExpression(
    val target: Expression,
    val property: String,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitPropertyAccess(this)
}


data class IndexAccessExpression(
    val target: Expression,
    val index: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitIndexAccess(this)
}






data class BinaryExpression(
    val left: Expression,
    val operator: BinaryOperator,
    val right: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitBinary(this)
}


data class UnaryExpression(
    val operator: UnaryOperator,
    val operand: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitUnary(this)
}






data class CallExpression(
    val callee: Expression,
    val arguments: List<Expression>,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitCall(this)
}


data class MethodCallExpression(
    val target: Expression,
    val method: String,
    val arguments: List<Expression>,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitMethodCall(this)
}


data class NewExpression(
    val constructor: Expression,
    val arguments: List<Expression>,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitNew(this)
}






data class ConditionalExpression(
    val condition: Expression,
    val thenBranch: Expression,
    val elseBranch: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitConditional(this)
}


data class LogicalAndExpression(
    val left: Expression,
    val right: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitLogicalAnd(this)
}


data class LogicalOrExpression(
    val left: Expression,
    val right: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitLogicalOr(this)
}


data class NullishCoalescingExpression(
    val left: Expression,
    val right: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitNullishCoalescing(this)
}






data class ArrowFunctionExpression(
    val parameters: List<String>,
    val body: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitArrowFunction(this)
}


data class TemplateLiteralExpression(
    val parts: List<TemplatePart>,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitTemplateLiteral(this)
}


sealed interface TemplatePart {
    data class StringPart(val value: String) : TemplatePart
    data class ExpressionPart(val expression: Expression) : TemplatePart
}


data class AssignmentExpression(
    val target: Expression,
    val operator: AssignmentOperator,
    val value: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitAssignment(this)
}


data class TypeofExpression(
    val operand: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitTypeof(this)
}


data class InstanceofExpression(
    val left: Expression,
    val right: Expression,
    override val sourceLocation: SourceLocation? = null
) : Expression {
    override fun <R> accept(visitor: ExpressionVisitor<R>): R = visitor.visitInstanceof(this)
}

