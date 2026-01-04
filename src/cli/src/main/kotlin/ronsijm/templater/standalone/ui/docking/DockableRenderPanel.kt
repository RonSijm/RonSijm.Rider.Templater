@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui.docking

import ModernDocking.Dockable
import ronsijm.templater.standalone.ui.RenderPanel
import java.awt.BorderLayout
import javax.swing.JPanel

class DockableRenderPanel : JPanel(BorderLayout()), Dockable {

    val panel = RenderPanel()

    init {
        add(panel, BorderLayout.CENTER)
    }

    override fun getPersistentID(): String = "Render"

    override fun getTabText(): String = "Render"

    override fun isFloatingAllowed(): Boolean = true

    override fun isClosable(): Boolean = false

    override fun isLimitedToRoot(): Boolean = false
}

