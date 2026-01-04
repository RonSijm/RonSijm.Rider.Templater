package ronsijm.templater.parser

import ronsijm.templater.ast.TemplateAST
import ronsijm.templater.common.CancelledResult
import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.ErrorTemplates
import ronsijm.templater.common.ModuleNames
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.common.Prefixes
import ronsijm.templater.common.TemplateSyntax
import ronsijm.templater.handlers.CommandExecutionHelper
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.modules.AppModuleProvider
import ronsijm.templater.modules.ConfigModule
import ronsijm.templater.modules.FrontmatterModule
import ronsijm.templater.modules.HooksModule
import ronsijm.templater.modules.ModuleFactory
import ronsijm.templater.script.MutableVariableUpdaterWrapper
import ronsijm.templater.script.NoOpExecutionCallback
import ronsijm.templater.script.ScriptEngine
import ronsijm.templater.script.ScriptExecutionCallback
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.utils.CancellationChecker
import ronsijm.templater.utils.ErrorMessages
import ronsijm.templater.utils.Logging
import ronsijm.templater.utils.NoCancellationChecker
import ronsijm.templater.utils.TextUtils


class TemplateParser(
    private val validateSyntax: Boolean = true,
    private val services: ServiceContainer = ServiceContainer()
) {

    companion object {
        private val LOG = Logging.getLogger<TemplateParser>()


        private val URL_OPTION_REGEX = Regex("""url\s*:\s*["']([^"']+)["']""")
    }

    private val templateRegex = TemplateSyntax.TEMPLATE_BLOCK_REGEX
    private val validator = TemplateValidator()
    private lateinit var scriptEngine: ScriptEngine
    private lateinit var moduleFactory: ModuleFactory
    private lateinit var moduleExecutor: HandlerModuleExecutor


    fun parse(
        content: String,
        context: TemplateContext,
        appModuleProvider: AppModuleProvider = NullAppModuleProvider,
        cancellationChecker: CancellationChecker = NoCancellationChecker,
        executionCallback: ScriptExecutionCallback = NoOpExecutionCallback,
        ast: TemplateAST? = null
    ): String {

        val cleanContent = if (content.startsWith("\uFEFF")) {
            content.substring(1)
        } else {
            content
        }


        if (validateSyntax) {
            val validationErrors = validator.validate(cleanContent)
            if (validationErrors.isNotEmpty()) {
                val errorMessage = validationErrors.joinToString("\n") { it.toString() }
                LOG?.warn("Template validation failed:\n$errorMessage")


            }
        }


        moduleFactory = ModuleFactory(context, appModuleProvider)


        val frontmatterAccess = moduleFactory.getFrontmatterModule()
        moduleExecutor = HandlerModuleExecutor(context)
        scriptEngine = ScriptEngine(frontmatterAccess, moduleExecutor, cancellationChecker, executionCallback)



        executionCallback.getVariableUpdater()?.let { callbackUpdater ->

            if (callbackUpdater is MutableVariableUpdaterWrapper) {
                callbackUpdater.setDelegate(scriptEngine.createVariableUpdater())
            }
        }

        var result = cleanContent
        var executionBlockIndex = 0

        templateRegex.findAll(cleanContent).forEach { match ->

            cancellationChecker.checkCancelled()


            if (scriptEngine.isReturnRequested()) {
                return cleanContent
            }

            val leftTrim = match.groupValues[1]
            val isExecution = match.groupValues[2] == "*"
            val command = match.groupValues[3].trim()
            val rightTrim = match.groupValues[4]


            val lineNumber = TextUtils.calculateLineNumber(cleanContent, match.range.first)


            executionCallback.setCurrentBlockLineNumber(lineNumber)
            executionCallback.setCurrentBlockContent(command)



            val matchText = match.value
            val matchStart = result.indexOf(matchText)
            if (matchStart == -1) {

                return@forEach
            }
            val matchEnd = matchStart + matchText.length


            val replacement = if (isExecution) {

                try {
                    LOG?.debug("Executing script block: $command")


                    scriptEngine.initializeResultAccumulator("")


                    val scriptResult = if (ast != null && executionBlockIndex < ast.blocks.size) {
                        val astBlock = ast.blocks[executionBlockIndex]
                        if (astBlock.isExecution && astBlock.statements.isNotEmpty()) {
                            LOG?.debug("Using AST-based execution with ${astBlock.statements.size} nodes")
                            scriptEngine.executeWithAST(astBlock.statements)
                        } else {

                            scriptEngine.execute(command)
                        }
                    } else {

                        scriptEngine.execute(command)
                    }
                    LOG?.debug("Script execution returned: $scriptResult")


                    val tRValue = scriptEngine.getResultAccumulator()
                    LOG?.debug("tR value: '$tRValue'")


                    executionBlockIndex++

                    tRValue
                } catch (e: Exception) {
                    LOG?.debug("Script execution error: ${e.message}", e)
                    ErrorMessages.scriptExecutionError(e.message)
                }
            } else {




                if (command == "tR") {
                    scriptEngine.getResultAccumulator()
                } else {

                    val varValue = scriptEngine.getVariable(command)
                    if (varValue != null) {
                        varValue.toString()
                    } else if (command.startsWith("tp.") || command.startsWith("await tp.")) {

                        try {
                            val result = executeCommand(command, context)

                            when (result) {
                                is CancelledResult -> when (services.settings.cancelBehavior) {
                                    CancelBehavior.KEEP_EXPRESSION -> matchText
                                    CancelBehavior.REMOVE_EXPRESSION -> ""
                                }
                                else -> result.toString()
                            }
                        } catch (e: Exception) {
                            ErrorMessages.scriptExecutionError(e.message)
                        }
                    } else {

                        try {
                            val exprResult = scriptEngine.evaluateExpression(command)
                            exprResult?.toString() ?: ""
                        } catch (e: Exception) {

                            try {
                                val result = executeCommand(command, context)
                                when (result) {
                                    is CancelledResult -> when (services.settings.cancelBehavior) {
                                        CancelBehavior.KEEP_EXPRESSION -> matchText
                                        CancelBehavior.REMOVE_EXPRESSION -> ""
                                    }
                                    else -> result.toString()
                                }
                            } catch (e2: Exception) {
                                ErrorMessages.scriptExecutionError(e.message)
                            }
                        }
                    }
                }
            }


            var trimStart = matchStart
            var trimEnd = matchEnd


            when (leftTrim) {
                "_" -> {

                    while (trimStart > 0 && result[trimStart - 1].isWhitespace()) {
                        trimStart--
                    }
                }
                "-" -> {

                    if (trimStart > 0 && result[trimStart - 1] == '\n') {
                        trimStart--

                        if (trimStart > 0 && result[trimStart - 1] == '\r') {
                            trimStart--
                        }
                    }
                }
            }


            when (rightTrim) {
                "_" -> {

                    while (trimEnd < result.length && result[trimEnd].isWhitespace()) {
                        trimEnd++
                    }
                }
                "-" -> {

                    if (trimEnd < result.length && result[trimEnd] == '\r') {
                        trimEnd++
                    }
                    if (trimEnd < result.length && result[trimEnd] == '\n') {
                        trimEnd++
                    }
                }
            }


            val before = result.substring(0, trimStart)
            val after = result.substring(trimEnd)
            result = before + replacement + after


            executionCallback.onBlockProcessed(
                originalBlock = matchText,
                replacement = replacement,
                currentDocument = result,
                lineNumber = lineNumber
            )
        }

        return result
    }


    private fun executeCommand(command: String, context: TemplateContext): CommandResult {

        val withoutAwait = if (command.startsWith(Prefixes.AWAIT)) {
            command.substring(Prefixes.AWAIT.length).trim()
        } else {
            command
        }


        val normalized = if (withoutAwait.startsWith(Prefixes.TP)) {
            withoutAwait.substring(Prefixes.TP.length)
        } else {
            withoutAwait
        }


        val dotIndex = normalized.indexOf('.')
        if (dotIndex == -1) {
            throw TemplateExecutionException(
                message = ErrorTemplates.INVALID_COMMAND_FORMAT,
                command = command,
                suggestion = ErrorTemplates.COMMAND_FORMAT_SUGGESTION
            )
        }

        val module = normalized.substring(0, dotIndex)
        val rest = normalized.substring(dotIndex + 1)

        return try {

            when (module) {
                ModuleNames.FRONTMATTER -> {

                    val parts = normalized.split(".")
                    val value = moduleFactory.getFrontmatterModule().getValue(parts)
                    return OkValueResult(value?.toString() ?: "")
                }
                ModuleNames.HOOKS -> return OkValueResult("")
                ModuleNames.CONFIG -> {
                    val property = rest.substringBefore("(").substringBefore(".")
                    return OkValueResult(moduleFactory.getConfigModule().executeProperty(property) ?: "")
                }
                ModuleNames.APP -> return OkValueResult("")
                ModuleNames.OBSIDIAN -> {

                    if (rest.startsWith("request")) {
                        return OkValueResult(executeObsidianRequest(rest, context))
                    }
                    return OkValueResult("")
                }
            }


            if (HandlerRegistry.commandsByModule.containsKey(module)) {

                val parts = listOf(module, rest)
                return CommandExecutionHelper.executeModuleCommand(module, parts, moduleExecutor)
            }


            val availableModules = HandlerRegistry.commandsByModule.keys.joinToString(", ") +
                ", ${ModuleNames.SPECIAL_MODULES.joinToString(", ")}"
            val suggestion = ErrorTemplates.unknownModuleSuggestion(module)
                ?: "Available modules: $availableModules"
            throw TemplateExecutionException(
                message = ErrorTemplates.UNKNOWN_MODULE.format(module),
                command = command,
                suggestion = suggestion
            )
        } catch (e: TemplateExecutionException) {
            throw e
        } catch (e: Exception) {
            throw TemplateExecutionException(
                message = e.message ?: "Unknown error",
                command = command,
                cause = e
            )
        }
    }


    private fun executeObsidianRequest(functionCall: String, context: TemplateContext): String {
        val argsString = if (functionCall.contains("(")) {
            functionCall.substringAfter("(").substringBeforeLast(")")
        } else ""

        if (argsString.isBlank()) {
            return ErrorMessages.urlRequiredError("tp.obsidian.request")
        }

        val trimmed = argsString.trim()


        val url = if (trimmed.startsWith("{")) {

            val urlMatch = URL_OPTION_REGEX.find(trimmed)
            urlMatch?.groupValues?.get(1) ?: return ErrorMessages.missingOptionError("url", "request")
        } else {

            trimmed.removeSurrounding("\"").removeSurrounding("'")
        }

        return HandlerRegistry.executeCommand("web", "request", listOf(url), context).toString()
    }
}