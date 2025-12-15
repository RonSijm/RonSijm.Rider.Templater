package ronsijm.templater.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for Templater plugin
 * Settings are stored at application level (shared across all projects)
 */
@State(
    name = "TemplaterSettings",
    storages = [Storage("TemplaterSettings.xml")]
)
class TemplaterSettings : PersistentStateComponent<TemplaterSettings> {

    /**
     * Enable experimental parallel execution of independent template blocks
     * When enabled, blocks that don't depend on each other will be executed concurrently
     */
    var enableParallelExecution: Boolean = false

    /**
     * Enable syntax validation before template execution
     * Shows warnings for malformed template syntax
     */
    var enableSyntaxValidation: Boolean = true

    /**
     * Show execution statistics in the notification after template execution
     * Includes timing and parallelization info when parallel execution is enabled
     */
    var showExecutionStats: Boolean = false

    override fun getState(): TemplaterSettings = this

    override fun loadState(state: TemplaterSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): TemplaterSettings {
            return ApplicationManager.getApplication().getService(TemplaterSettings::class.java)
                ?: TemplaterSettings()
        }
    }
}

