package ronsijm.templater.parallel

import ronsijm.templater.handlers.CancelledResult
import ronsijm.templater.handlers.CommandResult
import ronsijm.templater.handlers.OkValueResult
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.modules.*
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateValidator
import ronsijm.templater.script.ScriptEngine
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.utils.CommandExecutionHelper
import ronsijm.templater.utils.ErrorMessages
import ronsijm.templater.utils.Logging
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*

/**
 * Template parser with parallel execution support
 * Analyzes dependencies between blocks and executes independent blocks in parallel
 */
class ParallelTemplateParser(
    private val validateSyntax: Boolean = true,
    private val services: ServiceContainer = ServiceContainer.createDefault(),
    private val enableParallel: Boolean = true
) {
    companion object {
        private val LOG = Logging.getLogger<ParallelTemplateParser>()
    }

    private val templateRegex = Regex("""<%([_-])?(\*)?(.+?)([_-])?%>""", RegexOption.DOT_MATCHES_ALL)
    private val validator = TemplateValidator()
    private val executor = ParallelExecutor()

    /**
     * Parse and execute template with parallel execution where possible
     */
    fun parse(content: String, context: TemplateContext, project: Project? = null): String {
        if (validateSyntax) {
            val validationErrors = validator.validate(content)
            if (validationErrors.isNotEmpty()) {
                LOG?.warn("Template validation failed:\n${validationErrors.joinToString("\n")}")
            }
        }

        // Extract all blocks
        val blocks = extractBlocks(content)
        
        if (blocks.isEmpty()) {
            return content
        }

        // Get execution plan for logging
        val plan = executor.getExecutionPlan(blocks)
        LOG?.info("Parallel execution plan: ${plan.phases.size} phases, " +
                "${plan.parallelizableBlocks}/${plan.totalBlocks} parallelizable " +
                "(${(plan.parallelizationRatio * 100).toInt()}%)")

        // Create shared script engine for variable state
        val moduleFactory = ModuleFactory(context, services, project)
        val enhancedContext = context.copy(
            executeDateCommand = { function, args -> 
                HandlerRegistry.executeCommand("date", function, args, context) 
            },
            executeFrontmatterCommand = { parts -> 
                moduleFactory.getFrontmatterModule().getValue(parts) 
            }
        )
        val scriptEngine = ScriptEngine(enhancedContext)

        // Execute blocks and collect results
        val results = if (enableParallel && plan.parallelizableBlocks > 0) {
            executeParallel(blocks, plan, scriptEngine, enhancedContext, moduleFactory)
        } else {
            executeSequential(blocks, scriptEngine, enhancedContext, moduleFactory)
        }

        // Apply results to content
        return applyResults(content, blocks, results)
    }

    private fun extractBlocks(content: String): List<TemplateBlock> {
        return templateRegex.findAll(content).mapIndexed { index, match ->
            TemplateBlock(
                id = index,
                matchText = match.value,
                command = match.groupValues[3].trim(),
                isExecution = match.groupValues[2] == "*",
                leftTrim = match.groupValues[1],
                rightTrim = match.groupValues[4],
                originalStart = match.range.first,
                originalEnd = match.range.last + 1
            )
        }.toList()
    }

    private fun executeSequential(
        blocks: List<TemplateBlock>,
        scriptEngine: ScriptEngine,
        context: TemplateContext,
        moduleFactory: ModuleFactory
    ): Map<Int, String> {
        val results = mutableMapOf<Int, String>()

        for (block in blocks) {
            // Check if return was requested - stop processing further blocks
            if (scriptEngine.isReturnRequested()) {
                // For remaining blocks, keep original text or empty based on type
                results[block.id] = ""
                continue
            }
            results[block.id] = executeBlock(block, scriptEngine, context, moduleFactory)
        }

        return results
    }

    private fun executeParallel(
        blocks: List<TemplateBlock>,
        plan: ExecutionPlan,
        scriptEngine: ScriptEngine,
        context: TemplateContext,
        moduleFactory: ModuleFactory
    ): Map<Int, String> {
        return runBlocking {
            val results = mutableMapOf<Int, String>()
            
            for (phase in plan.phases) {
                if (phase.canParallelize) {
                    // Execute in parallel
                    val phaseResults = coroutineScope {
                        phase.blocks.map { analysis ->
                            async(Dispatchers.Default) {
                                analysis.block.id to executeBlock(
                                    analysis.block, scriptEngine, context, moduleFactory
                                )
                            }
                        }.awaitAll()
                    }
                    results.putAll(phaseResults.toMap())
                } else {
                    // Execute sequentially
                    for (analysis in phase.blocks) {
                        results[analysis.block.id] = executeBlock(
                            analysis.block, scriptEngine, context, moduleFactory
                        )
                    }
                }
            }
            
            results
        }
    }

    private fun executeBlock(
        block: TemplateBlock,
        scriptEngine: ScriptEngine,
        context: TemplateContext,
        moduleFactory: ModuleFactory
    ): String {
        // Implementation continues in next section...
        return executeBlockImpl(block, scriptEngine, context, moduleFactory)
    }

    private fun executeBlockImpl(
        block: TemplateBlock,
        scriptEngine: ScriptEngine,
        context: TemplateContext,
        moduleFactory: ModuleFactory
    ): String {
        return try {
            if (block.isExecution) {
                // Execution block
                synchronized(scriptEngine) {
                    scriptEngine.initializeResultAccumulator("")
                    scriptEngine.execute(block.command)
                    scriptEngine.getResultAccumulator() ?: ""
                }
            } else {
                // Interpolation block
                if (block.command == "tR") {
                    synchronized(scriptEngine) {
                        scriptEngine.getResultAccumulator() ?: ""
                    }
                } else {
                    val varValue = synchronized(scriptEngine) {
                        scriptEngine.getVariable(block.command)
                    }
                    if (varValue != null) {
                        varValue.toString()
                    } else {
                        val result = executeCommand(block.command, context, moduleFactory)
                        // CancelledResult means user cancelled - check cancel behavior setting
                        when (result) {
                            is CancelledResult -> when (services.settings.cancelBehavior) {
                                CancelBehavior.KEEP_EXPRESSION -> block.matchText
                                CancelBehavior.REMOVE_EXPRESSION -> ""
                            }
                            else -> result.toString()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOG?.warn("Block execution error: ${e.message}", e)
            ErrorMessages.scriptExecutionError(e.message)
        }
    }

    /**
     * Execute a command.
     * @return [CommandResult] representing the outcome of the command
     */
    private fun executeCommand(
        command: String,
        context: TemplateContext,
        moduleFactory: ModuleFactory
    ): CommandResult {
        // Remove 'await' keyword if present
        val withoutAwait = if (command.startsWith("await ")) command.substring(6).trim() else command
        val normalized = if (withoutAwait.startsWith("tp.")) withoutAwait.substring(3) else withoutAwait

        // Extract module name (first part before .) - don't split on dots inside arguments
        val dotIndex = normalized.indexOf('.')
        if (dotIndex == -1) return OkValueResult("")

        val module = normalized.substring(0, dotIndex)
        val rest = normalized.substring(dotIndex + 1) // Everything after module.
        val parts = listOf(module, rest)

        return when (module) {
            "frontmatter" -> {
                // Frontmatter needs the full path split (but only outside of parentheses)
                val frontmatterParts = splitOutsideParentheses(normalized)
                OkValueResult(moduleFactory.getFrontmatterModule().getValue(frontmatterParts)?.toString() ?: "")
            }
            "date" -> CommandExecutionHelper.executeDateCommand(parts, context, errorOnMissingParts = false)
            "file" -> CommandExecutionHelper.executeFileCommand(parts, context, errorOnMissingParts = false)
            // system commands can return CancelledResult when cancelled
            "system" -> CommandExecutionHelper.executeSystemCommand(parts, context, errorOnMissingParts = false)
            "config" -> OkValueResult(moduleFactory.getConfigModule().executeProperty(rest.substringBefore("(").substringBefore(".")) ?: "")
            else -> OkValueResult("")
        }
    }

    /** Split string on dots, but only outside of parentheses and quotes */
    private fun splitOutsideParentheses(str: String): List<String> {
        val parts = mutableListOf<String>()
        var current = StringBuilder()
        var parenDepth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (char in str) {
            when {
                (char == '"' || char == '\'') && parenDepth == 0 -> {
                    if (!inQuotes) {
                        inQuotes = true
                        quoteChar = char
                    } else if (char == quoteChar) {
                        inQuotes = false
                    }
                    current.append(char)
                }
                char == '(' && !inQuotes -> {
                    parenDepth++
                    current.append(char)
                }
                char == ')' && !inQuotes -> {
                    parenDepth--
                    current.append(char)
                }
                char == '.' && parenDepth == 0 && !inQuotes -> {
                    parts.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }

        if (current.isNotEmpty()) {
            parts.add(current.toString())
        }

        return parts
    }

    private fun applyResults(
        content: String,
        blocks: List<TemplateBlock>,
        results: Map<Int, String>
    ): String {
        var result = content

        // Process blocks in reverse order to maintain correct positions
        for (block in blocks.sortedByDescending { it.originalStart }) {
            val replacement = results[block.id] ?: ""

            // Calculate trim boundaries
            var trimStart = block.originalStart
            var trimEnd = block.originalEnd

            // Left trim
            when (block.leftTrim) {
                "_" -> {
                    while (trimStart > 0 && result[trimStart - 1].isWhitespace()) trimStart--
                }
                "-" -> {
                    if (trimStart > 0 && result[trimStart - 1] == '\n') {
                        trimStart--
                        if (trimStart > 0 && result[trimStart - 1] == '\r') trimStart--
                    }
                }
            }

            // Right trim
            when (block.rightTrim) {
                "_" -> {
                    while (trimEnd < result.length && result[trimEnd].isWhitespace()) trimEnd++
                }
                "-" -> {
                    if (trimEnd < result.length && result[trimEnd] == '\r') trimEnd++
                    if (trimEnd < result.length && result[trimEnd] == '\n') trimEnd++
                }
            }

            result = result.substring(0, trimStart) + replacement + result.substring(trimEnd)
        }

        return result
    }
}