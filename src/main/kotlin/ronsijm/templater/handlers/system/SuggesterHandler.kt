package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "system",
    description = "Prompts user to choose from a list",
    example = "suggester(['Option 1', 'Option 2'], null, false, 'Choose', 10)"
)
class SuggesterHandler : CommandHandler<SuggesterRequest, String?> {
    override fun handle(request: SuggesterRequest, context: TemplateContext): String? {
        val actualItems = request.items ?: request.textItems

        return context.services.systemOperationsService.suggester(
            request.textItems,
            actualItems,
            request.throwOnCancel,
            request.placeholder,
            request.limit
        )?.toString()
    }
}

