package ronsijm.templater.standalone.ui

import ronsijm.templater.debug.DebugBreakpoint
import ronsijm.templater.standalone.ui.util.ScrollPaneWrapper
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel

class VariablesPanel : JPanel(BorderLayout()) {

    private val statusLabel = JLabel("No debug session active")
    private val variablesTableModel = DefaultTableModel(arrayOf("Variable", "Value"), 0)
    private val variablesTable = JTable(variablesTableModel)


    private var variableUpdater: ((String, String) -> Boolean)? = null

    init {
        setupUI()
    }

    private fun setupUI() {
        val statusPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            add(statusLabel, BorderLayout.CENTER)
        }


        variablesTable.setDefaultEditor(
            Object::class.java,
            javax.swing.DefaultCellEditor(javax.swing.JTextField())
        )


        variablesTable.model.addTableModelListener { event ->
            if (event.type == javax.swing.event.TableModelEvent.UPDATE) {
                val row = event.firstRow
                if (row >= 0 && row < variablesTableModel.rowCount) {
                    val varName = variablesTableModel.getValueAt(row, 0) as String
                    val newValue = variablesTableModel.getValueAt(row, 1) as String
                    onVariableEdited(varName, newValue)
                }
            }
        }

        val tablePanel = JPanel(BorderLayout()).apply {

            add(ScrollPaneWrapper.wrap(JScrollPane(variablesTable)), BorderLayout.CENTER)
        }

        val contentPanel = JPanel(BorderLayout())
        contentPanel.add(statusPanel, BorderLayout.NORTH)
        contentPanel.add(tablePanel, BorderLayout.CENTER)

        add(contentPanel, BorderLayout.CENTER)
    }


    fun updateVariables(breakpoint: DebugBreakpoint) {
        val step = breakpoint.step
        val lineNumber = step.displayLineNumber

        statusLabel.text = "Paused at line ${lineNumber ?: "?"}"


        variablesTableModel.rowCount = 0
        step.variables.forEach { (name, value) ->
            variablesTableModel.addRow(arrayOf(name, value?.toString() ?: "null"))
        }
    }


    fun clearVariables() {
        statusLabel.text = "Running..."

    }


    fun reset() {
        statusLabel.text = "No debug session active"
        variablesTableModel.rowCount = 0
    }


    private fun onVariableEdited(name: String, newValue: String) {
        val updater = variableUpdater
        if (updater != null) {
            val success = updater(name, newValue)
            if (!success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to update variable '$name'",
                    "Variable Update Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }


    fun setVariableUpdater(updater: ((String, String) -> Boolean)?) {
        this.variableUpdater = updater
    }
}

