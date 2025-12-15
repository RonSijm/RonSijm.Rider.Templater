package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "system",
    description = "Prompts user to choose multiple items from a list",
    example = "multi_suggester(['Option 1', 'Option 2'], null, false, 'Choose', 10)"
)
class MultiSuggesterHandler : CommandHandler<MultiSuggesterRequest, String?> {
    override fun handle(request: MultiSuggesterRequest, context: TemplateContext): String? {
        val actualItems = request.items ?: request.textItems

        val result = context.services.systemOperationsService.multiSuggester(
            request.textItems,
            actualItems,
            request.throwOnCancel,
            request.placeholder,
            request.limit
        )

        return result?.toString()
    }
}

