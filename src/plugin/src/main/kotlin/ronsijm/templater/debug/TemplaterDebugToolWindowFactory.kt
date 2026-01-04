package ronsijm.templater.debug

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import ronsijm.templater.debug.DebugAction
import ronsijm.templater.debug.DebugBreakpoint
import ronsijm.templater.debug.ExecutionStep
import ronsijm.templater.settings.TemplaterSettings
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.util.Timer
import java.util.TimerTask
import javax.swing.*
import javax.swing.table.DefaultTableModel


class TemplaterDebugToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = TemplaterDebugPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}


class TemplaterDebugPanel(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val statusLabel = JBLabel("No debug session active")
    private val currentStepLabel = JBLabel("")
    private val callStackModel = DefaultListModel<String>()
    private val callStackList = JList(callStackModel)


    private val lineHighlighter = DebugLineHighlighter(project)


    private var currentSessionFile: VirtualFile? = null


    private val continueButton = JButton(AllIcons.Actions.Resume).apply {
        toolTipText = "Continue (F9)"
        isEnabled = false
    }
    private val stepIntoButton = JButton(AllIcons.Actions.TraceInto).apply {
        toolTipText = "Step Into (F7)"
        isEnabled = false
    }
    private val stepOverButton = JButton(AllIcons.Actions.TraceOver).apply {
        toolTipText = "Step Over (F8)"
        isEnabled = false
    }
    private val stepOutButton = JButton(AllIcons.Actions.StepOut).apply {
        toolTipText = "Step Out (Shift+F8)"
        isEnabled = false
    }
    private val stopButton = JButton(AllIcons.Actions.Suspend).apply {
        toolTipText = "Stop (Ctrl+F2)"
        isEnabled = false
    }


    private val autoStepButton = JButton(AllIcons.Actions.RunAll).apply {
        toolTipText = "Auto Step - automatically step through at configured speed"
        isEnabled = false
    }


    private var autoStepTimer: Timer? = null
    private var isAutoStepping = false

    private val debugService = TemplaterDebugService.getInstance(project)

    init {
        setupUI()
        setupListeners()
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
            border = JBUI.Borders.empty(5)
            add(JBLabel("Current Step: "), BorderLayout.WEST)
            add(currentStepLabel, BorderLayout.CENTER)
        }


        val tracePanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("Execution Trace")
            add(JBScrollPane(callStackList), BorderLayout.CENTER)
        }


        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(stepPanel, BorderLayout.NORTH)
        contentPanel.add(tracePanel, BorderLayout.CENTER)


        setToolbar(toolbar)
        setContent(contentPanel)
    }

    private fun setupListeners() {

        continueButton.addActionListener {
            stopAutoStep()
            debugService.getCurrentSession()?.continueExecution()
        }
        stepIntoButton.addActionListener {
            stopAutoStep()
            debugService.getCurrentSession()?.stepInto()
        }
        stepOverButton.addActionListener {
            stopAutoStep()
            debugService.getCurrentSession()?.stepOver()
        }
        stepOutButton.addActionListener {
            stopAutoStep()
            debugService.getCurrentSession()?.stepOut()
        }
        stopButton.addActionListener {
            stopAutoStep()
            debugService.stopSession()
        }
        autoStepButton.addActionListener { toggleAutoStep() }


        debugService.addListener(object : DebugEventListener {
            override fun onSessionStarted(session: ActiveDebugSession) {



                session.addStateListener(object : SessionStateListener {
                    override fun onPaused(breakpoint: DebugBreakpoint) {
                        ApplicationManager.getApplication().invokeLater {
                            updateUIForPause(breakpoint)

                            if (isAutoStepping) {
                                scheduleNextAutoStep()
                            }
                        }
                    }

                    override fun onResumed(action: DebugAction) {
                        ApplicationManager.getApplication().invokeLater {
                            updateUIForResume()
                        }
                    }
                })


                ApplicationManager.getApplication().invokeLater {
                    updateUIForSessionStart(session)
                }
            }

            override fun onSessionEnded(session: ActiveDebugSession) {
                ApplicationManager.getApplication().invokeLater {
                    stopAutoStep()
                    updateUIForSessionEnd()
                }
            }
        })
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
        autoStepButton.icon = AllIcons.Actions.Pause
        autoStepButton.toolTipText = "Stop Auto Step"


        if (debugService.getCurrentSession() != null) {
            scheduleNextAutoStep()
        }
    }

    private fun stopAutoStep() {
        isAutoStepping = false
        autoStepTimer?.cancel()
        autoStepTimer = null
        autoStepButton.icon = AllIcons.Actions.RunAll
        autoStepButton.toolTipText = "Auto Step - automatically step through at configured speed"
    }

    private fun scheduleNextAutoStep() {
        autoStepTimer?.cancel()

        val settings = TemplaterSettings.getInstance()
        val delayMs = settings.stepDelayMilliseconds.toLong()

        autoStepTimer = Timer("AutoStep", true)
        autoStepTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (isAutoStepping) {
                    ApplicationManager.getApplication().invokeLater {
                        debugService.getCurrentSession()?.stepInto()
                    }
                }
            }
        }, delayMs)
    }

    private fun updateUIForSessionStart(session: ActiveDebugSession) {
        statusLabel.text = "Debugging: ${session.file.name}"
        stopButton.isEnabled = true
        currentSessionFile = session.file
    }

    private fun updateUIForSessionEnd() {
        statusLabel.text = "No debug session active"
        currentStepLabel.text = ""
        setControlsEnabled(false)
        callStackModel.clear()


        lineHighlighter.clearHighlight()
        currentSessionFile = null
    }

    private fun updateUIForPause(breakpoint: DebugBreakpoint) {
        val step = breakpoint.step


        val lineNumber = step.displayLineNumber

        currentStepLabel.text = "${step.description} (Line ${lineNumber ?: "?"})"
        setControlsEnabled(true)


        val file = currentSessionFile
        if (file != null && lineNumber != null) {
            lineHighlighter.highlightLine(file, lineNumber)
        }


        callStackModel.clear()
        breakpoint.trace.allSteps.forEach { traceStep ->
            val prefix = when (traceStep.type) {
                ExecutionStep.StepType.TEMPLATE_START -> "? "
                ExecutionStep.StepType.TEMPLATE_END -> "? "
                ExecutionStep.StepType.BLOCK_START -> "? "
                ExecutionStep.StepType.BLOCK_END -> "? "
                ExecutionStep.StepType.ERROR -> "? "
                else -> "  "
            }
            val marker = if (traceStep.id == step.id) " ? CURRENT" else ""
            callStackModel.addElement("$prefix${traceStep.description}$marker")
        }


        val currentIndex = breakpoint.trace.allSteps.indexOfFirst { it.id == step.id }
        if (currentIndex >= 0) {
            callStackList.ensureIndexIsVisible(currentIndex)
            callStackList.selectedIndex = currentIndex
        }
    }

    private fun updateUIForResume() {
        setControlsEnabled(false)
        currentStepLabel.text = "Running..."


        lineHighlighter.clearHighlight()
    }

    private fun setControlsEnabled(enabled: Boolean) {
        continueButton.isEnabled = enabled
        stepIntoButton.isEnabled = enabled
        stepOverButton.isEnabled = enabled
        stepOutButton.isEnabled = enabled
        autoStepButton.isEnabled = enabled || isAutoStepping
        stopButton.isEnabled = debugService.isDebugging()
    }
}
