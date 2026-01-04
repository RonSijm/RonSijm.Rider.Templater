@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui.docking

import ModernDocking.Dockable
import ronsijm.templater.standalone.ui.DebugPanel
import java.awt.BorderLayout
import javax.swing.JPanel

class DockableDebugPanel : JPanel(BorderLayout()), Dockable {

    val panel = DebugPanel()

    init {
        add(panel, BorderLayout.CENTER)
    }

    override fun getPersistentID(): String = "Debug"

    override fun getTabText(): String = "Debug"

    override fun isFloatingAllowed(): Boolean = true

    override fun isClosable(): Boolean = false

    override fun isLimitedToRoot(): Boolean = false
}

