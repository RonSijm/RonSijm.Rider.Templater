package ronsijm.templater.standalone.ui.settings

import ronsijm.templater.standalone.settings.AppSettings
import java.awt.FlowLayout
import javax.swing.*

class HotkeysConfigurable : Configurable {

    override val displayName = "Hotkeys"

    private lateinit var panel: JPanel
    private lateinit var toggleBreakpointField: JTextField

    override fun createComponent(): JComponent {
        panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)


        toggleBreakpointField = JTextField(10)
        val breakpointPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        breakpointPanel.add(JLabel("Toggle Breakpoint:"))
        breakpointPanel.add(toggleBreakpointField)
        breakpointPanel.add(JLabel("(e.g., F9, F8, CTRL+B)"))
        panel.add(breakpointPanel)

        panel.add(Box.createVerticalGlue())

        return panel
    }

    override fun reset() {
        toggleBreakpointField.text = AppSettings.getToggleBreakpointHotkey()
    }

    override fun apply() {
        AppSettings.setToggleBreakpointHotkey(toggleBreakpointField.text)
    }

    override fun isModified(): Boolean {
        return toggleBreakpointField.text != AppSettings.getToggleBreakpointHotkey()
    }
}

