package ronsijm.templater.standalone.ui.menu

import ronsijm.templater.standalone.RecentFolders
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.*

class MenuBarManager(
    private val onOpenFolder: () -> Unit,
    private val onOpenFile: () -> Unit,
    private val onSaveFile: () -> Unit,
    private val onOpenRecentFolder: (File) -> Unit,
    private val onRunTemplate: () -> Unit,
    private val onStartDebugging: () -> Unit,
    private val onStopDebugging: () -> Unit,
    private val onToggleBreakpoint: () -> Unit,
    private val onShowSettings: () -> Unit,
    private val onSaveLayout: () -> Unit,
    private val onLoadLayout: () -> Unit,
    private val onDockAllFloating: () -> Unit,
    private val onShowAbout: () -> Unit
) {

    private var recentFoldersMenu: JMenu? = null

    fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        menuBar.add(createFileMenu())
        menuBar.add(createRunMenu())
        menuBar.add(createDebugMenu())
        menuBar.add(createToolsMenu())
        menuBar.add(createLayoutMenu())
        menuBar.add(createHelpMenu())

        return menuBar
    }

    fun refreshRecentFolders() {
        recentFoldersMenu?.let { updateRecentFoldersMenu(it) }
    }

    private fun createFileMenu(): JMenu {
        val menu = JMenu("File")

        menu.add(JMenuItem("Open Folder...").apply {
            addActionListener { onOpenFolder() }
        })
        menu.add(JMenuItem("Open File...").apply {
            addActionListener { onOpenFile() }
        })
        menu.add(JMenuItem("Save").apply {
            addActionListener { onSaveFile() }
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
        })

        recentFoldersMenu = JMenu("Recent Folders")
        updateRecentFoldersMenu(recentFoldersMenu!!)
        menu.add(recentFoldersMenu)

        menu.addSeparator()
        menu.add(JMenuItem("Exit").apply {
            addActionListener { System.exit(0) }
        })

        return menu
    }

    private fun createRunMenu(): JMenu {
        val menu = JMenu("Run")

        menu.add(JMenuItem("Run Template").apply {
            addActionListener { onRunTemplate() }
        })

        return menu
    }

    private fun createDebugMenu(): JMenu {
        val menu = JMenu("Debug")

        menu.add(JMenuItem("Start Debugging").apply {
            addActionListener { onStartDebugging() }
        })
        menu.add(JMenuItem("Stop Debugging").apply {
            addActionListener { onStopDebugging() }
        })
        menu.addSeparator()
        menu.add(JMenuItem("Toggle Breakpoint").apply {
            addActionListener { onToggleBreakpoint() }
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)
        })

        return menu
    }

    private fun createToolsMenu(): JMenu {
        val menu = JMenu("Tools")

        menu.add(JMenuItem("Settings...").apply {
            addActionListener { onShowSettings() }
        })

        return menu
    }

    private fun createLayoutMenu(): JMenu {
        val menu = JMenu("Layout")

        menu.add(JMenuItem("Save Layout").apply {
            addActionListener { onSaveLayout() }
        })
        menu.add(JMenuItem("Load Layout").apply {
            addActionListener { onLoadLayout() }
        })
        menu.addSeparator()
        menu.add(JMenuItem("Dock All Floating Windows").apply {
            addActionListener { onDockAllFloating() }
        })

        return menu
    }

    private fun createHelpMenu(): JMenu {
        val menu = JMenu("Help")

        menu.add(JMenuItem("About").apply {
            addActionListener { onShowAbout() }
        })

        return menu
    }

    private fun updateRecentFoldersMenu(menu: JMenu) {
        menu.removeAll()

        val recentFolders = RecentFolders.getRecentFolders()
        if (recentFolders.isEmpty()) {
            menu.add(JMenuItem("(No recent folders)").apply {
                isEnabled = false
            })
        } else {
            for (folder in recentFolders) {
                menu.add(JMenuItem(folder.absolutePath).apply {
                    addActionListener { onOpenRecentFolder(folder) }
                })
            }
        }
    }
}

