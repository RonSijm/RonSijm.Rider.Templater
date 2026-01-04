package ronsijm.templater.script

import ronsijm.templater.ast.StatementNode
import ronsijm.templater.common.FrontmatterAccess
import ronsijm.templater.common.ModuleExecutor
import ronsijm.templater.utils.CancellationChecker
import ronsijm.templater.utils.Logging
import ronsijm.templater.utils.NoCancellationChecker

class ScriptEngine(
    frontmatterAccess: FrontmatterAccess,
    moduleExecutor: ModuleExecutor,
    private val cancellationChecker: CancellationChecker = NoCancellationChecker,
    private val executionCallback: ScriptExecutionCallback = NoOpExecutionCallback
) {

    companion object {
        private val LOG = Logging.getLogger<ScriptEngine>()
    }

    private val scriptContext = ScriptContext()
    private val moduleRegistry = ModuleRegistry(frontmatterAccess, moduleExecutor)
    private val lexer = ScriptLexer()
    private val evaluator = ScriptEvaluator(scriptContext, moduleRegistry)
    private val parser = ScriptParser()
    private val executor = ScriptExecutor(scriptContext, evaluator, parser, cancellationChecker, executionCallback)

    init {
        evaluator.setStatementExecutor { statement -> executor.executeStatement(statement) }
    }

    fun initializeResultAccumulator(currentOutput: String) {
        scriptContext.initializeResultAccumulator(currentOutput)
    }

    fun getResultAccumulator(): String {
        return scriptContext.getResultAccumulator()
    }


    fun execute(script: String): String {
        val processedScript = lexer.preprocessScript(script)
        LOG?.debug("Preprocessed script:\n$processedScript")

        val statements = lexer.smartSplitStatements(processedScript)

        var i = 0
        while (i < statements.size) {
            if (scriptContext.isReturnRequested()) {
                LOG?.debug("Return requested, stopping execution")
                break
            }

            val statement = statements[i]
            LOG?.debug("Processing statement $i: $statement")

            if (statement.startsWith("for ") || statement.startsWith("for(")) {
                val loopInfo = parser.extractForLoop(statements, i)
                if (loopInfo != null) {
                    executionCallback.enterBlock("for", loopInfo.first)
                    executor.executeForLoop(loopInfo.first, loopInfo.second)
                    executionCallback.exitBlock("for")
                    i = loopInfo.third
                } else {
                    executor.executeStatement(statement)
                    i++
                }
            }
            else if (statement.startsWith("if ") || statement.startsWith("if(")) {
                LOG?.debug("Detected if statement at index $i")
                val ifInfo = parser.extractIfStatement(statements, i)
                if (ifInfo != null) {
                    LOG?.debug("Successfully extracted if/else block, jumping to index ${ifInfo.fourth}")
                    executionCallback.enterBlock("if", ifInfo.first)
                    executor.executeIfStatement(ifInfo.first, ifInfo.second, ifInfo.third)
                    executionCallback.exitBlock("if")
                    i = ifInfo.fourth
                } else {
                    LOG?.debug("Failed to extract if/else block")
                    executor.executeStatement(statement)
                    i++
                }
            }
            else if (statement.startsWith("while ") || statement.startsWith("while(")) {
                LOG?.debug("Detected while loop at index $i")
                val whileInfo = parser.extractWhileLoop(statements, i)
                if (whileInfo != null) {
                    LOG?.debug("Successfully extracted while loop, jumping to index ${whileInfo.third}")
                    executionCallback.enterBlock("while", whileInfo.first)
                    executor.executeWhileLoop(whileInfo.first, whileInfo.second)
                    executionCallback.exitBlock("while")
                    i = whileInfo.third
                } else {
                    LOG?.debug("Failed to extract while loop")
                    executor.executeStatement(statement)
                    i++
                }
            }
            else if (statement.startsWith("try ") || statement.startsWith("try{") || statement == "try") {
                LOG?.debug("Detected try/catch at index $i")
                val tryCatchInfo = parser.extractTryCatch(statements, i)
                if (tryCatchInfo != null) {
                    LOG?.debug("Successfully extracted try/catch block, jumping to index ${tryCatchInfo.second}")
                    executionCallback.enterBlock("try", "try/catch")
                    executor.executeTryCatch(tryCatchInfo.first)
                    executionCallback.exitBlock("try")
                    i = tryCatchInfo.second
                } else {
                    LOG?.debug("Failed to extract try/catch block")
                    executor.executeStatement(statement)
                    i++
                }
            }
            else {
                executor.executeStatement(statement)
                i++
            }
        }

        return scriptContext.getResultAccumulator()
    }


    fun executeWithAST(nodes: List<StatementNode>): String {
        LOG?.debug("Executing ${nodes.size} AST nodes")

        for ((index, node) in nodes.withIndex()) {
            if (scriptContext.isReturnRequested()) {
                LOG?.debug("Return requested, stopping execution")
                break
            }

            LOG?.debug("Processing node $index: ${node.code.take(50)}")


            val action = executionCallback.beforeStatement(
                node = node,
                variables = scriptContext.getAllVariables()
            )

            if (action == ExecutionAction.STOP) {
                LOG?.debug("Execution callback requested stop")
                break
            }



            executor.executeNode(node)


            executionCallback.afterStatement(
                node = node,
                variables = scriptContext.getAllVariables()
            )
        }

        return scriptContext.getResultAccumulator()
    }

    fun evaluateExpression(expression: String): Any? {
        return evaluator.evaluateExpression(expression)
    }

    fun getVariable(name: String): Any? {
        return scriptContext.getVariable(name)
    }

    fun getAllVariables(): Map<String, Any?> {
        return scriptContext.getAllVariables()
    }

    fun isReturnRequested(): Boolean {
        return scriptContext.isReturnRequested()
    }

    fun createVariableUpdater(): VariableUpdater {
        return object : VariableUpdater {
            override fun updateVariable(name: String, value: String): Boolean {
                return try {



                    val parsedValue = when {

                        (value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'")) ||
                        (value.startsWith("`") && value.endsWith("`")) -> evaluator.evaluateExpression(value)


                        value.toIntOrNull() != null || value.toDoubleOrNull() != null -> evaluator.evaluateExpression(value)


                        value == "true" || value == "false" || value == "null" -> evaluator.evaluateExpression(value)


                        value.startsWith("[") || value.startsWith("{") -> evaluator.evaluateExpression(value)


                        value.contains("(") || value.contains("+") || value.contains("-") ||
                        value.contains("*") || value.contains("/") || value.contains(".") -> evaluator.evaluateExpression(value)



                        else -> value
                    }
                    scriptContext.setVariable(name, parsedValue)
                    true
                } catch (e: Exception) {
                    LOG?.warn("Failed to update variable $name to $value: ${e.message}")
                    false
                }
            }
        }
    }

}