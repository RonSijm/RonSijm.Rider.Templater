package ronsijm.templater.script.ast

import ronsijm.templater.script.ScriptContext


class AstInterpreter(
    private val context: ScriptContext,
    private val functionCaller: (String, List<Any?>) -> Any? = { _, _ -> null },
    private val methodCaller: (Any?, String, List<Any?>) -> Any? = { _, _, _ -> null }
) {
    private var returnValue: Any? = null
    private var returnRequested = false

    class RuntimeError(message: String, val location: SourceLocation) : RuntimeException(message)

    fun execute(program: Program): Any? {
        for (statement in program.statements) {
            executeStatement(statement)
            if (returnRequested) break
        }
        return returnValue
    }

    fun evaluate(expression: Expression): Any? = evaluateExpression(expression)



    private fun executeStatement(statement: Statement) {
        if (returnRequested) return

        when (statement) {
            is BlockStatement -> executeBlock(statement)
            is ExpressionStatement -> evaluateExpression(statement.expression)
            is VariableDeclaration -> executeVariableDeclaration(statement)
            is IfStatement -> executeIf(statement)
            is ForStatement -> executeFor(statement)
            is ForOfStatement -> executeForOf(statement)
            is WhileStatement -> executeWhile(statement)
            is ReturnStatement -> executeReturn(statement)
            is BreakStatement -> {  }
            is ContinueStatement -> {  }
            is FunctionDeclaration -> executeFunction(statement)
            is TryStatement -> executeTry(statement)
            is ThrowStatement -> throw RuntimeError(
                evaluateExpression(statement.expression)?.toString() ?: "null",
                statement.location
            )
            is EmptyStatement -> {  }
            is ResultAccumulatorStatement -> executeResultAccumulator(statement)
            is UpdateStatement -> executeUpdate(statement)
        }
    }

    private fun executeBlock(block: BlockStatement) {
        for (statement in block.statements) {
            executeStatement(statement)
            if (returnRequested) break
        }
    }

    private fun executeVariableDeclaration(decl: VariableDeclaration) {
        val value = decl.initializer?.let { evaluateExpression(it) }
        context.setVariable(decl.name, value)
    }

    private fun executeIf(stmt: IfStatement) {
        val condition = evaluateExpression(stmt.condition)
        if (isTruthy(condition)) {
            executeStatement(stmt.thenBranch)
        } else {
            stmt.elseBranch?.let { executeStatement(it) }
        }
    }

    private fun executeFor(stmt: ForStatement) {
        stmt.init?.let { executeStatement(it) }
        var iterations = 0
        val maxIterations = 100_000

        while (iterations < maxIterations) {
            val condition = stmt.condition?.let { evaluateExpression(it) } ?: true
            if (!isTruthy(condition)) break

            executeStatement(stmt.body)
            if (returnRequested) break

            stmt.update?.let { evaluateExpression(it) }
            iterations++
        }
    }

    private fun executeForOf(stmt: ForOfStatement) {
        val iterable = evaluateExpression(stmt.iterable)
        val items: List<Any?> = when (iterable) {
            is List<*> -> iterable
            is String -> iterable.toList().map { it.toString() }
            else -> return
        }

        for (item in items) {
            context.setVariable(stmt.variableName, item)
            executeStatement(stmt.body)
            if (returnRequested) break
        }
        context.removeVariable(stmt.variableName)
    }

    private fun executeWhile(stmt: WhileStatement) {
        var iterations = 0
        val maxIterations = 100_000

        while (iterations < maxIterations) {
            val condition = evaluateExpression(stmt.condition)
            if (!isTruthy(condition)) break

            executeStatement(stmt.body)
            if (returnRequested) break
            iterations++
        }
    }

    private fun executeReturn(stmt: ReturnStatement) {
        returnValue = stmt.value?.let { evaluateExpression(it) }
        returnRequested = true
    }

    private fun executeFunction(stmt: FunctionDeclaration) {

        context.setVariable(stmt.name, AstFunction(stmt.parameters, stmt.body))
    }

    private fun executeTry(stmt: TryStatement) {
        try {
            executeBlock(stmt.tryBlock)
        } catch (e: RuntimeError) {
            if (stmt.catchBlock != null && stmt.catchParam != null) {
                context.setVariable(stmt.catchParam, e.message)
                executeBlock(stmt.catchBlock)
                context.removeVariable(stmt.catchParam)
            }
        } finally {
            stmt.finallyBlock?.let { executeBlock(it) }
        }
    }

    private fun executeResultAccumulator(stmt: ResultAccumulatorStatement) {
        val value = evaluateExpression(stmt.value)
        when (stmt.operator) {
            AssignmentOperator.ASSIGN -> context.setResult(value?.toString() ?: "")
            AssignmentOperator.PLUS_ASSIGN -> context.appendToResult(value?.toString() ?: "")
            else -> {  }
        }
    }

    private fun executeUpdate(stmt: UpdateStatement) {
        val current = context.getVariable(stmt.variable)
        val numValue = toNumber(current) ?: 0.0
        val newValue = when (stmt.operator) {
            UpdateOperator.INCREMENT -> numValue + 1
            UpdateOperator.DECREMENT -> numValue - 1
        }
        context.setVariable(stmt.variable, if (newValue == newValue.toInt().toDouble()) newValue.toInt() else newValue)
    }



    private fun evaluateExpression(expr: Expression): Any? {
        return when (expr) {
            is NumberLiteral -> expr.value
            is StringLiteral -> expr.value
            is BooleanLiteral -> expr.value
            is NullLiteral -> null
            is ArrayLiteral -> expr.elements.map { evaluateExpression(it) }.toMutableList()
            is ObjectLiteral -> expr.properties.associate { it.first to evaluateExpression(it.second) }.toMutableMap()
            is TemplateLiteral -> evaluateTemplateLiteral(expr)
            is Identifier -> context.getVariable(expr.name)
            is MemberAccess -> evaluateMemberAccess(expr)
            is IndexAccess -> evaluateIndexAccess(expr)
            is BinaryExpression -> evaluateBinary(expr)
            is UnaryExpression -> evaluateUnary(expr)
            is TernaryExpression -> evaluateTernary(expr)
            is CallExpression -> evaluateCall(expr)
            is ArrowFunction -> AstFunction(expr.parameters, expr.body)
            is NewExpression -> evaluateNew(expr)
            is AssignmentExpression -> evaluateAssignment(expr)
        }
    }

    private fun evaluateTemplateLiteral(expr: TemplateLiteral): String {
        return expr.parts.joinToString("") { part ->
            when (part) {
                is TemplateString -> part.value
                is TemplateExpression -> evaluateExpression(part.expression)?.toString() ?: ""
            }
        }
    }

    private fun evaluateMemberAccess(expr: MemberAccess): Any? {
        val obj = evaluateExpression(expr.obj)
        return when (obj) {
            is Map<*, *> -> obj[expr.property]
            is List<*> -> when (expr.property) {
                "length" -> obj.size
                "first" -> obj.firstOrNull()
                "last" -> obj.lastOrNull()
                else -> null
            }
            is String -> when (expr.property) {
                "length" -> obj.length
                else -> null
            }
            else -> methodCaller(obj, expr.property, emptyList())
        }
    }

    private fun evaluateIndexAccess(expr: IndexAccess): Any? {
        val obj = evaluateExpression(expr.obj)
        val index = evaluateExpression(expr.index)
        return when (obj) {
            is List<*> -> {
                val idx = toInt(index) ?: return null
                obj.getOrNull(idx)
            }
            is Map<*, *> -> obj[index]
            is String -> {
                val idx = toInt(index) ?: return null
                obj.getOrNull(idx)?.toString()
            }
            else -> null
        }
    }

    private fun evaluateBinary(expr: BinaryExpression): Any? {

        if (expr.operator == BinaryOperator.AND) {
            val left = evaluateExpression(expr.left)
            return if (!isTruthy(left)) left else evaluateExpression(expr.right)
        }
        if (expr.operator == BinaryOperator.OR) {
            val left = evaluateExpression(expr.left)
            return if (isTruthy(left)) left else evaluateExpression(expr.right)
        }

        val left = evaluateExpression(expr.left)
        val right = evaluateExpression(expr.right)

        return when (expr.operator) {
            BinaryOperator.PLUS -> add(left, right)
            BinaryOperator.MINUS -> subtract(left, right)
            BinaryOperator.MULTIPLY -> multiply(left, right)
            BinaryOperator.DIVIDE -> divide(left, right)
            BinaryOperator.MODULO -> modulo(left, right)
            BinaryOperator.EQUALS -> looseEquals(left, right)
            BinaryOperator.NOT_EQUALS -> !looseEquals(left, right)
            BinaryOperator.STRICT_EQUALS -> strictEquals(left, right)
            BinaryOperator.STRICT_NOT_EQUALS -> !strictEquals(left, right)
            BinaryOperator.LESS_THAN -> compare(left, right) < 0
            BinaryOperator.LESS_THAN_OR_EQUAL -> compare(left, right) <= 0
            BinaryOperator.GREATER_THAN -> compare(left, right) > 0
            BinaryOperator.GREATER_THAN_OR_EQUAL -> compare(left, right) >= 0
            BinaryOperator.AND, BinaryOperator.OR -> throw IllegalStateException("Should be handled above")
            BinaryOperator.BITWISE_AND -> bitwiseAnd(left, right)
            BinaryOperator.BITWISE_OR -> bitwiseOr(left, right)
            BinaryOperator.BITWISE_XOR -> bitwiseXor(left, right)
            BinaryOperator.LEFT_SHIFT -> leftShift(left, right)
            BinaryOperator.RIGHT_SHIFT -> rightShift(left, right)
            BinaryOperator.UNSIGNED_RIGHT_SHIFT -> unsignedRightShift(left, right)
        }
    }

    private fun evaluateUnary(expr: UnaryExpression): Any? {
        val operand = evaluateExpression(expr.operand)
        return when (expr.operator) {
            UnaryOperator.NEGATE -> negate(operand)
            UnaryOperator.NOT -> !isTruthy(operand)
            UnaryOperator.TYPEOF -> typeOf(operand)
            UnaryOperator.BITWISE_NOT -> bitwiseNot(operand)
        }
    }

    private fun evaluateTernary(expr: TernaryExpression): Any? {
        val condition = evaluateExpression(expr.condition)
        return if (isTruthy(condition)) {
            evaluateExpression(expr.thenBranch)
        } else {
            evaluateExpression(expr.elseBranch)
        }
    }

    private fun evaluateCall(expr: CallExpression): Any? {
        val args = expr.arguments.map { evaluateExpression(it) }

        return when (val callee = expr.callee) {
            is Identifier -> {
                val func = context.getVariable(callee.name)
                if (func is AstFunction) {
                    callAstFunction(func, args)
                } else {
                    functionCaller(callee.name, args)
                }
            }
            is MemberAccess -> {
                val obj = evaluateExpression(callee.obj)
                methodCaller(obj, callee.property, args)
            }
            else -> null
        }
    }

    private fun callAstFunction(func: AstFunction, args: List<Any?>): Any? {

        val savedReturn = returnRequested
        val savedValue = returnValue
        returnRequested = false
        returnValue = null


        func.parameters.forEachIndexed { index, param ->
            context.setVariable(param, args.getOrNull(index))
        }


        when (val body = func.body) {
            is BlockStatement -> executeBlock(body)
            is Expression -> returnValue = evaluateExpression(body)
            else -> { }
        }

        val result = returnValue


        returnRequested = savedReturn
        returnValue = savedValue

        return result
    }

    private fun evaluateNew(expr: NewExpression): Any? {

        return evaluateCall(CallExpression(expr.callee, expr.arguments, expr.location))
    }

    private fun evaluateAssignment(expr: AssignmentExpression): Any? {
        val value = evaluateExpression(expr.value)
        val target = expr.target

        val finalValue = when (expr.operator) {
            AssignmentOperator.ASSIGN -> value
            AssignmentOperator.PLUS_ASSIGN -> add(getTargetValue(target), value)
            AssignmentOperator.MINUS_ASSIGN -> subtract(getTargetValue(target), value)
            AssignmentOperator.MULTIPLY_ASSIGN -> multiply(getTargetValue(target), value)
            AssignmentOperator.DIVIDE_ASSIGN -> divide(getTargetValue(target), value)
            AssignmentOperator.MODULO_ASSIGN -> modulo(getTargetValue(target), value)
        }

        setTargetValue(target, finalValue)
        return finalValue
    }

    private fun getTargetValue(target: Expression): Any? = evaluateExpression(target)

    private fun setTargetValue(target: Expression, value: Any?) {
        when (target) {
            is Identifier -> context.setVariable(target.name, value)
            is IndexAccess -> {
                val obj = evaluateExpression(target.obj)
                val index = evaluateExpression(target.index)
                if (obj is MutableList<*> && index is Int) {
                    @Suppress("UNCHECKED_CAST")
                    (obj as MutableList<Any?>)[index] = value
                } else if (obj is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    (obj as MutableMap<Any?, Any?>)[index] = value
                }
            }
            is MemberAccess -> {
                val obj = evaluateExpression(target.obj)
                if (obj is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    (obj as MutableMap<String, Any?>)[target.property] = value
                }
            }
            else -> { }
        }
    }



    private fun isTruthy(value: Any?): Boolean = when (value) {
        null -> false
        is Boolean -> value
        is Number -> value.toDouble() != 0.0
        is String -> value.isNotEmpty()
        else -> true
    }

    private fun toNumber(value: Any?): Double? = when (value) {
        null -> 0.0
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        is Boolean -> if (value) 1.0 else 0.0
        else -> null
    }

    private fun toInt(value: Any?): Int? = when (value) {
        is Int -> value
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }

    private fun add(left: Any?, right: Any?): Any? {
        if (left is String || right is String) {
            return (left?.toString() ?: "") + (right?.toString() ?: "")
        }
        val l = toNumber(left) ?: return null
        val r = toNumber(right) ?: return null
        val result = l + r
        return if (result == result.toInt().toDouble()) result.toInt() else result
    }

    private fun subtract(left: Any?, right: Any?): Any? {
        val l = toNumber(left) ?: return null
        val r = toNumber(right) ?: return null
        val result = l - r
        return if (result == result.toInt().toDouble()) result.toInt() else result
    }

    private fun multiply(left: Any?, right: Any?): Any? {
        val l = toNumber(left) ?: return null
        val r = toNumber(right) ?: return null
        val result = l * r
        return if (result == result.toInt().toDouble()) result.toInt() else result
    }

    private fun divide(left: Any?, right: Any?): Any? {
        val l = toNumber(left) ?: return null
        val r = toNumber(right) ?: return null
        if (r == 0.0) return Double.POSITIVE_INFINITY
        val result = l / r
        return if (result == result.toInt().toDouble()) result.toInt() else result
    }

    private fun modulo(left: Any?, right: Any?): Any? {
        val l = toNumber(left) ?: return null
        val r = toNumber(right) ?: return null
        val result = l % r
        return if (result == result.toInt().toDouble()) result.toInt() else result
    }

    private fun negate(value: Any?): Any? {
        val n = toNumber(value) ?: return null
        val result = -n
        return if (result == result.toInt().toDouble()) result.toInt() else result
    }

    private fun looseEquals(left: Any?, right: Any?): Boolean {
        if (left == right) return true
        if (left == null || right == null) return left == null && right == null
        if (left is Number && right is Number) return left.toDouble() == right.toDouble()
        if (left is String && right is Number) return left.toDoubleOrNull() == right.toDouble()
        if (left is Number && right is String) return left.toDouble() == right.toDoubleOrNull()
        return left == right
    }

    private fun strictEquals(left: Any?, right: Any?): Boolean {
        if (left == null && right == null) return true
        if (left == null || right == null) return false
        if (left::class != right::class) return false
        return left == right
    }

    private fun compare(left: Any?, right: Any?): Int {
        val l = toNumber(left) ?: return 0
        val r = toNumber(right) ?: return 0
        return l.compareTo(r)
    }

    private fun typeOf(value: Any?): String = when (value) {
        null -> "object"
        is Boolean -> "boolean"
        is Number -> "number"
        is String -> "string"
        is AstFunction -> "function"
        else -> "object"
    }

    private fun bitwiseAnd(left: Any?, right: Any?): Int = (toInt(left) ?: 0) and (toInt(right) ?: 0)
    private fun bitwiseOr(left: Any?, right: Any?): Int = (toInt(left) ?: 0) or (toInt(right) ?: 0)
    private fun bitwiseXor(left: Any?, right: Any?): Int = (toInt(left) ?: 0) xor (toInt(right) ?: 0)
    private fun bitwiseNot(value: Any?): Int = (toInt(value) ?: 0).inv()
    private fun leftShift(left: Any?, right: Any?): Int = (toInt(left) ?: 0) shl (toInt(right) ?: 0)
    private fun rightShift(left: Any?, right: Any?): Int = (toInt(left) ?: 0) shr (toInt(right) ?: 0)
    private fun unsignedRightShift(left: Any?, right: Any?): Int = (toInt(left) ?: 0) ushr (toInt(right) ?: 0)
}


data class AstFunction(
    val parameters: List<String>,
    val body: AstNode
)

