package ronsijm.templater.script

import ronsijm.templater.common.BuiltinObjects
import ronsijm.templater.common.Prefixes
import ronsijm.templater.script.builtins.MathObject
import ronsijm.templater.script.compiler.BytecodeVM
import ronsijm.templater.script.compiler.ExpressionCache
import ronsijm.templater.script.evaluators.ArithmeticEvaluator
import ronsijm.templater.script.evaluators.ArrowFunctionHandler
import ronsijm.templater.script.evaluators.BytecodeEvaluationHelper
import ronsijm.templater.script.evaluators.FunctionCallExecutor
import ronsijm.templater.script.evaluators.LiteralParser
import ronsijm.templater.script.evaluators.LogicalExpressionEvaluator
import ronsijm.templater.script.evaluators.NewExpressionEvaluator
import ronsijm.templater.script.evaluators.PropertyAccessEvaluator
import ronsijm.templater.script.evaluators.TemplateLiteralEvaluator
import ronsijm.templater.script.evaluators.TernaryOperatorEvaluator
import ronsijm.templater.script.evaluators.TypeofOperatorEvaluator
import ronsijm.templater.script.methods.ArrayMethodExecutor
import ronsijm.templater.script.methods.StringMethodExecutor
import ronsijm.templater.script.profiling.ProfilingContext
import ronsijm.templater.utils.Logging

class ScriptEvaluator(
    private val scriptContext: ScriptContext,
    private val moduleRegistry: ModuleRegistry
) {

    companion object {
        private val LOG = Logging.getLogger<ScriptEvaluator>()

        private val SIMPLE_VAR_REGEX = Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")
        private val ARRAY_ACCESS_REGEX = Regex("^[a-zA-Z_][a-zA-Z0-9_]*\\[.+\\].*$")
        private val PROPERTY_ACCESS_REGEX = Regex("^[a-zA-Z_][a-zA-Z0-9_]*\\.[a-zA-Z_][a-zA-Z0-9_]*$")


        val profiler get() = ProfilingContext.evaluatorProfiler

        fun resetProfiling() {
            ProfilingContext.reset()
        }

        fun getProfilingReport(): String {
            return ProfilingContext.evaluatorProfiler.getReport(
                ProfilingContext.bytecodeVMProfiler,
                ProfilingContext.arithmeticProfiler
            )
        }
    }

    private var statementExecutor: ((String) -> Unit)? = null
    private val expressionCache = ExpressionCache()
    private val bytecodeVM: BytecodeVM by lazy {
        BytecodeVM(
            scriptContext,
            functionCaller = { name, args -> bytecodeCallFunction(name, args) },
            methodCaller = { obj, method, args -> bytecodeCallMethod(obj, method, args) },
            propertyGetter = { obj, prop -> bytecodeGetProperty(obj, prop) },
            indexGetter = { obj, index -> bytecodeGetIndex(obj, index) }
        )
    }

    private fun bytecodeCallFunction(name: String, args: List<Any?>): Any? {
        if (name.startsWith(Prefixes.MATH)) {
            val methodName = name.substring(Prefixes.MATH.length)
            return MathObject.execute(methodName, args)
        }
        return functionCallExecutor.executeFunctionCall(name, args)
    }

    private fun bytecodeCallMethod(obj: Any?, method: String, args: List<Any?>): Any? {
        return when (obj) {
            is List<*> -> ArrayMethodExecutor.execute(obj, method, args) { fn, fnArgs ->
                executeArrowFunction(fn as ArrowFunction, fnArgs)
            }
            is String, is StringBuilder -> StringMethodExecutor.execute(obj!!, method, args)
            else -> null
        }
    }

    private fun bytecodeGetProperty(obj: Any?, prop: String): Any? {
        return when (obj) {
            is List<*> -> if (prop == "length") obj.size else null
            is String -> if (prop == "length") obj.length else null
            is Map<*, *> -> obj[prop]
            else -> null
        }
    }

    private fun bytecodeGetIndex(obj: Any?, index: Any?): Any? {
        return when (obj) {
            is List<*> -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx] else null
            }
            is String -> {
                val idx = (index as? Number)?.toInt() ?: return null
                if (idx in obj.indices) obj[idx].toString() else null
            }
            is Map<*, *> -> obj[index?.toString()]
            else -> null
        }
    }

    private val literalParser: LiteralParser by lazy {
        LiteralParser { evaluateExpression(it) }
    }

    private val arithmeticEvaluator: ArithmeticEvaluator by lazy {
        ArithmeticEvaluator({ evaluateExpression(it) }, literalParser)
    }

    private val arrowFunctionHandler: ArrowFunctionHandler by lazy {
        ArrowFunctionHandler(scriptContext, { evaluateExpression(it) }) { statementExecutor }
    }

    private val templateLiteralEvaluator: TemplateLiteralEvaluator by lazy {
        TemplateLiteralEvaluator { evaluateExpression(it) }
    }

    private val functionCallExecutor: FunctionCallExecutor by lazy {
        FunctionCallExecutor(
            scriptContext,
            moduleRegistry,
            { evaluateExpression(it) },
            { fn, args -> executeArrowFunction(fn, args) }
        )
    }

    private val logicalExpressionEvaluator: LogicalExpressionEvaluator by lazy {
        LogicalExpressionEvaluator { evaluateExpression(it) }
    }

    private val propertyAccessEvaluator: PropertyAccessEvaluator by lazy {
        PropertyAccessEvaluator(
            scriptContext,
            { evaluateExpression(it) },
            { functionCallExecutor.parseArguments(it) },
            { fn, args -> executeArrowFunction(fn, args) }
        )
    }

    private val ternaryOperatorEvaluator: TernaryOperatorEvaluator by lazy {
        TernaryOperatorEvaluator(
            { evaluateExpression(it) },
            { logicalExpressionEvaluator.isTruthy(it) }
        )
    }

    private val typeofOperatorEvaluator: TypeofOperatorEvaluator by lazy {
        TypeofOperatorEvaluator(scriptContext, { evaluateExpression(it) })
    }

    private val bytecodeEvaluationHelper: BytecodeEvaluationHelper by lazy {
        BytecodeEvaluationHelper(expressionCache, bytecodeVM)
    }

    private val newExpressionEvaluator: NewExpressionEvaluator by lazy {
        NewExpressionEvaluator(propertyAccessEvaluator) { functionCallExecutor.parseArguments(it) }
    }

    fun setStatementExecutor(executor: (String) -> Unit) {
        this.statementExecutor = executor
    }

    fun invalidateVariableCache(varName: String) {
        bytecodeVM.invalidateVariable(varName)
    }

    fun clearVariableCache() {
        bytecodeVM.clearCache()
    }

    fun evaluateExpression(expression: String): Any? {
        val expr = expression.trim()
        if (expr.isEmpty()) return null

        val cleanExpr = if (expr.startsWith(Prefixes.AWAIT)) expr.substring(Prefixes.AWAIT.length).trim() else expr
        val firstChar = cleanExpr[0]

        if (firstChar.isDigit() || (firstChar == '-' && cleanExpr.length > 1 && cleanExpr[1].isDigit())) {
            val isSimpleNumber = cleanExpr.all { it.isDigit() || it == '.' || it == '-' }
            if (isSimpleNumber) {
                cleanExpr.toIntOrNull()?.let { return it }
                cleanExpr.toDoubleOrNull()?.let { return it }
            }
        }

        if (firstChar.isLetter() || firstChar == '_') {
            val isSimpleVar = cleanExpr.all { it.isLetterOrDigit() || it == '_' }
            if (isSimpleVar) {
                when (cleanExpr) {
                    "true" -> return true
                    "false" -> return false
                    "null" -> return null
                    "tR" -> return scriptContext.getResultAccumulator()
                }
                if (scriptContext.hasVariable(cleanExpr)) {
                    return scriptContext.getVariable(cleanExpr)
                }
            }
        }

        val bytecodeResult = bytecodeEvaluationHelper.tryBytecodeEvaluation(cleanExpr)
        if (bytecodeResult != null) {
            if (ProfilingContext.isEnabled) {
                profiler.bytecodeEvalCount++
            }
            return bytecodeResult.value
        }

        if (firstChar == '!' && cleanExpr.length > 1 && cleanExpr[1] != '=') {
            val innerExpr = cleanExpr.substring(1).trim()
            val innerValue = ProfilingContext.profiled(profiler::recursiveEvalTime, profiler::recursiveEvalCount) {
                evaluateExpression(innerExpr)
            }
            return !logicalExpressionEvaluator.isTruthy(innerValue)
        }

        typeofOperatorEvaluator.evaluateTypeofOperator(cleanExpr)?.let { return it }

        if (arrowFunctionHandler.isTopLevelArrowFunction(cleanExpr)) {
            return arrowFunctionHandler.parseArrowFunction(cleanExpr)
        }

        ternaryOperatorEvaluator.evaluateTernaryOperator(cleanExpr)?.let { return it.value }

        logicalExpressionEvaluator.findTopLevelLogicalOperator(cleanExpr)?.let { opIndex ->
            return logicalExpressionEvaluator.evaluateLogicalExpression(cleanExpr, opIndex)
        }

        val comparisonOp = ProfilingContext.profiled(profiler::findComparisonOpTime, profiler::findComparisonOpCount) {
            arithmeticEvaluator.findComparisonOperator(cleanExpr)
        }
        if (comparisonOp != null) {
            return ProfilingContext.profiled(profiler::evaluateComparisonTime, profiler::evaluateComparisonCount) {
                arithmeticEvaluator.evaluateComparison(cleanExpr, comparisonOp)
            }
        }

        val arithmeticOp = ProfilingContext.profiled(profiler::findArithmeticOpTime, profiler::findArithmeticOpCount) {
            arithmeticEvaluator.findArithmeticOperator(cleanExpr)
        }
        if (arithmeticOp != null) {
            return ProfilingContext.profiled(profiler::evaluateArithmeticTime, profiler::evaluateArithmeticCount) {
                arithmeticEvaluator.evaluateArithmetic(cleanExpr, arithmeticOp)
            }
        }

        if (LiteralParser.isTemplateLiteral(cleanExpr)) {
            return evaluateTemplateLiteral(cleanExpr.substring(1, cleanExpr.length - 1))
        }

        if (LiteralParser.isDoubleQuotedString(cleanExpr) || LiteralParser.isSingleQuotedString(cleanExpr)) {
            return literalParser.parseStringLiteral(cleanExpr)
        }

        if (LiteralParser.isArrayLiteral(cleanExpr)) {
            return ProfilingContext.profiled(profiler::literalParseTime, profiler::literalParseCount) {
                literalParser.parseArrayLiteral(cleanExpr)
            }
        }

        if (LiteralParser.isObjectLiteral(cleanExpr)) {
            return ProfilingContext.profiled(profiler::literalParseTime, profiler::literalParseCount) {
                literalParser.parseObjectLiteral(cleanExpr)
            }
        }

        if (cleanExpr.matches(SIMPLE_VAR_REGEX)) {
            return ProfilingContext.profiled(profiler::variableLookupTime, profiler::variableLookupCount) {
                scriptContext.getVariable(cleanExpr)
            }
        }

        if (cleanExpr.matches(ARRAY_ACCESS_REGEX)) {
            return ProfilingContext.profiled(profiler::arrayAccessTime, profiler::arrayAccessCount) {
                propertyAccessEvaluator.evaluateArrayAccessWithChain(cleanExpr)
            }
        }

        if (cleanExpr.matches(PROPERTY_ACCESS_REGEX) && !cleanExpr.startsWith(Prefixes.TP)) {
            if (cleanExpr.startsWith(Prefixes.MATH)) {
                MathObject.getConstant(cleanExpr.substringAfter(Prefixes.MATH))?.let { return it }
            }
            return propertyAccessEvaluator.evaluateVariableProperty(cleanExpr)
        }


        if (newExpressionEvaluator.canEvaluate(cleanExpr)) {
            return newExpressionEvaluator.tryEvaluate(cleanExpr)
        }

        if (cleanExpr.startsWith("(")) {
            val closeIndex = propertyAccessEvaluator.findMatchingParen(cleanExpr, 0)
            if (closeIndex > 0) {
                val innerResult = evaluateExpression(cleanExpr.substring(1, closeIndex).trim())

                if (closeIndex < cleanExpr.length - 1) {
                    val remaining = cleanExpr.substring(closeIndex + 1).trim()
                    if (remaining.isNotEmpty()) {
                        if (remaining.startsWith(".") || remaining.startsWith("[")) {
                            return propertyAccessEvaluator.evaluateChainedMethodCall(innerResult, remaining)
                        }
                        return evaluateExpression("$innerResult$remaining")
                    }
                }
                return innerResult
            }
        }

        if (cleanExpr.contains("(")) {
            return evaluateFunctionCall(cleanExpr)
        }

        if (cleanExpr.startsWith(Prefixes.TP) && cleanExpr.contains(".")) {
            return evaluatePropertyAccess(cleanExpr)
        }


        return ProfilingContext.profiled(profiler::literalParseTime, profiler::literalParseCount) {
            cleanExpr.toIntOrNull() ?: cleanExpr.toDoubleOrNull()
        } ?: cleanExpr
    }

    private fun evaluateTemplateLiteral(template: String): String {
        val result = StringBuilder()
        var i = 0

        while (i < template.length) {
            if (i < template.length - 1 && template[i] == '$' && template[i + 1] == '{') {
                var braceDepth = 1
                var j = i + 2
                while (j < template.length && braceDepth > 0) {
                    when (template[j]) {
                        '{' -> braceDepth++
                        '}' -> braceDepth--
                    }
                    j++
                }

                val expression = template.substring(i + 2, j - 1)
                val value = evaluateExpression(expression)
                result.append(value?.toString() ?: "")

                i = j
            } else if (template[i] == '\\' && i < template.length - 1 && template[i + 1] == 'n') {
                result.append('\n')
                i += 2
            } else if (template[i] == '\\' && i < template.length - 1 && template[i + 1] == 't') {
                result.append('\t')
                i += 2
            } else {
                result.append(template[i])
                i++
            }
        }

        return result.toString()
    }

    private fun evaluatePropertyAccess(expression: String): Any? {
        val expr = expression.trim()
        return functionCallExecutor.executeFunctionCall(expr, emptyList())
    }

    private fun evaluateFunctionCall(expression: String): Any? {
        val expr = expression.trim()

        val cleanExpr = if (expr.startsWith(Prefixes.AWAIT)) {
            expr.substring(Prefixes.AWAIT.length).trim()
        } else {
            expr
        }

        val firstParenIndex = cleanExpr.indexOf('(')
        if (firstParenIndex == -1) return null

        val functionPath = cleanExpr.substring(0, firstParenIndex)

        val (argsString, endIndex) = propertyAccessEvaluator.extractMatchingParenContentWithIndex(cleanExpr, firstParenIndex)
        val args = functionCallExecutor.parseArguments(argsString)
        var result = functionCallExecutor.executeFunctionCall(functionPath, args)

        if (endIndex < cleanExpr.length - 1) {
            val remaining = cleanExpr.substring(endIndex + 1).trim()
            if (remaining.startsWith(".") || remaining.startsWith("[")) {
                result = propertyAccessEvaluator.evaluateChainedMethodCall(result, remaining)
            }
        }

        return result
    }

    fun executeArrowFunction(fn: ArrowFunction, args: List<Any?>): Any? {
        return arrowFunctionHandler.executeArrowFunction(fn, args)
    }

}
