package ronsijm.templater.settings

import org.junit.Test
import org.junit.Assert.*


class TemplaterSettingsTest {

    @Test
    fun `test default settings values`() {
        val settings = TemplaterSettings()


        assertFalse("Parallel execution should be disabled by default", settings.enableParallelExecution)
        assertTrue("Syntax validation should be enabled by default", settings.enableSyntaxValidation)
        assertTrue("Selection-only execution should be enabled by default", settings.enableSelectionOnlyExecution)
        assertEquals("Cancel behavior should be REMOVE_EXPRESSION by default", CancelBehavior.REMOVE_EXPRESSION, settings.cancelBehavior)
        assertEquals("Hotkey popup behavior should be ALWAYS by default", PopupBehavior.ALWAYS, settings.popupBehaviorHotkey)
        assertEquals("Gutter popup behavior should be ONLY_ON_ERROR by default", PopupBehavior.ONLY_ON_ERROR, settings.popupBehaviorGutter)
        assertFalse("Execution stats should be disabled by default", settings.showExecutionStats)
        assertFalse("Performance profiling should be disabled by default", settings.enablePerformanceProfiling)
        assertFalse("Mermaid export should be disabled by default", settings.enableMermaidExport)
    }

    @Test
    fun `test settings state persistence`() {
        val settings = TemplaterSettings()


        settings.enableParallelExecution = true
        settings.enableSyntaxValidation = false
        settings.cancelBehavior = CancelBehavior.KEEP_EXPRESSION
        settings.popupBehaviorHotkey = PopupBehavior.NEVER
        settings.showExecutionStats = true


        val state = settings.getState()
        val newSettings = TemplaterSettings()
        newSettings.loadState(state)


        assertTrue("Parallel execution should be persisted", newSettings.enableParallelExecution)
        assertFalse("Syntax validation should be persisted", newSettings.enableSyntaxValidation)
        assertEquals("Cancel behavior should be persisted", CancelBehavior.KEEP_EXPRESSION, newSettings.cancelBehavior)
        assertEquals("Popup behavior should be persisted", PopupBehavior.NEVER, newSettings.popupBehaviorHotkey)
        assertTrue("Execution stats should be persisted", newSettings.showExecutionStats)
    }

    @Test
    fun `test mermaid settings persistence`() {
        val settings = TemplaterSettings()


        settings.enableMermaidExport = true
        settings.mermaidOutputLocation = MermaidOutputLocation.DEDICATED_FOLDER
        settings.mermaidOutputFolder = "/custom/path"
        settings.includeMermaidExplanation = false


        val state = settings.getState()
        val newSettings = TemplaterSettings()
        newSettings.loadState(state)


        assertTrue("Mermaid export should be persisted", newSettings.enableMermaidExport)
        assertEquals("Mermaid output location should be persisted", MermaidOutputLocation.DEDICATED_FOLDER, newSettings.mermaidOutputLocation)
        assertEquals("Mermaid output folder should be persisted", "/custom/path", newSettings.mermaidOutputFolder)
        assertFalse("Mermaid explanation setting should be persisted", newSettings.includeMermaidExplanation)
    }

    @Test
    fun `test mermaid node styles persistence`() {
        val settings = TemplaterSettings()


        settings.mermaidStyleStartEnd = "fill:#ff0000,stroke:#000"
        settings.mermaidStyleCondition = "fill:#00ff00,stroke:#000"


        val state = settings.getState()
        val newSettings = TemplaterSettings()
        newSettings.loadState(state)


        assertEquals("Start/End style should be persisted", "fill:#ff0000,stroke:#000", newSettings.mermaidStyleStartEnd)
        assertEquals("Condition style should be persisted", "fill:#00ff00,stroke:#000", newSettings.mermaidStyleCondition)
    }
}

