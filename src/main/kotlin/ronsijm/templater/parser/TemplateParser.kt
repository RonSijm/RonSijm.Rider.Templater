package ronsijm.templater.parser

import ronsijm.templater.handlers.CancelledResult
import ronsijm.templater.handlers.CommandResult
import ronsijm.templater.handlers.OkValueResult
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.modules.*
import ronsijm.templater.script.ScriptEngine
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.utils.CommandExecutionHelper
import ronsijm.templater.utils.ErrorMessages
import ronsijm.templater.utils.Logging
import com.intellij.openapi.project.Project

/**
 * Template parser supporting Templater syntax
 * Supports:
 * - Interpolation commands: <% tp.module.function() %>
 * - Execution commands: <%* code %>
 * - Whitespace control: <%-, -%>, <%_, _%>
 */
class TemplateParser(
    private val validateSyntax: Boolean = true,
    private val services: ServiceContainer = ServiceContainer.createDefault()
) {

    companion object {
        private val LOG = Logging.getLogger<TemplateParser>()
    }

    // Regex to match template commands with optional whitespace control and execution marker
    // Groups: 1=left trim, 2=execution marker (*), 3=command content, 4=right trim
    private val templateRegex = Regex("""<%([_-])?(\*)?(.+?)([_-])?%>""", RegexOption.DOT_MATCHES_ALL)

    // Template validator
    private val validator = TemplateValidator()

    // Script engine for execution commands
    private var scriptEngine: ScriptEngine? = null

    // Module factory for centralized module creation
    private var moduleFactory: ModuleFactory? = null

    /**
     * Parse and execute template commands in content
     * @throws TemplateValidationException if validation is enabled and template has syntax errors
     */
    fun parse(content: String, context: TemplateContext, project: Project? = null): String {
        // Validate template syntax if enabled
        if (validateSyntax) {
            val validationErrors = validator.validate(content)
            if (validationErrors.isNotEmpty()) {
                val errorMessage = validationErrors.joinToString("\n") { it.toString() }
                LOG?.warn("Template validation failed:\n$errorMessage")
                // For now, just log warnings - don't throw to maintain backward compatibility
                // In the future, this could be configurable to throw exceptions
            }
        }
        // Initialize modules with dependency injection using ModuleFactory
        // Only needed for non-command-based modules (frontmatter, hooks, config, app)
        moduleFactory = ModuleFactory(context, services, project)

        // Create script engine with enhanced context
        // Use HandlerRegistry for date commands, ModuleFactory for frontmatter
        val enhancedContext = context.copy(
            executeDateCommand = { function, args -> HandlerRegistry.executeCommand("date", function, args, context) },
            executeFrontmatterCommand = { parts -> moduleFactory!!.getFrontmatterModule().getValue(parts) }
        )
        scriptEngine = ScriptEngine(enhancedContext)

        var result = content

        templateRegex.findAll(content).forEach { match ->
            // Check if return was requested - stop processing further blocks
            if (scriptEngine?.isReturnRequested() == true) {
                return@forEach
            }

            val leftTrim = match.groupValues[1]
            val isExecution = match.groupValues[2] == "*"
            val command = match.groupValues[3].trim()
            val rightTrim = match.groupValues[4]

            // Find the match in the current result string
            // We need to search for it since previous replacements may have changed positions
            val matchText = match.value
            val matchStart = result.indexOf(matchText)
            if (matchStart == -1) {
                // Match not found (already processed or error)
                return@forEach
            }
            val matchEnd = matchStart + matchText.length

            // Determine what to replace the command with
            val replacement = if (isExecution) {
                // Execution command - use script engine
                try {
                    LOG?.debug("Executing script block: $command")

                    // Initialize tR to empty string for this script block
                    scriptEngine?.initializeResultAccumulator("")

                    // Execute the script
                    val scriptResult = scriptEngine?.execute(command)
                    LOG?.debug("Script execution returned: $scriptResult")

                    // Get the accumulated tR value - this replaces the script block
                    val tRValue = scriptEngine?.getResultAccumulator() ?: ""
                    LOG?.debug("tR value: '$tRValue'")

                    tRValue
                } catch (e: Exception) {
                    LOG?.debug("Script execution error: ${e.message}", e)
                    ErrorMessages.scriptExecutionError(e.message)
                }
            } else {
                // Interpolation command - check if it's tR first
                if (command == "tR") {
                    scriptEngine?.getResultAccumulator() ?: ""
                } else {
                    // Check if it's a simple variable reference
                    val varValue = scriptEngine?.getVariable(command)
                    if (varValue != null) {
                        varValue.toString()
                    } else if (command.startsWith("tp.") || command.startsWith("await tp.")) {
                        // It's a tp.module.function() command - execute it
                        try {
                            val result = executeCommand(command, enhancedContext)
                            // CancelledResult means user cancelled - check cancel behavior setting
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
                        // Try to evaluate as an expression (e.g., variable.method() or arithmetic)
                        try {
                            val exprResult = scriptEngine?.evaluateExpression(command)
                            exprResult?.toString() ?: ""
                        } catch (e: Exception) {
                            // If expression evaluation fails, try as a command (for backwards compatibility)
                            try {
                                val result = executeCommand(command, enhancedContext)
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

            // Apply whitespace trimming
            var trimStart = matchStart
            var trimEnd = matchEnd

            // Left trim
            when (leftTrim) {
                "_" -> {
                    // Trim all whitespace before
                    while (trimStart > 0 && result[trimStart - 1].isWhitespace()) {
                        trimStart--
                    }
                }
                "-" -> {
                    // Trim one newline before
                    if (trimStart > 0 && result[trimStart - 1] == '\n') {
                        trimStart--
                        // Also trim \r if it's \r\n
                        if (trimStart > 0 && result[trimStart - 1] == '\r') {
                            trimStart--
                        }
                    }
                }
            }

            // Right trim
            when (rightTrim) {
                "_" -> {
                    // Trim all whitespace after
                    while (trimEnd < result.length && result[trimEnd].isWhitespace()) {
                        trimEnd++
                    }
                }
                "-" -> {
                    // Trim one newline after
                    if (trimEnd < result.length && result[trimEnd] == '\r') {
                        trimEnd++
                    }
                    if (trimEnd < result.length && result[trimEnd] == '\n') {
                        trimEnd++
                    }
                }
            }

            // Replace the command (and trimmed whitespace) with the result
            val before = result.substring(0, trimStart)
            val after = result.substring(trimEnd)
            result = before + replacement + after
        }

        return result
    }

    /**
     * Execute a template command
     * Example: "tp.frontmatter.title" or "tp.date.now()"
     * @return [CommandResult] representing the outcome of the command
     */
    private fun executeCommand(command: String, context: TemplateContext): CommandResult {
        // Remove 'await' keyword if present
        val withoutAwait = if (command.startsWith("await ")) {
            command.substring(6).trim()
        } else {
            command
        }

        // Remove "tp." prefix if present
        val normalized = if (withoutAwait.startsWith("tp.")) {
            withoutAwait.substring(3)
        } else {
            withoutAwait
        }

        // Extract module name (first part before .)
        val dotIndex = normalized.indexOf('.')
        if (dotIndex == -1) {
            throw TemplateExecutionException(
                message = "Invalid command format",
                command = command,
                suggestion = "Commands should be in format: tp.module.function()"
            )
        }

        val module = normalized.substring(0, dotIndex)
        val rest = normalized.substring(dotIndex + 1) // Everything after module.

        return try {
            // Handle special non-handler modules first
            when (module) {
                "frontmatter" -> {
                    // Frontmatter needs the full path split
                    val parts = normalized.split(".")
                    val value = moduleFactory?.getFrontmatterModule()?.getValue(parts)
                    return OkValueResult(value?.toString() ?: "")
                }
                "hooks" -> return OkValueResult("") // Hooks don't output anything
                "config" -> {
                    val property = rest.substringBefore("(").substringBefore(".")
                    return OkValueResult(moduleFactory?.getConfigModule()?.executeProperty(property) ?: "")
                }
                "app" -> return OkValueResult("") // App module is for script blocks
                "obsidian" -> {
                    // Alias: tp.obsidian.request({url}) -> tp.web.request(url)
                    if (rest.startsWith("request")) {
                        return OkValueResult(executeObsidianRequest(rest, context))
                    }
                    return OkValueResult("")
                }
            }

            // Try handler-based modules via HandlerRegistry
            if (HandlerRegistry.commandsByModule.containsKey(module)) {
                // Split carefully - only split up to the function name, not inside args
                val parts = listOf(module, rest)
                return CommandExecutionHelper.executeModuleCommand(module, parts, context)
            }

            // Unknown module - provide helpful suggestions
            val availableModules = HandlerRegistry.commandsByModule.keys.joinToString(", ") +
                ", frontmatter, hooks, config, app, obsidian"
            val suggestion = when {
                module.startsWith("dat") -> "Did you mean 'tp.date'?"
                module.startsWith("fil") -> "Did you mean 'tp.file'?"
                module.startsWith("front") -> "Did you mean 'tp.frontmatter'?"
                module.startsWith("sys") -> "Did you mean 'tp.system'?"
                else -> "Available modules: $availableModules"
            }
            throw TemplateExecutionException(
                message = "Unknown module: $module",
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

    /** Handle tp.obsidian.request({url: x}) by extracting URL and calling web.request */
    private fun executeObsidianRequest(functionCall: String, context: TemplateContext): String {
        val argsString = if (functionCall.contains("(")) {
            functionCall.substringAfter("(").substringBeforeLast(")")
        } else ""

        if (argsString.isBlank()) {
            return "[Error: URL required for tp.obsidian.request]"
        }

        val trimmed = argsString.trim()

        // Parse the argument - could be object literal or string
        val url = if (trimmed.startsWith("{")) {
            // Object literal: { url: "..." } or { url: '...' }
            val urlMatch = Regex("""url\s*:\s*["']([^"']+)["']""").find(trimmed)
            urlMatch?.groupValues?.get(1) ?: return "[Error: Missing 'url' in request options]"
        } else {
            // Plain string argument - remove surrounding quotes
            trimmed.removeSurrounding("\"").removeSurrounding("'")
        }

        return HandlerRegistry.executeCommand("web", "request", listOf(url), context).toString()
    }
}