package ronsijm.templater.standalone.ui

import ronsijm.templater.debug.ControlFlowGraph
import ronsijm.templater.debug.MermaidExporter
import ronsijm.templater.settings.MermaidNodeStyles
import ronsijm.templater.standalone.services.TemplateExecutionService
import ronsijm.templater.standalone.ui.theme.FlatLafThemeColors
import ronsijm.templater.standalone.ui.util.BackgroundTask
import ronsijm.templater.ui.diagram.ControlFlowDiagramPanel
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.FlowLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.*

enum class ControlFlowViewMode(val displayName: String) {
    VISUAL_FLOWCHART("Visual Flowchart"),
    MERMAID_FLOWCHART("Mermaid Flowchart"),
    MERMAID_SEQUENCE("Mermaid Sequence Diagram")
}

class ControlFlowPanel : JPanel(BorderLayout()) {


    val diagramPanel = ControlFlowDiagramPanel(FlatLafThemeColors.instance)
    var currentGraph: ControlFlowGraph? = null
        private set


    var lineToNodeMap: Map<Int, String> = emptyMap()
        private set
    var codeToNodeMap: Map<String, String> = emptyMap()
        private set

    private val mermaidTextArea = JTextArea()
    private val statusLabel = JLabel("No template loaded")
    private var currentContent: String? = null
    private var nodeStyles: MermaidNodeStyles = MermaidNodeStyles()
    private val mermaidExporter = MermaidExporter()


    private val cardLayout = CardLayout()
    private val contentPanel = JPanel(cardLayout)
    private var currentViewMode = ControlFlowViewMode.VISUAL_FLOWCHART


    private lateinit var viewModeCombo: JComboBox<ControlFlowViewMode>
    private lateinit var resetViewButton: JButton
    private lateinit var copyButton: JButton

    init {
        setupUI()
    }

    private fun setupUI() {

        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT))


        toolbar.add(JLabel("View:"))
        viewModeCombo = JComboBox(ControlFlowViewMode.entries.toTypedArray())
        viewModeCombo.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean
            ) = super.getListCellRendererComponent(list, (value as? ControlFlowViewMode)?.displayName ?: value, index, isSelected, cellHasFocus)
        }
        viewModeCombo.addActionListener {
            currentViewMode = viewModeCombo.selectedItem as ControlFlowViewMode
            updateView()
        }
        toolbar.add(viewModeCombo)

        toolbar.add(Box.createHorizontalStrut(10))

        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { refresh() }
        toolbar.add(refreshButton)

        resetViewButton = JButton("Reset View")
        resetViewButton.addActionListener { diagramPanel.resetView() }
        toolbar.add(resetViewButton)

        copyButton = JButton("Copy")
        copyButton.addActionListener { copyMermaidToClipboard() }
        copyButton.isVisible = false
        toolbar.add(copyButton)

        toolbar.add(Box.createHorizontalStrut(20))
        toolbar.add(statusLabel)

        add(toolbar, BorderLayout.NORTH)


        contentPanel.add(diagramPanel, "visual")


        mermaidTextArea.isEditable = false
        mermaidTextArea.font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
        val mermaidScrollPane = JScrollPane(mermaidTextArea)
        contentPanel.add(mermaidScrollPane, "mermaid")

        add(contentPanel, BorderLayout.CENTER)
    }


    fun updateContent(content: String) {
        currentContent = content
        buildGraph(content)
    }


    fun setNodeStyles(styles: MermaidNodeStyles) {
        nodeStyles = styles

        currentContent?.let { buildGraph(it) }
    }


    fun refresh() {
        currentContent?.let { buildGraph(it) }
    }


    fun clear() {
        currentContent = null
        currentGraph = null
        diagramPanel.setGraph(null)
        mermaidTextArea.text = ""
        statusLabel.text = "No template loaded"
    }


    fun getGraph(): ControlFlowGraph? = currentGraph


    private fun updateView() {
        when (currentViewMode) {
            ControlFlowViewMode.VISUAL_FLOWCHART -> {
                cardLayout.show(contentPanel, "visual")
                resetViewButton.isVisible = true
                copyButton.isVisible = false
                currentGraph?.let { diagramPanel.setGraph(it, nodeStyles) }
            }
            ControlFlowViewMode.MERMAID_FLOWCHART -> {
                cardLayout.show(contentPanel, "mermaid")
                resetViewButton.isVisible = false
                copyButton.isVisible = true
                currentGraph?.let { graph ->
                    mermaidTextArea.text = mermaidExporter.exportFlowchart(graph, nodeStyles = nodeStyles)
                    mermaidTextArea.caretPosition = 0
                }
            }
            ControlFlowViewMode.MERMAID_SEQUENCE -> {
                cardLayout.show(contentPanel, "mermaid")
                resetViewButton.isVisible = false
                copyButton.isVisible = true
                currentGraph?.let { graph ->
                    mermaidTextArea.text = mermaidExporter.exportSequenceDiagram(graph)
                    mermaidTextArea.caretPosition = 0
                }
            }
        }
    }


    private fun copyMermaidToClipboard() {
        val text = mermaidTextArea.text
        if (text.isNotBlank()) {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
            statusLabel.text = "Copied to clipboard!"
        }
    }

    private fun buildGraph(content: String) {
        BackgroundTask.execute(
            task = {
                SwingUtilities.invokeLater {
                    statusLabel.text = "Building control flow..."
                }

                val result = TemplateExecutionService.trace(content)

                SwingUtilities.invokeLater {
                    if (result.success && result.graph != null && !result.graph.isEmpty) {
                        currentGraph = result.graph


                        println("DEBUG: Control Flow Graph Nodes:")
                        result.graph.nodes.forEach { node ->
                            println("  Node ${node.id}: type=${node.type}, line=${node.lineNumber}, code=${node.code?.take(50)}")
                        }


                        lineToNodeMap = result.graph.nodes
                            .filter { it.lineNumber != null }
                            .associate { it.lineNumber!! to it.id }


                        codeToNodeMap = result.graph.nodes
                            .filter { it.code != null }
                            .associate { it.code!!.trim() to it.id }

                        println("DEBUG: lineToNodeMap = $lineToNodeMap")
                        println("DEBUG: codeToNodeMap keys = ${codeToNodeMap.keys}")

                        statusLabel.text = "Nodes: ${result.graph.nodes.size}, Edges: ${result.graph.edges.size}"
                        updateView()
                    } else if (result.error != null) {
                        statusLabel.text = "Error: ${result.error.message}"
                        currentGraph = null
                        lineToNodeMap = emptyMap()
                        codeToNodeMap = emptyMap()
                        diagramPanel.setGraph(null)
                        mermaidTextArea.text = ""
                    } else {
                        currentGraph = null
                        lineToNodeMap = emptyMap()
                        codeToNodeMap = emptyMap()
                        diagramPanel.setGraph(null)
                        mermaidTextArea.text = ""
                        statusLabel.text = "No template blocks found"
                    }
                }
            },
            onError = { e ->
                statusLabel.text = "Error: ${e.message}"
                currentGraph = null
                diagramPanel.setGraph(null)
                mermaidTextArea.text = ""
            }
        )
    }
}

