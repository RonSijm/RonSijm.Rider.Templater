package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Returns the file path",
    example = "path(true)",
    pure = true
)
class PathHandler : CommandHandler<PathRequest, String> {
    override fun handle(request: PathRequest, context: TemplateContext): String {
        return context.filePath ?: ""
    }
}

