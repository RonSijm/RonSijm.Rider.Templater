package ronsijm.templater.editor

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.JBColor
import ronsijm.templater.common.TemplateSyntax
import ronsijm.templater.debug.TemplaterDebugService
import ronsijm.templater.utils.TextUtils
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon

class TemplateBreakpointGutterProvider : LineMarkerProvider {

    private val templateRegex = TemplateSyntax.TEMPLATE_BLOCK_REGEX

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {

        if (element !is PsiFile) return null

        val file = element.containingFile ?: return null
        val virtualFile = file.virtualFile ?: return null


        val fileName = virtualFile.name.lowercase()
        if (!fileName.endsWith(".md") && !fileName.endsWith(".markdown")) {
            return null
        }

        return null
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        if (elements.isEmpty()) return

        val file = elements.firstOrNull()?.containingFile ?: return
        val virtualFile = file.virtualFile ?: return
        val project = file.project


        val fileName = virtualFile.name.lowercase()
        if (!fileName.endsWith(".md") && !fileName.endsWith(".markdown")) {
            return
        }

        val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return
        val text = document.text
        val debugService = TemplaterDebugService.getInstance(project)



        val templateLines = mutableSetOf<Int>()
        templateRegex.findAll(text).forEach { match ->

            val startPos = match.range.first
            val endPos = match.range.last + 1


            val lineRange = TextUtils.calculateLineRange(text, startPos, endPos)


            for (lineNum in lineRange) {
                templateLines.add(lineNum)
            }
        }



        for (element in elements) {
            if (element !is PsiFile) continue

            for (lineNumber in templateLines) {

                val hasBreakpoint = debugService.hasBreakpoint(virtualFile, lineNumber)
                if (!hasBreakpoint) continue

                val lineStartOffset = document.getLineStartOffset(lineNumber - 1)


                val elementAtLine = file.findElementAt(lineStartOffset) ?: continue

                val marker = LineMarkerInfo(
                    elementAtLine,
                    elementAtLine.textRange,
                    BreakpointIcon.ENABLED,
                    { "Remove Breakpoint (Line $lineNumber)" },
                    { mouseEvent, _ ->
                        debugService.toggleBreakpoint(virtualFile, lineNumber)

                        com.intellij.codeInsight.daemon.DaemonCodeAnalyzer.getInstance(project)
                            .restart(file)
                    },
                    GutterIconRenderer.Alignment.RIGHT,
                    { "Remove Breakpoint" }
                )

                result.add(marker)
            }
            break
        }
    }
}


object BreakpointIcon {
    val ENABLED: Icon = object : Icon {
        override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
            val g2 = g.create()
            g2.color = JBColor(Color(0xCC, 0x00, 0x00), Color(0xCC, 0x33, 0x33))
            g2.fillOval(x + 2, y + 2, 12, 12)
            g2.dispose()
        }
        override fun getIconWidth(): Int = 16
        override fun getIconHeight(): Int = 16
    }
}

