package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Includes/reads the contents of a file",
    example = "include('path/to/file.md')",
    pure = true
)
class IncludeHandler : CommandHandler<IncludeRequest, String> {
    override fun handle(request: IncludeRequest, context: TemplateContext): String {
        if (request.includeLink.isEmpty()) return ""

        return context.services.fileOperationService.include(request.includeLink) ?: ""
    }
}

