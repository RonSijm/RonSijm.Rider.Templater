package ronsijm.templater.standalone.ui.settings

import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.settings.PopupBehavior
import ronsijm.templater.standalone.settings.AppSettings
import java.awt.FlowLayout
import javax.swing.*

class ExecutionConfigurable : Configurable {

    override val displayName = "Execution"

    private lateinit var panel: JPanel
    private lateinit var enableParallelExecutionCheckbox: JCheckBox
    private lateinit var enableSyntaxValidationCheckbox: JCheckBox
    private lateinit var enableSelectionOnlyCheckbox: JCheckBox
    private lateinit var showExecutionStatsCheckbox: JCheckBox
    private lateinit var enableProfilingCheckbox: JCheckBox
    private lateinit var cancelBehaviorCombo: JComboBox<CancelBehavior>
    private lateinit var popupBehaviorHotkeyCombo: JComboBox<PopupBehavior>
    private lateinit var popupBehaviorGutterCombo: JComboBox<PopupBehavior>

    override fun createComponent(): JComponent {
        panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        panel.add(createSectionLabel("Execution Options"))

        enableParallelExecutionCheckbox = JCheckBox("Enable parallel execution")
        enableSyntaxValidationCheckbox = JCheckBox("Enable syntax validation")
        enableSelectionOnlyCheckbox = JCheckBox("Execute selection only when available")
        showExecutionStatsCheckbox = JCheckBox("Show execution statistics")
        enableProfilingCheckbox = JCheckBox("Enable performance profiling")

        panel.add(enableParallelExecutionCheckbox)
        panel.add(enableSyntaxValidationCheckbox)
        panel.add(enableSelectionOnlyCheckbox)
        panel.add(showExecutionStatsCheckbox)
        panel.add(enableProfilingCheckbox)


        panel.add(Box.createVerticalStrut(10))
        panel.add(createSectionLabel("Cancel Behavior"))

        cancelBehaviorCombo = JComboBox(CancelBehavior.entries.toTypedArray())
        val cancelPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        cancelPanel.add(JLabel("When cancelled:"))
        cancelPanel.add(cancelBehaviorCombo)
        panel.add(cancelPanel)


        panel.add(Box.createVerticalStrut(10))
        panel.add(createSectionLabel("Popup Behavior"))

        popupBehaviorHotkeyCombo = JComboBox(PopupBehavior.entries.toTypedArray())
        val hotkeyPopupPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        hotkeyPopupPanel.add(JLabel("On hotkey execution:"))
        hotkeyPopupPanel.add(popupBehaviorHotkeyCombo)
        panel.add(hotkeyPopupPanel)

        popupBehaviorGutterCombo = JComboBox(PopupBehavior.entries.toTypedArray())
        val gutterPopupPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        gutterPopupPanel.add(JLabel("On gutter execution:"))
        gutterPopupPanel.add(popupBehaviorGutterCombo)
        panel.add(gutterPopupPanel)

        panel.add(Box.createVerticalGlue())

        return panel
    }

    override fun reset() {
        enableParallelExecutionCheckbox.isSelected = AppSettings.enableParallelExecution
        enableSyntaxValidationCheckbox.isSelected = AppSettings.enableSyntaxValidation
        enableSelectionOnlyCheckbox.isSelected = AppSettings.enableSelectionOnlyExecution
        showExecutionStatsCheckbox.isSelected = AppSettings.showExecutionStats
        enableProfilingCheckbox.isSelected = AppSettings.enablePerformanceProfiling
        cancelBehaviorCombo.selectedItem = AppSettings.cancelBehavior
        popupBehaviorHotkeyCombo.selectedItem = AppSettings.popupBehaviorHotkey
        popupBehaviorGutterCombo.selectedItem = AppSettings.popupBehaviorGutter
    }

    override fun apply() {
        AppSettings.enableParallelExecution = enableParallelExecutionCheckbox.isSelected
        AppSettings.enableSyntaxValidation = enableSyntaxValidationCheckbox.isSelected
        AppSettings.enableSelectionOnlyExecution = enableSelectionOnlyCheckbox.isSelected
        AppSettings.showExecutionStats = showExecutionStatsCheckbox.isSelected
        AppSettings.enablePerformanceProfiling = enableProfilingCheckbox.isSelected
        AppSettings.cancelBehavior = cancelBehaviorCombo.selectedItem as CancelBehavior
        AppSettings.popupBehaviorHotkey = popupBehaviorHotkeyCombo.selectedItem as PopupBehavior
        AppSettings.popupBehaviorGutter = popupBehaviorGutterCombo.selectedItem as PopupBehavior
    }

    override fun isModified(): Boolean {
        return enableParallelExecutionCheckbox.isSelected != AppSettings.enableParallelExecution ||
               enableSyntaxValidationCheckbox.isSelected != AppSettings.enableSyntaxValidation ||
               enableSelectionOnlyCheckbox.isSelected != AppSettings.enableSelectionOnlyExecution ||
               showExecutionStatsCheckbox.isSelected != AppSettings.showExecutionStats ||
               enableProfilingCheckbox.isSelected != AppSettings.enablePerformanceProfiling ||
               cancelBehaviorCombo.selectedItem != AppSettings.cancelBehavior ||
               popupBehaviorHotkeyCombo.selectedItem != AppSettings.popupBehaviorHotkey ||
               popupBehaviorGutterCombo.selectedItem != AppSettings.popupBehaviorGutter
    }

    private fun createSectionLabel(text: String): JLabel {
        val label = JLabel(text)
        label.font = label.font.deriveFont(java.awt.Font.BOLD)
        label.border = BorderFactory.createEmptyBorder(5, 0, 5, 0)
        return label
    }
}

