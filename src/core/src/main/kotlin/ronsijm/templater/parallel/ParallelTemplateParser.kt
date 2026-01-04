package ronsijm.templater.parallel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import ronsijm.templater.common.CancelledResult
import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.handlers.CommandExecutionHelper
import ronsijm.templater.modules.AppModuleProvider
import ronsijm.templater.parser.HandlerModuleExecutor
import ronsijm.templater.modules.ModuleFactory
import ronsijm.templater.parser.TemplateBlockProcessor
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateValidator
import ronsijm.templater.script.ScriptEngine
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.utils.CancellationChecker
import ronsijm.templater.utils.ErrorMessages
import ronsijm.templater.utils.Logging
import ronsijm.templater.utils.NoCancellationChecker
import java.util.concurrent.ConcurrentHashMap


class ParallelTemplateParser(
    private val validateSyntax: Boolean = true,
    private val services: ServiceContainer = ServiceContainer(),
    private val enableParallel: Boolean = true
) {
    companion object {
        private val LOG = Logging.getLogger<ParallelTemplateParser>()
    }

    private val validator = TemplateValidator()
    private val executor = ParallelExecutor()


    fun parse(
        content: String,
        context: TemplateContext,
        appModuleProvider: AppModuleProvider = NullAppModuleProvider,
        cancellationChecker: CancellationChecker = NoCancellationChecker
    ): String {
        if (validateSyntax) {
            TemplateBlockProcessor.validateAndLog(content, validator)
        }


        val blocks = TemplateBlockProcessor.extractBlocks(content)

        if (blocks.isEmpty()) {
            return content
        }


        val plan = executor.getExecutionPlan(blocks)
        LOG?.info("Parallel execution plan: ${plan.phases.size} phases, " +
                "${plan.parallelizableBlocks}/${plan.totalBlocks} parallelizable " +
                "(${(plan.parallelizationRatio * 100).toInt()}%)")


        val moduleFactory = ModuleFactory(context, appModuleProvider)
        val frontmatterAccess = moduleFactory.getFrontmatterModule()
        val moduleExecutor = HandlerModuleExecutor(context)
        val scriptEngine = ScriptEngine(frontmatterAccess, moduleExecutor, cancellationChecker)


        val results = if (enableParallel && plan.parallelizableBlocks > 0) {
            executeParallel(blocks, plan, scriptEngine, context, moduleFactory, moduleExecutor, cancellationChecker)
        } else {
            executeSequential(blocks, scriptEngine, context, moduleFactory, moduleExecutor, cancellationChecker)
        }


        return TemplateBlockProcessor.applyResults(content, blocks, results)
    }

    private fun executeSequential(
        blocks: List<TemplateBlock>,
        scriptEngine: ScriptEngine,
        context: TemplateContext,
        moduleFactory: ModuleFactory,
        moduleExecutor: HandlerModuleExecutor,
        cancellationChecker: CancellationChecker
    ): Map<Int, String> {
        val results = mutableMapOf<Int, String>()

        for (block in blocks) {

            cancellationChecker.checkCancelled()


            if (scriptEngine.isReturnRequested()) {

                results[block.id] = ""
                continue
            }
            results[block.id] = executeBlock(block, scriptEngine, context, moduleFactory, moduleExecutor)
        }

        return results
    }

    private fun executeParallel(
        blocks: List<TemplateBlock>,
        plan: ExecutionPlan,
        scriptEngine: ScriptEngine,
        context: TemplateContext,
        moduleFactory: ModuleFactory,
        moduleExecutor: HandlerModuleExecutor,
        cancellationChecker: CancellationChecker
    ): Map<Int, String> {
        return runBlocking {
            val results = ConcurrentHashMap<Int, String>()

            for (phase in plan.phases) {

                cancellationChecker.checkCancelled()

                if (phase.canParallelize) {

                    val phaseResults = coroutineScope {
                        phase.blocks.map { analysis ->
                            async(Dispatchers.Default) {
                                analysis.block.id to executeBlock(
                                    analysis.block, scriptEngine, context, moduleFactory, moduleExecutor
                                )
                            }
                        }.awaitAll()
                    }
                    results.putAll(phaseResults.toMap())
                } else {

                    for (analysis in phase.blocks) {
                        results[analysis.block.id] = executeBlock(
                            analysis.block, scriptEngine, context, moduleFactory, moduleExecutor
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
        moduleFactory: ModuleFactory,
        moduleExecutor: HandlerModuleExecutor
    ): String {
        return executeBlockImpl(block, scriptEngine, context, moduleFactory, moduleExecutor)
    }

    private fun executeBlockImpl(
        block: TemplateBlock,
        scriptEngine: ScriptEngine,
        context: TemplateContext,
        moduleFactory: ModuleFactory,
        moduleExecutor: HandlerModuleExecutor
    ): String {
        return try {
            if (block.isExecution) {

                synchronized(scriptEngine) {
                    scriptEngine.initializeResultAccumulator("")
                    scriptEngine.execute(block.command)
                    scriptEngine.getResultAccumulator() ?: ""
                }
            } else {

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
                        val result = executeCommand(block.command, context, moduleFactory, moduleExecutor)

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


    private fun executeCommand(
        command: String,
        context: TemplateContext,
        moduleFactory: ModuleFactory,
        moduleExecutor: HandlerModuleExecutor
    ): CommandResult {

        val withoutAwait = if (command.startsWith("await ")) command.substring(6).trim() else command
        val normalized = if (withoutAwait.startsWith("tp.")) withoutAwait.substring(3) else withoutAwait


        val dotIndex = normalized.indexOf('.')
        if (dotIndex == -1) return OkValueResult("")

        val module = normalized.substring(0, dotIndex)
        val rest = normalized.substring(dotIndex + 1)
        val parts = listOf(module, rest)

        return when (module) {
            "frontmatter" -> {

                val frontmatterParts = splitOutsideParentheses(normalized)
                OkValueResult(moduleFactory.getFrontmatterModule().getValue(frontmatterParts)?.toString() ?: "")
            }
            "date" -> CommandExecutionHelper.executeDateCommand(parts, moduleExecutor, errorOnMissingParts = false)
            "file" -> CommandExecutionHelper.executeFileCommand(parts, moduleExecutor, errorOnMissingParts = false)

            "system" -> CommandExecutionHelper.executeSystemCommand(parts, moduleExecutor, errorOnMissingParts = false)
            "config" -> OkValueResult(moduleFactory.getConfigModule().executeProperty(rest.substringBefore("(").substringBefore(".")) ?: "")
            else -> OkValueResult("")
        }
    }


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
}
