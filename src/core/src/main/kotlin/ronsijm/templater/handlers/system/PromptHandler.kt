package ronsijm.templater.handlers.system

import ronsijm.templater.common.CancelledResult
import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.handlers.CancellableHandler
import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
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
