package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "system",
    description = "Gets clipboard contents",
    example = "clipboard()",
    barrier = true
)
class ClipboardHandler : CommandHandler<ClipboardRequest, String?> {
    override fun handle(request: ClipboardRequest, context: TemplateContext): String? {
        return context.services.clipboardService.getClipboardText()
    }
}
