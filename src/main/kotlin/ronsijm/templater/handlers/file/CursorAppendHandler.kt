package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Appends content at cursor position",
    example = "cursor_append('text to append')",
    barrier = true
)
class CursorAppendHandler : CommandHandler<CursorAppendRequest, String> {
    override fun handle(request: CursorAppendRequest, context: TemplateContext): String {
        if (request.content.isEmpty()) return ""

        context.services.fileOperationService.cursorAppend(request.content)
        return ""
    }
}

