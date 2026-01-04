package ronsijm.templater.ast

interface AstVisitor<R> {

    fun visitNumberLiteral(node: NumberLiteral): R
    fun visitStringLiteral(node: StringLiteral): R
    fun visitBooleanLiteral(node: BooleanLiteral): R
    fun visitNullLiteral(node: NullLiteral): R
    fun visitArrayLiteral(node: ArrayLiteral): R
    fun visitObjectLiteral(node: ObjectLiteral): R
    fun visitTemplateLiteral(node: TemplateLiteral): R


    fun visitIdentifier(node: Identifier): R
    fun visitPropertyAccess(node: PropertyAccess): R
    fun visitIndexAccess(node: IndexAccess): R


    fun visitBinaryExpr(node: BinaryExpr): R
    fun visitUnaryExpr(node: UnaryExpr): R
    fun visitConditionalExpr(node: ConditionalExpr): R


    fun visitCallExpr(node: CallExpr): R
    fun visitMethodCallExpr(node: MethodCallExpr): R
    fun visitArrowFunctionExpr(node: ArrowFunctionExpr): R
    fun visitNewExpr(node: NewExpr): R


    fun visitAssignmentExpr(node: AssignmentExpr): R


    fun visitGroupExpr(node: GroupExpr): R
}

abstract class BaseAstVisitor<R>(private val defaultValue: R) : AstVisitor<R> {
    override fun visitNumberLiteral(node: NumberLiteral): R = defaultValue
    override fun visitStringLiteral(node: StringLiteral): R = defaultValue
    override fun visitBooleanLiteral(node: BooleanLiteral): R = defaultValue
    override fun visitNullLiteral(node: NullLiteral): R = defaultValue

    override fun visitArrayLiteral(node: ArrayLiteral): R {
        node.elements.forEach { it.accept(this) }
        return defaultValue
    }

    override fun visitObjectLiteral(node: ObjectLiteral): R {
        node.properties.forEach { it.value.accept(this) }
        return defaultValue
    }

    override fun visitTemplateLiteral(node: TemplateLiteral): R {
        node.parts.filterIsInstance<TemplateInterpolation>().forEach { it.expression.accept(this) }
        return defaultValue
    }

    override fun visitIdentifier(node: Identifier): R = defaultValue

    override fun visitPropertyAccess(node: PropertyAccess): R {
        node.obj.accept(this)
        return defaultValue
    }

    override fun visitIndexAccess(node: IndexAccess): R {
        node.obj.accept(this)
        node.index.accept(this)
        return defaultValue
    }

    override fun visitBinaryExpr(node: BinaryExpr): R {
        node.left.accept(this)
        node.right.accept(this)
        return defaultValue
    }

    override fun visitUnaryExpr(node: UnaryExpr): R {
        node.operand.accept(this)
        return defaultValue
    }

    override fun visitConditionalExpr(node: ConditionalExpr): R {
        node.condition.accept(this)
        node.consequent.accept(this)
        node.alternate.accept(this)
        return defaultValue
    }

    override fun visitCallExpr(node: CallExpr): R {
        node.callee.accept(this)
        node.arguments.forEach { it.accept(this) }
        return defaultValue
    }

    override fun visitMethodCallExpr(node: MethodCallExpr): R {
        node.obj.accept(this)
        node.arguments.forEach { it.accept(this) }
        return defaultValue
    }

    override fun visitArrowFunctionExpr(node: ArrowFunctionExpr): R {
        node.body.accept(this)
        return defaultValue
    }

    override fun visitNewExpr(node: NewExpr): R {
        node.callee.accept(this)
        node.arguments.forEach { it.accept(this) }
        return defaultValue
    }

    override fun visitAssignmentExpr(node: AssignmentExpr): R {
        node.target.accept(this)
        node.value.accept(this)
        return defaultValue
    }

    override fun visitGroupExpr(node: GroupExpr): R {
        node.expression.accept(this)
        return defaultValue
    }
}

abstract class TransformingVisitor : AstVisitor<Expression> {
    override fun visitNumberLiteral(node: NumberLiteral): Expression = node
    override fun visitStringLiteral(node: StringLiteral): Expression = node
    override fun visitBooleanLiteral(node: BooleanLiteral): Expression = node
    override fun visitNullLiteral(node: NullLiteral): Expression = node
    override fun visitIdentifier(node: Identifier): Expression = node

    override fun visitArrayLiteral(node: ArrayLiteral): Expression =
        node.copy(elements = node.elements.map { it.accept(this) })

    override fun visitObjectLiteral(node: ObjectLiteral): Expression =
        node.copy(properties = node.properties.map { it.copy(value = it.value.accept(this)) })

    override fun visitTemplateLiteral(node: TemplateLiteral): Expression =
        node.copy(parts = node.parts.map { part ->
            when (part) {
                is TemplateString -> part
                is TemplateInterpolation -> TemplateInterpolation(part.expression.accept(this))
            }
        })

    override fun visitPropertyAccess(node: PropertyAccess): Expression =
        node.copy(obj = node.obj.accept(this))

    override fun visitIndexAccess(node: IndexAccess): Expression =
        node.copy(obj = node.obj.accept(this), index = node.index.accept(this))

    override fun visitBinaryExpr(node: BinaryExpr): Expression =
        node.copy(left = node.left.accept(this), right = node.right.accept(this))

    override fun visitUnaryExpr(node: UnaryExpr): Expression =
        node.copy(operand = node.operand.accept(this))

    override fun visitConditionalExpr(node: ConditionalExpr): Expression =
        node.copy(
            condition = node.condition.accept(this),
            consequent = node.consequent.accept(this),
            alternate = node.alternate.accept(this)
        )

    override fun visitCallExpr(node: CallExpr): Expression =
        node.copy(callee = node.callee.accept(this), arguments = node.arguments.map { it.accept(this) })

    override fun visitMethodCallExpr(node: MethodCallExpr): Expression =
        node.copy(obj = node.obj.accept(this), arguments = node.arguments.map { it.accept(this) })

    override fun visitArrowFunctionExpr(node: ArrowFunctionExpr): Expression =
        node.copy(body = node.body.accept(this))

    override fun visitNewExpr(node: NewExpr): Expression =
        node.copy(callee = node.callee.accept(this), arguments = node.arguments.map { it.accept(this) })

    override fun visitAssignmentExpr(node: AssignmentExpr): Expression =
        node.copy(target = node.target.accept(this), value = node.value.accept(this))

    override fun visitGroupExpr(node: GroupExpr): Expression =
        node.copy(expression = node.expression.accept(this))
}

