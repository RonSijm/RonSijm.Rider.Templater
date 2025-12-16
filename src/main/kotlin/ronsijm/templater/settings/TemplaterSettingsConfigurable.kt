package ronsijm.templater.settings

import com.intellij.openapi.options.Configurable
import javax.swing.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets

/**
 * Settings UI for Templater plugin
 * Accessible via Settings > Tools > Templater
 */
class TemplaterSettingsConfigurable : Configurable {

    private var settingsPanel: JPanel? = null
    private var parallelExecutionCheckbox: JCheckBox? = null
    private var syntaxValidationCheckbox: JCheckBox? = null
    private var showStatsCheckbox: JCheckBox? = null
    private var selectionOnlyCheckbox: JCheckBox? = null
    private var popupBehaviorComboBox: JComboBox<String>? = null
    private var cancelBehaviorComboBox: JComboBox<String>? = null

    override fun getDisplayName(): String = "Templater"

    override fun createComponent(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
            gridx = 0
            weightx = 1.0
        }

        // Experimental section header
        gbc.gridy = 0
        val experimentalLabel = JLabel("<html><b>Experimental Features</b></html>")
        panel.add(experimentalLabel, gbc)

        // Parallel execution checkbox
        gbc.gridy = 1
        gbc.insets = Insets(5, 20, 5, 5)
        parallelExecutionCheckbox = JCheckBox("Enable parallel execution (experimental)").apply {
            toolTipText = """
                <html>
                When enabled, independent template blocks will be executed concurrently.<br>
                This can speed up templates with many independent operations.<br>
                <br>
                <b>Note:</b> This is experimental. Disable if you experience issues.
                </html>
            """.trimIndent()
        }
        panel.add(parallelExecutionCheckbox, gbc)

        // Parallel execution description
        gbc.gridy = 2
        gbc.insets = Insets(0, 40, 10, 5)
        val parallelDesc = JLabel("<html><font color='gray' size='-1'>Analyzes dependencies between blocks and runs independent blocks concurrently</font></html>")
        panel.add(parallelDesc, gbc)

        // General section header
        gbc.gridy = 3
        gbc.insets = Insets(15, 5, 5, 5)
        val generalLabel = JLabel("<html><b>General</b></html>")
        panel.add(generalLabel, gbc)

        // Syntax validation checkbox
        gbc.gridy = 4
        gbc.insets = Insets(5, 20, 5, 5)
        syntaxValidationCheckbox = JCheckBox("Enable syntax validation").apply {
            toolTipText = "Show warnings for malformed template syntax before execution"
        }
        panel.add(syntaxValidationCheckbox, gbc)

        // Show stats checkbox
        gbc.gridy = 5
        showStatsCheckbox = JCheckBox("Show execution statistics").apply {
            toolTipText = "Display timing and parallelization info in the notification after execution"
        }
        panel.add(showStatsCheckbox, gbc)

        // Selection only checkbox
        gbc.gridy = 6
        selectionOnlyCheckbox = JCheckBox("Execute current selection only").apply {
            toolTipText = "When text is selected, only execute templates in the selection instead of the entire document"
        }
        panel.add(selectionOnlyCheckbox, gbc)

        // Popup behavior label
        gbc.gridy = 7
        gbc.insets = Insets(10, 20, 5, 5)
        val popupLabel = JLabel("Show popup after execution:")
        panel.add(popupLabel, gbc)

        // Popup behavior combo box
        gbc.gridy = 8
        gbc.insets = Insets(0, 20, 5, 5)
        val popupOptions = arrayOf("Always", "Only on error", "Never")
        popupBehaviorComboBox = JComboBox(popupOptions).apply {
            toolTipText = "Control when to show the popup notification after template execution"
        }
        panel.add(popupBehaviorComboBox, gbc)

        // Cancel behavior label
        gbc.gridy = 9
        gbc.insets = Insets(10, 20, 5, 5)
        val cancelLabel = JLabel("When dialog is cancelled:")
        panel.add(cancelLabel, gbc)

        // Cancel behavior combo box
        gbc.gridy = 10
        gbc.insets = Insets(0, 20, 5, 5)
        val cancelOptions = arrayOf("Remove expression (replace with empty)", "Keep original expression")
        cancelBehaviorComboBox = JComboBox(cancelOptions).apply {
            toolTipText = "What to do when user cancels a prompt or suggester dialog"
        }
        panel.add(cancelBehaviorComboBox, gbc)

        // Spacer to push everything to the top
        gbc.gridy = 11
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(JPanel(), gbc)

        settingsPanel = panel
        reset()
        return panel
    }

    override fun isModified(): Boolean {
        val settings = TemplaterSettings.getInstance()
        return parallelExecutionCheckbox?.isSelected != settings.enableParallelExecution ||
               syntaxValidationCheckbox?.isSelected != settings.enableSyntaxValidation ||
               showStatsCheckbox?.isSelected != settings.showExecutionStats ||
               selectionOnlyCheckbox?.isSelected != settings.enableSelectionOnlyExecution ||
               getSelectedPopupBehavior() != settings.popupBehavior ||
               getSelectedCancelBehavior() != settings.cancelBehavior
    }

    override fun apply() {
        val settings = TemplaterSettings.getInstance()
        settings.enableParallelExecution = parallelExecutionCheckbox?.isSelected ?: false
        settings.enableSyntaxValidation = syntaxValidationCheckbox?.isSelected ?: true
        settings.showExecutionStats = showStatsCheckbox?.isSelected ?: false
        settings.enableSelectionOnlyExecution = selectionOnlyCheckbox?.isSelected ?: true
        settings.popupBehavior = getSelectedPopupBehavior()
        settings.cancelBehavior = getSelectedCancelBehavior()
    }

    override fun reset() {
        val settings = TemplaterSettings.getInstance()
        parallelExecutionCheckbox?.isSelected = settings.enableParallelExecution
        syntaxValidationCheckbox?.isSelected = settings.enableSyntaxValidation
        showStatsCheckbox?.isSelected = settings.showExecutionStats
        selectionOnlyCheckbox?.isSelected = settings.enableSelectionOnlyExecution
        popupBehaviorComboBox?.selectedIndex = settings.popupBehavior.ordinal
        cancelBehaviorComboBox?.selectedIndex = settings.cancelBehavior.ordinal
    }

    private fun getSelectedPopupBehavior(): PopupBehavior {
        return when (popupBehaviorComboBox?.selectedIndex) {
            0 -> PopupBehavior.ALWAYS
            1 -> PopupBehavior.ONLY_ON_ERROR
            2 -> PopupBehavior.NEVER
            else -> PopupBehavior.ALWAYS
        }
    }

    private fun getSelectedCancelBehavior(): CancelBehavior {
        return when (cancelBehaviorComboBox?.selectedIndex) {
            0 -> CancelBehavior.REMOVE_EXPRESSION
            1 -> CancelBehavior.KEEP_EXPRESSION
            else -> CancelBehavior.REMOVE_EXPRESSION
        }
    }

    override fun disposeUIResources() {
        settingsPanel = null
        parallelExecutionCheckbox = null
        syntaxValidationCheckbox = null
        showStatsCheckbox = null
        selectionOnlyCheckbox = null
        popupBehaviorComboBox = null
        cancelBehaviorComboBox = null
    }
}

