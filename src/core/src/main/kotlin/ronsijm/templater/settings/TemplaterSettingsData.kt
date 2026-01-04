package ronsijm.templater.settings


interface TemplaterSettingsData {
    var enableParallelExecution: Boolean
    var enableSyntaxValidation: Boolean
    var enableSelectionOnlyExecution: Boolean
    var cancelBehavior: CancelBehavior


    var popupBehaviorHotkey: PopupBehavior
    var popupBehaviorGutter: PopupBehavior
    var showExecutionStats: Boolean


    var enablePerformanceProfiling: Boolean


    var enableMermaidExport: Boolean
    var mermaidOutputLocation: MermaidOutputLocation
    var mermaidOutputFolder: String
    var includeMermaidExplanation: Boolean
    var mermaidNodeStyles: MermaidNodeStyles


    var debugIncrementalUpdates: Boolean


    var enableStepByStepMode: Boolean
    var stepDelayMilliseconds: Int


    @Deprecated("Use popupBehaviorHotkey instead", ReplaceWith("popupBehaviorHotkey"))
    var popupBehavior: PopupBehavior
        get() = popupBehaviorHotkey
        set(value) { popupBehaviorHotkey = value }
}


class SimpleTemplaterSettings(
    override var enableParallelExecution: Boolean = false,
    override var enableSyntaxValidation: Boolean = true,
    override var enableSelectionOnlyExecution: Boolean = true,
    override var cancelBehavior: CancelBehavior = CancelBehavior.REMOVE_EXPRESSION,
    override var popupBehaviorHotkey: PopupBehavior = PopupBehavior.ALWAYS,
    override var popupBehaviorGutter: PopupBehavior = PopupBehavior.ONLY_ON_ERROR,
    override var showExecutionStats: Boolean = false,
    override var enablePerformanceProfiling: Boolean = false,
    override var enableMermaidExport: Boolean = false,
    override var mermaidOutputLocation: MermaidOutputLocation = MermaidOutputLocation.SAME_AS_SCRIPT,
    override var mermaidOutputFolder: String = "",
    override var includeMermaidExplanation: Boolean = true,
    override var mermaidNodeStyles: MermaidNodeStyles = MermaidNodeStyles(),
    override var debugIncrementalUpdates: Boolean = true,
    override var enableStepByStepMode: Boolean = false,
    override var stepDelayMilliseconds: Int = 500
) : TemplaterSettingsData {


    fun loadFrom(other: TemplaterSettingsData) {
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
    }
}
