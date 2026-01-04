package ronsijm.templater.expression


interface ExpressionVisitor<R> {

    fun visitIntLiteral(expr: LiteralExpression.IntLiteral): R
    fun visitDoubleLiteral(expr: LiteralExpression.DoubleLiteral): R
    fun visitStringLiteral(expr: LiteralExpression.StringLiteral): R
    fun visitBooleanLiteral(expr: LiteralExpression.BooleanLiteral): R
    fun visitNullLiteral(expr: LiteralExpression.NullLiteral): R
    fun visitArrayLiteral(expr: LiteralExpression.ArrayLiteral): R
    fun visitObjectLiteral(expr: LiteralExpression.ObjectLiteral): R


    fun visitVariable(expr: VariableExpression): R
    fun visitPropertyAccess(expr: PropertyAccessExpression): R
    fun visitIndexAccess(expr: IndexAccessExpression): R


    fun visitBinary(expr: BinaryExpression): R
    fun visitUnary(expr: UnaryExpression): R


    fun visitCall(expr: CallExpression): R
    fun visitMethodCall(expr: MethodCallExpression): R
    fun visitNew(expr: NewExpression): R


    fun visitConditional(expr: ConditionalExpression): R
    fun visitLogicalAnd(expr: LogicalAndExpression): R
    fun visitLogicalOr(expr: LogicalOrExpression): R
    fun visitNullishCoalescing(expr: NullishCoalescingExpression): R


    fun visitArrowFunction(expr: ArrowFunctionExpression): R
    fun visitTemplateLiteral(expr: TemplateLiteralExpression): R
    fun visitAssignment(expr: AssignmentExpression): R
    fun visitTypeof(expr: TypeofExpression): R
    fun visitInstanceof(expr: InstanceofExpression): R
}


abstract class AbstractExpressionVisitor<R> : ExpressionVisitor<R> {
    protected open fun defaultVisit(expr: Expression): R {
        throw UnsupportedOperationException("Unsupported expression type: ${expr::class.simpleName}")
    }

    override fun visitIntLiteral(expr: LiteralExpression.IntLiteral): R = defaultVisit(expr)
    override fun visitDoubleLiteral(expr: LiteralExpression.DoubleLiteral): R = defaultVisit(expr)
    override fun visitStringLiteral(expr: LiteralExpression.StringLiteral): R = defaultVisit(expr)
    override fun visitBooleanLiteral(expr: LiteralExpression.BooleanLiteral): R = defaultVisit(expr)
    override fun visitNullLiteral(expr: LiteralExpression.NullLiteral): R = defaultVisit(expr)
    override fun visitArrayLiteral(expr: LiteralExpression.ArrayLiteral): R = defaultVisit(expr)
    override fun visitObjectLiteral(expr: LiteralExpression.ObjectLiteral): R = defaultVisit(expr)
    override fun visitVariable(expr: VariableExpression): R = defaultVisit(expr)
    override fun visitPropertyAccess(expr: PropertyAccessExpression): R = defaultVisit(expr)
    override fun visitIndexAccess(expr: IndexAccessExpression): R = defaultVisit(expr)
    override fun visitBinary(expr: BinaryExpression): R = defaultVisit(expr)
    override fun visitUnary(expr: UnaryExpression): R = defaultVisit(expr)
    override fun visitCall(expr: CallExpression): R = defaultVisit(expr)
    override fun visitMethodCall(expr: MethodCallExpression): R = defaultVisit(expr)
    override fun visitNew(expr: NewExpression): R = defaultVisit(expr)
    override fun visitConditional(expr: ConditionalExpression): R = defaultVisit(expr)
    override fun visitLogicalAnd(expr: LogicalAndExpression): R = defaultVisit(expr)
    override fun visitLogicalOr(expr: LogicalOrExpression): R = defaultVisit(expr)
    override fun visitNullishCoalescing(expr: NullishCoalescingExpression): R = defaultVisit(expr)
    override fun visitArrowFunction(expr: ArrowFunctionExpression): R = defaultVisit(expr)
    override fun visitTemplateLiteral(expr: TemplateLiteralExpression): R = defaultVisit(expr)
    override fun visitAssignment(expr: AssignmentExpression): R = defaultVisit(expr)
    override fun visitTypeof(expr: TypeofExpression): R = defaultVisit(expr)
    override fun visitInstanceof(expr: InstanceofExpression): R = defaultVisit(expr)
}


abstract class RecursiveExpressionVisitor : ExpressionVisitor<Unit> {
    override fun visitIntLiteral(expr: LiteralExpression.IntLiteral) {}
    override fun visitDoubleLiteral(expr: LiteralExpression.DoubleLiteral) {}
    override fun visitStringLiteral(expr: LiteralExpression.StringLiteral) {}
    override fun visitBooleanLiteral(expr: LiteralExpression.BooleanLiteral) {}
    override fun visitNullLiteral(expr: LiteralExpression.NullLiteral) {}

    override fun visitArrayLiteral(expr: LiteralExpression.ArrayLiteral) {
        expr.elements.forEach { it.accept(this) }
    }

    override fun visitObjectLiteral(expr: LiteralExpression.ObjectLiteral) {
        expr.properties.values.forEach { it.accept(this) }
    }

    override fun visitVariable(expr: VariableExpression) {}

    override fun visitPropertyAccess(expr: PropertyAccessExpression) {
        expr.target.accept(this)
    }

    override fun visitIndexAccess(expr: IndexAccessExpression) {
        expr.target.accept(this)
        expr.index.accept(this)
    }

    override fun visitBinary(expr: BinaryExpression) {
        expr.left.accept(this)
        expr.right.accept(this)
    }

    override fun visitUnary(expr: UnaryExpression) {
        expr.operand.accept(this)
    }

    override fun visitCall(expr: CallExpression) {
        expr.callee.accept(this)
        expr.arguments.forEach { it.accept(this) }
    }

    override fun visitMethodCall(expr: MethodCallExpression) {
        expr.target.accept(this)
        expr.arguments.forEach { it.accept(this) }
    }

    override fun visitNew(expr: NewExpression) {
        expr.constructor.accept(this)
        expr.arguments.forEach { it.accept(this) }
    }

    override fun visitConditional(expr: ConditionalExpression) {
        expr.condition.accept(this)
        expr.thenBranch.accept(this)
        expr.elseBranch.accept(this)
    }

    override fun visitLogicalAnd(expr: LogicalAndExpression) {
        expr.left.accept(this)
        expr.right.accept(this)
    }

    override fun visitLogicalOr(expr: LogicalOrExpression) {
        expr.left.accept(this)
        expr.right.accept(this)
    }

    override fun visitNullishCoalescing(expr: NullishCoalescingExpression) {
        expr.left.accept(this)
        expr.right.accept(this)
    }

    override fun visitArrowFunction(expr: ArrowFunctionExpression) {
        expr.body.accept(this)
    }

    override fun visitTemplateLiteral(expr: TemplateLiteralExpression) {
        expr.parts.filterIsInstance<TemplatePart.ExpressionPart>()
            .forEach { it.expression.accept(this) }
    }

    override fun visitAssignment(expr: AssignmentExpression) {
        expr.target.accept(this)
        expr.value.accept(this)
    }

    override fun visitTypeof(expr: TypeofExpression) {
        expr.operand.accept(this)
    }

    override fun visitInstanceof(expr: InstanceofExpression) {
        expr.left.accept(this)
        expr.right.accept(this)
    }
}

