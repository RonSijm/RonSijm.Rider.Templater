package ronsijm.templater.expression.visitors

import ronsijm.templater.expression.*


class VariableCollector : RecursiveExpressionVisitor() {
    private val _variables = mutableSetOf<String>()
    private val _assignedVariables = mutableSetOf<String>()
    private val _calledFunctions = mutableSetOf<String>()
    private val _accessedProperties = mutableSetOf<String>()


    val variables: Set<String> get() = _variables


    val assignedVariables: Set<String> get() = _assignedVariables


    val readOnlyVariables: Set<String> get() = _variables - _assignedVariables


    val calledFunctions: Set<String> get() = _calledFunctions


    val accessedProperties: Set<String> get() = _accessedProperties

    override fun visitVariable(expr: VariableExpression) {
        _variables.add(expr.name)
    }

    override fun visitPropertyAccess(expr: PropertyAccessExpression) {
        super.visitPropertyAccess(expr)
        _accessedProperties.add(expr.property)
    }

    override fun visitCall(expr: CallExpression) {
        super.visitCall(expr)
        if (expr.callee is VariableExpression) {
            _calledFunctions.add((expr.callee as VariableExpression).name)
        }
    }

    override fun visitAssignment(expr: AssignmentExpression) {
        super.visitAssignment(expr)
        if (expr.target is VariableExpression) {
            _assignedVariables.add((expr.target as VariableExpression).name)
        }
    }

    override fun visitArrowFunction(expr: ArrowFunctionExpression) {


    }


    fun reset() {
        _variables.clear()
        _assignedVariables.clear()
        _calledFunctions.clear()
        _accessedProperties.clear()
    }

    companion object {

        fun collect(expr: Expression): VariableCollector {
            val collector = VariableCollector()
            expr.accept(collector)
            return collector
        }


        fun getVariables(expr: Expression): Set<String> = collect(expr).variables


        fun getReadOnlyVariables(expr: Expression): Set<String> = collect(expr).readOnlyVariables
    }
}


class PurityChecker : RecursiveExpressionVisitor() {
    private var _isPure = true

    val isPure: Boolean get() = _isPure

    override fun visitAssignment(expr: AssignmentExpression) {
        _isPure = false
    }

    override fun visitCall(expr: CallExpression) {

        _isPure = false
    }

    override fun visitMethodCall(expr: MethodCallExpression) {

        _isPure = false
    }

    override fun visitNew(expr: NewExpression) {

        _isPure = false
    }

    fun reset() {
        _isPure = true
    }

    companion object {

        fun isPure(expr: Expression): Boolean {
            val checker = PurityChecker()
            expr.accept(checker)
            return checker.isPure
        }
    }
}


class ComplexityCalculator : ExpressionVisitor<Int> {
    override fun visitIntLiteral(expr: LiteralExpression.IntLiteral) = 1
    override fun visitDoubleLiteral(expr: LiteralExpression.DoubleLiteral) = 1
    override fun visitStringLiteral(expr: LiteralExpression.StringLiteral) = 1
    override fun visitBooleanLiteral(expr: LiteralExpression.BooleanLiteral) = 1
    override fun visitNullLiteral(expr: LiteralExpression.NullLiteral) = 1
    override fun visitArrayLiteral(expr: LiteralExpression.ArrayLiteral) = 1 + expr.elements.sumOf { it.accept(this) }
    override fun visitObjectLiteral(expr: LiteralExpression.ObjectLiteral) = 1 + expr.properties.values.sumOf { it.accept(this) }
    override fun visitVariable(expr: VariableExpression) = 1
    override fun visitPropertyAccess(expr: PropertyAccessExpression) = 2 + expr.target.accept(this)
    override fun visitIndexAccess(expr: IndexAccessExpression) = 2 + expr.target.accept(this) + expr.index.accept(this)
    override fun visitBinary(expr: BinaryExpression) = 1 + expr.left.accept(this) + expr.right.accept(this)
    override fun visitUnary(expr: UnaryExpression) = 1 + expr.operand.accept(this)
    override fun visitCall(expr: CallExpression) = 5 + expr.callee.accept(this) + expr.arguments.sumOf { it.accept(this) }
    override fun visitMethodCall(expr: MethodCallExpression) = 5 + expr.target.accept(this) + expr.arguments.sumOf { it.accept(this) }
    override fun visitNew(expr: NewExpression) = 10 + expr.constructor.accept(this) + expr.arguments.sumOf { it.accept(this) }
    override fun visitConditional(expr: ConditionalExpression) = 2 + expr.condition.accept(this) + expr.thenBranch.accept(this) + expr.elseBranch.accept(this)
    override fun visitLogicalAnd(expr: LogicalAndExpression) = 1 + expr.left.accept(this) + expr.right.accept(this)
    override fun visitLogicalOr(expr: LogicalOrExpression) = 1 + expr.left.accept(this) + expr.right.accept(this)
    override fun visitNullishCoalescing(expr: NullishCoalescingExpression) = 1 + expr.left.accept(this) + expr.right.accept(this)
    override fun visitArrowFunction(expr: ArrowFunctionExpression) = 3 + expr.body.accept(this)
    override fun visitTemplateLiteral(expr: TemplateLiteralExpression) = 2 + expr.parts.filterIsInstance<TemplatePart.ExpressionPart>().sumOf { it.expression.accept(this) }
    override fun visitAssignment(expr: AssignmentExpression) = 2 + expr.target.accept(this) + expr.value.accept(this)
    override fun visitTypeof(expr: TypeofExpression) = 2 + expr.operand.accept(this)
    override fun visitInstanceof(expr: InstanceofExpression) = 3 + expr.left.accept(this) + expr.right.accept(this)

    companion object {
        private val instance = ComplexityCalculator()
        fun calculate(expr: Expression): Int = expr.accept(instance)
    }
}

