package ronsijm.templater.expression.visitors

import ronsijm.templater.expression.*
import ronsijm.templater.utils.TypeConverter
import kotlin.math.pow


interface EvaluationContext {
    fun getVariable(name: String): Any?
    fun setVariable(name: String, value: Any?)
    fun callFunction(name: String, args: List<Any?>): Any?
    fun getProperty(target: Any?, property: String): Any?
    fun getIndex(target: Any?, index: Any?): Any?
    fun createInstance(constructor: Any?, args: List<Any?>): Any?
}


class SimpleEvaluationContext(
    private val variables: MutableMap<String, Any?> = mutableMapOf(),
    private val functions: Map<String, (List<Any?>) -> Any?> = emptyMap()
) : EvaluationContext {
    override fun getVariable(name: String): Any? = variables[name]
    override fun setVariable(name: String, value: Any?) { variables[name] = value }
    override fun callFunction(name: String, args: List<Any?>): Any? =
        functions[name]?.invoke(args) ?: throw IllegalArgumentException("Unknown function: $name")
    override fun getProperty(target: Any?, property: String): Any? {
        return when (target) {
            is Map<*, *> -> target[property]
            else -> target?.javaClass?.getMethod("get${property.replaceFirstChar { it.uppercase() }}")?.invoke(target)
        }
    }
    override fun getIndex(target: Any?, index: Any?): Any? {
        return when (target) {
            is List<*> -> target[(index as Number).toInt()]
            is Array<*> -> target[(index as Number).toInt()]
            is Map<*, *> -> target[index]
            is String -> target[(index as Number).toInt()]
            else -> null
        }
    }
    override fun createInstance(constructor: Any?, args: List<Any?>): Any? {
        throw UnsupportedOperationException("Instance creation not supported in simple context")
    }
}


class ExpressionEvaluator(private val context: EvaluationContext) : ExpressionVisitor<Any?> {
    override fun visitIntLiteral(expr: LiteralExpression.IntLiteral): Any? = expr.value
    override fun visitDoubleLiteral(expr: LiteralExpression.DoubleLiteral): Any? = expr.value
    override fun visitStringLiteral(expr: LiteralExpression.StringLiteral): Any? = expr.value
    override fun visitBooleanLiteral(expr: LiteralExpression.BooleanLiteral): Any? = expr.value
    override fun visitNullLiteral(expr: LiteralExpression.NullLiteral): Any? = null

    override fun visitArrayLiteral(expr: LiteralExpression.ArrayLiteral): Any? =
        expr.elements.map { it.accept(this) }

    override fun visitObjectLiteral(expr: LiteralExpression.ObjectLiteral): Any? =
        expr.properties.mapValues { it.value.accept(this) }

    override fun visitVariable(expr: VariableExpression): Any? = context.getVariable(expr.name)

    override fun visitPropertyAccess(expr: PropertyAccessExpression): Any? =
        context.getProperty(expr.target.accept(this), expr.property)

    override fun visitIndexAccess(expr: IndexAccessExpression): Any? =
        context.getIndex(expr.target.accept(this), expr.index.accept(this))

    override fun visitBinary(expr: BinaryExpression): Any? {
        val left = expr.left.accept(this)
        val right = expr.right.accept(this)
        return evaluateBinary(left, expr.operator, right)
    }

    override fun visitUnary(expr: UnaryExpression): Any? {
        val operand = expr.operand.accept(this)
        return evaluateUnary(expr.operator, operand)
    }

    override fun visitCall(expr: CallExpression): Any? {
        val callee = expr.callee
        val args = expr.arguments.map { it.accept(this) }
        return when (callee) {
            is VariableExpression -> context.callFunction(callee.name, args)
            else -> throw UnsupportedOperationException("Cannot call non-variable expression")
        }
    }

    override fun visitMethodCall(expr: MethodCallExpression): Any? {
        val target = expr.target.accept(this)
        val args = expr.arguments.map { it.accept(this) }
        return invokeMethod(target, expr.method, args)
    }

    override fun visitNew(expr: NewExpression): Any? {
        val constructor = expr.constructor.accept(this)
        val args = expr.arguments.map { it.accept(this) }
        return context.createInstance(constructor, args)
    }

    override fun visitConditional(expr: ConditionalExpression): Any? =
        if (isTruthy(expr.condition.accept(this))) expr.thenBranch.accept(this) else expr.elseBranch.accept(this)

    override fun visitLogicalAnd(expr: LogicalAndExpression): Any? {
        val left = expr.left.accept(this)
        return if (!isTruthy(left)) left else expr.right.accept(this)
    }

    override fun visitLogicalOr(expr: LogicalOrExpression): Any? {
        val left = expr.left.accept(this)
        return if (isTruthy(left)) left else expr.right.accept(this)
    }

    override fun visitNullishCoalescing(expr: NullishCoalescingExpression): Any? {
        val left = expr.left.accept(this)
        return left ?: expr.right.accept(this)
    }

    override fun visitArrowFunction(expr: ArrowFunctionExpression): Any? {
        return ArrowFunctionValue(expr.parameters, expr.body, this)
    }

    override fun visitTemplateLiteral(expr: TemplateLiteralExpression): Any? {
        return expr.parts.joinToString("") { part ->
            when (part) {
                is TemplatePart.StringPart -> part.value
                is TemplatePart.ExpressionPart -> part.expression.accept(this)?.toString() ?: ""
            }
        }
    }

    override fun visitAssignment(expr: AssignmentExpression): Any? {
        val value = when (expr.operator) {
            AssignmentOperator.ASSIGN -> expr.value.accept(this)
            else -> {
                val current = expr.target.accept(this)
                val newValue = expr.value.accept(this)
                evaluateCompoundAssignment(current, expr.operator, newValue)
            }
        }
        when (val target = expr.target) {
            is VariableExpression -> context.setVariable(target.name, value)
            else -> throw UnsupportedOperationException("Cannot assign to ${target::class.simpleName}")
        }
        return value
    }

    override fun visitTypeof(expr: TypeofExpression): Any? {
        val operand = expr.operand.accept(this)
        return when (operand) {
            null -> "null"
            is Boolean -> "boolean"
            is Number -> "number"
            is String -> "string"
            is Function<*> -> "function"
            else -> "object"
        }
    }

    override fun visitInstanceof(expr: InstanceofExpression): Any? {
        val left = expr.left.accept(this)
        val right = expr.right.accept(this)
        return when (right) {
            is Class<*> -> right.isInstance(left)
            else -> false
        }
    }

    private fun evaluateBinary(left: Any?, op: BinaryOperator, right: Any?): Any? = when (op) {
        BinaryOperator.ADD -> add(left, right)
        BinaryOperator.SUBTRACT -> subtract(left, right)
        BinaryOperator.MULTIPLY -> multiply(left, right)
        BinaryOperator.DIVIDE -> divide(left, right)
        BinaryOperator.MODULO -> modulo(left, right)
        BinaryOperator.POWER -> power(left, right)
        BinaryOperator.EQUAL, BinaryOperator.STRICT_EQUAL -> left == right
        BinaryOperator.NOT_EQUAL, BinaryOperator.STRICT_NOT_EQUAL -> left != right
        BinaryOperator.LESS_THAN -> compare(left, right) < 0
        BinaryOperator.LESS_THAN_OR_EQUAL -> compare(left, right) <= 0
        BinaryOperator.GREATER_THAN -> compare(left, right) > 0
        BinaryOperator.GREATER_THAN_OR_EQUAL -> compare(left, right) >= 0
        BinaryOperator.CONCAT -> "${left ?: ""}${right ?: ""}"
        else -> throw UnsupportedOperationException("Unsupported operator: $op")
    }
    private fun evaluateUnary(op: UnaryOperator, operand: Any?): Any? = when (op) {
        UnaryOperator.NEGATE -> negate(operand)
        UnaryOperator.PLUS -> toNumber(operand)
        UnaryOperator.NOT -> !isTruthy(operand)
        else -> throw UnsupportedOperationException("Unsupported operator: $op")
    }
    private fun add(a: Any?, b: Any?): Any? = when {
        a is String || b is String -> "${a ?: ""}${b ?: ""}"
        a is Double || b is Double -> toDouble(a) + toDouble(b)
        else -> toInt(a) + toInt(b)
    }
    private fun subtract(a: Any?, b: Any?): Any? = when {
        a is Double || b is Double -> toDouble(a) - toDouble(b)
        else -> toInt(a) - toInt(b)
    }
    private fun multiply(a: Any?, b: Any?): Any? = when {
        a is Double || b is Double -> toDouble(a) * toDouble(b)
        else -> toInt(a) * toInt(b)
    }
    private fun divide(a: Any?, b: Any?): Any? = toDouble(a) / toDouble(b)
    private fun modulo(a: Any?, b: Any?): Any? = toInt(a) % toInt(b)
    private fun power(a: Any?, b: Any?): Any? = toDouble(a).pow(toDouble(b))
    private fun negate(a: Any?): Any? = when (a) { is Double -> -a; else -> -toInt(a) }
    private fun compare(a: Any?, b: Any?): Int = when {
        a is Number && b is Number -> toDouble(a).compareTo(toDouble(b))
        a is String && b is String -> a.compareTo(b)
        else -> 0
    }
    private fun toInt(a: Any?): Int = TypeConverter.toInt(a)
    private fun toDouble(a: Any?): Double = TypeConverter.toDouble(a)
    private fun toNumber(a: Any?): Number = (a as? Number) ?: 0
    private fun isTruthy(a: Any?): Boolean = TypeConverter.toBoolean(a)
    private fun evaluateCompoundAssignment(current: Any?, op: AssignmentOperator, value: Any?): Any? = when (op) {
        AssignmentOperator.ADD_ASSIGN -> add(current, value)
        AssignmentOperator.SUBTRACT_ASSIGN -> subtract(current, value)
        AssignmentOperator.MULTIPLY_ASSIGN -> multiply(current, value)
        AssignmentOperator.DIVIDE_ASSIGN -> divide(current, value)
        else -> throw UnsupportedOperationException("Unsupported compound assignment: $op")
    }
    private fun invokeMethod(target: Any?, method: String, args: List<Any?>): Any? {
        if (target == null) return null
        return when (method) {
            "toString" -> target.toString()
            "length" -> when (target) { is String -> target.length; is List<*> -> target.size; else -> null }
            else -> null
        }
    }
}


class ArrowFunctionValue(
    val parameters: List<String>,
    val body: Expression,
    private val evaluator: ExpressionEvaluator
) {
    fun invoke(args: List<Any?>): Any? = body.accept(evaluator)
}

