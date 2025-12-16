package ronsijm.templater.script

import ronsijm.templater.handlers.CancelledResult
import ronsijm.templater.handlers.CommandResult
import ronsijm.templater.handlers.ErrorResult
import ronsijm.templater.handlers.OkValueResult
import ronsijm.templater.handlers.generated.HandlerRegistry

/**
 * Routes tp.module.function() calls to the right handler.
 * Frontmatter is special-cased since it's not command-based.
 * Obsidian module is aliased to web module for compatibility.
 */
class ModuleRegistry(private val context: ScriptContext) {

    fun executeFunction(functionPath: String, args: List<Any?>): Any? {
        val normalized = functionPath.removePrefix("tp.")
        val parts = normalized.split(".")
        if (parts.isEmpty()) return null

        val module = parts[0]
        val function = parts.getOrNull(1) ?: return null

        if (module == "frontmatter") {
            // FrontmatterModule expects the full path including "frontmatter"
            return context.frontmatterModule.getValue(listOf("frontmatter") + parts.drop(1))
        }

        // tp.obsidian.request({url: x}) -> tp.web.request(url)
        if (module == "obsidian" && function == "request") {
            return handleObsidianRequest(args)
        }

        return extractValue(HandlerRegistry.executeCommand(module, function, args, context.getTemplateContext()))
    }

    /** Handle tp.obsidian.request({url: x}) by extracting URL and calling web.request */
    private fun handleObsidianRequest(args: List<Any?>): Any? {
        if (args.isEmpty()) return "[Error: URL required for tp.obsidian.request]"

        val url = when (val firstArg = args[0]) {
            is Map<*, *> -> firstArg["url"]?.toString() ?: return "[Error: Missing 'url' in request options]"
            is String -> firstArg
            else -> return "[Error: Invalid argument for tp.obsidian.request]"
        }

        return extractValue(HandlerRegistry.executeCommand("web", "request", listOf(url), context.getTemplateContext()))
    }

    /**
     * Extract the underlying value from a CommandResult.
     * For OkValueResult, returns the wrapped value.
     * For CancelledResult, returns null.
     * For ErrorResult, returns null.
     * For OkResult, returns empty string.
     */
    private fun extractValue(result: CommandResult): Any? {
        return when (result) {
            is OkValueResult<*> -> result.value
            is CancelledResult -> null
            is ErrorResult -> null
            else -> result.toString()
        }
    }
}