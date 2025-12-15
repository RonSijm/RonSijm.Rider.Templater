package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Returns the current file selection",
    example = "selection()"
)
class SelectionHandler : CommandHandler<SelectionRequest, String> {
    override fun handle(request: SelectionRequest, context: TemplateContext): String {
        return context.services.fileOperationService.getSelection() ?: ""
    }
}

