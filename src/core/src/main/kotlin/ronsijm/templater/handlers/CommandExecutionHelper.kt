package ronsijm.templater.handlers

import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.ModuleExecutor
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.utils.ArgumentParser


object CommandExecutionHelper {


    fun executeModuleCommand(
        module: String,
        parts: List<String>,
        moduleExecutor: ModuleExecutor,
        errorOnMissingParts: Boolean = true
    ): CommandResult {
        if (parts.size < 2) {
            if (errorOnMissingParts) {
                throw IllegalArgumentException("${module.replaceFirstChar { it.uppercase() }} command requires a function name")
            }
            return OkValueResult("")
        }

        val function = parts[1]
        val (functionName, args) = parseFunctionCall(function)
        return moduleExecutor.executeModuleFunction(module, functionName, args)
    }

    fun parseFunctionCall(function: String): Pair<String, List<Any?>> {
        val functionName = function.substringBefore("(")
        val argsString = if (function.contains("(")) function.substringAfter("(").substringBefore(")") else ""
        return Pair(functionName, ArgumentParser.parseArgumentString(argsString))
    }

    fun executeDateCommand(parts: List<String>, moduleExecutor: ModuleExecutor, errorOnMissingParts: Boolean = true) =
        executeModuleCommand("date", parts, moduleExecutor, errorOnMissingParts)

    fun executeFileCommand(parts: List<String>, moduleExecutor: ModuleExecutor, errorOnMissingParts: Boolean = true) =
        executeModuleCommand("file", parts, moduleExecutor, errorOnMissingParts)

    fun executeSystemCommand(parts: List<String>, moduleExecutor: ModuleExecutor, errorOnMissingParts: Boolean = true) =
        executeModuleCommand("system", parts, moduleExecutor, errorOnMissingParts)

    fun executeWebCommand(parts: List<String>, moduleExecutor: ModuleExecutor, errorOnMissingParts: Boolean = true) =
        executeModuleCommand("web", parts, moduleExecutor, errorOnMissingParts)
}
