package ronsijm.templater.script

import ronsijm.templater.common.ScriptEngineDefaults
import ronsijm.templater.script.executors.ConditionEvaluator
import ronsijm.templater.script.executors.ConditionalExecutor
import ronsijm.templater.script.executors.FunctionExecutor
import ronsijm.templater.script.executors.LoopExecutor
import ronsijm.templater.script.executors.StatementClassifier
import ronsijm.templater.script.executors.TryCatchExecutor
import ronsijm.templater.script.parser.IfStatementParser
import ronsijm.templater.script.parser.TryCatchParser
import ronsijm.templater.script.profiling.ProfilingContext
import ronsijm.templater.utils.CancellationChecker
import ronsijm.templater.utils.Logging
import ronsijm.templater.utils.NoCancellationChecker

class ScriptExecutor(
    private val scriptContext: ScriptContext,
    private val evaluator: ScriptEvaluator,
    private val parser: ScriptParser,
    private val cancellationChecker: CancellationChecker = NoCancellationChecker,
    private val executionCallback: ScriptExecutionCallback = NoOpExecutionCallback
) {

    private val conditionEvaluator = ConditionEvaluator { expr -> evaluator.evaluateExpression(expr) }

    private val loopExecutor = LoopExecutor(
        scriptContext = scriptContext,
        cancellationChecker = cancellationChecker,
        clearCacheCallback = { evaluator.clearVariableCache() },
        onIterationCompleteCallback = { loopType, iterationNumber ->
            executionCallback.onLoopIteration(loopType, iterationNumber, scriptContext.getAllVariables())
        }
    )

    private val assignmentExecutor = ronsijm.templater.script.executors.AssignmentExecutor(
        scriptContext = scriptContext,
        evaluateExpression = { expr -> evaluator.evaluateExpression(expr) },
        profiler = profiler,
        profilingEnabled = { profilingEnabled }
    )


    private val statementClassifier = StatementClassifier(parser)

    private val conditionalExecutor = ConditionalExecutor(
        conditionEvaluator = conditionEvaluator,
        statementExecutor = { stmt -> executeStatement(stmt) }
    )

    private val tryCatchExecutor = TryCatchExecutor(
        scriptContext = scriptContext,
        statementExecutor = { stmt -> executeStatement(stmt) }
    )

    private val functionExecutor = FunctionExecutor(
        scriptContext = scriptContext,
        bodyExecutor = { body -> executeBody(body) }
    )

    companion object {
        private val LOG = Logging.getLogger<ScriptExecutor>()


        var profilingEnabled: Boolean
            get() = ProfilingContext.isEnabled
            set(value) { ProfilingContext.isEnabled = value }


        val profiler get() = ProfilingContext.executorProfiler

        fun resetProfiling() {
            ProfilingContext.reset()
        }

        fun getProfilingReport(): String {
            return ProfilingContext.getReport()
        }
    }

    fun executeForLoop(loopHeader: String, loopBody: List<String>) {

        if (parser.isForOfLoop(loopHeader)) {
            executeForOfLoop(loopHeader, loopBody)
            return
        }


        val loopInfo = parser.parseForLoopHeader(loopHeader) ?: return


        val startValue = evaluator.evaluateExpression(loopInfo.startExpr)?.let {
            when (it) {
                is Int -> it
                is Double -> it.toInt()
                is Long -> it.toInt()
                is Number -> it.toInt()
                else -> it.toString().toIntOrNull()
            }
        } ?: return

        val endValue = evaluator.evaluateExpression(loopInfo.endExpr)?.let {
            when (it) {
                is Int -> it
                is Double -> it.toInt()
                is Long -> it.toInt()
                is Number -> it.toInt()
                else -> it.toString().toIntOrNull()
            }
        } ?: return


        val classifiedBody = loopBody.map { classifyStatement(it) }

        executeForLoop(loopInfo, startValue, endValue, classifiedBody)

        scriptContext.removeVariable(loopInfo.varName)
    }

    private fun executeForLoop(
        loopInfo: ronsijm.templater.script.parser.ForLoopParser.ForLoopInfo,
        startValue: Int,
        endValue: Int,
        classifiedBody: List<ClassifiedStatement>
    ) {
        loopExecutor.executeCountedLoop(
            startValue = startValue,
            endValue = endValue,
            isIncrement = loopInfo.isIncrement,
            checkCondition = { current, end -> parser.checkCondition(current, loopInfo.conditionOperator, end) },
            varName = loopInfo.varName,
            executeBody = {
                for (classified in classifiedBody) {
                    executeClassifiedStatement(classified)
                    if (scriptContext.isReturnRequested()) return@executeCountedLoop
                }
            }
        )
    }

    private fun executeForOfLoop(loopHeader: String, loopBody: List<String>) {
        val loopInfo = parser.parseForOfLoopHeader(loopHeader) ?: return

        val arrayValue = evaluator.evaluateExpression(loopInfo.arrayExpression)

        val items: List<Any?> = when (arrayValue) {
            is List<*> -> arrayValue
            is String -> arrayValue.toList().map { it.toString() }
            else -> return
        }

        val classifiedBody = loopBody.map { classifyStatement(it) }

        loopExecutor.executeForOfLoop(
            items = items,
            varName = loopInfo.varName,
            executeBody = {
                for (classified in classifiedBody) {
                    executeClassifiedStatement(classified)
                    if (scriptContext.isReturnRequested()) return@executeForOfLoop
                }
            }
        )

        scriptContext.removeVariable(loopInfo.varName)
    }

    private fun classifyStatement(statement: String): ClassifiedStatement {
        return statementClassifier.classify(statement)
    }

    private fun executeClassifiedStatement(classified: ClassifiedStatement) {
        if (scriptContext.isReturnRequested()) return

        when (classified) {
            is ClassifiedStatement.Empty -> {  }
            is ClassifiedStatement.Brace -> {  }
            is ClassifiedStatement.ReturnVoid -> scriptContext.requestReturn()
            is ClassifiedStatement.ReturnValue -> {
                val value = ProfilingContext.profiled(profiler::evalExprTime, profiler::evalExprCount) {
                    evaluator.evaluateExpression(classified.valueExpr)
                }
                scriptContext.setReturnValue(value)
                scriptContext.requestReturn()
            }
            is ClassifiedStatement.ForLoop -> ProfilingContext.profiled(profiler::forLoopTime, profiler::forLoopCount) {
                executeForLoop(classified.header, classified.body)
            }
            is ClassifiedStatement.WhileLoop -> executeWhileLoop(classified.condition, classified.body)
            is ClassifiedStatement.IfStatement -> ProfilingContext.profiled(profiler::ifStatementTime, profiler::ifStatementCount) {
                executeIfStatement(classified.header, classified.body, classified.elseBranches)
            }
            is ClassifiedStatement.FunctionDecl -> executeFunctionDeclaration(classified.original)
            is ClassifiedStatement.VarDecl -> ProfilingContext.profiled(profiler::varDeclTime, profiler::varDeclCount) {
                assignmentExecutor.executeVariableDeclaration(classified.original)
            }
            is ClassifiedStatement.TrAccumulator -> assignmentExecutor.executeResultAccumulator(classified.original)
            is ClassifiedStatement.Increment -> ProfilingContext.profiled(profiler::incrementTime, profiler::incrementCount) {
                assignmentExecutor.executeIncrement(classified.varName)
            }
            is ClassifiedStatement.Decrement -> assignmentExecutor.executeDecrement(classified.varName)
            is ClassifiedStatement.CompoundAssign -> ProfilingContext.profiled(profiler::compoundAssignTime, profiler::compoundAssignCount) {
                assignmentExecutor.executeCompoundAssignment(classified.original)
            }
            is ClassifiedStatement.ArrayAssign -> ProfilingContext.profiled(profiler::arrayAssignTime, profiler::arrayAssignCount) {
                val index = evaluator.evaluateExpression(classified.indexExpr)?.let {
                    when (it) {
                        is Int -> it
                        is Double -> it.toInt()
                        is Number -> it.toInt()
                        else -> it.toString().toIntOrNull()
                    }
                }
                if (index != null) {
                    val value = evaluator.evaluateExpression(classified.valueExpr)
                    val array = scriptContext.getVariable(classified.arrayName)
                    if (array is MutableList<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (array as MutableList<Any?>)[index] = value
                    }
                }
            }
            is ClassifiedStatement.SimpleAssign -> ProfilingContext.profiled(profiler::simpleAssignTime, profiler::simpleAssignCount) {
                val value = evaluator.evaluateExpression(classified.valueExpr)
                scriptContext.setVariable(classified.varName, value)
            }
            is ClassifiedStatement.Assignment -> assignmentExecutor.executeAssignment(classified.original)
            is ClassifiedStatement.FunctionCall -> ProfilingContext.profiled(profiler::evalExprTime, profiler::evalExprCount) {
                evaluator.evaluateExpression(classified.original)
            }
            is ClassifiedStatement.Other -> executeStatement(classified.original)
        }
    }

    fun executeWhileLoop(condition: String, loopBody: List<String>) {
        LOG?.debug("executeWhileLoop: condition=$condition, body=$loopBody")

        val maxIterations = ScriptEngineDefaults.MAX_WHILE_ITERATIONS
        val classifiedBody = loopBody.map { classifyStatement(it) }
        var iterations = 0

        loopExecutor.executeWhileLoop(
            maxIterations = maxIterations,
            checkCondition = {
                val conditionValue = evaluator.evaluateExpression(condition)
                val isTruthy = when (conditionValue) {
                    null -> false
                    is Boolean -> conditionValue
                    is Number -> conditionValue.toDouble() != 0.0 && !conditionValue.toDouble().isNaN()
                    is String -> conditionValue.isNotEmpty()
                    else -> true
                }
                isTruthy
            },
            executeBody = {
                for (classified in classifiedBody) {
                    executeClassifiedStatement(classified)
                    if (scriptContext.isReturnRequested()) return@executeWhileLoop
                }
                iterations++
            }
        )

        if (iterations >= maxIterations) {
            LOG?.warn("While loop exceeded maximum iterations ($maxIterations)")
        }
    }

    fun executeIfStatement(
        ifHeader: String,
        ifBody: List<String>,
        elseBranches: List<Pair<String?, List<String>>>
    ) {
        conditionalExecutor.execute(ifHeader, ifBody, elseBranches)
    }

    @Suppress("UnusedPrivateMember")
    private fun evaluateCondition(condition: String): Boolean {
        return conditionEvaluator.evaluate(condition)
    }

    fun executeStatement(statement: String) {
        val stmt = statement.trim()
        LOG?.debug("executeStatement: $stmt")

        if (stmt.isEmpty()) return

        if (scriptContext.isReturnRequested()) return

        if (stmt == "{" || stmt == "}") return

        if (stmt == "return" || stmt == "return;") {
            scriptContext.requestReturn()
            return
        }

        if (stmt.startsWith("for ") || stmt.startsWith("for(")) {
            val loopInfo = parser.extractForLoop(listOf(stmt), 0)
            if (loopInfo != null) {
                executeForLoop(loopInfo.first, loopInfo.second)
                return
            }
        }

        if (stmt.startsWith("while ") || stmt.startsWith("while(")) {
            val loopInfo = parser.extractWhileLoop(listOf(stmt), 0)
            if (loopInfo != null) {
                executeWhileLoop(loopInfo.first, loopInfo.second)
                return
            }
        }

        if (stmt.startsWith("if ") || stmt.startsWith("if(")) {
            val ifInfo = IfStatementParser.extractIfStatementFromSingleStatement(stmt, 0, parser)
            if (ifInfo != null) {
                executeIfStatement(ifInfo.first, ifInfo.second, ifInfo.third)
                return
            }
        }

        if (stmt.startsWith("function ")) {
            executeFunctionDeclaration(stmt)
            return
        }

        if (stmt.startsWith("return ")) {
            val valueExpr = stmt.removePrefix("return ").removeSuffix(";").trim()
            val value = evaluator.evaluateExpression(valueExpr)
            val returnValue = if (value is StringBuilder) value.toString() else value
            scriptContext.setReturnValue(returnValue)
            scriptContext.requestReturn()
            return
        }

        if (stmt.startsWith("let ") || stmt.startsWith("const ") || stmt.startsWith("var ")) {
            assignmentExecutor.executeVariableDeclaration(stmt)
            return
        }

        if (stmt.startsWith("tR")) {
            assignmentExecutor.executeResultAccumulator(stmt)
            return
        }

        if (stmt.endsWith("++")) {
            val varName = stmt.dropLast(2).trim()
            assignmentExecutor.executeIncrement(varName)
            return
        }
        if (stmt.endsWith("--")) {
            val varName = stmt.dropLast(2).trim()
            assignmentExecutor.executeDecrement(varName)
            return
        }

        if (stmt.contains("+=") || stmt.contains("-=") || stmt.contains("*=") || stmt.contains("/=")) {
            assignmentExecutor.executeCompoundAssignment(stmt)
            return
        }

        if (stmt.contains("=") && !stmt.contains("==") && !stmt.contains("=>")) {
            assignmentExecutor.executeAssignment(stmt)
            return
        }

        if (stmt.contains("(")) {
            evaluator.evaluateExpression(stmt)
            return
        }
    }

    fun executeTryCatch(tryCatchInfo: TryCatchParser.TryCatchInfo) {
        tryCatchExecutor.execute(tryCatchInfo)
    }

    private fun executeFunctionDeclaration(statement: String) {
        functionExecutor.executeFunctionDeclaration(statement)
    }

    private fun executeBody(body: String) {
        val statements = parser.splitStatements(body)
        var i = 0
        while (i < statements.size) {
            if (scriptContext.isReturnRequested()) break

            val stmt = statements[i]

            if (stmt.startsWith("for ") || stmt.startsWith("for(")) {
                val loopInfo = parser.extractForLoop(statements, i)
                if (loopInfo != null) {
                    executeForLoop(loopInfo.first, loopInfo.second)
                    i = loopInfo.third
                    continue
                }
            }

            if (stmt.startsWith("while ") || stmt.startsWith("while(")) {
                val whileInfo = parser.extractWhileLoop(statements, i)
                if (whileInfo != null) {
                    executeWhileLoop(whileInfo.first, whileInfo.second)
                    i = whileInfo.third
                    continue
                }
            }

            if (stmt.startsWith("if ") || stmt.startsWith("if(")) {
                val ifInfo = parser.extractIfStatement(statements, i)
                if (ifInfo != null) {
                    executeIfStatement(ifInfo.first, ifInfo.second, ifInfo.third)
                    i = ifInfo.fourth
                    continue
                }
            }

            executeStatement(stmt)
            i++
        }
    }


    fun executeNode(node: ronsijm.templater.ast.StatementNode) {
        when (node.type) {
            ronsijm.templater.ast.StatementType.FOR_LOOP -> {

                val loopHeader = node.code.removePrefix("for (").removeSuffix(")")
                executeForLoopWithNodes(loopHeader, node.children)
            }
            ronsijm.templater.ast.StatementType.IF_STATEMENT -> {

                val condition = node.code.removePrefix("if (").removeSuffix(")")
                executeIfStatementWithNodes(condition, node.children, node.elseBranches)
            }
            ronsijm.templater.ast.StatementType.WHILE_LOOP -> {

                val condition = node.code.removePrefix("while (").removeSuffix(")")
                executeWhileLoopWithNodes(condition, node.children)
            }
            else -> {

                executeStatement(node.code)
            }
        }
    }


    private fun executeBodyNodeWithCallbacks(node: ronsijm.templater.ast.StatementNode): Boolean {

        val action = executionCallback.beforeStatement(
            node = node,
            variables = scriptContext.getAllVariables()
        )

        if (action == ExecutionAction.STOP) {
            return false
        }


        executeNode(node)


        executionCallback.afterStatement(
            node = node,
            variables = scriptContext.getAllVariables()
        )

        return true
    }


    private fun executeForLoopWithNodes(loopHeader: String, bodyNodes: List<ronsijm.templater.ast.StatementNode>) {

        if (parser.isForOfLoop(loopHeader)) {
            executeForOfLoopWithNodes(loopHeader, bodyNodes)
            return
        }


        val loopInfo = parser.parseForLoopHeader(loopHeader) ?: return


        val startValue = evaluator.evaluateExpression(loopInfo.startExpr)?.let {
            when (it) {
                is Int -> it
                is Double -> it.toInt()
                is Long -> it.toInt()
                is Number -> it.toInt()
                else -> it.toString().toIntOrNull()
            }
        } ?: return

        val endValue = evaluator.evaluateExpression(loopInfo.endExpr)?.let {
            when (it) {
                is Int -> it
                is Double -> it.toInt()
                is Long -> it.toInt()
                is Number -> it.toInt()
                else -> it.toString().toIntOrNull()
            }
        } ?: return


        executionCallback.enterBlock("for", loopHeader)


        var shouldStop = false
        loopExecutor.executeCountedLoop(
            startValue = startValue,
            endValue = endValue,
            isIncrement = loopInfo.isIncrement,
            checkCondition = { current, end -> parser.checkCondition(current, loopInfo.conditionOperator, end) },
            varName = loopInfo.varName,
            executeBody = {

                for (bodyNode in bodyNodes) {
                    if (!executeBodyNodeWithCallbacks(bodyNode)) {
                        shouldStop = true
                        return@executeCountedLoop
                    }
                    if (scriptContext.isReturnRequested()) return@executeCountedLoop
                }
            }
        )
        if (shouldStop) return


        executionCallback.exitBlock("for")

        scriptContext.removeVariable(loopInfo.varName)
    }


    private fun executeForOfLoopWithNodes(loopHeader: String, bodyNodes: List<ronsijm.templater.ast.StatementNode>) {
        val loopInfo = parser.parseForOfLoopHeader(loopHeader) ?: return


        val arrayValue = evaluator.evaluateExpression(loopInfo.arrayExpression)


        val items: List<Any?> = when (arrayValue) {
            is List<*> -> arrayValue
            is String -> arrayValue.toList().map { it.toString() }
            else -> return
        }


        executionCallback.enterBlock("for-of", loopHeader)


        var shouldStop = false
        loopExecutor.executeForOfLoop(
            items = items,
            varName = loopInfo.varName,
            executeBody = {

                for (bodyNode in bodyNodes) {
                    if (!executeBodyNodeWithCallbacks(bodyNode)) {
                        shouldStop = true
                        return@executeForOfLoop
                    }
                    if (scriptContext.isReturnRequested()) return@executeForOfLoop
                }
            }
        )
        if (shouldStop) return


        executionCallback.exitBlock("for-of")

        scriptContext.removeVariable(loopInfo.varName)
    }


    private fun executeIfStatementWithNodes(
        condition: String,
        thenNodes: List<ronsijm.templater.ast.StatementNode>,
        elseBranches: List<Pair<String?, List<ronsijm.templater.ast.StatementNode>>> = emptyList()
    ) {
        val conditionResult = conditionEvaluator.evaluate(condition)

        if (conditionResult) {

            if (thenNodes.isNotEmpty()) {
                executionCallback.enterBlock("if", condition)
            }


            for (thenNode in thenNodes) {
                if (!executeBodyNodeWithCallbacks(thenNode)) return
            }


            if (thenNodes.isNotEmpty()) {
                executionCallback.exitBlock("if")
            }
        } else {

            for ((elseCond, elseNodes) in elseBranches) {
                if (elseCond == null) {

                    if (elseNodes.isNotEmpty()) {
                        executionCallback.enterBlock("else", "")
                    }

                    for (elseNode in elseNodes) {
                        if (!executeBodyNodeWithCallbacks(elseNode)) return
                    }

                    if (elseNodes.isNotEmpty()) {
                        executionCallback.exitBlock("else")
                    }
                    break
                } else {

                    val elseIfResult = conditionEvaluator.evaluate(elseCond)
                    if (elseIfResult) {
                        if (elseNodes.isNotEmpty()) {
                            executionCallback.enterBlock("else if", elseCond)
                        }

                        for (elseNode in elseNodes) {
                            if (!executeBodyNodeWithCallbacks(elseNode)) return
                        }

                        if (elseNodes.isNotEmpty()) {
                            executionCallback.exitBlock("else if")
                        }
                        break
                    }
                }
            }
        }
    }


    private fun executeWhileLoopWithNodes(condition: String, bodyNodes: List<ronsijm.templater.ast.StatementNode>) {

        executionCallback.enterBlock("while", condition)


        var shouldStop = false
        loopExecutor.executeWhileLoop(
            maxIterations = ScriptEngineDefaults.MAX_WHILE_ITERATIONS,
            checkCondition = { conditionEvaluator.evaluate(condition) },
            executeBody = {

                for (bodyNode in bodyNodes) {
                    if (!executeBodyNodeWithCallbacks(bodyNode)) {
                        shouldStop = true
                        return@executeWhileLoop
                    }
                    if (scriptContext.isReturnRequested()) return@executeWhileLoop
                }
            }
        )


        executionCallback.exitBlock("while")
    }

}
