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

        // Spacer to push everything to the top
        gbc.gridy = 6
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
               showStatsCheckbox?.isSelected != settings.showExecutionStats
    }

    override fun apply() {
        val settings = TemplaterSettings.getInstance()
        settings.enableParallelExecution = parallelExecutionCheckbox?.isSelected ?: false
        settings.enableSyntaxValidation = syntaxValidationCheckbox?.isSelected ?: true
        settings.showExecutionStats = showStatsCheckbox?.isSelected ?: false
    }

    override fun reset() {
        val settings = TemplaterSettings.getInstance()
        parallelExecutionCheckbox?.isSelected = settings.enableParallelExecution
        syntaxValidationCheckbox?.isSelected = settings.enableSyntaxValidation
        showStatsCheckbox?.isSelected = settings.showExecutionStats
    }

    override fun disposeUIResources() {
        settingsPanel = null
        parallelExecutionCheckbox = null
        syntaxValidationCheckbox = null
        showStatsCheckbox = null
    }
}

