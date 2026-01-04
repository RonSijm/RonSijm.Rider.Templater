package ronsijm.templater.debug

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class TemplaterVariablesToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = TemplaterVariablesPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

class TemplaterVariablesPanel(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val statusLabel = JBLabel("No debug session active")
    private val variablesTableModel = DefaultTableModel(arrayOf("Variable", "Value"), 0)
    private val variablesTable = JBTable(variablesTableModel)

    private val debugService = TemplaterDebugService.getInstance(project)

    init {
        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val statusPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5)
            add(statusLabel, BorderLayout.CENTER)
        }

        variablesTable.setDefaultEditor(Object::class.java, javax.swing.DefaultCellEditor(javax.swing.JTextField()))

        variablesTableModel.addTableModelListener { e ->
            if (e.type == javax.swing.event.TableModelEvent.UPDATE) {
                val row = e.firstRow
                if (row >= 0 && row < variablesTableModel.rowCount) {
                    val varName = variablesTableModel.getValueAt(row, 0) as String
                    val newValue = variablesTableModel.getValueAt(row, 1) as String

                    val session = debugService.getCurrentSession()
                    if (session != null) {
                        val success = session.updateVariable(varName, newValue)
                        if (!success) {
                            javax.swing.SwingUtilities.invokeLater {
                                javax.swing.JOptionPane.showMessageDialog(
                                    variablesTable,
                                    "Failed to update variable '$varName'. Invalid value: $newValue",
                                    "Variable Update Failed",
                                    javax.swing.JOptionPane.ERROR_MESSAGE
                                )
                            }
                        }
                    }
                }
            }
        }

        val tablePanel = JPanel(BorderLayout()).apply {
            add(JBScrollPane(variablesTable), BorderLayout.CENTER)
        }

        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(statusPanel, BorderLayout.NORTH)
        contentPanel.add(tablePanel, BorderLayout.CENTER)

        setContent(contentPanel)
    }

    private fun setupListeners() {

        debugService.addListener(object : DebugEventListener {
            override fun onSessionStarted(session: ActiveDebugSession) {

                session.addStateListener(object : SessionStateListener {
                    override fun onPaused(breakpoint: DebugBreakpoint) {
                        ApplicationManager.getApplication().invokeLater {
                            updateVariables(breakpoint)
                        }
                    }

                    override fun onResumed(action: DebugAction) {
                        ApplicationManager.getApplication().invokeLater {
                            clearVariables()
                        }
                    }
                })

                ApplicationManager.getApplication().invokeLater {
                    statusLabel.text = "Debugging: ${session.file.name}"
                }
            }

            override fun onSessionEnded(session: ActiveDebugSession) {
                ApplicationManager.getApplication().invokeLater {
                    statusLabel.text = "No debug session active"
                    variablesTableModel.rowCount = 0
                }
            }
        })
    }

    private fun updateVariables(breakpoint: DebugBreakpoint) {
        val step = breakpoint.step
        val lineNumber = step.displayLineNumber

        statusLabel.text = "Paused at line ${lineNumber ?: "?"}"


        variablesTableModel.rowCount = 0
        step.variables.forEach { (name, value) ->
            variablesTableModel.addRow(arrayOf(name, value?.toString() ?: "null"))
        }
    }

    private fun clearVariables() {
        statusLabel.text = "Running..."

    }
}

