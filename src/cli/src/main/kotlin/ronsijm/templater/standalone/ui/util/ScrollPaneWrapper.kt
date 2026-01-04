package ronsijm.templater.standalone.ui.util

import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

object ScrollPaneWrapper {
    fun wrap(
        component: JComponent,
        preferredWidth: Int = 400,
        preferredHeight: Int = 300
    ): JPanel {
        return object : JPanel(BorderLayout()) {
            override fun getPreferredSize(): Dimension {

                return Dimension(preferredWidth, preferredHeight)
            }
        }.apply {
            add(component, BorderLayout.CENTER)
        }
    }
}
