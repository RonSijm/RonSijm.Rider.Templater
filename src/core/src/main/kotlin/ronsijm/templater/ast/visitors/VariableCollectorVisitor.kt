package ronsijm.templater.ast.visitors

import ronsijm.templater.ast.*

class VariableCollectorVisitor : BaseAstVisitor<Unit>(Unit) {
    private val _variables = mutableSetOf<String>()
    private val _assigned = mutableSetOf<String>()
    private val _called = mutableSetOf<String>()

    val variables: Set<String> get() = _variables.toSet()
    val assigned: Set<String> get() = _assigned.toSet()
    val called: Set<String> get() = _called.toSet()
    val freeVariables: Set<String> get() = _variables - _assigned

    override fun visitIdentifier(node: Identifier) {
        _variables.add(node.name)
    }

    override fun visitAssignmentExpr(node: AssignmentExpr) {

        when (val target = node.target) {
            is Identifier -> _assigned.add(target.name)
            else -> target.accept(this)
        }
        node.value.accept(this)
    }

    override fun visitCallExpr(node: CallExpr) {
        when (val callee = node.callee) {
            is Identifier -> _called.add(callee.name)
            else -> callee.accept(this)
        }
        node.arguments.forEach { it.accept(this) }
    }

    override fun visitMethodCallExpr(node: MethodCallExpr) {
        _called.add(node.method)
        node.obj.accept(this)
        node.arguments.forEach { it.accept(this) }
    }

    override fun visitArrowFunctionExpr(node: ArrowFunctionExpr) {

        val childCollector = VariableCollectorVisitor()
        node.body.accept(childCollector)


        _called.addAll(childCollector.called)


        val params = node.params.toSet()
        _variables.addAll(childCollector.freeVariables - params)
    }

    companion object {
        fun collect(expression: Expression): VariableCollectorVisitor {
            val visitor = VariableCollectorVisitor()
            expression.accept(visitor)
            return visitor
        }
    }
}

class PurityCheckVisitor : AstVisitor<Boolean> {
    override fun visitNumberLiteral(node: NumberLiteral) = true
    override fun visitStringLiteral(node: StringLiteral) = true
    override fun visitBooleanLiteral(node: BooleanLiteral) = true
    override fun visitNullLiteral(node: NullLiteral) = true
    override fun visitIdentifier(node: Identifier) = true

    override fun visitArrayLiteral(node: ArrayLiteral) =
        node.elements.all { it.accept(this) }

    override fun visitObjectLiteral(node: ObjectLiteral) =
        node.properties.all { it.value.accept(this) }

    override fun visitTemplateLiteral(node: TemplateLiteral) =
        node.parts.filterIsInstance<TemplateInterpolation>().all { it.expression.accept(this) }

    override fun visitPropertyAccess(node: PropertyAccess) =
        node.obj.accept(this)

    override fun visitIndexAccess(node: IndexAccess) =
        node.obj.accept(this) && node.index.accept(this)

    override fun visitBinaryExpr(node: BinaryExpr) =
        node.left.accept(this) && node.right.accept(this)

    override fun visitUnaryExpr(node: UnaryExpr): Boolean {

        if (node.operator == UnaryOperator.INCREMENT || node.operator == UnaryOperator.DECREMENT) {
            return false
        }
        return node.operand.accept(this)
    }

    override fun visitConditionalExpr(node: ConditionalExpr) =
        node.condition.accept(this) && node.consequent.accept(this) && node.alternate.accept(this)


    override fun visitCallExpr(node: CallExpr) = false
    override fun visitMethodCallExpr(node: MethodCallExpr) = false
    override fun visitNewExpr(node: NewExpr) = false

    override fun visitArrowFunctionExpr(node: ArrowFunctionExpr) = true


    override fun visitAssignmentExpr(node: AssignmentExpr) = false

    override fun visitGroupExpr(node: GroupExpr) =
        node.expression.accept(this)

    companion object {
        private val instance = PurityCheckVisitor()

        fun isPure(expression: Expression): Boolean =
            expression.accept(instance)
    }
}

class ComplexityVisitor : AstVisitor<Int> {
    override fun visitNumberLiteral(node: NumberLiteral) = 1
    override fun visitStringLiteral(node: StringLiteral) = 1
    override fun visitBooleanLiteral(node: BooleanLiteral) = 1
    override fun visitNullLiteral(node: NullLiteral) = 1
    override fun visitIdentifier(node: Identifier) = 1

    override fun visitArrayLiteral(node: ArrayLiteral) =
        1 + node.elements.sumOf { it.accept(this) }

    override fun visitObjectLiteral(node: ObjectLiteral) =
        1 + node.properties.sumOf { it.value.accept(this) }

    override fun visitTemplateLiteral(node: TemplateLiteral) =
        1 + node.parts.filterIsInstance<TemplateInterpolation>().sumOf { it.expression.accept(this) }

    override fun visitPropertyAccess(node: PropertyAccess) =
        1 + node.obj.accept(this)

    override fun visitIndexAccess(node: IndexAccess) =
        1 + node.obj.accept(this) + node.index.accept(this)

    override fun visitBinaryExpr(node: BinaryExpr) =
        1 + node.left.accept(this) + node.right.accept(this)

    override fun visitUnaryExpr(node: UnaryExpr) =
        1 + node.operand.accept(this)

    override fun visitConditionalExpr(node: ConditionalExpr) =
        1 + node.condition.accept(this) + node.consequent.accept(this) + node.alternate.accept(this)

    override fun visitCallExpr(node: CallExpr) =
        2 + node.callee.accept(this) + node.arguments.sumOf { it.accept(this) }

    override fun visitMethodCallExpr(node: MethodCallExpr) =
        2 + node.obj.accept(this) + node.arguments.sumOf { it.accept(this) }

    override fun visitArrowFunctionExpr(node: ArrowFunctionExpr) =
        2 + node.body.accept(this)

    override fun visitNewExpr(node: NewExpr) =
        3 + node.callee.accept(this) + node.arguments.sumOf { it.accept(this) }

    override fun visitAssignmentExpr(node: AssignmentExpr) =
        1 + node.target.accept(this) + node.value.accept(this)

    override fun visitGroupExpr(node: GroupExpr) =
        node.expression.accept(this)

    companion object {
        private val instance = ComplexityVisitor()

        fun complexity(expression: Expression): Int =
            expression.accept(instance)
    }
}

