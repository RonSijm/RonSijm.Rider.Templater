@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui.docking

import ModernDocking.Dockable
import ronsijm.templater.standalone.ui.VariablesPanel
import java.awt.BorderLayout
import javax.swing.JPanel

class DockableVariablesPanel : JPanel(BorderLayout()), Dockable {

    val panel = VariablesPanel()

    init {
        add(panel, BorderLayout.CENTER)
    }

    override fun getPersistentID(): String = "Variables"

    override fun getTabText(): String = "Variables"

    override fun isFloatingAllowed(): Boolean = true

    override fun isClosable(): Boolean = false

    override fun isLimitedToRoot(): Boolean = false
}

