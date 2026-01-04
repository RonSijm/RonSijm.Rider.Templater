package ronsijm.templater.ui.settings

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.settings.MermaidOutputLocation
import ronsijm.templater.settings.PopupBehavior

class CommonUISettingsTest {

    @Test
    fun `DefaultCommonUISettings has sensible defaults`() {
        val settings = DefaultCommonUISettings()


        assertFalse(settings.enableParallelExecution)
        assertTrue(settings.enableSyntaxValidation)
        assertTrue(settings.enableSelectionOnlyExecution)
        assertEquals(CancelBehavior.REMOVE_EXPRESSION, settings.cancelBehavior)
        assertEquals(PopupBehavior.ALWAYS, settings.popupBehaviorHotkey)
        assertEquals(PopupBehavior.ONLY_ON_ERROR, settings.popupBehaviorGutter)
        assertFalse(settings.showExecutionStats)
        assertFalse(settings.enablePerformanceProfiling)
        assertFalse(settings.enableMermaidExport)
        assertEquals(MermaidOutputLocation.SAME_AS_SCRIPT, settings.mermaidOutputLocation)
        assertTrue(settings.debugIncrementalUpdates)


        assertTrue(settings.controlFlowPanelVisible)
        assertFalse(settings.mermaidPanelVisible)
        assertTrue(settings.variablesPanelVisible)

        assertEquals(MermaidDiagramType.FLOWCHART, settings.mermaidDiagramType)

        assertTrue(settings.showRunGutterIcons)
        assertTrue(settings.showBreakpointGutterIcons)

        assertTrue(settings.showSuccessNotifications)
        assertTrue(settings.showErrorNotifications)
    }

    @Test
    fun `copyFrom copies all settings`() {
        val source = DefaultCommonUISettings().apply {
            enableParallelExecution = true
            enableSyntaxValidation = false
            enableSelectionOnlyExecution = false
            cancelBehavior = CancelBehavior.KEEP_EXPRESSION
            popupBehaviorHotkey = PopupBehavior.NEVER
            popupBehaviorGutter = PopupBehavior.ALWAYS
            showExecutionStats = true
            enablePerformanceProfiling = true
            enableMermaidExport = true
            mermaidOutputLocation = MermaidOutputLocation.DEDICATED_FOLDER
            debugIncrementalUpdates = false

            controlFlowPanelVisible = false
            mermaidPanelVisible = true
            variablesPanelVisible = false

            mermaidDiagramType = MermaidDiagramType.SEQUENCE

            showRunGutterIcons = false
            showBreakpointGutterIcons = false

            showSuccessNotifications = false
            showErrorNotifications = false
        }

        val target = DefaultCommonUISettings()
        target.copyFrom(source)

        assertEquals(source.enableParallelExecution, target.enableParallelExecution)
        assertEquals(source.enableSyntaxValidation, target.enableSyntaxValidation)
        assertEquals(source.enableSelectionOnlyExecution, target.enableSelectionOnlyExecution)
        assertEquals(source.cancelBehavior, target.cancelBehavior)
        assertEquals(source.popupBehaviorHotkey, target.popupBehaviorHotkey)
        assertEquals(source.popupBehaviorGutter, target.popupBehaviorGutter)
        assertEquals(source.showExecutionStats, target.showExecutionStats)
        assertEquals(source.enablePerformanceProfiling, target.enablePerformanceProfiling)
        assertEquals(source.enableMermaidExport, target.enableMermaidExport)
        assertEquals(source.mermaidOutputLocation, target.mermaidOutputLocation)
        assertEquals(source.debugIncrementalUpdates, target.debugIncrementalUpdates)

        assertEquals(source.controlFlowPanelVisible, target.controlFlowPanelVisible)
        assertEquals(source.mermaidPanelVisible, target.mermaidPanelVisible)
        assertEquals(source.variablesPanelVisible, target.variablesPanelVisible)

        assertEquals(source.mermaidDiagramType, target.mermaidDiagramType)

        assertEquals(source.showRunGutterIcons, target.showRunGutterIcons)
        assertEquals(source.showBreakpointGutterIcons, target.showBreakpointGutterIcons)

        assertEquals(source.showSuccessNotifications, target.showSuccessNotifications)
        assertEquals(source.showErrorNotifications, target.showErrorNotifications)
    }

    @Test
    fun `copy creates independent copy`() {
        val original = DefaultCommonUISettings()
        original.enableParallelExecution = true
        original.controlFlowPanelVisible = false

        val copy = original.copy()

        assertEquals(original.enableParallelExecution, copy.enableParallelExecution)
        assertEquals(original.controlFlowPanelVisible, copy.controlFlowPanelVisible)


        copy.enableParallelExecution = false
        copy.controlFlowPanelVisible = true

        assertTrue(original.enableParallelExecution)
        assertFalse(original.controlFlowPanelVisible)
    }

    @Test
    fun `mermaidNodeStyles is initialized`() {
        val settings = DefaultCommonUISettings()
        assertNotNull(settings.mermaidNodeStyles)
    }

    @Test
    fun `copyFrom copies mermaidNodeStyles values`() {
        val source = DefaultCommonUISettings()
        val target = DefaultCommonUISettings()


        source.mermaidNodeStyles.startEnd = ronsijm.templater.settings.MermaidNodeStyle("#fff", "#000", "3px")

        target.copyFrom(source)


        assertEquals("#fff", target.mermaidNodeStyles.startEnd.fill)
        assertEquals("#000", target.mermaidNodeStyles.startEnd.stroke)
    }
}

class MermaidDiagramTypeTest {

    @Test
    fun `all enum values exist`() {
        assertEquals(2, MermaidDiagramType.entries.size)
        assertNotNull(MermaidDiagramType.FLOWCHART)
        assertNotNull(MermaidDiagramType.SEQUENCE)
    }
}

