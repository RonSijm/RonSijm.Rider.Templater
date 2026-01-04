package ronsijm.templater.standalone.ui.layout

import ronsijm.templater.standalone.services.LayoutPersistenceService
import ronsijm.templater.standalone.services.LayoutPersistenceService.LayoutData
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane

class LayoutManager(private val window: JFrame) {

    private fun getCurrentLayout(): LayoutData {
        return LayoutData(
            x = window.location.x,
            y = window.location.y,
            width = window.width,
            height = window.height,
            extendedState = window.extendedState
        )
    }

    private fun applyLayout(layout: LayoutData) {
        window.setLocation(layout.x, layout.y)
        window.setSize(layout.width, layout.height)
        window.extendedState = layout.extendedState
    }

    fun loadDefaultLayout() {
        val layout = LayoutPersistenceService.loadDefaultLayout(getCurrentLayout())
        applyLayout(layout)
    }

    fun saveDefaultLayout() {
        LayoutPersistenceService.saveDefaultLayout(getCurrentLayout())
    }

    fun saveLayoutWithDialog() {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Save Layout"
        chooser.selectedFile = File("templater-layout.properties")

        if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            try {
                LayoutPersistenceService.saveLayout(chooser.selectedFile, getCurrentLayout())
                JOptionPane.showMessageDialog(
                    window,
                    "Layout saved successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                )
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    window,
                    "Error saving layout: ${e.message}",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }

    fun loadLayoutWithDialog() {
        val chooser = JFileChooser()
        chooser.dialogTitle = "Load Layout"

        if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            try {
                val layout = LayoutPersistenceService.loadLayout(chooser.selectedFile, getCurrentLayout())
                applyLayout(layout)
                JOptionPane.showMessageDialog(
                    window,
                    "Layout loaded successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                )
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(
                    window,
                    "Error loading layout: ${e.message}",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }
}

