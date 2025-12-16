package ronsijm.templater.script

import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.Logging

/**
 * Simple scripting engine for template execution commands
 * Supports:
 * - Variable assignments: let x = value
 * - Function calls: tp.module.function(args)
 * - String concatenation: "text" + variable
 * - Await expressions: await tp.system.prompt()
 */
class ScriptEngine(templateContext: TemplateContext) {

    companion object {
        private val LOG = Logging.getLogger<ScriptEngine>()
    }

    // Core components for script execution
    private val scriptContext = ScriptContext(templateContext)
    private val moduleRegistry = ModuleRegistry(scriptContext)
    private val lexer = ScriptLexer()
    private val evaluator = ScriptEvaluator(scriptContext, moduleRegistry)
    private val parser = ScriptParser()
    private val executor = ScriptExecutor(scriptContext, evaluator, parser)

    /**
     * Initialize the tR variable with the current template output
     * This should be called before executing each template block
     */
    fun initializeResultAccumulator(currentOutput: String) {
        scriptContext.initializeResultAccumulator(currentOutput)
    }

    /**
     * Get the current value of tR
     */
    fun getResultAccumulator(): String {
        return scriptContext.getResultAccumulator()
    }

    /**
     * Execute a script block
     * Returns the accumulated result (tR)
     */
    fun execute(script: String): String {
        // Handle multi-line constructs like for loops
        val processedScript = lexer.preprocessScript(script)
        LOG?.debug("Preprocessed script:\n$processedScript")

        // Split into statements (by newlines and semicolons, but not inside parentheses)
        val statements = lexer.smartSplitStatements(processedScript)

        var i = 0
        while (i < statements.size) {
            val statement = statements[i]
            LOG?.debug("Processing statement $i: $statement")

            // Check if this is a for loop
            if (statement.startsWith("for ") || statement.startsWith("for(")) {
                // Find the loop body
                val loopInfo = parser.extractForLoop(statements, i)
                if (loopInfo != null) {
                    executor.executeForLoop(loopInfo.first, loopInfo.second)
                    i = loopInfo.third // Skip to after the loop
                } else {
                    executor.executeStatement(statement)
                    i++
                }
            }
            // Check if this is an if statement
            else if (statement.startsWith("if ") || statement.startsWith("if(")) {
                LOG?.debug("Detected if statement at index $i")
                // Find the if/else block
                val ifInfo = parser.extractIfStatement(statements, i)
                if (ifInfo != null) {
                    LOG?.debug("Successfully extracted if/else block, jumping to index ${ifInfo.fourth}")
                    executor.executeIfStatement(ifInfo.first, ifInfo.second, ifInfo.third)
                    i = ifInfo.fourth // Skip to after the if/else block
                } else {
                    LOG?.debug("Failed to extract if/else block")
                    executor.executeStatement(statement)
                    i++
                }
            }
            // Check if this is a try/catch block
            else if (statement.startsWith("try ") || statement.startsWith("try{") || statement == "try") {
                LOG?.debug("Detected try/catch at index $i")
                val tryCatchInfo = parser.extractTryCatch(statements, i)
                if (tryCatchInfo != null) {
                    LOG?.debug("Successfully extracted try/catch block, jumping to index ${tryCatchInfo.second}")
                    executor.executeTryCatch(tryCatchInfo.first)
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

    /**
     * Evaluate an expression and return its value
     * Delegates to ScriptEvaluator
     */
    fun evaluateExpression(expression: String): Any? {
        return evaluator.evaluateExpression(expression)
    }

    /**
     * Get a variable value
     */
    fun getVariable(name: String): Any? {
        return scriptContext.getVariable(name)
    }

}