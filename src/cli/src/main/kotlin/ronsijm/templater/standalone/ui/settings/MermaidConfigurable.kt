package ronsijm.templater.standalone.ui.settings

import ronsijm.templater.settings.MermaidOutputLocation
import ronsijm.templater.standalone.settings.AppSettings
import ronsijm.templater.ui.settings.MermaidDiagramType
import java.awt.FlowLayout
import javax.swing.*

class MermaidConfigurable : Configurable {

    override val displayName = "Mermaid"

    private lateinit var panel: JPanel
    private lateinit var enableMermaidExportCheckbox: JCheckBox
    private lateinit var includeMermaidExplanationCheckbox: JCheckBox
    private lateinit var mermaidDiagramTypeCombo: JComboBox<MermaidDiagramType>
    private lateinit var mermaidOutputLocationCombo: JComboBox<MermaidOutputLocation>
    private lateinit var mermaidOutputFolderField: JTextField
    private lateinit var debugIncrementalUpdatesCheckbox: JCheckBox

    override fun createComponent(): JComponent {
        panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        panel.add(createSectionLabel("Mermaid Export"))

        enableMermaidExportCheckbox = JCheckBox("Enable Mermaid export")
        includeMermaidExplanationCheckbox = JCheckBox("Include explanations in Mermaid output")
        panel.add(enableMermaidExportCheckbox)
        panel.add(includeMermaidExplanationCheckbox)

        panel.add(Box.createVerticalStrut(10))


        mermaidDiagramTypeCombo = JComboBox(MermaidDiagramType.entries.toTypedArray())
        val typePanel = JPanel(FlowLayout(FlowLayout.LEFT))
        typePanel.add(JLabel("Diagram type:"))
        typePanel.add(mermaidDiagramTypeCombo)
        panel.add(typePanel)


        mermaidOutputLocationCombo = JComboBox(MermaidOutputLocation.entries.toTypedArray())
        val locationPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        locationPanel.add(JLabel("Output location:"))
        locationPanel.add(mermaidOutputLocationCombo)
        panel.add(locationPanel)


        mermaidOutputFolderField = JTextField(20)
        val folderPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        folderPanel.add(JLabel("Custom folder:"))
        folderPanel.add(mermaidOutputFolderField)
        panel.add(folderPanel)


        panel.add(Box.createVerticalStrut(10))
        panel.add(createSectionLabel("Debug"))

        debugIncrementalUpdatesCheckbox = JCheckBox("Enable incremental debug updates")
        panel.add(debugIncrementalUpdatesCheckbox)

        panel.add(Box.createVerticalGlue())

        return panel
    }

    override fun reset() {
        enableMermaidExportCheckbox.isSelected = AppSettings.enableMermaidExport
        includeMermaidExplanationCheckbox.isSelected = AppSettings.includeMermaidExplanation
        mermaidDiagramTypeCombo.selectedItem = AppSettings.mermaidDiagramType
        mermaidOutputLocationCombo.selectedItem = AppSettings.mermaidOutputLocation
        mermaidOutputFolderField.text = AppSettings.mermaidOutputFolder
        debugIncrementalUpdatesCheckbox.isSelected = AppSettings.debugIncrementalUpdates
    }

    override fun apply() {
        AppSettings.enableMermaidExport = enableMermaidExportCheckbox.isSelected
        AppSettings.includeMermaidExplanation = includeMermaidExplanationCheckbox.isSelected
        AppSettings.mermaidDiagramType = mermaidDiagramTypeCombo.selectedItem as MermaidDiagramType
        AppSettings.mermaidOutputLocation = mermaidOutputLocationCombo.selectedItem as MermaidOutputLocation
        AppSettings.mermaidOutputFolder = mermaidOutputFolderField.text
        AppSettings.debugIncrementalUpdates = debugIncrementalUpdatesCheckbox.isSelected
    }

    override fun isModified(): Boolean {
        return enableMermaidExportCheckbox.isSelected != AppSettings.enableMermaidExport ||
               includeMermaidExplanationCheckbox.isSelected != AppSettings.includeMermaidExplanation ||
               mermaidDiagramTypeCombo.selectedItem != AppSettings.mermaidDiagramType ||
               mermaidOutputLocationCombo.selectedItem != AppSettings.mermaidOutputLocation ||
               mermaidOutputFolderField.text != AppSettings.mermaidOutputFolder ||
               debugIncrementalUpdatesCheckbox.isSelected != AppSettings.debugIncrementalUpdates
    }

    private fun createSectionLabel(text: String): JLabel {
        val label = JLabel(text)
        label.font = label.font.deriveFont(java.awt.Font.BOLD)
        label.border = BorderFactory.createEmptyBorder(5, 0, 5, 0)
        return label
    }
}

