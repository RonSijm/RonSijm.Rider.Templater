package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.ErrorMessages
import ronsijm.templater.utils.FilePathValidator

@RegisterHandler(
    module = "file",
    description = "Includes/reads the contents of a file",
    example = "include('path/to/file.md')",
    pure = true
)
class IncludeHandler : CommandHandler<IncludeRequest, String> {
    override fun handle(request: IncludeRequest, context: TemplateContext): String {
        if (request.includeLink.isEmpty()) return ""

        val validation = FilePathValidator.validatePath(request.includeLink)
        if (!validation.isValid) {
            return ErrorMessages.validationError(validation.errorMessage ?: "Invalid path")
        }

        return context.services.fileOperationService.include(request.includeLink) ?: ""
    }
}
