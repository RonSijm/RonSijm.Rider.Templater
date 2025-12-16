package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Checks if a file exists",
    example = "exists('path/to/file.md')",
    pure = true
)
class ExistsHandler : CommandHandler<ExistsRequest, String> {
    override fun handle(request: ExistsRequest, context: TemplateContext): String {
        if (request.filepath.isEmpty()) return "false"

        val exists = context.services.fileOperationService.exists(request.filepath)
        return exists.toString()
    }
}

