package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Returns the file content",
    example = "content()",
    pure = true
)
class ContentHandler : CommandHandler<ContentRequest, String> {
    override fun handle(request: ContentRequest, context: TemplateContext): String {
        return context.fileContent ?: ""
    }
}
