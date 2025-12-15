package ronsijm.templater.utils

import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.TemplateContext

/** Shared logic for executing module commands - used by both TemplateParser and ParallelTemplateParser */
object CommandExecutionHelper {

    fun executeModuleCommand(
        module: String,
        parts: List<String>,
        context: TemplateContext,
        errorOnMissingParts: Boolean = true
    ): String {
        if (parts.size < 2) {
            if (errorOnMissingParts) {
                throw IllegalArgumentException("${module.replaceFirstChar { it.uppercase() }} command requires a function name")
            }
            return ""
        }

        val function = parts[1]
        val (functionName, args) = parseFunctionCall(function)
        return HandlerRegistry.executeCommand(module, functionName, args, context)?.toString() ?: ""
    }

    fun parseFunctionCall(function: String): Pair<String, List<Any?>> {
        val functionName = function.substringBefore("(")
        val argsString = if (function.contains("(")) function.substringAfter("(").substringBefore(")") else ""
        return Pair(functionName, ArgumentParser.parseArgumentString(argsString))
    }

    fun executeDateCommand(parts: List<String>, context: TemplateContext, errorOnMissingParts: Boolean = true) =
        executeModuleCommand("date", parts, context, errorOnMissingParts)

    fun executeFileCommand(parts: List<String>, context: TemplateContext, errorOnMissingParts: Boolean = true) =
        executeModuleCommand("file", parts, context, errorOnMissingParts)

    fun executeSystemCommand(parts: List<String>, context: TemplateContext, errorOnMissingParts: Boolean = true) =
        executeModuleCommand("system", parts, context, errorOnMissingParts)

    fun executeWebCommand(parts: List<String>, context: TemplateContext, errorOnMissingParts: Boolean = true) =
        executeModuleCommand("web", parts, context, errorOnMissingParts)
}

