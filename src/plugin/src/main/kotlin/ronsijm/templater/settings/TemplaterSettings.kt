package ronsijm.templater.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "TemplaterSettings",
    storages = [Storage("TemplaterSettings.xml")]
)
class TemplaterSettings : PersistentStateComponent<TemplaterSettings>, TemplaterSettingsData {


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
    override var debugIncrementalUpdates: Boolean = true
    override var mermaidOutputFolder: String = ""


    override var enableStepByStepMode: Boolean = false


    override var stepDelayMilliseconds: Int = 500
    override var includeMermaidExplanation: Boolean = true

    var mermaidStyleStartEnd: String = "fill:#9f9,stroke:#333,stroke-width:2px"
    var mermaidStyleCondition: String = "fill:#ffd700,stroke:#333,stroke-width:2px"
    var mermaidStyleLoop: String = "fill:#87ceeb,stroke:#333,stroke-width:2px"
    var mermaidStyleLoopEnd: String = "fill:#b0e0e6,stroke:#333,stroke-width:1px"
    var mermaidStyleInterpolation: String = "fill:#98fb98,stroke:#333,stroke-width:1px"
    var mermaidStyleExecution: String = "fill:#bbf,stroke:#333,stroke-width:1px"
    var mermaidStyleFuncDecl: String = "fill:#dda0dd,stroke:#333,stroke-width:1px"
    var mermaidStyleFuncCall: String = "fill:#ff9,stroke:#333,stroke-width:1px"
    var mermaidStyleVariable: String = "fill:#fbf,stroke:#333,stroke-width:1px"
    var mermaidStyleReturnNode: String = "fill:#ffa07a,stroke:#333,stroke-width:1px"
    var mermaidStyleFork: String = "fill:#ff6b6b,stroke:#333,stroke-width:2px"
    var mermaidStyleJoin: String = "fill:#4ecdc4,stroke:#333,stroke-width:2px"
    var mermaidStyleError: String = "fill:#f66,stroke:#333,stroke-width:2px"


    override var mermaidNodeStyles: MermaidNodeStyles
        get() = MermaidNodeStyles(
            startEnd = MermaidNodeStyle.fromClassDef(mermaidStyleStartEnd),
            condition = MermaidNodeStyle.fromClassDef(mermaidStyleCondition),
            loop = MermaidNodeStyle.fromClassDef(mermaidStyleLoop),
            loopEnd = MermaidNodeStyle.fromClassDef(mermaidStyleLoopEnd),
            interpolation = MermaidNodeStyle.fromClassDef(mermaidStyleInterpolation),
            execution = MermaidNodeStyle.fromClassDef(mermaidStyleExecution),
            funcDecl = MermaidNodeStyle.fromClassDef(mermaidStyleFuncDecl),
            funcCall = MermaidNodeStyle.fromClassDef(mermaidStyleFuncCall),
            variable = MermaidNodeStyle.fromClassDef(mermaidStyleVariable),
            returnNode = MermaidNodeStyle.fromClassDef(mermaidStyleReturnNode),
            fork = MermaidNodeStyle.fromClassDef(mermaidStyleFork),
            join = MermaidNodeStyle.fromClassDef(mermaidStyleJoin),
            error = MermaidNodeStyle.fromClassDef(mermaidStyleError)
        )
        set(value) {
            mermaidStyleStartEnd = value.startEnd.toClassDef()
            mermaidStyleCondition = value.condition.toClassDef()
            mermaidStyleLoop = value.loop.toClassDef()
            mermaidStyleLoopEnd = value.loopEnd.toClassDef()
            mermaidStyleInterpolation = value.interpolation.toClassDef()
            mermaidStyleExecution = value.execution.toClassDef()
            mermaidStyleFuncDecl = value.funcDecl.toClassDef()
            mermaidStyleFuncCall = value.funcCall.toClassDef()
            mermaidStyleVariable = value.variable.toClassDef()
            mermaidStyleReturnNode = value.returnNode.toClassDef()
            mermaidStyleFork = value.fork.toClassDef()
            mermaidStyleJoin = value.join.toClassDef()
            mermaidStyleError = value.error.toClassDef()
        }

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
