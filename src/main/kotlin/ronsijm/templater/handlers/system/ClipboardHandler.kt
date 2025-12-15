package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.ErrorMessages

@RegisterHandler(
    module = "system",
    description = "Gets clipboard contents",
    example = "clipboard()"
)
class ClipboardHandler : CommandHandler<ClipboardRequest, String?> {
    override fun handle(request: ClipboardRequest, context: TemplateContext): String? {
        return try {
            context.services.clipboardService.getClipboardText()
        } catch (e: Exception) {
            ErrorMessages.clipboardError(e.message ?: "Unknown error")
        }
    }
}

