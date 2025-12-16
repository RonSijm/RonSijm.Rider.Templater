package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Creates a new file",
    example = "create_new('template content', 'filename.md', true, 'folder')",
    barrier = true
)
class CreateNewHandler : CommandHandler<CreateNewRequest, String> {
    override fun handle(request: CreateNewRequest, context: TemplateContext): String {
        if (request.template.isEmpty()) return ""

        context.services.fileOperationService.createNew(request.template, request.filename, request.openNew, request.folder)
        return ""
    }
}

