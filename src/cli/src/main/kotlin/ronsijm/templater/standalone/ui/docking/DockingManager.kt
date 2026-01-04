@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui.docking

import ModernDocking.DockingRegion
import ModernDocking.app.Docking
import ModernDocking.app.RootDockingPanel
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities

class DockingManager(private val mainWindow: JFrame) {


    val dockableFileTree = DockableFileTreePanel()
    val dockableEditor = DockableEditorPanel()
    val dockableDebug = DockableDebugPanel()
    val dockableVariables = DockableVariablesPanel()
    val dockableRender = DockableRenderPanel()
    val dockableControlFlow = DockableControlFlowPanel()
    val dockableAlgorithmVisualization = DockableAlgorithmVisualizationPanel()

    fun setupDocking() {

        Docking.initialize(mainWindow)


        val rootPanel = RootDockingPanel(mainWindow)
        mainWindow.contentPane.add(rootPanel, BorderLayout.CENTER)


        Docking.registerDockable(dockableFileTree)
        Docking.registerDockable(dockableEditor)
        Docking.registerDockable(dockableDebug)
        Docking.registerDockable(dockableVariables)
        Docking.registerDockable(dockableRender)
        Docking.registerDockable(dockableControlFlow)
        Docking.registerDockable(dockableAlgorithmVisualization)



        Docking.dock(dockableEditor, mainWindow)


        Docking.dock(dockableFileTree, dockableEditor, DockingRegion.WEST, 0.2)


        Docking.dock(dockableDebug, dockableEditor, DockingRegion.SOUTH, 0.3)


        Docking.dock(dockableVariables, dockableEditor, DockingRegion.EAST, 0.2)


        Docking.dock(dockableRender, dockableVariables, DockingRegion.CENTER)


        Docking.dock(dockableControlFlow, dockableVariables, DockingRegion.CENTER)


        Docking.dock(dockableAlgorithmVisualization, dockableVariables, DockingRegion.CENTER)
    }


    fun dockAllFloatingWindows() {
        try {

            val floatingFrames = java.awt.Window.getWindows().filterIsInstance<JFrame>().filter { frame ->
                frame != mainWindow && frame.isVisible && frame.title != "About"
            }


            if (!isDocked(dockableEditor)) {
                Docking.dock(dockableEditor, mainWindow)
            }


            if (!isDocked(dockableFileTree)) {
                Docking.dock(dockableFileTree, dockableEditor, DockingRegion.WEST, 0.2)
            }

            if (!isDocked(dockableDebug)) {
                Docking.dock(dockableDebug, dockableEditor, DockingRegion.SOUTH, 0.3)
            }

            if (!isDocked(dockableVariables)) {
                Docking.dock(dockableVariables, dockableEditor, DockingRegion.EAST, 0.2)
            }

            if (!isDocked(dockableRender)) {
                Docking.dock(dockableRender, dockableVariables, DockingRegion.CENTER)
            }

            if (!isDocked(dockableControlFlow)) {
                Docking.dock(dockableControlFlow, dockableVariables, DockingRegion.CENTER)
            }

            if (!isDocked(dockableAlgorithmVisualization)) {
                Docking.dock(dockableAlgorithmVisualization, dockableVariables, DockingRegion.CENTER)
            }


            floatingFrames.forEach { frame ->
                frame.dispose()
            }

            JOptionPane.showMessageDialog(
                mainWindow,
                "All floating windows have been docked",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                mainWindow,
                "Error docking windows: ${e.message}",
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }


    private fun isDocked(dockable: JPanel): Boolean {
        return dockable.parent != null && SwingUtilities.isDescendingFrom(dockable, mainWindow)
    }
}

