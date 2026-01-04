@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui

import ronsijm.templater.standalone.RecentFolders
import ronsijm.templater.standalone.settings.AppSettings
import ronsijm.templater.standalone.ui.debug.DebugController
import ronsijm.templater.standalone.ui.dialog.TemplateResultDialog
import ronsijm.templater.standalone.ui.docking.DockingManager
import ronsijm.templater.standalone.ui.execution.TemplateRunner
import ronsijm.templater.standalone.ui.layout.LayoutManager
import ronsijm.templater.standalone.ui.menu.MenuBarManager
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JOptionPane

class MainWindow : JFrame("Templater") {


    private val dockingManager = DockingManager(this)
    private val layoutManager = LayoutManager(this)
    private lateinit var menuBarManager: MenuBarManager
    private lateinit var connectionManager: PanelConnectionManager
    private lateinit var templateRunner: TemplateRunner
    private lateinit var debugController: DebugController


    val fileTreePanel: FileTreePanel get() = dockingManager.dockableFileTree.panel
    val editorPanel: TabbedEditorPanel get() = dockingManager.dockableEditor.panel
    val debugPanel: DebugPanel get() = dockingManager.dockableDebug.panel
    val variablesPanel: VariablesPanel get() = dockingManager.dockableVariables.panel
    val renderPanel: RenderPanel get() = dockingManager.dockableRender.panel
    val controlFlowPanel: ControlFlowPanel get() = dockingManager.dockableControlFlow.panel
    val algorithmVisualizationPanel: ronsijm.templater.ui.visualization.AlgorithmVisualizationPanel
        get() = dockingManager.dockableAlgorithmVisualization.panel

    init {
        setupWindow()
        dockingManager.setupDocking()
        setupManagers()
        setupMenuBar()
        connectionManager.setupConnections()
        layoutManager.loadDefaultLayout()
        loadLastFolderIfEnabled()

        isVisible = true
    }

    private fun setupWindow() {
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(1400, 900)
        setLocationRelativeTo(null)

        addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent?) {
                layoutManager.saveDefaultLayout()
            }
        })
    }

    private fun setupManagers() {
        templateRunner = TemplateRunner(this, editorPanel, renderPanel)
        debugController = DebugController(this, editorPanel, debugPanel, variablesPanel, templateRunner)
        connectionManager = PanelConnectionManager(
            this, fileTreePanel, editorPanel, debugPanel,
            variablesPanel, renderPanel, controlFlowPanel, algorithmVisualizationPanel
        )
    }

    private fun setupMenuBar() {
        menuBarManager = MenuBarManager(
            onOpenFolder = { fileTreePanel.openFolder() },
            onOpenFile = { editorPanel.openFile() },
            onSaveFile = { editorPanel.saveFile() },
            onOpenRecentFolder = { folder -> fileTreePanel.loadFolder(folder) },
            onRunTemplate = { templateRunner.runTemplate() },
            onStartDebugging = { debugController.startDebugging() },
            onStopDebugging = { debugController.stopDebugging() },
            onToggleBreakpoint = { editorPanel.toggleBreakpoint() },
            onShowSettings = { showSettingsDialog() },
            onSaveLayout = { layoutManager.saveLayoutWithDialog() },
            onLoadLayout = { layoutManager.loadLayoutWithDialog() },
            onDockAllFloating = { dockingManager.dockAllFloatingWindows() },
            onShowAbout = { showAboutDialog() }
        )
        jMenuBar = menuBarManager.createMenuBar()
    }

    private fun showAboutDialog() {
        JOptionPane.showMessageDialog(
            this,
            "Templater Standalone\nVersion 1.0.0\n\nA template debugging tool\n\nMade by Ron Sijm",
            "About Templater",
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    private fun showSettingsDialog() {
        val dialog = SettingsDialog(this)
        dialog.isVisible = true
    }

    private fun loadLastFolderIfEnabled() {
        if (AppSettings.autoOpenLastFolder) {
            val lastFolder = RecentFolders.getMostRecentFolder()
            if (lastFolder != null && lastFolder.exists()) {
                fileTreePanel.loadFolder(lastFolder)
            }
        }
    }
}

