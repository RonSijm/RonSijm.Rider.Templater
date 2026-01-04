package ronsijm.templater.standalone.ui

import ronsijm.templater.standalone.settings.AppSettings
import java.awt.BorderLayout
import java.io.File
import javax.swing.*
import javax.swing.Icon

class TabbedEditorPanel : JPanel(BorderLayout()) {

    private val tabbedPane = JTabbedPane()
    private val editors = mutableListOf<EditorPanel>()


    private val contentChangeListeners = mutableListOf<(String) -> Unit>()
    private val breakpointListeners = mutableListOf<(Set<Int>) -> Unit>()

    init {
        add(tabbedPane, BorderLayout.CENTER)


        tabbedPane.addChangeListener {
            SwingUtilities.invokeLater {
                getCurrentEditor()?.requestFocusInWindow()
            }
        }


        addNewTab()
    }


    fun addNewTab(): EditorPanel {
        val editor = EditorPanel()


        contentChangeListeners.forEach { listener ->
            editor.addContentChangeListener(listener)
        }
        breakpointListeners.forEach { listener ->
            editor.addBreakpointListener(listener)
        }

        editors.add(editor)

        val tabIndex = tabbedPane.tabCount
        tabbedPane.addTab("Untitled", editor)
        tabbedPane.setTabComponentAt(tabIndex, createTabComponent("Untitled", tabIndex))
        tabbedPane.selectedIndex = tabIndex

        return editor
    }


    private fun createTabComponent(title: String, index: Int): JPanel {
        val panel = JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0))
        panel.isOpaque = false

        val label = JLabel(title)
        panel.add(label)


        val closeButton = JButton()


        val closeIcon = UIManager.getIcon("InternalFrame.closeIcon")
            ?: createCloseIcon()

        closeButton.icon = closeIcon
        closeButton.preferredSize = java.awt.Dimension(16, 16)
        closeButton.toolTipText = "Close this tab"
        closeButton.isBorderPainted = false
        closeButton.isFocusPainted = false
        closeButton.isContentAreaFilled = false
        closeButton.margin = java.awt.Insets(0, 0, 0, 0)
        closeButton.addActionListener {
            closeTab(index)
        }
        panel.add(closeButton)

        return panel
    }


    private fun createCloseIcon(): Icon {
        return object : Icon {
            override fun getIconWidth() = 12
            override fun getIconHeight() = 12

            override fun paintIcon(c: java.awt.Component?, g: java.awt.Graphics?, x: Int, y: Int) {
                g?.let {
                    it.color = java.awt.Color.GRAY

                    it.drawLine(x + 2, y + 2, x + 10, y + 10)
                    it.drawLine(x + 2, y + 10, x + 10, y + 2)
                }
            }
        }
    }


    fun loadFile(file: File) {

        val existingTabIndex = findTabWithFile(file)

        if (existingTabIndex >= 0) {

            tabbedPane.selectedIndex = existingTabIndex
            return
        }

        val behavior = AppSettings.getFileOpenBehavior()

        when (behavior) {
            AppSettings.FileOpenBehavior.CURRENT_WINDOW -> {

                getCurrentEditor()?.loadFile(file)
                updateCurrentTabTitle(file.name)
            }
            AppSettings.FileOpenBehavior.NEW_TAB -> {

                val editor = addNewTab()
                editor.loadFile(file)
                updateCurrentTabTitle(file.name)
            }
        }
    }


    fun loadFileInNewTab(file: File) {

        val existingTabIndex = findTabWithFile(file)

        if (existingTabIndex >= 0) {

            tabbedPane.selectedIndex = existingTabIndex
            return
        }


        val editor = addNewTab()
        editor.loadFile(file)
        updateCurrentTabTitle(file.name)
    }


    private fun findTabWithFile(file: File): Int {
        for (i in editors.indices) {
            val editorFile = editors[i].getCurrentFile()
            if (editorFile != null && editorFile.absolutePath == file.absolutePath) {
                return i
            }
        }
        return -1
    }


    fun getCurrentEditor(): EditorPanel? {
        val index = tabbedPane.selectedIndex
        return if (index >= 0 && index < editors.size) {
            editors[index]
        } else {
            null
        }
    }


    fun getAllEditors(): List<EditorPanel> = editors.toList()


    fun closeCurrentTab() {
        val index = tabbedPane.selectedIndex
        closeTab(index)
    }


    private fun closeTab(index: Int) {
        if (index < 0 || index >= editors.size) return


        val editor = editors[index]
        if (editor.hasUnsavedChanges()) {
            val result = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save changes to this file?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            )

            when (result) {
                JOptionPane.YES_OPTION -> {
                    editor.saveFile()

                }
                JOptionPane.CANCEL_OPTION -> {

                    return
                }

            }
        }


        if (editors.size > 1) {
            tabbedPane.removeTabAt(index)
            editors.removeAt(index)
        }
    }


    private fun updateCurrentTabTitle(title: String) {
        val index = tabbedPane.selectedIndex
        if (index >= 0) {
            tabbedPane.setTabComponentAt(index, createTabComponent(title, index))
        }
    }


    fun getCurrentFile(): File? = getCurrentEditor()?.getCurrentFile()


    fun getText(): String = getCurrentEditor()?.getText() ?: ""


    fun getBreakpoints(): Set<Int> = getCurrentEditor()?.getBreakpoints() ?: emptySet()


    fun saveFile() {
        getCurrentEditor()?.saveFile()
    }


    fun openFile() {
        val chooser = JFileChooser()
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            loadFile(chooser.selectedFile)
        }
    }


    fun toggleBreakpoint() {
        getCurrentEditor()?.toggleBreakpoint()
    }


    fun addBreakpointListener(listener: (Set<Int>) -> Unit) {
        breakpointListeners.add(listener)
        editors.forEach { it.addBreakpointListener(listener) }
    }


    fun addContentChangeListener(listener: (String) -> Unit) {
        contentChangeListeners.add(listener)
        editors.forEach { it.addContentChangeListener(listener) }
    }
}

