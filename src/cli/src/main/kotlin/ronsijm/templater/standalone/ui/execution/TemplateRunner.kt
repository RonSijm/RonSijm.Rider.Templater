package ronsijm.templater.standalone.ui.execution

import ronsijm.templater.standalone.services.TemplateExecutionService
import ronsijm.templater.standalone.settings.AppSettings
import ronsijm.templater.standalone.ui.RenderPanel
import ronsijm.templater.standalone.ui.TabbedEditorPanel
import ronsijm.templater.standalone.ui.dialog.TemplateResultDialog
import java.io.File
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class TemplateRunner(
    private val mainWindow: JFrame,
    private val editorPanel: TabbedEditorPanel,
    private val renderPanel: RenderPanel
) {

    fun runTemplate() {
        val file = editorPanel.getCurrentFile()
        if (file == null) {
            JOptionPane.showMessageDialog(
                mainWindow,
                "No file is currently open",
                "Cannot Run Template",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val content = editorPanel.getText()


        Thread {
            val result = TemplateExecutionService.execute(content, file)

            SwingUtilities.invokeLater {
                if (result.success) {
                    handleTemplateResult(file, result.output)
                } else {
                    JOptionPane.showMessageDialog(
                        mainWindow,
                        "Error running template: ${result.error?.message}\n\n${result.error?.stackTraceToString()}",
                        "Template Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }.start()
    }


    fun handleTemplateResult(sourceFile: File, result: String) {

        updateRenderPanel(result)

        val behavior = AppSettings.getAfterRunningBehavior()

        when (behavior) {
            AppSettings.AfterRunningBehavior.OVERWRITE_AUTOMATICALLY -> {
                sourceFile.writeText(result)
                editorPanel.loadFile(sourceFile)
            }
            AppSettings.AfterRunningBehavior.SAVE_SIDE_BY_SIDE -> {
                val postfix = AppSettings.getSideBySidePostfix()
                val outputFile = File(
                    sourceFile.parentFile,
                    sourceFile.nameWithoutExtension + postfix + "." + sourceFile.extension
                )
                outputFile.writeText(result)
            }
        }


        if (AppSettings.getShowDialogAfterRun()) {
            showTemplateResultDialog(result)
        }
    }


    fun updateRenderPanel(result: String) {
        if (result.trim().startsWith("<html>", ignoreCase = true) ||
            result.trim().startsWith("<!DOCTYPE", ignoreCase = true)) {
            renderPanel.setRenderedContent(result)
        } else {
            renderPanel.setMarkdownContent(result)
        }
    }


    private fun showTemplateResultDialog(result: String) {
        TemplateResultDialog.show(
            parent = mainWindow,
            result = result,
            currentFile = editorPanel.getCurrentFile(),
            onOverwrite = { newContent ->
                editorPanel.getCurrentFile()?.let { file ->
                    editorPanel.loadFile(file)
                }
            }
        )
    }
}

