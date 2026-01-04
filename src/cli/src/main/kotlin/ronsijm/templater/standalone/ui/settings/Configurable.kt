package ronsijm.templater.standalone.ui.settings

import javax.swing.JComponent

interface Configurable {
    val displayName: String
    fun createComponent(): JComponent
    fun reset()
    fun apply()
    fun isModified(): Boolean
    fun disposeUIResources() {}
}

