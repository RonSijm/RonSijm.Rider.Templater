package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.ErrorMessages
import ronsijm.templater.utils.FilePathValidator

@RegisterHandler(
    module = "file",
    description = "Renames the current file",
    example = "rename('new-name.md')",
    barrier = true
)
class RenameHandler : CommandHandler<RenameRequest, String> {
    override fun handle(request: RenameRequest, context: TemplateContext): String {
        if (request.newName.isEmpty()) return ""

        val validation = FilePathValidator.validateRename(request.newName)
        if (!validation.isValid) {
            return ErrorMessages.validationError(validation.errorMessage ?: "Invalid file name")
        }

        context.services.fileOperationService.rename(request.newName)
        return ""
    }
}
