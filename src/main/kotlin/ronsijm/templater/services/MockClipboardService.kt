package ronsijm.templater.services

class MockClipboardService(private var content: String = "") : ClipboardService {
    override fun getClipboardText() = content
    override fun setClipboardText(text: String) { content = text }
}

