package ronsijm.templater.debug

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Font


class DebugLineHighlighter(private val project: Project) {

    private var currentHighlighter: RangeHighlighter? = null
    private var currentEditor: Editor? = null

    companion object {

        private val DEBUG_LINE_COLOR = JBColor(
            Color(255, 255, 128, 100),
            Color(73, 72, 62)
        )


        private val EXECUTION_POINT_COLOR = JBColor(
            Color(255, 200, 0),
            Color(252, 225, 128)
        )


        private const val DEBUG_HIGHLIGHT_LAYER = HighlighterLayer.SELECTION + 100
    }


    fun highlightLine(file: VirtualFile, lineNumber: Int) {
        ApplicationManager.getApplication().invokeLater {

            clearHighlight()


            val editor = findEditorForFile(file) ?: return@invokeLater


            val line = lineNumber - 1
            if (line < 0 || line >= editor.document.lineCount) return@invokeLater


            val lineStartOffset = editor.document.getLineStartOffset(line)
            val lineEndOffset = editor.document.getLineEndOffset(line)


            val attributes = TextAttributes().apply {
                backgroundColor = DEBUG_LINE_COLOR
                fontType = Font.BOLD
            }


            val markupModel = editor.markupModel
            currentHighlighter = markupModel.addRangeHighlighter(
                lineStartOffset,
                lineEndOffset,
                DEBUG_HIGHLIGHT_LAYER,
                attributes,
                HighlighterTargetArea.LINES_IN_RANGE
            )

            currentEditor = editor


            editor.scrollingModel.scrollTo(
                editor.offsetToLogicalPosition(lineStartOffset),
                ScrollType.CENTER
            )


            editor.caretModel.moveToOffset(lineStartOffset)
        }
    }


    fun clearHighlight() {
        ApplicationManager.getApplication().invokeLater {
            currentHighlighter?.let { highlighter ->
                currentEditor?.markupModel?.removeHighlighter(highlighter)
            }
            currentHighlighter = null
            currentEditor = null
        }
    }


    private fun findEditorForFile(file: VirtualFile): Editor? {
        val fileEditorManager = FileEditorManager.getInstance(project)


        val editors = fileEditorManager.getEditors(file)
        for (fileEditor in editors) {
            if (fileEditor is TextEditor) {
                return fileEditor.editor
            }
        }


        fileEditorManager.openFile(file, true)


        val openedEditors = fileEditorManager.getEditors(file)
        for (fileEditor in openedEditors) {
            if (fileEditor is TextEditor) {
                return fileEditor.editor
            }
        }

        return null
    }
}

