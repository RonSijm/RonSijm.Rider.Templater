package ronsijm.templater.modules

import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.CommandMetadata
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.FrontmatterParser

/**
 * Base class for all template modules
 * Provides common functionality for module command execution.
 *
 * Note: Command-based modules (date, file, system, web) are now handled by HandlerRegistry.
 * This base class is used by non-command-based modules like FrontmatterModule, HooksModule, etc.
 *
 * The context parameter is nullable to support stateless modules.
 */
abstract class BaseModule(protected val context: TemplateContext? = null) {

    companion object {
        /**
         * Empty context for stateless modules
         */
        val EMPTY_CONTEXT = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "",
            filePath = ""
        )
    }

    /**
     * Command registry - automatically built from command list using metadata.name
     * This eliminates the duplication of having the name in both the map key and metadata
     */
    private val commandRegistry: Map<String, Command> by lazy {
        getCommandList().associateBy { it.metadata.name }
    }

    /**
     * Subclasses override this to provide their list of commands
     * The command names are automatically extracted from metadata
     */
    protected abstract fun getCommandList(): List<Command>

    /**
     * Execute a command by name
     * @param function The function name (from metadata)
     * @param args The function arguments
     * @return The result or null
     */
    fun executeCommand(function: String, args: List<Any?>): String? {
        return commandRegistry[function]?.execute(args, context ?: EMPTY_CONTEXT)
    }

    /**
     * Get all available commands for this module
     * Used for auto-discovery and IntelliSense
     */
    fun getAvailableCommands(): List<CommandMetadata> {
        return commandRegistry.values.map { it.metadata }
    }

    /**
     * Get the command registry for external access
     */
    fun getCommands(): Map<String, Command> = commandRegistry
}

