package ronsijm.templater.services.mock

import ronsijm.templater.services.ClipboardService


object NullClipboardService : ClipboardService {
    override fun getClipboardText() = ""
    override fun setClipboardText(text: String) {}
}

