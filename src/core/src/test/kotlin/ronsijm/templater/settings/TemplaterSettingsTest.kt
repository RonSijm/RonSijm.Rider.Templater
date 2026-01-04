package ronsijm.templater.settings

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class TemplaterSettingsTest {

    @Test
    fun `test PopupBehavior enum values`() {
        val values = PopupBehavior.values()

        assertEquals(3, values.size)
        assertEquals(PopupBehavior.ALWAYS, values[0])
        assertEquals(PopupBehavior.ONLY_ON_ERROR, values[1])
        assertEquals(PopupBehavior.NEVER, values[2])
    }

    @Test
    fun `test PopupBehavior ordinal values for combo box mapping`() {
        assertEquals(0, PopupBehavior.ALWAYS.ordinal)
        assertEquals(1, PopupBehavior.ONLY_ON_ERROR.ordinal)
        assertEquals(2, PopupBehavior.NEVER.ordinal)
    }

    @Test
    fun `test PopupBehavior valueOf`() {
        assertEquals(PopupBehavior.ALWAYS, PopupBehavior.valueOf("ALWAYS"))
        assertEquals(PopupBehavior.ONLY_ON_ERROR, PopupBehavior.valueOf("ONLY_ON_ERROR"))
        assertEquals(PopupBehavior.NEVER, PopupBehavior.valueOf("NEVER"))
    }

    @Test
    fun `test SimpleTemplaterSettings default values`() {
        val settings = SimpleTemplaterSettings()

        assertFalse(settings.enableParallelExecution)
        assertTrue(settings.enableSyntaxValidation)
        assertFalse(settings.showExecutionStats)
        assertTrue(settings.enableSelectionOnlyExecution)
        assertEquals(PopupBehavior.ALWAYS, settings.popupBehaviorHotkey)
        assertEquals(PopupBehavior.ONLY_ON_ERROR, settings.popupBehaviorGutter)
        assertFalse(settings.enableMermaidExport)
        assertEquals(MermaidOutputLocation.SAME_AS_SCRIPT, settings.mermaidOutputLocation)
        assertEquals("", settings.mermaidOutputFolder)
        assertTrue(settings.includeMermaidExplanation)
    }

    @Test
    fun `test SimpleTemplaterSettings popupBehaviorHotkey can be changed`() {
        val settings = SimpleTemplaterSettings()

        settings.popupBehaviorHotkey = PopupBehavior.ONLY_ON_ERROR
        assertEquals(PopupBehavior.ONLY_ON_ERROR, settings.popupBehaviorHotkey)

        settings.popupBehaviorHotkey = PopupBehavior.NEVER
        assertEquals(PopupBehavior.NEVER, settings.popupBehaviorHotkey)

        settings.popupBehaviorHotkey = PopupBehavior.ALWAYS
        assertEquals(PopupBehavior.ALWAYS, settings.popupBehaviorHotkey)
    }

    @Test
    fun `test SimpleTemplaterSettings popupBehaviorGutter can be changed`() {
        val settings = SimpleTemplaterSettings()

        settings.popupBehaviorGutter = PopupBehavior.ALWAYS
        assertEquals(PopupBehavior.ALWAYS, settings.popupBehaviorGutter)

        settings.popupBehaviorGutter = PopupBehavior.NEVER
        assertEquals(PopupBehavior.NEVER, settings.popupBehaviorGutter)

        settings.popupBehaviorGutter = PopupBehavior.ONLY_ON_ERROR
        assertEquals(PopupBehavior.ONLY_ON_ERROR, settings.popupBehaviorGutter)
    }

    @Test
    fun `test SimpleTemplaterSettings implements TemplaterSettingsData`() {
        val settings: TemplaterSettingsData = SimpleTemplaterSettings()
        settings.popupBehaviorHotkey = PopupBehavior.NEVER
        settings.popupBehaviorGutter = PopupBehavior.ALWAYS

        assertEquals(PopupBehavior.NEVER, settings.popupBehaviorHotkey)
        assertEquals(PopupBehavior.ALWAYS, settings.popupBehaviorGutter)
    }

    @Test
    fun `test SimpleTemplaterSettings loadFrom copies values`() {
        val settings = SimpleTemplaterSettings()
        val sourceSettings = SimpleTemplaterSettings(
            enableParallelExecution = true,
            enableSyntaxValidation = false,
            showExecutionStats = true,
            enableSelectionOnlyExecution = false,
            popupBehaviorHotkey = PopupBehavior.ONLY_ON_ERROR,
            popupBehaviorGutter = PopupBehavior.NEVER,
            enableMermaidExport = true,
            mermaidOutputLocation = MermaidOutputLocation.DEDICATED_FOLDER,
            mermaidOutputFolder = "/custom/path",
            includeMermaidExplanation = false
        )

        settings.loadFrom(sourceSettings)

        assertTrue(settings.enableParallelExecution)
        assertFalse(settings.enableSyntaxValidation)
        assertTrue(settings.showExecutionStats)
        assertFalse(settings.enableSelectionOnlyExecution)
        assertEquals(PopupBehavior.ONLY_ON_ERROR, settings.popupBehaviorHotkey)
        assertEquals(PopupBehavior.NEVER, settings.popupBehaviorGutter)
        assertTrue(settings.enableMermaidExport)
        assertEquals(MermaidOutputLocation.DEDICATED_FOLDER, settings.mermaidOutputLocation)
        assertEquals("/custom/path", settings.mermaidOutputFolder)
        assertFalse(settings.includeMermaidExplanation)
    }

    @Test
    fun `test MermaidOutputLocation enum values`() {
        val values = MermaidOutputLocation.values()

        assertEquals(2, values.size)
        assertEquals(MermaidOutputLocation.SAME_AS_SCRIPT, values[0])
        assertEquals(MermaidOutputLocation.DEDICATED_FOLDER, values[1])
    }

    @Test
    fun `test MermaidOutputLocation ordinal values for combo box mapping`() {
        assertEquals(0, MermaidOutputLocation.SAME_AS_SCRIPT.ordinal)
        assertEquals(1, MermaidOutputLocation.DEDICATED_FOLDER.ordinal)
    }

    @Test
    fun `test popup behavior logic - ALWAYS shows on success`() {
        val settings = SimpleTemplaterSettings()
        settings.popupBehaviorHotkey = PopupBehavior.ALWAYS

        val shouldShowOnSuccess = settings.popupBehaviorHotkey == PopupBehavior.ALWAYS
        val shouldShowOnError = settings.popupBehaviorHotkey != PopupBehavior.NEVER

        assertTrue(shouldShowOnSuccess)
        assertTrue(shouldShowOnError)
    }

    @Test
    fun `test popup behavior logic - ONLY_ON_ERROR hides success`() {
        val settings = SimpleTemplaterSettings()
        settings.popupBehaviorHotkey = PopupBehavior.ONLY_ON_ERROR

        val shouldShowOnSuccess = settings.popupBehaviorHotkey == PopupBehavior.ALWAYS
        val shouldShowOnError = settings.popupBehaviorHotkey != PopupBehavior.NEVER

        assertFalse(shouldShowOnSuccess)
        assertTrue(shouldShowOnError)
    }

    @Test
    fun `test popup behavior logic - NEVER hides all popups`() {
        val settings = SimpleTemplaterSettings()
        settings.popupBehaviorHotkey = PopupBehavior.NEVER

        val shouldShowOnSuccess = settings.popupBehaviorHotkey == PopupBehavior.ALWAYS
        val shouldShowOnError = settings.popupBehaviorHotkey != PopupBehavior.NEVER

        assertFalse(shouldShowOnSuccess)
        assertFalse(shouldShowOnError)
    }
}
