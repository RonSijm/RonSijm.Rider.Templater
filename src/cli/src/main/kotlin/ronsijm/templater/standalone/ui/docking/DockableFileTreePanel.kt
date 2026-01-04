@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui.docking

import ModernDocking.Dockable
import ronsijm.templater.standalone.ui.FileTreePanel
import java.awt.BorderLayout
import javax.swing.JPanel

class DockableFileTreePanel : JPanel(BorderLayout()), Dockable {

    val panel = FileTreePanel()

    init {
        add(panel, BorderLayout.CENTER)
    }

    override fun getPersistentID(): String = "FileTree"

    override fun getTabText(): String = "Files"

    override fun isFloatingAllowed(): Boolean = true

    override fun isClosable(): Boolean = false

    override fun isLimitedToRoot(): Boolean = false
}

