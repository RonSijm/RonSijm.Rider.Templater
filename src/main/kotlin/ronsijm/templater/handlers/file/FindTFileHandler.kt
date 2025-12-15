package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Finds a file by name",
    example = "find_t_file('filename.md')"
)
class FindTFileHandler : CommandHandler<FindTFileRequest, String> {
    override fun handle(request: FindTFileRequest, context: TemplateContext): String {
        if (request.filename.isEmpty()) return ""

        return context.services.fileOperationService.findFile(request.filename) ?: ""
    }
}

