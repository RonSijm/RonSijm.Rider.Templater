package ronsijm.templater.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*


class MermaidSettingsPanel {

    var enableMermaidExportCheckbox: JCheckBox? = null
        private set
    var mermaidOutputLocationComboBox: JComboBox<String>? = null
        private set
    var mermaidOutputFolderField: TextFieldWithBrowseButton? = null
        private set
    var mermaidFolderLabel: JLabel? = null
        private set
    var includeMermaidExplanationCheckbox: JCheckBox? = null
        private set


    var styleFields: MutableMap<String, Triple<JTextField, JTextField, JTextField>>? = null
        private set

    fun createPanel(): JPanel {
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
        val exportLabel = JLabel("<html><b>Export Settings</b></html>")
        panel.add(exportLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(5, 20, 5, 5)
        enableMermaidExportCheckbox = JCheckBox("Enable Mermaid diagram export").apply {
            toolTipText = """
                <html>
                When enabled, a Mermaid flowchart of the template execution flow is saved.<br>
                Useful for debugging and visualizing template logic.
                </html>
            """.trimIndent()
            addActionListener { updateFieldsEnabled() }
        }
        panel.add(enableMermaidExportCheckbox, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(10, 20, 5, 5)
        val mermaidLocationLabel = JLabel("Output location:")
        panel.add(mermaidLocationLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(0, 20, 5, 5)
        val mermaidLocationOptions = arrayOf("Same directory as template", "Dedicated folder")
        mermaidOutputLocationComboBox = JComboBox(mermaidLocationOptions).apply {
            toolTipText = "Choose where to save the Mermaid diagram files"
            addActionListener { updateFieldsEnabled() }
        }
        panel.add(mermaidOutputLocationComboBox, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(10, 20, 5, 5)
        mermaidFolderLabel = JLabel("Output folder:")
        panel.add(mermaidFolderLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(0, 20, 5, 5)
        mermaidOutputFolderField = TextFieldWithBrowseButton().apply {
            toolTipText = "Folder path for Mermaid exports (absolute or relative to project root)"
            addBrowseFolderListener(
                "Select Mermaid Output Folder",
                "Choose the folder where Mermaid diagram files will be saved",
                null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
            )
        }
        panel.add(mermaidOutputFolderField, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(10, 20, 5, 5)
        includeMermaidExplanationCheckbox = JCheckBox("Include parallel execution explanations").apply {
            toolTipText = """
                <html>
                When enabled, adds text comments to Mermaid diagrams explaining<br>
                why blocks can run in parallel (variable dependencies, etc.)
                </html>
            """.trimIndent()
        }
        panel.add(includeMermaidExplanationCheckbox, gbc)


        gbc.gridy = row++
        gbc.insets = Insets(20, 5, 5, 5)
        val stylesLabel = JLabel("<html><b>Node Styles</b></html>")
        panel.add(stylesLabel, gbc)

        gbc.gridy = row++
        gbc.insets = Insets(5, 20, 10, 5)
        val stylesDesc = JLabel("<html><font color='gray' size='-1'>Customize colors for different node types in the flowchart</font></html>")
        panel.add(stylesDesc, gbc)


        styleFields = mutableMapOf()
        for ((styleName, displayName) in MermaidNodeStyles.STYLE_NAMES) {
            gbc.gridy = row++
            gbc.insets = Insets(5, 20, 2, 5)
            panel.add(JLabel(displayName), gbc)

            gbc.gridy = row++
            gbc.insets = Insets(0, 20, 5, 5)
            val styleRow = createStyleRow(styleName)
            panel.add(styleRow, gbc)
        }

        gbc.gridy = row
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        panel.add(JPanel(), gbc)

        return panel
    }

    private fun createStyleRow(styleName: String): JPanel {
        val row = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            anchor = GridBagConstraints.WEST
            insets = Insets(0, 0, 0, 10)
        }

        gbc.gridx = 0
        row.add(JLabel("Fill:"), gbc)

        gbc.gridx = 1
        val fillField = JTextField(7).apply {
            toolTipText = "Fill color (e.g., #9f9, #ff6b6b)"
        }
        row.add(fillField, gbc)

        gbc.gridx = 2
        row.add(JLabel("Stroke:"), gbc)

        gbc.gridx = 3
        val strokeField = JTextField(7).apply {
            toolTipText = "Stroke color (e.g., #333)"
        }
        row.add(strokeField, gbc)

        gbc.gridx = 4
        row.add(JLabel("Width:"), gbc)

        gbc.gridx = 5
        val widthField = JTextField(5).apply {
            toolTipText = "Stroke width (e.g., 1px, 2px)"
        }
        row.add(widthField, gbc)

        styleFields!![styleName] = Triple(fillField, strokeField, widthField)
        return row
    }

    fun updateFieldsEnabled() {
        val mermaidEnabled = enableMermaidExportCheckbox?.isSelected ?: false
        val dedicatedFolder = mermaidOutputLocationComboBox?.selectedIndex == 1

        mermaidOutputLocationComboBox?.isEnabled = mermaidEnabled
        mermaidFolderLabel?.isEnabled = mermaidEnabled && dedicatedFolder
        mermaidOutputFolderField?.isEnabled = mermaidEnabled && dedicatedFolder
        includeMermaidExplanationCheckbox?.isEnabled = mermaidEnabled


        styleFields?.values?.forEach { (fill, stroke, width) ->
            fill.isEnabled = mermaidEnabled
            stroke.isEnabled = mermaidEnabled
            width.isEnabled = mermaidEnabled
        }
    }

    @Suppress("ComplexCondition")
    fun isModified(settings: TemplaterSettings): Boolean {
        if (enableMermaidExportCheckbox?.isSelected != settings.enableMermaidExport ||
            getSelectedOutputLocation() != settings.mermaidOutputLocation ||
            mermaidOutputFolderField?.text != settings.mermaidOutputFolder ||
            includeMermaidExplanationCheckbox?.isSelected != settings.includeMermaidExplanation) {
            return true
        }


        val currentStyles = settings.mermaidNodeStyles
        return isStyleModified("startEnd", currentStyles.startEnd) ||
               isStyleModified("condition", currentStyles.condition) ||
               isStyleModified("loop", currentStyles.loop) ||
               isStyleModified("loopEnd", currentStyles.loopEnd) ||
               isStyleModified("interpolation", currentStyles.interpolation) ||
               isStyleModified("execution", currentStyles.execution) ||
               isStyleModified("funcDecl", currentStyles.funcDecl) ||
               isStyleModified("funcCall", currentStyles.funcCall) ||
               isStyleModified("variable", currentStyles.variable) ||
               isStyleModified("returnNode", currentStyles.returnNode) ||
               isStyleModified("fork", currentStyles.fork) ||
               isStyleModified("join", currentStyles.join) ||
               isStyleModified("error", currentStyles.error)
    }

    private fun isStyleModified(styleName: String, style: MermaidNodeStyle): Boolean {
        val fields = styleFields?.get(styleName) ?: return false
        return fields.first.text != style.fill ||
               fields.second.text != style.stroke ||
               fields.third.text != style.strokeWidth
    }

    fun apply(settings: TemplaterSettings) {
        settings.enableMermaidExport = enableMermaidExportCheckbox?.isSelected ?: false
        settings.mermaidOutputLocation = getSelectedOutputLocation()
        settings.mermaidOutputFolder = mermaidOutputFolderField?.text ?: ""
        settings.includeMermaidExplanation = includeMermaidExplanationCheckbox?.isSelected ?: true


        settings.mermaidNodeStyles = MermaidNodeStyles(
            startEnd = getStyleFromFields("startEnd"),
            condition = getStyleFromFields("condition"),
            loop = getStyleFromFields("loop"),
            loopEnd = getStyleFromFields("loopEnd"),
            interpolation = getStyleFromFields("interpolation"),
            execution = getStyleFromFields("execution"),
            funcDecl = getStyleFromFields("funcDecl"),
            funcCall = getStyleFromFields("funcCall"),
            variable = getStyleFromFields("variable"),
            returnNode = getStyleFromFields("returnNode"),
            fork = getStyleFromFields("fork"),
            join = getStyleFromFields("join"),
            error = getStyleFromFields("error")
        )
    }

    private fun getStyleFromFields(styleName: String): MermaidNodeStyle {
        val fields = styleFields?.get(styleName)
        return if (fields != null) {
            MermaidNodeStyle(
                fill = fields.first.text.ifBlank { "#fff" },
                stroke = fields.second.text.ifBlank { "#333" },
                strokeWidth = fields.third.text.ifBlank { "1px" }
            )
        } else {
            MermaidNodeStyle("#fff", "#333", "1px")
        }
    }

    fun reset(settings: TemplaterSettings) {
        enableMermaidExportCheckbox?.isSelected = settings.enableMermaidExport
        mermaidOutputLocationComboBox?.selectedIndex = settings.mermaidOutputLocation.ordinal
        mermaidOutputFolderField?.text = settings.mermaidOutputFolder
        includeMermaidExplanationCheckbox?.isSelected = settings.includeMermaidExplanation


        val styles = settings.mermaidNodeStyles
        setStyleFields("startEnd", styles.startEnd)
        setStyleFields("condition", styles.condition)
        setStyleFields("loop", styles.loop)
        setStyleFields("loopEnd", styles.loopEnd)
        setStyleFields("interpolation", styles.interpolation)
        setStyleFields("execution", styles.execution)
        setStyleFields("funcDecl", styles.funcDecl)
        setStyleFields("funcCall", styles.funcCall)
        setStyleFields("variable", styles.variable)
        setStyleFields("returnNode", styles.returnNode)
        setStyleFields("fork", styles.fork)
        setStyleFields("join", styles.join)
        setStyleFields("error", styles.error)

        updateFieldsEnabled()
    }

    private fun setStyleFields(styleName: String, style: MermaidNodeStyle) {
        val fields = styleFields?.get(styleName) ?: return
        fields.first.text = style.fill
        fields.second.text = style.stroke
        fields.third.text = style.strokeWidth
    }

    fun getSelectedOutputLocation(): MermaidOutputLocation {
        return when (mermaidOutputLocationComboBox?.selectedIndex) {
            0 -> MermaidOutputLocation.SAME_AS_SCRIPT
            1 -> MermaidOutputLocation.DEDICATED_FOLDER
            else -> MermaidOutputLocation.SAME_AS_SCRIPT
        }
    }

    fun dispose() {
        enableMermaidExportCheckbox = null
        mermaidOutputLocationComboBox = null
        mermaidOutputFolderField = null
        mermaidFolderLabel = null
        includeMermaidExplanationCheckbox = null
        styleFields = null
    }
}

