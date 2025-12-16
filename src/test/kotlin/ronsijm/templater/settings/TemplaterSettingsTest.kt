package ronsijm.templater.settings

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for TemplaterSettings using SimpleTemplaterSettings (no IntelliJ dependency)
 */
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
        assertEquals(PopupBehavior.ALWAYS, settings.popupBehavior)
    }

    @Test
    fun `test SimpleTemplaterSettings popupBehavior can be changed`() {
        val settings = SimpleTemplaterSettings()

        settings.popupBehavior = PopupBehavior.ONLY_ON_ERROR
        assertEquals(PopupBehavior.ONLY_ON_ERROR, settings.popupBehavior)

        settings.popupBehavior = PopupBehavior.NEVER
        assertEquals(PopupBehavior.NEVER, settings.popupBehavior)

        settings.popupBehavior = PopupBehavior.ALWAYS
        assertEquals(PopupBehavior.ALWAYS, settings.popupBehavior)
    }

    @Test
    fun `test SimpleTemplaterSettings implements TemplaterSettingsData`() {
        val settings: TemplaterSettingsData = SimpleTemplaterSettings()
        settings.popupBehavior = PopupBehavior.NEVER

        assertEquals(PopupBehavior.NEVER, settings.popupBehavior)
    }

    @Test
    fun `test SimpleTemplaterSettings loadFrom copies values`() {
        val settings = SimpleTemplaterSettings()
        val sourceSettings = SimpleTemplaterSettings(
            enableParallelExecution = true,
            enableSyntaxValidation = false,
            showExecutionStats = true,
            enableSelectionOnlyExecution = false,
            popupBehavior = PopupBehavior.ONLY_ON_ERROR
        )

        settings.loadFrom(sourceSettings)

        assertTrue(settings.enableParallelExecution)
        assertFalse(settings.enableSyntaxValidation)
        assertTrue(settings.showExecutionStats)
        assertFalse(settings.enableSelectionOnlyExecution)
        assertEquals(PopupBehavior.ONLY_ON_ERROR, settings.popupBehavior)
    }

    @Test
    fun `test popup behavior logic - ALWAYS shows on success`() {
        val settings = SimpleTemplaterSettings()
        settings.popupBehavior = PopupBehavior.ALWAYS

        val shouldShowOnSuccess = settings.popupBehavior == PopupBehavior.ALWAYS
        val shouldShowOnError = settings.popupBehavior != PopupBehavior.NEVER

        assertTrue(shouldShowOnSuccess)
        assertTrue(shouldShowOnError)
    }

    @Test
    fun `test popup behavior logic - ONLY_ON_ERROR hides success`() {
        val settings = SimpleTemplaterSettings()
        settings.popupBehavior = PopupBehavior.ONLY_ON_ERROR

        val shouldShowOnSuccess = settings.popupBehavior == PopupBehavior.ALWAYS
        val shouldShowOnError = settings.popupBehavior != PopupBehavior.NEVER

        assertFalse(shouldShowOnSuccess)
        assertTrue(shouldShowOnError)
    }

    @Test
    fun `test popup behavior logic - NEVER hides all popups`() {
        val settings = SimpleTemplaterSettings()
        settings.popupBehavior = PopupBehavior.NEVER

        val shouldShowOnSuccess = settings.popupBehavior == PopupBehavior.ALWAYS
        val shouldShowOnError = settings.popupBehavior != PopupBehavior.NEVER

        assertFalse(shouldShowOnSuccess)
        assertFalse(shouldShowOnError)
    }
}

