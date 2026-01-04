package ronsijm.templater.parser

import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.ErrorResult
import ronsijm.templater.common.ModuleExecutor
import ronsijm.templater.common.ModuleNames
import ronsijm.templater.common.Prefixes
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.utils.ErrorMessages


class HandlerModuleExecutor(
    private val templateContext: TemplateContext
) : ModuleExecutor {

    override fun executeModuleFunction(module: String, function: String, args: List<Any?>): CommandResult {

        if (module == ModuleNames.OBSIDIAN && function == "request") {
            return handleObsidianRequest(args)
        }

        return HandlerRegistry.executeCommand(module, function, args, templateContext)
    }


    private fun handleObsidianRequest(args: List<Any?>): CommandResult {
        if (args.isEmpty()) {
            return ErrorResult(ErrorMessages.urlRequiredError("${Prefixes.TP}${ModuleNames.OBSIDIAN}.request"))
        }

        val url = when (val firstArg = args[0]) {
            is Map<*, *> -> firstArg["url"]?.toString()
                ?: return ErrorResult(ErrorMessages.missingOptionError("url", "request"))
            is String -> firstArg
            else -> return ErrorResult(ErrorMessages.invalidArgumentError("${Prefixes.TP}${ModuleNames.OBSIDIAN}.request"))
        }

        return HandlerRegistry.executeCommand(ModuleNames.WEB, "request", listOf(url), templateContext)
    }
}

