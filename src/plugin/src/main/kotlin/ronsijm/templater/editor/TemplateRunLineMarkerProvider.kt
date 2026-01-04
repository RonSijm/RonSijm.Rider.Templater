package ronsijm.templater.editor

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement


class TemplateRunLineMarkerProvider : LineMarkerProvider {

    companion object {
        private val TEMPLATE_START_PATTERNS = listOf("<%*", "<%_", "<%-", "<%")


        fun findTemplateBlockAt(documentText: String, lineStartOffset: Int): Pair<Int, Int>? {
            var searchStart = lineStartOffset

            var templateStart = -1
            var startPattern = ""

            for (pattern in TEMPLATE_START_PATTERNS) {
                val idx = documentText.indexOf(pattern, searchStart)
                if (idx >= 0 && (templateStart < 0 || idx < templateStart)) {
                    templateStart = idx
                    startPattern = pattern
                }
            }

            val textBefore = documentText.substring(0, minOf(lineStartOffset + 100, documentText.length))
            var lastOpenBefore = -1
            for (pattern in TEMPLATE_START_PATTERNS) {
                val idx = textBefore.lastIndexOf(pattern)
                if (idx >= 0 && idx > lastOpenBefore) {
                    val closingAfterOpen = documentText.indexOf("%>", idx)
                    if (closingAfterOpen < 0 || closingAfterOpen >= lineStartOffset) {
                        lastOpenBefore = idx
                        startPattern = pattern
                    }
                }
            }

            if (lastOpenBefore >= 0 && (templateStart < 0 || lastOpenBefore < templateStart)) {
                templateStart = lastOpenBefore
            }

            if (templateStart < 0) return null

            val templateEnd = documentText.indexOf("%>", templateStart + startPattern.length)
            if (templateEnd < 0) return null

            return Pair(templateStart, templateEnd + 2)
        }
    }

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is LeafPsiElement) return null

        val file = element.containingFile ?: return null
        val virtualFile = file.virtualFile ?: return null
        val fileName = file.name.lowercase()
        if (!fileName.endsWith(".md") && !fileName.endsWith(".markdown")) return null

        val document = file.viewProvider.document ?: return null
        val documentText = document.text
        val lineStartOffset = element.textRange.startOffset
        val lineNumber = document.getLineNumber(lineStartOffset)


        if (lineNumber == 0) return null

        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(TextRange(lineStart, lineEnd))

        val hasTemplateStartOnLine = TEMPLATE_START_PATTERNS.any { pattern ->
            lineText.contains(pattern)
        }

        if (!hasTemplateStartOnLine) return null

        val textBeforeElement = document.getText(TextRange(lineStart, lineStartOffset))
        if (textBeforeElement.contains("<%")) return null

        val elementText = element.text
        val elementHasTemplate = TEMPLATE_START_PATTERNS.any { elementText.contains(it) }
        if (!elementHasTemplate && textBeforeElement.isNotEmpty()) return null

        return createRunSingleMarker(element, document, documentText, lineStart, virtualFile, file)
    }

    private fun createRunAllMarker(
        element: PsiElement,
        document: com.intellij.openapi.editor.Document,
        virtualFile: com.intellij.openapi.vfs.VirtualFile,
        file: com.intellij.psi.PsiFile
    ): LineMarkerInfo<PsiElement> {
        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Actions.RunAll,
            { "Run All Templates" },
            { mouseEvent, psiElement ->
                val project = psiElement.project
                val editor = FileEditorManager.getInstance(project).selectedTextEditor

                if (editor != null && editor.document == document) {
                    val dataContext = SimpleDataContext.builder()
                        .add(CommonDataKeys.PROJECT, project)
                        .add(CommonDataKeys.EDITOR, editor)
                        .add(CommonDataKeys.VIRTUAL_FILE, virtualFile)
                        .add(CommonDataKeys.PSI_FILE, file)
                        .build()

                    val action = ActionManager.getInstance().getAction("Templater.ExecuteTemplate")
                    action?.let {
                        val event = com.intellij.openapi.actionSystem.AnActionEvent.createFromAnAction(
                            action,
                            mouseEvent,
                            "TemplateGutterRunAll",
                            dataContext
                        )
                        action.actionPerformed(event)
                    }
                }
            },
            GutterIconRenderer.Alignment.LEFT,
            { "Run All Templates" }
        )
    }

    private fun createRunSingleMarker(
        element: PsiElement,
        document: com.intellij.openapi.editor.Document,
        documentText: String,
        lineStart: Int,
        virtualFile: com.intellij.openapi.vfs.VirtualFile,
        file: com.intellij.psi.PsiFile
    ): LineMarkerInfo<PsiElement> {
        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Actions.Execute,
            { "Run This Template" },
            { mouseEvent, psiElement ->
                val project = psiElement.project
                val editor = FileEditorManager.getInstance(project).selectedTextEditor

                if (editor != null && editor.document == document) {
                    val blockRange = findTemplateBlockAt(documentText, lineStart)

                    if (blockRange != null) {
                        editor.selectionModel.setSelection(blockRange.first, blockRange.second)

                        val dataContext = SimpleDataContext.builder()
                            .add(CommonDataKeys.PROJECT, project)
                            .add(CommonDataKeys.EDITOR, editor)
                            .add(CommonDataKeys.VIRTUAL_FILE, virtualFile)
                            .add(CommonDataKeys.PSI_FILE, file)
                            .build()

                        val action = ActionManager.getInstance().getAction("Templater.ExecuteTemplate")
                        action?.let {
                            val event = com.intellij.openapi.actionSystem.AnActionEvent.createFromAnAction(
                                action,
                                mouseEvent,
                                "TemplateGutterRunSingle",
                                dataContext
                            )
                            action.actionPerformed(event)
                        }
                    }
                }
            },
            GutterIconRenderer.Alignment.LEFT,
            { "Run This Template" }
        )
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        if (elements.isEmpty()) return


        val firstElement = elements.first()
        val file = firstElement.containingFile ?: return
        val virtualFile = file.virtualFile ?: return
        val fileName = file.name.lowercase()
        if (!fileName.endsWith(".md") && !fileName.endsWith(".markdown")) return

        val document = file.viewProvider.document ?: return
        val documentText = document.text


        val hasAnyTemplates = TEMPLATE_START_PATTERNS.any { documentText.contains(it) }
        if (!hasAnyTemplates) return


        val firstElementOnLine0 = elements.firstOrNull { element ->
            element is LeafPsiElement && document.getLineNumber(element.textRange.startOffset) == 0
        } ?: return


        result.add(createRunAllMarker(firstElementOnLine0, document, virtualFile, file))
    }
}
