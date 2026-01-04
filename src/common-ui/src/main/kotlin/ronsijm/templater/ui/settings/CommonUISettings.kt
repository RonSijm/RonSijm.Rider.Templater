package ronsijm.templater.ui.settings

import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.settings.MermaidNodeStyles
import ronsijm.templater.settings.MermaidOutputLocation
import ronsijm.templater.settings.PopupBehavior
import ronsijm.templater.settings.TemplaterSettingsData

interface CommonUISettings : TemplaterSettingsData {


    var controlFlowPanelVisible: Boolean
    var mermaidPanelVisible: Boolean
    var variablesPanelVisible: Boolean


    var mermaidDiagramType: MermaidDiagramType


    var showRunGutterIcons: Boolean
    var showBreakpointGutterIcons: Boolean


    var showSuccessNotifications: Boolean
    var showErrorNotifications: Boolean
}


enum class MermaidDiagramType {
    FLOWCHART,
    SEQUENCE
}


open class DefaultCommonUISettings : CommonUISettings {


    override var enableParallelExecution: Boolean = false
    override var enableSyntaxValidation: Boolean = true
    override var enableSelectionOnlyExecution: Boolean = true
    override var cancelBehavior: CancelBehavior = CancelBehavior.REMOVE_EXPRESSION
    override var popupBehaviorHotkey: PopupBehavior = PopupBehavior.ALWAYS
    override var popupBehaviorGutter: PopupBehavior = PopupBehavior.ONLY_ON_ERROR
    override var showExecutionStats: Boolean = false
    override var enablePerformanceProfiling: Boolean = false
    override var enableMermaidExport: Boolean = false
    override var mermaidOutputLocation: MermaidOutputLocation = MermaidOutputLocation.SAME_AS_SCRIPT
    override var mermaidOutputFolder: String = ""
    override var includeMermaidExplanation: Boolean = true
    override var mermaidNodeStyles: MermaidNodeStyles = MermaidNodeStyles()
    override var debugIncrementalUpdates: Boolean = true
    override var enableStepByStepMode: Boolean = false
    override var stepDelayMilliseconds: Int = 500


    override var controlFlowPanelVisible: Boolean = true
    override var mermaidPanelVisible: Boolean = false
    override var variablesPanelVisible: Boolean = true

    override var mermaidDiagramType: MermaidDiagramType = MermaidDiagramType.FLOWCHART

    override var showRunGutterIcons: Boolean = true
    override var showBreakpointGutterIcons: Boolean = true

    override var showSuccessNotifications: Boolean = true
    override var showErrorNotifications: Boolean = true


    fun copyFrom(other: CommonUISettings) {
        enableParallelExecution = other.enableParallelExecution
        enableSyntaxValidation = other.enableSyntaxValidation
        enableSelectionOnlyExecution = other.enableSelectionOnlyExecution
        cancelBehavior = other.cancelBehavior
        popupBehaviorHotkey = other.popupBehaviorHotkey
        popupBehaviorGutter = other.popupBehaviorGutter
        showExecutionStats = other.showExecutionStats
        enablePerformanceProfiling = other.enablePerformanceProfiling
        enableMermaidExport = other.enableMermaidExport
        mermaidOutputLocation = other.mermaidOutputLocation
        mermaidOutputFolder = other.mermaidOutputFolder
        includeMermaidExplanation = other.includeMermaidExplanation
        mermaidNodeStyles.loadFrom(other.mermaidNodeStyles)
        debugIncrementalUpdates = other.debugIncrementalUpdates
        enableStepByStepMode = other.enableStepByStepMode
        stepDelayMilliseconds = other.stepDelayMilliseconds

        controlFlowPanelVisible = other.controlFlowPanelVisible
        mermaidPanelVisible = other.mermaidPanelVisible
        variablesPanelVisible = other.variablesPanelVisible

        mermaidDiagramType = other.mermaidDiagramType

        showRunGutterIcons = other.showRunGutterIcons
        showBreakpointGutterIcons = other.showBreakpointGutterIcons

        showSuccessNotifications = other.showSuccessNotifications
        showErrorNotifications = other.showErrorNotifications
    }


    fun copy(): DefaultCommonUISettings {
        val copy = DefaultCommonUISettings()
        copy.copyFrom(this)
        return copy
    }
}

