package ronsijm.templater.standalone.ui.debug

import ronsijm.templater.standalone.ui.DebugPanel
import ronsijm.templater.standalone.ui.TabbedEditorPanel
import ronsijm.templater.standalone.ui.VariablesPanel
import ronsijm.templater.standalone.ui.execution.TemplateRunner
import java.io.File
import javax.swing.JFrame
import javax.swing.JOptionPane

class DebugController(
    private val mainWindow: JFrame,
    private val editorPanel: TabbedEditorPanel,
    private val debugPanel: DebugPanel,
    private val variablesPanel: VariablesPanel,
    private val templateRunner: TemplateRunner
) {

    fun startDebugging() {
        val file = editorPanel.getCurrentFile()
        if (file == null) {
            JOptionPane.showMessageDialog(
                mainWindow,
                "No file is currently open",
                "Cannot Start Debugging",
                JOptionPane.WARNING_MESSAGE
            )
            return
        }

        val content = editorPanel.getText()
        val breakpoints = editorPanel.getBreakpoints()

        debugPanel.startDebugSession(
            file = file,
            content = content,
            breakpoints = breakpoints,
            onComplete = { result ->
                handleDebugComplete(file, result)
            }
        )
    }


    fun stopDebugging() {
        debugPanel.stopDebugging()
        variablesPanel.reset()
    }


    private fun handleDebugComplete(file: File, result: String?) {
        variablesPanel.reset()

        if (result != null) {
            templateRunner.handleTemplateResult(file, result)
        }
    }
}

