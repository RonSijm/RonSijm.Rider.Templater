package ronsijm.templater.services

interface ClipboardService {
    fun getClipboardText(): String
    fun setClipboardText(text: String)
}

