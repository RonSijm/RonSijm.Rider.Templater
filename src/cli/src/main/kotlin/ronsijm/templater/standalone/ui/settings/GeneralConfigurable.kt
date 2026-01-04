package ronsijm.templater.standalone.ui.settings

import ronsijm.templater.standalone.services.FileAssociationService
import ronsijm.templater.standalone.settings.AppSettings
import java.awt.FlowLayout
import javax.swing.*

class GeneralConfigurable : Configurable {

    override val displayName = "General"

    private lateinit var panel: JPanel
    private lateinit var fileOpenBehaviorCombo: JComboBox<String>
    private lateinit var autoOpenLastFolderCheckbox: JCheckBox
    private lateinit var showRunGutterIconsCheckbox: JCheckBox
    private lateinit var showBreakpointGutterIconsCheckbox: JCheckBox
    private lateinit var showSuccessNotificationsCheckbox: JCheckBox
    private lateinit var showErrorNotificationsCheckbox: JCheckBox
    private lateinit var fileAssociationStatusLabel: JLabel

    override fun createComponent(): JComponent {
        panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)


        panel.add(createSectionLabel("Startup"))

        autoOpenLastFolderCheckbox = JCheckBox("Automatically open last folder on startup")
        panel.add(autoOpenLastFolderCheckbox)


        panel.add(Box.createVerticalStrut(10))
        panel.add(createSectionLabel("File Handling"))

        fileOpenBehaviorCombo = JComboBox(arrayOf("Current Window", "New Tab"))
        val fileOpenPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        fileOpenPanel.add(JLabel("Open file in:"))
        fileOpenPanel.add(fileOpenBehaviorCombo)
        panel.add(fileOpenPanel)


        if (FileAssociationService.isWindows()) {
            panel.add(Box.createVerticalStrut(10))
            panel.add(createSectionLabel("File Association (Windows)"))

            val fileAssocPanel = JPanel(FlowLayout(FlowLayout.LEFT))

            val registerButton = JButton("Register .md files")
            registerButton.addActionListener { registerFileAssociation() }
            fileAssocPanel.add(registerButton)

            val unregisterButton = JButton("Unregister")
            unregisterButton.addActionListener { unregisterFileAssociation() }
            fileAssocPanel.add(unregisterButton)

            panel.add(fileAssocPanel)

            fileAssociationStatusLabel = JLabel(getFileAssociationStatus())
            panel.add(fileAssociationStatusLabel)
        }


        panel.add(Box.createVerticalStrut(10))
        panel.add(createSectionLabel("Gutter Icons"))

        showRunGutterIconsCheckbox = JCheckBox("Show run icons in gutter")
        showBreakpointGutterIconsCheckbox = JCheckBox("Show breakpoint icons in gutter")
        panel.add(showRunGutterIconsCheckbox)
        panel.add(showBreakpointGutterIconsCheckbox)


        panel.add(Box.createVerticalStrut(10))
        panel.add(createSectionLabel("Notifications"))

        showSuccessNotificationsCheckbox = JCheckBox("Show success notifications")
        showErrorNotificationsCheckbox = JCheckBox("Show error notifications")
        panel.add(showSuccessNotificationsCheckbox)
        panel.add(showErrorNotificationsCheckbox)

        panel.add(Box.createVerticalGlue())

        return panel
    }

    private fun getFileAssociationStatus(): String {
        return if (FileAssociationService.isRegistered()) {
            "Status: Registered - Right-click .md files to 'Open with Templater'"
        } else {
            "Status: Not registered"
        }
    }

    private fun registerFileAssociation() {
        val result = FileAssociationService.registerFileAssociation()
        result.fold(
            onSuccess = { message ->
                JOptionPane.showMessageDialog(panel, message, "Success", JOptionPane.INFORMATION_MESSAGE)
                if (::fileAssociationStatusLabel.isInitialized) {
                    fileAssociationStatusLabel.text = getFileAssociationStatus()
                }
            },
            onFailure = { error ->
                JOptionPane.showMessageDialog(panel, error.message, "Error", JOptionPane.ERROR_MESSAGE)
            }
        )
    }

    private fun unregisterFileAssociation() {
        val result = FileAssociationService.unregisterFileAssociation()
        result.fold(
            onSuccess = { message ->
                JOptionPane.showMessageDialog(panel, message, "Success", JOptionPane.INFORMATION_MESSAGE)
                if (::fileAssociationStatusLabel.isInitialized) {
                    fileAssociationStatusLabel.text = getFileAssociationStatus()
                }
            },
            onFailure = { error ->
                JOptionPane.showMessageDialog(panel, error.message, "Error", JOptionPane.ERROR_MESSAGE)
            }
        )
    }

    override fun reset() {
        autoOpenLastFolderCheckbox.isSelected = AppSettings.autoOpenLastFolder
        fileOpenBehaviorCombo.selectedIndex = when (AppSettings.getFileOpenBehavior()) {
            AppSettings.FileOpenBehavior.CURRENT_WINDOW -> 0
            AppSettings.FileOpenBehavior.NEW_TAB -> 1
        }
        showRunGutterIconsCheckbox.isSelected = AppSettings.showRunGutterIcons
        showBreakpointGutterIconsCheckbox.isSelected = AppSettings.showBreakpointGutterIcons
        showSuccessNotificationsCheckbox.isSelected = AppSettings.showSuccessNotifications
        showErrorNotificationsCheckbox.isSelected = AppSettings.showErrorNotifications
    }

    override fun apply() {
        AppSettings.autoOpenLastFolder = autoOpenLastFolderCheckbox.isSelected
        val behavior = when (fileOpenBehaviorCombo.selectedIndex) {
            0 -> AppSettings.FileOpenBehavior.CURRENT_WINDOW
            1 -> AppSettings.FileOpenBehavior.NEW_TAB
            else -> AppSettings.FileOpenBehavior.CURRENT_WINDOW
        }
        AppSettings.setFileOpenBehavior(behavior)
        AppSettings.showRunGutterIcons = showRunGutterIconsCheckbox.isSelected
        AppSettings.showBreakpointGutterIcons = showBreakpointGutterIconsCheckbox.isSelected
        AppSettings.showSuccessNotifications = showSuccessNotificationsCheckbox.isSelected
        AppSettings.showErrorNotifications = showErrorNotificationsCheckbox.isSelected
    }

    override fun isModified(): Boolean {
        val currentBehavior = when (fileOpenBehaviorCombo.selectedIndex) {
            0 -> AppSettings.FileOpenBehavior.CURRENT_WINDOW
            1 -> AppSettings.FileOpenBehavior.NEW_TAB
            else -> AppSettings.FileOpenBehavior.CURRENT_WINDOW
        }

        return autoOpenLastFolderCheckbox.isSelected != AppSettings.autoOpenLastFolder ||
               currentBehavior != AppSettings.getFileOpenBehavior() ||
               showRunGutterIconsCheckbox.isSelected != AppSettings.showRunGutterIcons ||
               showBreakpointGutterIconsCheckbox.isSelected != AppSettings.showBreakpointGutterIcons ||
               showSuccessNotificationsCheckbox.isSelected != AppSettings.showSuccessNotifications ||
               showErrorNotificationsCheckbox.isSelected != AppSettings.showErrorNotifications
    }

    private fun createSectionLabel(text: String): JLabel {
        val label = JLabel(text)
        label.font = label.font.deriveFont(java.awt.Font.BOLD)
        label.border = BorderFactory.createEmptyBorder(5, 0, 5, 0)
        return label
    }
}

