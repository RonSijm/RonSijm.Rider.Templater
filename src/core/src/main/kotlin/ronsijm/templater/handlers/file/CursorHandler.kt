package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Sets the cursor position",
    example = "cursor(0)",
    barrier = true
)
class CursorHandler : CommandHandler<CursorRequest, String> {
    override fun handle(request: CursorRequest, context: TemplateContext): String {
        context.services.fileOperationService.setCursor(request.order)
        return ""
    }
}
