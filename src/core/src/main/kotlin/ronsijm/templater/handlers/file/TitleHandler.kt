package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Returns the file title (name without extension)",
    example = "title()",
    pure = true
)
class TitleHandler : CommandHandler<TitleRequest, String> {
    override fun handle(request: TitleRequest, context: TemplateContext): String {
        val fileName = context.fileName
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
    }
}
