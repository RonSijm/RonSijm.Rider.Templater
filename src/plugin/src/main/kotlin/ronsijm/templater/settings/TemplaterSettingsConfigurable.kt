package ronsijm.templater.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBScrollPane
import javax.swing.*
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets


class TemplaterSettingsConfigurable : Configurable {

    private var tabbedPane: JTabbedPane? = null
    private var parallelExecutionCheckbox: JCheckBox? = null
    private var syntaxValidationCheckbox: JCheckBox? = null
    private var selectionOnlyCheckbox: JCheckBox? = null
    private var cancelBehaviorComboBox: JComboBox<String>? = null

    private var popupBehaviorHotkeyComboBox: JComboBox<String>? = null
    private var popupBehaviorGutterComboBox: JComboBox<String>? = null
    private var showStatsCheckbox: JCheckBox? = null
    private var enableProfilingCheckbox: JCheckBox? = null


    private var debugIncrementalUpdatesCheckbox: JCheckBox? = null


    private var mermaidPanel: MermaidSettingsPanel? = null

    override fun getDisplayName(): String = "Templater"

    override fun createComponent(): JComponent {
        tabbedPane = JTabbedPane()


        val generalPanel = createGeneralPanel()
        tabbedPane!!.addTab("General", JBScrollPane(generalPanel))


        mermaidPanel = MermaidSettingsPanel()
        tabbedPane!!.addTab("Mermaid Diagrams", JBScrollPane(mermaidPanel!!.createPanel()))

        reset()
        mermaidPanel?.updateFieldsEnabled()
        return tabbedPane!!
    }

    private fun createGeneralPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
            gridx = 0
            weightx = 1.0
        }

        var row = 0

        gbc.gridy = row++
        val experimentalLabel = JLabel("<html><b>Experimental Features</b></html>")
        panel.add(experimentalLabel, gbc)

        gbc.gridy = row++
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

        gbc.gridy = row++
        gbc.insets = Insets(0, 40, 10, 5)
        val parallelDesc = JLabel("<html><font color='gray' size='-1'>Analyzes dependencies between blocks and runs independent blocks concurrently</font></html>")
        panel.add(parallelDesc, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(15, 5, 5, 5)
        val generalLabel = JLabel("<html><b>General</b></html>")
        panel.add(generalLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(5, 20, 5, 5)
        syntaxValidationCheckbox = JCheckBox("Enable syntax validation").apply {
            toolTipText = "Show warnings for malformed template syntax before execution"
        }
        panel.add(syntaxValidationCheckbox, gbc)

        gbc.gridy = row++
        selectionOnlyCheckbox = JCheckBox("Execute current selection only").apply {
            toolTipText = "When text is selected, only execute templates in the selection instead of the entire document"
        }
        panel.add(selectionOnlyCheckbox, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(10, 20, 5, 5)
        val cancelLabel = JLabel("When dialog is cancelled:")
        panel.add(cancelLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(0, 20, 5, 5)
        val cancelOptions = arrayOf("Remove expression (replace with empty)", "Keep original expression")
        cancelBehaviorComboBox = JComboBox(cancelOptions).apply {
            toolTipText = "What to do when user cancels a prompt or suggester dialog"
        }
        panel.add(cancelBehaviorComboBox, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(15, 5, 5, 5)
        val popupLabel = JLabel("<html><b>Popup</b></html>")
        panel.add(popupLabel, gbc)

        val popupOptions = arrayOf("Always", "Only on error", "Never")

        gbc.gridy = row++
        gbc.insets = Insets(5, 20, 5, 5)
        val popupHotkeyLabel = JLabel("Show popup after execution from hotkey:")
        panel.add(popupHotkeyLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(0, 20, 5, 5)
        popupBehaviorHotkeyComboBox = JComboBox(popupOptions).apply {
            toolTipText = "Control when to show the popup notification after executing via keyboard shortcut (Alt+R / Alt+Shift+R)"
        }
        panel.add(popupBehaviorHotkeyComboBox, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(10, 20, 5, 5)
        val popupGutterLabel = JLabel("Show popup after execution from gutter:")
        panel.add(popupGutterLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(0, 20, 5, 5)
        popupBehaviorGutterComboBox = JComboBox(popupOptions).apply {
            toolTipText = "Control when to show the popup notification after executing via gutter run button"
        }
        panel.add(popupBehaviorGutterComboBox, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(10, 20, 5, 5)
        showStatsCheckbox = JCheckBox("Show execution statistics").apply {
            toolTipText = "Display timing and parallelization info in the notification after execution"
        }
        panel.add(showStatsCheckbox, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(15, 5, 5, 5)
        val performanceLabel = JLabel("<html><b>Performance</b></html>")
        panel.add(performanceLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(5, 20, 5, 5)
        enableProfilingCheckbox = JCheckBox("Enable performance profiling (for debugging)").apply {
            toolTipText = """
                <html>
                When enabled, detailed timing information is collected for script execution.<br>
                <br>
                <b>Warning:</b> This adds overhead and should only be enabled when debugging performance issues.
                </html>
            """.trimIndent()
        }
        panel.add(enableProfilingCheckbox, gbc)


        gbc.gridy = row++
        gbc.insets = Insets(15, 5, 5, 5)
        val debugLabel = JLabel("<html><b>Debugging</b></html>")
        panel.add(debugLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(5, 20, 5, 5)
        debugIncrementalUpdatesCheckbox = JCheckBox("Update document incrementally during debugging").apply {
            toolTipText = """
                <html>
                When enabled, the document is updated after each template block is processed.<br>
                This allows you to see the results as you step through the template.<br>
                <br>
                Disable this to only update the document when debugging completes.
                </html>
            """.trimIndent()
        }
        panel.add(debugIncrementalUpdatesCheckbox, gbc)

        gbc.gridy = row
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(JPanel(), gbc)

        return panel
    }

    @Suppress("ComplexCondition")
    override fun isModified(): Boolean {
        val settings = TemplaterSettings.getInstance()


        if (parallelExecutionCheckbox?.isSelected != settings.enableParallelExecution ||
            syntaxValidationCheckbox?.isSelected != settings.enableSyntaxValidation ||
            selectionOnlyCheckbox?.isSelected != settings.enableSelectionOnlyExecution ||
            getSelectedCancelBehavior() != settings.cancelBehavior ||
            getSelectedPopupBehaviorHotkey() != settings.popupBehaviorHotkey ||
            getSelectedPopupBehaviorGutter() != settings.popupBehaviorGutter ||
            showStatsCheckbox?.isSelected != settings.showExecutionStats ||
            enableProfilingCheckbox?.isSelected != settings.enablePerformanceProfiling ||
            debugIncrementalUpdatesCheckbox?.isSelected != settings.debugIncrementalUpdates) {
            return true
        }


        return mermaidPanel?.isModified(settings) ?: false
    }

    override fun apply() {
        val settings = TemplaterSettings.getInstance()
        settings.enableParallelExecution = parallelExecutionCheckbox?.isSelected ?: false
        settings.enableSyntaxValidation = syntaxValidationCheckbox?.isSelected ?: true
        settings.enableSelectionOnlyExecution = selectionOnlyCheckbox?.isSelected ?: true
        settings.cancelBehavior = getSelectedCancelBehavior()
        settings.popupBehaviorHotkey = getSelectedPopupBehaviorHotkey()
        settings.popupBehaviorGutter = getSelectedPopupBehaviorGutter()
        settings.showExecutionStats = showStatsCheckbox?.isSelected ?: false
        settings.enablePerformanceProfiling = enableProfilingCheckbox?.isSelected ?: false
        settings.debugIncrementalUpdates = debugIncrementalUpdatesCheckbox?.isSelected ?: true


        mermaidPanel?.apply(settings)
    }

    override fun reset() {
        val settings = TemplaterSettings.getInstance()
        parallelExecutionCheckbox?.isSelected = settings.enableParallelExecution
        syntaxValidationCheckbox?.isSelected = settings.enableSyntaxValidation
        selectionOnlyCheckbox?.isSelected = settings.enableSelectionOnlyExecution
        cancelBehaviorComboBox?.selectedIndex = settings.cancelBehavior.ordinal
        popupBehaviorHotkeyComboBox?.selectedIndex = settings.popupBehaviorHotkey.ordinal
        popupBehaviorGutterComboBox?.selectedIndex = settings.popupBehaviorGutter.ordinal
        showStatsCheckbox?.isSelected = settings.showExecutionStats
        enableProfilingCheckbox?.isSelected = settings.enablePerformanceProfiling
        debugIncrementalUpdatesCheckbox?.isSelected = settings.debugIncrementalUpdates


        mermaidPanel?.reset(settings)
    }

    private fun getSelectedPopupBehaviorHotkey(): PopupBehavior {
        return when (popupBehaviorHotkeyComboBox?.selectedIndex) {
            0 -> PopupBehavior.ALWAYS
            1 -> PopupBehavior.ONLY_ON_ERROR
            2 -> PopupBehavior.NEVER
            else -> PopupBehavior.ALWAYS
        }
    }

    private fun getSelectedPopupBehaviorGutter(): PopupBehavior {
        return when (popupBehaviorGutterComboBox?.selectedIndex) {
            0 -> PopupBehavior.ALWAYS
            1 -> PopupBehavior.ONLY_ON_ERROR
            2 -> PopupBehavior.NEVER
            else -> PopupBehavior.ONLY_ON_ERROR
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
        tabbedPane = null
        parallelExecutionCheckbox = null
        syntaxValidationCheckbox = null
        selectionOnlyCheckbox = null
        cancelBehaviorComboBox = null
        popupBehaviorHotkeyComboBox = null
        popupBehaviorGutterComboBox = null
        showStatsCheckbox = null
        enableProfilingCheckbox = null
        debugIncrementalUpdatesCheckbox = null
        mermaidPanel?.dispose()
        mermaidPanel = null
    }
}
