package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Moves the current file to a new location",
    example = "move('new/path/')",
    barrier = true
)
class MoveHandler : CommandHandler<MoveRequest, String> {
    override fun handle(request: MoveRequest, context: TemplateContext): String {
        if (request.newPath.isEmpty()) return ""

        context.services.fileOperationService.move(request.newPath)
        return ""
    }
}

