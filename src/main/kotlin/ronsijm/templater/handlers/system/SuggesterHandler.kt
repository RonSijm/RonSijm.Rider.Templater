package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.*
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "system",
    description = "Prompts user to choose from a list",
    example = "suggester(['Option 1', 'Option 2'], null, false, 'Choose', 10)"
)
class SuggesterHandler : CommandHandler<SuggesterRequest, CommandResult>, CancellableHandler {
    override fun handle(request: SuggesterRequest, context: TemplateContext): CommandResult {
        val actualItems = request.items ?: request.textItems

        val result = context.services.systemOperationsService.suggester(
            request.textItems,
            actualItems,
            request.throwOnCancel,
            request.placeholder,
            request.limit
        )
        return if (result != null) OkValueResult(result.toString()) else CancelledResult
    }
}

