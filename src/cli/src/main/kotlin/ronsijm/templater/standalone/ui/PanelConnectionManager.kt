package ronsijm.templater.standalone.ui

import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.swing.JFrame

class PanelConnectionManager(
    private val mainWindow: JFrame,
    private val fileTreePanel: FileTreePanel,
    private val editorPanel: TabbedEditorPanel,
    private val debugPanel: DebugPanel,
    private val variablesPanel: VariablesPanel,
    private val renderPanel: RenderPanel,
    private val controlFlowPanel: ControlFlowPanel,
    private val algorithmVisualizationPanel: ronsijm.templater.ui.visualization.AlgorithmVisualizationPanel
) {

    fun setupConnections() {
        setupFileTreeConnections()
        setupEditorConnections()
        setupDebugConnections()
    }

    private fun setupFileTreeConnections() {

        fileTreePanel.addFileSelectionListener { file ->
            editorPanel.loadFile(file)
            updateAllVisualizationPanels()
        }


        fileTreePanel.addFileOpenInTabListener { file ->
            editorPanel.loadFileInNewTab(file)
            updateAllVisualizationPanels()
        }


        fileTreePanel.addFileOpenInWindowListener { file ->
            openFileInNewWindow(file)
        }
    }

    private fun setupEditorConnections() {

        editorPanel.addBreakpointListener { breakpoints ->

        }


        editorPanel.addContentChangeListener { content ->
            updateAllVisualizationPanels()
        }
    }

    private fun setupDebugConnections() {

        debugPanel.variablesPanel = variablesPanel
        debugPanel.editorPanel = editorPanel
        debugPanel.controlFlowPanel = controlFlowPanel
        debugPanel.algorithmVisualizationPanel = algorithmVisualizationPanel
    }


    fun updateAllVisualizationPanels() {
        updateRenderPanelFromEditor()
        updateControlFlowPanel()
    }


    fun updateRenderPanelFromEditor() {
        val content = editorPanel.getText()
        if (content.isNotBlank()) {
            val currentFile = editorPanel.getCurrentFile()
            if (currentFile != null && (currentFile.extension.equals("md", ignoreCase = true) ||
                                       currentFile.extension.equals("markdown", ignoreCase = true))) {
                renderPanel.setMarkdownContent(content)
            } else if (content.trim().startsWith("<html>", ignoreCase = true) ||
                      content.trim().startsWith("<!DOCTYPE", ignoreCase = true)) {
                renderPanel.setRenderedContent(content)
            } else {
                renderPanel.setMarkdownContent(content)
            }
        } else {
            renderPanel.clear()
        }
    }


    fun updateControlFlowPanel() {
        val content = editorPanel.getText()
        if (content.isNotBlank()) {
            controlFlowPanel.updateContent(content)
        } else {
            controlFlowPanel.clear()
        }
    }


    private fun openFileInNewWindow(file: File) {
        val window = JFrame(file.name)
        window.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        window.size = Dimension(800, 600)
        window.setLocationRelativeTo(mainWindow)

        val editor = EditorPanel()
        editor.loadFile(file)

        window.contentPane.add(editor, BorderLayout.CENTER)
        window.isVisible = true
    }
}

