package ronsijm.templater.diagram

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import ronsijm.templater.debug.ActiveDebugSession
import ronsijm.templater.debug.ControlFlowBuilder
import ronsijm.templater.debug.ControlFlowGraph
import ronsijm.templater.debug.DebugAction
import ronsijm.templater.debug.DebugBreakpoint
import ronsijm.templater.debug.DebugEventListener
import ronsijm.templater.debug.ExecutionStep
import ronsijm.templater.debug.FlowNode
import ronsijm.templater.debug.MermaidExporter
import ronsijm.templater.debug.SessionStateListener
import ronsijm.templater.debug.TemplaterDebugService
import ronsijm.templater.script.ScriptParser
import ronsijm.templater.settings.TemplaterSettings
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.SwingUtilities

class ControlFlowToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = ControlFlowToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

class ControlFlowToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val diagramPanel = ControlFlowDiagramPanel()
    private val statusLabel = JBLabel("Open a markdown file to see its control flow")
    private val debugStatusLabel = JBLabel("")
    private val scriptParser = ScriptParser()
    private val flowBuilder = ControlFlowBuilder(scriptParser)
    private val mermaidExporter = MermaidExporter()

    private var currentFile: VirtualFile? = null
    private var currentGraph: ControlFlowGraph? = null
    private var documentListener: DocumentListener? = null


    private var lineToNodeMap: Map<Int, String> = emptyMap()


    private var codeToNodeMap: Map<String, String> = emptyMap()


    private var currentDebugSession: ActiveDebugSession? = null
    private var sessionStateListener: SessionStateListener? = null

    init {

        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { refreshDiagram() }
        toolbar.add(refreshButton)

        val resetViewButton = JButton("Reset View")
        resetViewButton.addActionListener { diagramPanel.resetView() }
        toolbar.add(resetViewButton)

        val exportMermaidButton = JButton("Export to Mermaid")
        exportMermaidButton.addActionListener { exportToMermaid() }
        toolbar.add(exportMermaidButton)

        toolbar.add(statusLabel)

        add(toolbar, BorderLayout.NORTH)
        add(diagramPanel, BorderLayout.CENTER)


        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    onFileChanged(event.newFile)
                }
            }
        )


        setupDebugListener()


        val currentFile = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
        onFileChanged(currentFile)
    }

    private fun setupDebugListener() {
        val debugService = TemplaterDebugService.getInstance(project)

        debugService.addListener(object : DebugEventListener {
            override fun onSessionStarted(session: ActiveDebugSession) {
                ApplicationManager.getApplication().invokeLater {
                    onDebugSessionStarted(session)
                }
            }

            override fun onSessionEnded(session: ActiveDebugSession) {
                ApplicationManager.getApplication().invokeLater {
                    onDebugSessionEnded(session)
                }
            }
        })
    }

    private fun onDebugSessionStarted(session: ActiveDebugSession) {
        currentDebugSession = session


        if (session.file == currentFile) {
            diagramPanel.setRuntimeVisualizationEnabled(true)
            debugStatusLabel.text = "Debugging: ${session.file.name}"


            val listener = object : SessionStateListener {
                override fun onPaused(breakpoint: DebugBreakpoint) {
                    SwingUtilities.invokeLater {
                        onDebugPaused(breakpoint)
                    }
                }

                override fun onResumed(action: DebugAction) {
                    SwingUtilities.invokeLater {
                        onDebugResumed(action)
                    }
                }

                override fun onStopped() {
                    SwingUtilities.invokeLater {
                        onDebugStopped()
                    }
                }
            }
            sessionStateListener = listener
            session.addStateListener(listener)
        }
    }

    private fun onDebugSessionEnded(session: ActiveDebugSession) {
        if (session == currentDebugSession) {
            sessionStateListener?.let { session.removeStateListener(it) }
            sessionStateListener = null
            currentDebugSession = null

            debugStatusLabel.text = "Debug session ended"
            diagramPanel.setActiveNode(null)
        }
    }

    private fun onDebugPaused(breakpoint: DebugBreakpoint) {
        val lineNumber = breakpoint.step.displayLineNumber


        val statementId = breakpoint.step.statementNode?.id
        val nodeId = if (statementId != null) {

            currentGraph?.nodes?.find { it.id == statementId }?.id
        } else {


            val stepCode = breakpoint.step.input
            val stepType = breakpoint.step.type

            if (stepType == ExecutionStep.StepType.LOOP_ITERATION) {
                findLoopStartNode(lineNumber)
            } else {
                lineNumber?.let { findNodeForLine(it) }
                    ?: stepCode?.let { findNodeByCode(it) }
            }
        }

        if (nodeId != null) {
            diagramPanel.setActiveNode(nodeId)
            diagramPanel.scrollToNode(nodeId)
            debugStatusLabel.text = "Paused at line ${lineNumber ?: "?"}"
        } else {
            debugStatusLabel.text = "Paused at line ${lineNumber ?: "?"} (no matching node)"
        }
    }

    private fun onDebugResumed(action: DebugAction) {

        diagramPanel.activeNodeId?.let { nodeId ->
            diagramPanel.markNodeVisited(nodeId)
        }
        debugStatusLabel.text = "Running... (${action.name.lowercase()})"
    }

    private fun onDebugStopped() {
        debugStatusLabel.text = "Debug session stopped"
        diagramPanel.setActiveNode(null)
    }

    private fun findNodeForLine(lineNumber: Int): String? {

        lineToNodeMap[lineNumber]?.let { return it }


        val closestLine = lineToNodeMap.keys
            .filter { it <= lineNumber }
            .maxOrNull()

        return closestLine?.let { lineToNodeMap[it] }
    }

    private fun findNodeByCode(code: String): String? {
        val normalizedCode = code.trim()


        codeToNodeMap[normalizedCode]?.let { return it }


        val graph = currentGraph ?: return null
        return graph.nodes.find { node ->
            node.code?.trim() == normalizedCode ||
            normalizedCode.startsWith(node.code?.trim() ?: "") ||
            (node.code?.trim() ?: "").startsWith(normalizedCode)
        }?.id
    }


    private fun findLoopStartNode(lineNumber: Int?): String? {
        if (lineNumber == null) return null

        val graph = currentGraph ?: return null


        return graph.nodes.find { node ->
            node.type == FlowNode.NodeType.LOOP_START && node.lineNumber == lineNumber
        }?.id
    }

    private fun onFileChanged(file: VirtualFile?) {

        documentListener?.let { listener ->
            currentFile?.let { oldFile ->
                FileDocumentManager.getInstance().getDocument(oldFile)?.removeDocumentListener(listener)
            }
        }

        currentFile = file

        if (file == null || !isMarkdownFile(file)) {
            statusLabel.text = "Open a markdown file to see its control flow"
            diagramPanel.setGraph(null)
            return
        }


        val document = FileDocumentManager.getInstance().getDocument(file)
        if (document != null) {
            documentListener = object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) {

                    ApplicationManager.getApplication().invokeLater {
                        refreshDiagram()
                    }
                }
            }
            document.addDocumentListener(documentListener!!)
        }

        refreshDiagram()
    }

    private fun refreshDiagram() {
        val file = currentFile
        if (file == null || !isMarkdownFile(file)) {
            currentGraph = null
            lineToNodeMap = emptyMap()
            codeToNodeMap = emptyMap()
            return
        }

        val document = FileDocumentManager.getInstance().getDocument(file)
        if (document == null) {
            statusLabel.text = "Could not read file"
            currentGraph = null
            lineToNodeMap = emptyMap()
            codeToNodeMap = emptyMap()
            return
        }

        val content = document.text
        val graph = flowBuilder.buildFromTemplate(content)
        currentGraph = graph


        lineToNodeMap = graph.nodes
            .filter { it.lineNumber != null }
            .associate { it.lineNumber!! to it.id }


        codeToNodeMap = graph.nodes
            .filter { it.code != null }
            .associate { it.code!!.trim() to it.id }

        val settings = TemplaterSettings.getInstance()
        val nodeStyles = settings.mermaidNodeStyles

        diagramPanel.setGraph(graph, nodeStyles)

        val nodeCount = graph.nodes.size
        val edgeCount = graph.edges.size
        statusLabel.text = "${file.name} - $nodeCount nodes, $edgeCount edges"
    }

    private fun exportToMermaid() {
        val graph = currentGraph
        if (graph == null || graph.isEmpty) {
            Messages.showInfoMessage(
                project,
                "No control flow graph available. Open a markdown template file first.",
                "Export to Mermaid"
            )
            return
        }

        val file = currentFile
        val title = file?.nameWithoutExtension ?: "Template"

        val settings = TemplaterSettings.getInstance()
        val nodeStyles = settings.mermaidNodeStyles

        val mermaidCode = mermaidExporter.exportFlowchart(
            graph,
            title = "$title Control Flow",
            includeExplanations = true,
            nodeStyles = nodeStyles
        )


        CopyPasteManager.getInstance().setContents(StringSelection(mermaidCode))

        Messages.showInfoMessage(
            project,
            "Mermaid diagram code has been copied to clipboard.\n\n" +
                "You can paste it into:\n" +
                "• Mermaid Live Editor (mermaid.live)\n" +
                "• GitHub/GitLab markdown files\n" +
                "• Any Mermaid-compatible viewer",
            "Export to Mermaid"
        )
    }

    private fun isMarkdownFile(file: VirtualFile): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".md") || name.endsWith(".markdown")
    }
}

