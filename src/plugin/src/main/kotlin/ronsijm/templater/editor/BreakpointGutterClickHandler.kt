package ronsijm.templater.editor

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiManager
import ronsijm.templater.common.TemplateSyntax
import ronsijm.templater.debug.TemplaterDebugService

class BreakpointGutterClickHandler : EditorMouseListener {

    private val templateRegex = TemplateSyntax.TEMPLATE_BLOCK_REGEX

    override fun mouseClicked(event: EditorMouseEvent) {

        val mouseEvent = event.mouseEvent
        val editor = event.editor
        val component = mouseEvent.component


        if (component !is EditorGutterComponentEx) return

        val project = editor.project ?: return
        val document = editor.document
        val virtualFile = FileDocumentManager.getInstance().getFile(document) ?: return


        val fileName = virtualFile.name.lowercase()
        if (!fileName.endsWith(".md") && !fileName.endsWith(".markdown")) {
            return
        }


        val logicalPosition = editor.xyToLogicalPosition(mouseEvent.point)
        val lineNumber = logicalPosition.line + 1


        val text = document.text
        val lineStartOffset = document.getLineStartOffset(logicalPosition.line)
        val lineEndOffset = document.getLineEndOffset(logicalPosition.line)
        val lineText = text.substring(lineStartOffset, lineEndOffset)


        if (!templateRegex.containsMatchIn(lineText) && !lineText.contains("<%")) {


            val textBeforeLine = text.substring(0, lineStartOffset)
            val lastOpen = textBeforeLine.lastIndexOf("<%")
            val lastClose = textBeforeLine.lastIndexOf("%>")


            if (lastOpen <= lastClose) {
                return
            }
        }


        val debugService = TemplaterDebugService.getInstance(project)
        debugService.toggleBreakpoint(virtualFile, lineNumber)


        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
        if (psiFile != null) {
            DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
        }
    }
}

