@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui.docking

import ModernDocking.Dockable
import ronsijm.templater.standalone.ui.ControlFlowPanel
import java.awt.BorderLayout
import javax.swing.JPanel

class DockableControlFlowPanel : JPanel(BorderLayout()), Dockable {

    val panel = ControlFlowPanel()

    init {
        add(panel, BorderLayout.CENTER)
    }

    override fun getPersistentID(): String = "ControlFlow"

    override fun getTabText(): String = "Control Flow"

    override fun isFloatingAllowed(): Boolean = true

    override fun isClosable(): Boolean = false

    override fun isLimitedToRoot(): Boolean = false
}

