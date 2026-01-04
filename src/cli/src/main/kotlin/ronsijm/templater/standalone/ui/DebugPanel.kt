package ronsijm.templater.standalone.ui

import ronsijm.templater.debug.*
import ronsijm.templater.standalone.debug.StandaloneDebugSession
import ronsijm.templater.standalone.debug.StandaloneSessionStateListener
import ronsijm.templater.standalone.ui.util.ScrollPaneWrapper
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.util.Timer
import java.util.TimerTask
import javax.swing.*

class DebugPanel : JPanel(BorderLayout()) {

    private val statusLabel = JLabel("No debug session active")
    private val currentStepLabel = JLabel("")
    private val traceListModel = DefaultListModel<String>()
    private val traceList = JList(traceListModel)


    private val continueButton = JButton("Continue (F9)")
    private val stepIntoButton = JButton("Step Into (F7)")
    private val stepOverButton = JButton("Step Over (F8)")
    private val stepOutButton = JButton("Step Out (Shift+F8)")
    private val stopButton = JButton("Stop (Ctrl+F2)")
    private val autoStepButton = JButton("Auto Step")


    private var isAutoStepping = false
    private var autoStepTimer: Timer? = null
    private var autoStepDelayMs: Long = 500


    private var currentSession: StandaloneDebugSession? = null


    var variablesPanel: VariablesPanel? = null


    var editorPanel: TabbedEditorPanel? = null


    var controlFlowPanel: ControlFlowPanel? = null


    var algorithmVisualizationPanel: ronsijm.templater.ui.visualization.AlgorithmVisualizationPanel? = null

    init {
        setupUI()
        setupListeners()
        setControlsEnabled(false)
    }

    private fun setupUI() {

        val toolbar = JPanel(FlowLayout(FlowLayout.LEFT, 2, 2))
        toolbar.add(continueButton)
        toolbar.add(stepIntoButton)
        toolbar.add(stepOverButton)
        toolbar.add(stepOutButton)
        toolbar.add(JSeparator(SwingConstants.VERTICAL).apply {
            preferredSize = java.awt.Dimension(2, 24)
        })
        toolbar.add(autoStepButton)
        toolbar.add(JSeparator(SwingConstants.VERTICAL).apply {
            preferredSize = java.awt.Dimension(2, 24)
        })
        toolbar.add(stopButton)
        toolbar.add(Box.createHorizontalStrut(10))
        toolbar.add(statusLabel)


        val stepPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            add(JLabel("Current Step: "), BorderLayout.WEST)
            add(currentStepLabel, BorderLayout.CENTER)
        }


        val tracePanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("Execution Trace")

            add(ScrollPaneWrapper.wrap(JScrollPane(traceList)), BorderLayout.CENTER)
        }


        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(stepPanel, BorderLayout.NORTH)
        contentPanel.add(tracePanel, BorderLayout.CENTER)


        add(toolbar, BorderLayout.NORTH)
        add(contentPanel, BorderLayout.CENTER)
    }

    private fun setupListeners() {
        continueButton.addActionListener {
            stopAutoStep()
            currentSession?.continueExecution()
        }
        stepIntoButton.addActionListener {
            stopAutoStep()
            currentSession?.stepInto()
        }
        stepOverButton.addActionListener {
            stopAutoStep()
            currentSession?.stepOver()
        }
        stepOutButton.addActionListener {
            stopAutoStep()
            currentSession?.stepOut()
        }
        stopButton.addActionListener {
            stopAutoStep()
            stopDebugging()
        }
        autoStepButton.addActionListener { toggleAutoStep() }
    }

    fun startDebugging() {

        statusLabel.text = "Starting debug session..."
    }

    fun stopDebugging() {
        currentSession?.stop()
        currentSession = null
        updateUIForSessionEnd()
    }


    fun onBreakpointToggled(lineNumber: Int, isAdded: Boolean) {
        val session = currentSession ?: return

        if (isAdded) {
            session.addBreakpoint(lineNumber)
        } else {
            session.removeBreakpoint(lineNumber)
        }
    }


    fun startDebugSession(
        file: java.io.File,
        content: String,
        breakpoints: Set<Int>,
        onComplete: (String?) -> Unit
    ) {

        val session = StandaloneDebugSession(file, breakpoints)
        currentSession = session


        session.addStateListener(object : StandaloneSessionStateListener {
            override fun onPaused(breakpoint: DebugBreakpoint) {
                SwingUtilities.invokeLater {
                    updateUIForPause(breakpoint)
                }
            }

            override fun onResumed(action: DebugAction) {
                SwingUtilities.invokeLater {
                    updateUIForResume()
                }
            }

            override fun onStopped() {
                SwingUtilities.invokeLater {
                    updateUIForSessionEnd()
                    onComplete(session.getResult())
                }
            }
        })

        updateUIForSessionStart(file)


        Thread {
            session.execute(content)
        }.start()
    }

    private fun updateUIForSessionStart(file: java.io.File) {
        statusLabel.text = "Debugging: ${file.name}"
        stopButton.isEnabled = true
        traceListModel.clear()
    }

    private fun updateUIForSessionEnd() {
        statusLabel.text = "No debug session active"
        currentStepLabel.text = ""
        setControlsEnabled(false)
        traceListModel.clear()


        stopAutoStep()


        editorPanel?.getCurrentEditor()?.clearDebugLineHighlight()


        variablesPanel?.setVariableUpdater(null)


        controlFlowPanel?.let { panel ->
            panel.diagramPanel.clearRuntimeState()
            panel.diagramPanel.setRuntimeVisualizationEnabled(false)
        }
    }

    private fun updateUIForPause(breakpoint: DebugBreakpoint) {
        val step = breakpoint.step
        val lineNumber = step.displayLineNumber

        currentStepLabel.text = "${step.description} (Line ${lineNumber ?: "?"})"
        setControlsEnabled(true)


        updateTraceList(breakpoint.trace)


        variablesPanel?.updateVariables(breakpoint)


        algorithmVisualizationPanel?.updateVisualization(breakpoint)


        val session = currentSession
        if (session != null) {
            variablesPanel?.setVariableUpdater { name, value ->
                session.variableUpdater?.updateVariable(name, value) ?: false
            }
        }




        if (lineNumber != null && step.type == ronsijm.templater.debug.ExecutionStep.StepType.STATEMENT) {
            editorPanel?.getCurrentEditor()?.highlightDebugLine(lineNumber)
        }


        updateControlFlowHighlight(breakpoint)


        if (isAutoStepping) {
            scheduleNextAutoStep()
        }
    }

    private fun updateUIForResume() {
        setControlsEnabled(false)
        currentStepLabel.text = "Running..."


        controlFlowPanel?.let { panel ->
            panel.diagramPanel.getActiveNodeId()?.let { nodeId ->
                println("DEBUG: Marking node $nodeId as visited")
                panel.diagramPanel.markNodeVisited(nodeId)
            }
        }


        variablesPanel?.clearVariables()


        algorithmVisualizationPanel?.clearVisualization()
    }

    private fun updateTraceList(trace: ExecutionTrace) {
        traceListModel.clear()
        trace.allSteps.forEach { step ->
            val indent = "  ".repeat(getStepDepth(step, trace))
            val line = if (step.displayLineNumber != null) " [Line ${step.displayLineNumber}]" else ""
            traceListModel.addElement("$indent${step.description}$line")
        }


        if (traceListModel.size > 0) {
            traceList.ensureIndexIsVisible(traceListModel.size - 1)
        }
    }

    private fun getStepDepth(step: ExecutionStep, trace: ExecutionTrace): Int {
        var depth = 0
        var currentStep = step
        while (currentStep.parentId != null) {
            depth++
            currentStep = trace.getStep(currentStep.parentId!!) ?: break
        }
        return depth
    }

    private fun setControlsEnabled(enabled: Boolean) {
        continueButton.isEnabled = enabled
        stepIntoButton.isEnabled = enabled
        stepOverButton.isEnabled = enabled
        stepOutButton.isEnabled = enabled
        stopButton.isEnabled = currentSession != null
    }


    fun getCurrentSession(): StandaloneDebugSession? = currentSession


    private fun updateControlFlowHighlight(breakpoint: DebugBreakpoint) {
        val panel = controlFlowPanel ?: return
        val graph = panel.currentGraph ?: return


        panel.diagramPanel.setRuntimeVisualizationEnabled(true)


        val statementId = breakpoint.step.statementNode?.id
        val nodeId = if (statementId != null) {

            graph.nodes.find { it.id == statementId }?.id
        } else {


            val lineNumber = breakpoint.step.displayLineNumber
            val stepCode = breakpoint.step.input
            val stepType = breakpoint.step.type

            println("DEBUG: No statement ID, falling back to old matching. stepType=$stepType, lineNumber=$lineNumber")

            if (stepType == ronsijm.templater.debug.ExecutionStep.StepType.LOOP_ITERATION) {
                findLoopStartNode(lineNumber, graph)
            } else {
                lineNumber?.let { findNodeForLine(it, panel) }
                    ?: stepCode?.let { findNodeByCode(it, panel) }
            }
        }

        if (nodeId != null) {
            panel.diagramPanel.setActiveNode(nodeId)

        }
    }


    private fun findLoopStartNode(lineNumber: Int?, graph: ronsijm.templater.debug.ControlFlowGraph): String? {
        if (lineNumber == null) return null
        return graph.nodes.find { node ->
            node.type == ronsijm.templater.debug.FlowNode.NodeType.LOOP_START && node.lineNumber == lineNumber
        }?.id
    }


    private fun findNodeForLine(lineNumber: Int, panel: ControlFlowPanel): String? {

        panel.lineToNodeMap[lineNumber]?.let { return it }


        val closestLine = panel.lineToNodeMap.keys
            .filter { it <= lineNumber }
            .maxOrNull()

        return closestLine?.let { panel.lineToNodeMap[it] }
    }


    private fun findNodeByCode(code: String, panel: ControlFlowPanel): String? {
        val normalizedCode = code.trim()


        panel.codeToNodeMap[normalizedCode]?.let { return it }


        val graph = panel.currentGraph ?: return null
        return graph.nodes.find { node ->
            node.code?.trim() == normalizedCode ||
                normalizedCode.startsWith(node.code?.trim() ?: "") ||
                (node.code?.trim() ?: "").startsWith(normalizedCode)
        }?.id
    }


    private fun toggleAutoStep() {
        if (isAutoStepping) {
            stopAutoStep()
        } else {
            startAutoStep()
        }
    }


    private fun startAutoStep() {
        isAutoStepping = true
        autoStepButton.text = "Stop Auto Step"
        autoStepButton.toolTipText = "Stop automatic stepping"


        if (currentSession != null) {
            scheduleNextAutoStep()
        }
    }


    private fun stopAutoStep() {
        isAutoStepping = false
        autoStepTimer?.cancel()
        autoStepTimer = null
        autoStepButton.text = "Auto Step"
        autoStepButton.toolTipText = "Automatically step through execution at configured speed"
    }


    private fun scheduleNextAutoStep() {
        autoStepTimer?.cancel()

        autoStepTimer = Timer("AutoStep", true)
        autoStepTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (isAutoStepping) {
                    SwingUtilities.invokeLater {
                        currentSession?.stepInto()
                    }
                }
            }
        }, autoStepDelayMs)
    }
}

