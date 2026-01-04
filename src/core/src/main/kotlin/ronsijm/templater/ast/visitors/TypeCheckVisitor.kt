package ronsijm.templater.ast.visitors

import ronsijm.templater.ast.*

sealed interface TypeInfo {
    val name: String

    data object NumberType : TypeInfo { override val name = "number" }
    data object StringType : TypeInfo { override val name = "string" }
    data object BooleanType : TypeInfo { override val name = "boolean" }
    data object NullType : TypeInfo { override val name = "null" }
    data object UndefinedType : TypeInfo { override val name = "undefined" }
    data class ArrayType(val elementType: TypeInfo) : TypeInfo { override val name = "array<${elementType.name}>" }
    data class ObjectType(val properties: Map<String, TypeInfo>) : TypeInfo { override val name = "object" }
    data class FunctionType(val params: List<TypeInfo>, val returnType: TypeInfo) : TypeInfo {
        override val name = "(${params.joinToString { it.name }}) -> ${returnType.name}"
    }
    data object AnyType : TypeInfo { override val name = "any" }
    data class UnionType(val types: Set<TypeInfo>) : TypeInfo {
        override val name = types.joinToString(" | ") { it.name }
    }
}

data class TypeCheckResult(
    val type: TypeInfo,
    val errors: List<TypeError> = emptyList()
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()

    companion object {
        fun success(type: TypeInfo) = TypeCheckResult(type)
        fun error(type: TypeInfo, error: TypeError) = TypeCheckResult(type, listOf(error))
        fun errors(type: TypeInfo, errors: List<TypeError>) = TypeCheckResult(type, errors)
    }
}

data class TypeError(
    val message: String,
    val location: SourceLocation,
    val severity: Severity = Severity.ERROR
) {
    enum class Severity { WARNING, ERROR }
}

class TypeEnvironment(private val parent: TypeEnvironment? = null) {
    private val bindings = mutableMapOf<String, TypeInfo>()

    fun define(name: String, type: TypeInfo) {
        bindings[name] = type
    }

    fun lookup(name: String): TypeInfo? =
        bindings[name] ?: parent?.lookup(name)

    fun child(): TypeEnvironment = TypeEnvironment(this)
}

class TypeCheckVisitor(
    private val env: TypeEnvironment = TypeEnvironment()
) : AstVisitor<TypeCheckResult> {

    private val errors = mutableListOf<TypeError>()

    override fun visitNumberLiteral(node: NumberLiteral) =
        TypeCheckResult.success(TypeInfo.NumberType)

    override fun visitStringLiteral(node: StringLiteral) =
        TypeCheckResult.success(TypeInfo.StringType)

    override fun visitBooleanLiteral(node: BooleanLiteral) =
        TypeCheckResult.success(TypeInfo.BooleanType)

    override fun visitNullLiteral(node: NullLiteral) =
        TypeCheckResult.success(TypeInfo.NullType)

    override fun visitArrayLiteral(node: ArrayLiteral): TypeCheckResult {
        if (node.elements.isEmpty()) {
            return TypeCheckResult.success(TypeInfo.ArrayType(TypeInfo.AnyType))
        }
        val elementResults = node.elements.map { it.accept(this) }
        val allErrors = elementResults.flatMap { it.errors }
        val elementTypes = elementResults.map { it.type }.toSet()
        val elementType = if (elementTypes.size == 1) elementTypes.first() else TypeInfo.UnionType(elementTypes)
        return TypeCheckResult(TypeInfo.ArrayType(elementType), allErrors)
    }

    override fun visitObjectLiteral(node: ObjectLiteral): TypeCheckResult {
        val propTypes = mutableMapOf<String, TypeInfo>()
        val allErrors = mutableListOf<TypeError>()
        for (prop in node.properties) {
            val result = prop.value.accept(this)
            propTypes[prop.key] = result.type
            allErrors.addAll(result.errors)
        }
        return TypeCheckResult(TypeInfo.ObjectType(propTypes), allErrors)
    }

    override fun visitTemplateLiteral(node: TemplateLiteral): TypeCheckResult {
        val allErrors = mutableListOf<TypeError>()
        for (part in node.parts) {
            if (part is TemplateInterpolation) {
                val result = part.expression.accept(this)
                allErrors.addAll(result.errors)
            }
        }
        return TypeCheckResult(TypeInfo.StringType, allErrors)
    }

    override fun visitIdentifier(node: Identifier): TypeCheckResult {
        val type = env.lookup(node.name)
        return if (type != null) {
            TypeCheckResult.success(type)
        } else {
            TypeCheckResult.error(
                TypeInfo.AnyType,
                TypeError("Unknown identifier: ${node.name}", node.location)
            )
        }
    }

    override fun visitPropertyAccess(node: PropertyAccess): TypeCheckResult {
        val objResult = node.obj.accept(this)
        return when (val objType = objResult.type) {
            is TypeInfo.ObjectType -> {
                val propType = objType.properties[node.property]
                if (propType != null) {
                    TypeCheckResult(propType, objResult.errors)
                } else {
                    TypeCheckResult.errors(
                        TypeInfo.AnyType,
                        objResult.errors + TypeError("Property '${node.property}' not found", node.location)
                    )
                }
            }
            is TypeInfo.ArrayType -> {
                when (node.property) {
                    "length" -> TypeCheckResult(TypeInfo.NumberType, objResult.errors)
                    else -> TypeCheckResult(TypeInfo.AnyType, objResult.errors)
                }
            }
            is TypeInfo.StringType -> {
                when (node.property) {
                    "length" -> TypeCheckResult(TypeInfo.NumberType, objResult.errors)
                    else -> TypeCheckResult(TypeInfo.AnyType, objResult.errors)
                }
            }
            else -> TypeCheckResult(TypeInfo.AnyType, objResult.errors)
        }
    }

    override fun visitIndexAccess(node: IndexAccess): TypeCheckResult {
        val objResult = node.obj.accept(this)
        val indexResult = node.index.accept(this)
        val allErrors = objResult.errors + indexResult.errors
        return when (val objType = objResult.type) {
            is TypeInfo.ArrayType -> TypeCheckResult(objType.elementType, allErrors)
            is TypeInfo.StringType -> TypeCheckResult(TypeInfo.StringType, allErrors)
            else -> TypeCheckResult(TypeInfo.AnyType, allErrors)
        }
    }

    override fun visitBinaryExpr(node: BinaryExpr): TypeCheckResult {
        val leftResult = node.left.accept(this)
        val rightResult = node.right.accept(this)
        val allErrors = leftResult.errors + rightResult.errors

        val resultType = when (node.operator) {
            BinaryOperator.ADD -> {
                if (leftResult.type == TypeInfo.StringType || rightResult.type == TypeInfo.StringType) {
                    TypeInfo.StringType
                } else {
                    TypeInfo.NumberType
                }
            }
            BinaryOperator.SUBTRACT, BinaryOperator.MULTIPLY, BinaryOperator.DIVIDE, BinaryOperator.MODULO,
            BinaryOperator.LEFT_SHIFT, BinaryOperator.RIGHT_SHIFT, BinaryOperator.UNSIGNED_RIGHT_SHIFT,
            BinaryOperator.BITWISE_AND, BinaryOperator.BITWISE_OR, BinaryOperator.BITWISE_XOR -> TypeInfo.NumberType
            BinaryOperator.EQUAL, BinaryOperator.NOT_EQUAL, BinaryOperator.STRICT_EQUAL, BinaryOperator.STRICT_NOT_EQUAL,
            BinaryOperator.LESS_THAN, BinaryOperator.GREATER_THAN, BinaryOperator.LESS_EQUAL, BinaryOperator.GREATER_EQUAL -> TypeInfo.BooleanType
            BinaryOperator.AND, BinaryOperator.OR -> TypeInfo.BooleanType
            BinaryOperator.NULLISH_COALESCE -> TypeInfo.UnionType(setOf(leftResult.type, rightResult.type))
        }
        return TypeCheckResult(resultType, allErrors)
    }

    override fun visitUnaryExpr(node: UnaryExpr): TypeCheckResult {
        val operandResult = node.operand.accept(this)
        val resultType = when (node.operator) {
            UnaryOperator.NOT -> TypeInfo.BooleanType
            UnaryOperator.NEGATE, UnaryOperator.PLUS, UnaryOperator.INCREMENT, UnaryOperator.DECREMENT -> TypeInfo.NumberType
            UnaryOperator.TYPEOF -> TypeInfo.StringType
        }
        return TypeCheckResult(resultType, operandResult.errors)
    }

    override fun visitConditionalExpr(node: ConditionalExpr): TypeCheckResult {
        val condResult = node.condition.accept(this)
        val consResult = node.consequent.accept(this)
        val altResult = node.alternate.accept(this)
        val allErrors = condResult.errors + consResult.errors + altResult.errors
        val resultType = if (consResult.type == altResult.type) {
            consResult.type
        } else {
            TypeInfo.UnionType(setOf(consResult.type, altResult.type))
        }
        return TypeCheckResult(resultType, allErrors)
    }

    override fun visitCallExpr(node: CallExpr): TypeCheckResult {
        val calleeResult = node.callee.accept(this)
        val argResults = node.arguments.map { it.accept(this) }
        val allErrors = calleeResult.errors + argResults.flatMap { it.errors }
        return TypeCheckResult(TypeInfo.AnyType, allErrors)
    }

    override fun visitMethodCallExpr(node: MethodCallExpr): TypeCheckResult {
        val objResult = node.obj.accept(this)
        val argResults = node.arguments.map { it.accept(this) }
        val allErrors = objResult.errors + argResults.flatMap { it.errors }
        return TypeCheckResult(TypeInfo.AnyType, allErrors)
    }

    override fun visitArrowFunctionExpr(node: ArrowFunctionExpr): TypeCheckResult {
        val childEnv = env.child()
        node.params.forEach { childEnv.define(it, TypeInfo.AnyType) }
        val childVisitor = TypeCheckVisitor(childEnv)
        val bodyResult = node.body.accept(childVisitor)
        val paramTypes = node.params.map { TypeInfo.AnyType }
        return TypeCheckResult(TypeInfo.FunctionType(paramTypes, bodyResult.type), bodyResult.errors)
    }

    override fun visitNewExpr(node: NewExpr): TypeCheckResult {
        val calleeResult = node.callee.accept(this)
        val argResults = node.arguments.map { it.accept(this) }
        val allErrors = calleeResult.errors + argResults.flatMap { it.errors }
        return TypeCheckResult(TypeInfo.ObjectType(emptyMap()), allErrors)
    }

    override fun visitAssignmentExpr(node: AssignmentExpr): TypeCheckResult {
        val valueResult = node.value.accept(this)
        if (node.target is Identifier) {
            env.define(node.target.name, valueResult.type)
        }
        return valueResult
    }

    override fun visitGroupExpr(node: GroupExpr): TypeCheckResult =
        node.expression.accept(this)
}

