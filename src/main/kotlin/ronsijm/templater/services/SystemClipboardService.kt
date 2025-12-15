package ronsijm.templater.services

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

/**
 * Real implementation of ClipboardService using system clipboard
 */
class SystemClipboardService : ClipboardService {
    
    override fun getClipboardText(): String {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val contents = clipboard.getContents(null)
            
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                contents.getTransferData(DataFlavor.stringFlavor) as String
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    override fun setClipboardText(text: String) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(text)
            clipboard.setContents(selection, null)
        } catch (e: Exception) {
            // Silently fail
        }
    }
}

