package ronsijm.templater.settings

/**
 * Interface for Templater settings data.
 * This abstraction allows tests to work without IntelliJ Platform dependencies.
 */
interface TemplaterSettingsData {
    var enableParallelExecution: Boolean
    var enableSyntaxValidation: Boolean
    var showExecutionStats: Boolean
    var enableSelectionOnlyExecution: Boolean
    var popupBehavior: PopupBehavior
    var cancelBehavior: CancelBehavior
}

/**
 * Simple implementation of TemplaterSettingsData for testing purposes.
 * Does not depend on IntelliJ Platform.
 */
class SimpleTemplaterSettings(
    override var enableParallelExecution: Boolean = false,
    override var enableSyntaxValidation: Boolean = true,
    override var showExecutionStats: Boolean = false,
    override var enableSelectionOnlyExecution: Boolean = true,
    override var popupBehavior: PopupBehavior = PopupBehavior.ALWAYS,
    override var cancelBehavior: CancelBehavior = CancelBehavior.REMOVE_EXPRESSION
) : TemplaterSettingsData {

    /**
     * Copy values from another settings instance
     */
    fun loadFrom(other: TemplaterSettingsData) {
        enableParallelExecution = other.enableParallelExecution
        enableSyntaxValidation = other.enableSyntaxValidation
        showExecutionStats = other.showExecutionStats
        enableSelectionOnlyExecution = other.enableSelectionOnlyExecution
        popupBehavior = other.popupBehavior
        cancelBehavior = other.cancelBehavior
    }
}

