package ronsijm.templater.services.mock

import ronsijm.templater.services.ClipboardService

class MockClipboardService(private var content: String = "") : ClipboardService {
    override fun getClipboardText() = content
    override fun setClipboardText(text: String) { content = text }
}
