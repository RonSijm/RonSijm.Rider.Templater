package ronsijm.templater.standalone.actions

import javax.swing.Icon
import javax.swing.KeyStroke

interface Action {
    val name: String
    val description: String?
        get() = null
    val icon: Icon?
        get() = null
    val accelerator: KeyStroke?
        get() = null

    fun isEnabled(): Boolean = true
    fun execute()
}

interface ActionContext {
    fun getCurrentFile(): java.io.File?
    fun getEditorContent(): String
    fun getBreakpoints(): Set<Int>
    fun isDebugging(): Boolean
}

