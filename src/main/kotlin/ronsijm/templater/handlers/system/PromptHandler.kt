package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.*
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "system",
    description = "Prompts user for input",
    example = "prompt('Enter name', 'default', false, false)"
)
class PromptHandler : CommandHandler<PromptRequest, CommandResult>, CancellableHandler {
    override fun handle(request: PromptRequest, context: TemplateContext): CommandResult {
        val result = context.services.systemOperationsService.prompt(
            request.promptText,
            request.defaultValue,
            request.multiline,
            false
        )
        return if (result != null) OkValueResult(result) else CancelledResult
    }
}

