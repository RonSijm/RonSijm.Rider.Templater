@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui.docking

import ModernDocking.Dockable
import ronsijm.templater.standalone.ui.TabbedEditorPanel
import java.awt.BorderLayout
import javax.swing.JPanel

class DockableEditorPanel : JPanel(BorderLayout()), Dockable {

    val panel = TabbedEditorPanel()

    init {
        add(panel, BorderLayout.CENTER)
    }

    override fun getPersistentID(): String = "Editor"

    override fun getTabText(): String = "Editor"

    override fun isFloatingAllowed(): Boolean = true

    override fun isClosable(): Boolean = false

    override fun isLimitedToRoot(): Boolean = false
}

