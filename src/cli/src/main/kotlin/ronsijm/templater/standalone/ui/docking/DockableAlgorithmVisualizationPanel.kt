@file:Suppress("DEPRECATION")

package ronsijm.templater.standalone.ui.docking

import ModernDocking.Dockable
import ronsijm.templater.standalone.ui.theme.FlatLafThemeColors
import ronsijm.templater.ui.visualization.AlgorithmVisualizationPanel
import java.awt.BorderLayout
import javax.swing.JPanel

class DockableAlgorithmVisualizationPanel : JPanel(BorderLayout()), Dockable {

    val panel = AlgorithmVisualizationPanel(FlatLafThemeColors.instance)

    init {
        add(panel, BorderLayout.CENTER)
    }

    override fun getPersistentID(): String = "AlgorithmVisualization"

    override fun getTabText(): String = "Algorithm Visualization"

    override fun isFloatingAllowed(): Boolean = true

    override fun isClosable(): Boolean = false

    override fun isLimitedToRoot(): Boolean = false
}

