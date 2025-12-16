package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Renames the current file",
    example = "rename('new-name.md')",
    barrier = true
)
class RenameHandler : CommandHandler<RenameRequest, String> {
    override fun handle(request: RenameRequest, context: TemplateContext): String {
        if (request.newName.isEmpty()) return ""

        context.services.fileOperationService.rename(request.newName)
        return ""
    }
}

