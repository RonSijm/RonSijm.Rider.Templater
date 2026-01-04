package ronsijm.templater.editor

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileDocumentManager


class BreakpointEditorFactoryListener : EditorFactoryListener {

    private val clickHandler = BreakpointGutterClickHandler()

    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val document = editor.document
        val virtualFile = FileDocumentManager.getInstance().getFile(document) ?: return


        val fileName = virtualFile.name.lowercase()
        if (!fileName.endsWith(".md") && !fileName.endsWith(".markdown")) {
            return
        }


        editor.addEditorMouseListener(clickHandler)
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        val editor = event.editor

        editor.removeEditorMouseListener(clickHandler)
    }
}

