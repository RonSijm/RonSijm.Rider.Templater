package ronsijm.templater.standalone.ui.settings

import ronsijm.templater.standalone.settings.AppSettings
import java.awt.FlowLayout
import javax.swing.*

class AfterRunningConfigurable : Configurable {

    override val displayName = "After Running"

    private lateinit var panel: JPanel
    private lateinit var afterRunningBehaviorCombo: JComboBox<String>
    private lateinit var sideBySidePostfixField: JTextField
    private lateinit var showDialogAfterRunCheckbox: JCheckBox

    override fun createComponent(): JComponent {
        panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)


        afterRunningBehaviorCombo = JComboBox(arrayOf("Overwrite Automatically", "Save Side-by-Side"))
        val behaviorPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        behaviorPanel.add(JLabel("After running:"))
        behaviorPanel.add(afterRunningBehaviorCombo)
        panel.add(behaviorPanel)


        sideBySidePostfixField = JTextField(10)
        val postfixPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        postfixPanel.add(JLabel("Side-by-side postfix:"))
        postfixPanel.add(sideBySidePostfixField)
        postfixPanel.add(JLabel("(e.g., .output, .result)"))
        panel.add(postfixPanel)


        afterRunningBehaviorCombo.addActionListener {
            sideBySidePostfixField.isEnabled = afterRunningBehaviorCombo.selectedIndex == 1
        }


        showDialogAfterRunCheckbox = JCheckBox("Show dialog after run")
        val dialogPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        dialogPanel.add(showDialogAfterRunCheckbox)
        panel.add(dialogPanel)

        panel.add(Box.createVerticalGlue())

        return panel
    }

    override fun reset() {
        afterRunningBehaviorCombo.selectedIndex = when (AppSettings.getAfterRunningBehavior()) {
            AppSettings.AfterRunningBehavior.OVERWRITE_AUTOMATICALLY -> 0
            AppSettings.AfterRunningBehavior.SAVE_SIDE_BY_SIDE -> 1
        }
        sideBySidePostfixField.text = AppSettings.getSideBySidePostfix()
        sideBySidePostfixField.isEnabled = afterRunningBehaviorCombo.selectedIndex == 1
        showDialogAfterRunCheckbox.isSelected = AppSettings.getShowDialogAfterRun()
    }

    override fun apply() {
        val behavior = when (afterRunningBehaviorCombo.selectedIndex) {
            0 -> AppSettings.AfterRunningBehavior.OVERWRITE_AUTOMATICALLY
            1 -> AppSettings.AfterRunningBehavior.SAVE_SIDE_BY_SIDE
            else -> AppSettings.AfterRunningBehavior.OVERWRITE_AUTOMATICALLY
        }
        AppSettings.setAfterRunningBehavior(behavior)
        AppSettings.setSideBySidePostfix(sideBySidePostfixField.text)
        AppSettings.setShowDialogAfterRun(showDialogAfterRunCheckbox.isSelected)
    }

    override fun isModified(): Boolean {
        val currentBehavior = when (afterRunningBehaviorCombo.selectedIndex) {
            0 -> AppSettings.AfterRunningBehavior.OVERWRITE_AUTOMATICALLY
            1 -> AppSettings.AfterRunningBehavior.SAVE_SIDE_BY_SIDE
            else -> AppSettings.AfterRunningBehavior.OVERWRITE_AUTOMATICALLY
        }

        return currentBehavior != AppSettings.getAfterRunningBehavior() ||
               sideBySidePostfixField.text != AppSettings.getSideBySidePostfix() ||
               showDialogAfterRunCheckbox.isSelected != AppSettings.getShowDialogAfterRun()
    }
}

