package ronsijm.templater.standalone.settings

import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.settings.MermaidNodeStyle
import ronsijm.templater.settings.MermaidNodeStyles
import ronsijm.templater.settings.MermaidOutputLocation
import ronsijm.templater.settings.PopupBehavior
import ronsijm.templater.ui.settings.CommonUISettings
import ronsijm.templater.ui.settings.MermaidDiagramType
import java.awt.event.KeyEvent
import java.util.prefs.Preferences

object AppSettings : CommonUISettings {
    private val prefs = Preferences.userNodeForPackage(AppSettings::class.java)

    enum class FileOpenBehavior {
        CURRENT_WINDOW,
        NEW_TAB
    }

    enum class AfterRunningBehavior {
        OVERWRITE_AUTOMATICALLY,
        SAVE_SIDE_BY_SIDE
    }

    private const val KEY_FILE_OPEN_BEHAVIOR = "file_open_behavior"
    private const val KEY_TOGGLE_BREAKPOINT = "hotkey_toggle_breakpoint"
    private const val KEY_AFTER_RUNNING_BEHAVIOR = "after_running_behavior"
    private const val KEY_SIDE_BY_SIDE_POSTFIX = "side_by_side_postfix"
    private const val KEY_SHOW_DIALOG_AFTER_RUN = "show_dialog_after_run"
    private const val KEY_ENABLE_PARALLEL_EXECUTION = "enable_parallel_execution"
    private const val KEY_ENABLE_SYNTAX_VALIDATION = "enable_syntax_validation"
    private const val KEY_ENABLE_SELECTION_ONLY = "enable_selection_only"
    private const val KEY_CANCEL_BEHAVIOR = "cancel_behavior"
    private const val KEY_POPUP_BEHAVIOR_HOTKEY = "popup_behavior_hotkey"
    private const val KEY_POPUP_BEHAVIOR_GUTTER = "popup_behavior_gutter"
    private const val KEY_SHOW_EXECUTION_STATS = "show_execution_stats"
    private const val KEY_ENABLE_PROFILING = "enable_profiling"
    private const val KEY_ENABLE_MERMAID_EXPORT = "enable_mermaid_export"
    private const val KEY_MERMAID_OUTPUT_LOCATION = "mermaid_output_location"
    private const val KEY_MERMAID_OUTPUT_FOLDER = "mermaid_output_folder"
    private const val KEY_INCLUDE_MERMAID_EXPLANATION = "include_mermaid_explanation"
    private const val KEY_MERMAID_DIAGRAM_TYPE = "mermaid_diagram_type"
    private const val KEY_DEBUG_INCREMENTAL_UPDATES = "debug_incremental_updates"
    private const val KEY_CONTROL_FLOW_PANEL_VISIBLE = "control_flow_panel_visible"
    private const val KEY_MERMAID_PANEL_VISIBLE = "mermaid_panel_visible"
    private const val KEY_VARIABLES_PANEL_VISIBLE = "variables_panel_visible"
    private const val KEY_SHOW_RUN_GUTTER_ICONS = "show_run_gutter_icons"
    private const val KEY_SHOW_BREAKPOINT_GUTTER_ICONS = "show_breakpoint_gutter_icons"
    private const val KEY_SHOW_SUCCESS_NOTIFICATIONS = "show_success_notifications"
    private const val KEY_SHOW_ERROR_NOTIFICATIONS = "show_error_notifications"
    private const val KEY_AUTO_OPEN_LAST_FOLDER = "auto_open_last_folder"
    private const val KEY_ENABLE_STEP_BY_STEP_MODE = "enable_step_by_step_mode"
    private const val KEY_STEP_DELAY_MILLISECONDS = "step_delay_milliseconds"

    private const val DEFAULT_FILE_OPEN_BEHAVIOR = "CURRENT_WINDOW"
    private const val DEFAULT_TOGGLE_BREAKPOINT = "F9"
    private const val DEFAULT_AFTER_RUNNING_BEHAVIOR = "OVERWRITE_AUTOMATICALLY"
    private const val DEFAULT_SIDE_BY_SIDE_POSTFIX = ".output"
    private const val DEFAULT_SHOW_DIALOG_AFTER_RUN = true

    fun getFileOpenBehavior(): FileOpenBehavior {
        val value = prefs.get(KEY_FILE_OPEN_BEHAVIOR, DEFAULT_FILE_OPEN_BEHAVIOR)
        return try {
            FileOpenBehavior.valueOf(value)
        } catch (e: IllegalArgumentException) {
            FileOpenBehavior.CURRENT_WINDOW
        }
    }

    fun setFileOpenBehavior(behavior: FileOpenBehavior) {
        prefs.put(KEY_FILE_OPEN_BEHAVIOR, behavior.name)
    }

    fun getToggleBreakpointHotkey(): String {
        return prefs.get(KEY_TOGGLE_BREAKPOINT, DEFAULT_TOGGLE_BREAKPOINT)
    }

    fun setToggleBreakpointHotkey(hotkey: String) {
        prefs.put(KEY_TOGGLE_BREAKPOINT, hotkey)
    }

    fun parseHotkey(hotkey: String): javax.swing.KeyStroke? {
        return try {
            when (hotkey.uppercase()) {
                "F9" -> javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)
                "F8" -> javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0)
                "F10" -> javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0)
                "CTRL+B" -> javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK)
                else -> javax.swing.KeyStroke.getKeyStroke(hotkey)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getAfterRunningBehavior(): AfterRunningBehavior {
        val value = prefs.get(KEY_AFTER_RUNNING_BEHAVIOR, DEFAULT_AFTER_RUNNING_BEHAVIOR)
        return try {
            AfterRunningBehavior.valueOf(value)
        } catch (e: IllegalArgumentException) {
            AfterRunningBehavior.OVERWRITE_AUTOMATICALLY
        }
    }

    fun setAfterRunningBehavior(behavior: AfterRunningBehavior) {
        prefs.put(KEY_AFTER_RUNNING_BEHAVIOR, behavior.name)
    }

    fun getSideBySidePostfix(): String {
        return prefs.get(KEY_SIDE_BY_SIDE_POSTFIX, DEFAULT_SIDE_BY_SIDE_POSTFIX)
    }

    fun setSideBySidePostfix(postfix: String) {
        prefs.put(KEY_SIDE_BY_SIDE_POSTFIX, postfix)
    }

    fun getShowDialogAfterRun(): Boolean {
        return prefs.getBoolean(KEY_SHOW_DIALOG_AFTER_RUN, DEFAULT_SHOW_DIALOG_AFTER_RUN)
    }

    fun setShowDialogAfterRun(show: Boolean) {
        prefs.putBoolean(KEY_SHOW_DIALOG_AFTER_RUN, show)
    }

    override var enableParallelExecution: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_PARALLEL_EXECUTION, false)
        set(value) = prefs.putBoolean(KEY_ENABLE_PARALLEL_EXECUTION, value)

    override var enableSyntaxValidation: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_SYNTAX_VALIDATION, true)
        set(value) = prefs.putBoolean(KEY_ENABLE_SYNTAX_VALIDATION, value)

    override var enableSelectionOnlyExecution: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_SELECTION_ONLY, true)
        set(value) = prefs.putBoolean(KEY_ENABLE_SELECTION_ONLY, value)

    override var cancelBehavior: CancelBehavior
        get() = try {
            CancelBehavior.valueOf(prefs.get(KEY_CANCEL_BEHAVIOR, CancelBehavior.REMOVE_EXPRESSION.name))
        } catch (e: Exception) { CancelBehavior.REMOVE_EXPRESSION }
        set(value) = prefs.put(KEY_CANCEL_BEHAVIOR, value.name)

    override var popupBehaviorHotkey: PopupBehavior
        get() = try {
            PopupBehavior.valueOf(prefs.get(KEY_POPUP_BEHAVIOR_HOTKEY, PopupBehavior.ALWAYS.name))
        } catch (e: Exception) { PopupBehavior.ALWAYS }
        set(value) = prefs.put(KEY_POPUP_BEHAVIOR_HOTKEY, value.name)

    override var popupBehaviorGutter: PopupBehavior
        get() = try {
            PopupBehavior.valueOf(prefs.get(KEY_POPUP_BEHAVIOR_GUTTER, PopupBehavior.ONLY_ON_ERROR.name))
        } catch (e: Exception) { PopupBehavior.ONLY_ON_ERROR }
        set(value) = prefs.put(KEY_POPUP_BEHAVIOR_GUTTER, value.name)

    override var showExecutionStats: Boolean
        get() = prefs.getBoolean(KEY_SHOW_EXECUTION_STATS, false)
        set(value) = prefs.putBoolean(KEY_SHOW_EXECUTION_STATS, value)

    override var enablePerformanceProfiling: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_PROFILING, false)
        set(value) = prefs.putBoolean(KEY_ENABLE_PROFILING, value)

    override var enableMermaidExport: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_MERMAID_EXPORT, false)
        set(value) = prefs.putBoolean(KEY_ENABLE_MERMAID_EXPORT, value)

    override var mermaidOutputLocation: MermaidOutputLocation
        get() = try {
            MermaidOutputLocation.valueOf(prefs.get(KEY_MERMAID_OUTPUT_LOCATION, MermaidOutputLocation.SAME_AS_SCRIPT.name))
        } catch (e: Exception) { MermaidOutputLocation.SAME_AS_SCRIPT }
        set(value) = prefs.put(KEY_MERMAID_OUTPUT_LOCATION, value.name)

    override var mermaidOutputFolder: String
        get() = prefs.get(KEY_MERMAID_OUTPUT_FOLDER, "")
        set(value) = prefs.put(KEY_MERMAID_OUTPUT_FOLDER, value)

    override var includeMermaidExplanation: Boolean
        get() = prefs.getBoolean(KEY_INCLUDE_MERMAID_EXPLANATION, true)
        set(value) = prefs.putBoolean(KEY_INCLUDE_MERMAID_EXPLANATION, value)

    private val _mermaidNodeStyles = MermaidNodeStyles()
    override var mermaidNodeStyles: MermaidNodeStyles
        get() = _mermaidNodeStyles
        set(value) = _mermaidNodeStyles.loadFrom(value)

    override var debugIncrementalUpdates: Boolean
        get() = prefs.getBoolean(KEY_DEBUG_INCREMENTAL_UPDATES, true)
        set(value) = prefs.putBoolean(KEY_DEBUG_INCREMENTAL_UPDATES, value)

    override var controlFlowPanelVisible: Boolean
        get() = prefs.getBoolean(KEY_CONTROL_FLOW_PANEL_VISIBLE, true)
        set(value) = prefs.putBoolean(KEY_CONTROL_FLOW_PANEL_VISIBLE, value)

    override var mermaidPanelVisible: Boolean
        get() = prefs.getBoolean(KEY_MERMAID_PANEL_VISIBLE, false)
        set(value) = prefs.putBoolean(KEY_MERMAID_PANEL_VISIBLE, value)

    override var variablesPanelVisible: Boolean
        get() = prefs.getBoolean(KEY_VARIABLES_PANEL_VISIBLE, true)
        set(value) = prefs.putBoolean(KEY_VARIABLES_PANEL_VISIBLE, value)

    override var mermaidDiagramType: MermaidDiagramType
        get() = try {
            MermaidDiagramType.valueOf(prefs.get(KEY_MERMAID_DIAGRAM_TYPE, MermaidDiagramType.FLOWCHART.name))
        } catch (e: Exception) { MermaidDiagramType.FLOWCHART }
        set(value) = prefs.put(KEY_MERMAID_DIAGRAM_TYPE, value.name)

    override var showRunGutterIcons: Boolean
        get() = prefs.getBoolean(KEY_SHOW_RUN_GUTTER_ICONS, true)
        set(value) = prefs.putBoolean(KEY_SHOW_RUN_GUTTER_ICONS, value)

    override var showBreakpointGutterIcons: Boolean
        get() = prefs.getBoolean(KEY_SHOW_BREAKPOINT_GUTTER_ICONS, true)
        set(value) = prefs.putBoolean(KEY_SHOW_BREAKPOINT_GUTTER_ICONS, value)

    override var showSuccessNotifications: Boolean
        get() = prefs.getBoolean(KEY_SHOW_SUCCESS_NOTIFICATIONS, true)
        set(value) = prefs.putBoolean(KEY_SHOW_SUCCESS_NOTIFICATIONS, value)

    override var showErrorNotifications: Boolean
        get() = prefs.getBoolean(KEY_SHOW_ERROR_NOTIFICATIONS, true)
        set(value) = prefs.putBoolean(KEY_SHOW_ERROR_NOTIFICATIONS, value)

    var autoOpenLastFolder: Boolean
        get() = prefs.getBoolean(KEY_AUTO_OPEN_LAST_FOLDER, false)
        set(value) = prefs.putBoolean(KEY_AUTO_OPEN_LAST_FOLDER, value)

    override var enableStepByStepMode: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_STEP_BY_STEP_MODE, false)
        set(value) = prefs.putBoolean(KEY_ENABLE_STEP_BY_STEP_MODE, value)

    override var stepDelayMilliseconds: Int
        get() = prefs.getInt(KEY_STEP_DELAY_MILLISECONDS, 500)
        set(value) = prefs.putInt(KEY_STEP_DELAY_MILLISECONDS, value)
}

