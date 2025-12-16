package ronsijm.templater.handlers

import ronsijm.templater.parser.TemplateContext

/**
 * Interface for command execution logic
 * Separates execution concerns from documentation
 */
interface CommandExecutor {
    /**
     * Execute the command with given arguments
     * @param args Command arguments (can be strings or other types)
     * @param context Template context containing file info, frontmatter, callbacks
     * @return [CommandResult] - either [OkResult], [OkValueResult], or [CancelledResult]
     */
    fun execute(args: List<Any?>, context: TemplateContext): CommandResult
}

