package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Returns the file name",
    example = "name()",
    pure = true
)
class NameHandler : CommandHandler<NameRequest, String> {
    override fun handle(request: NameRequest, context: TemplateContext): String {
        return context.fileName
    }
}

