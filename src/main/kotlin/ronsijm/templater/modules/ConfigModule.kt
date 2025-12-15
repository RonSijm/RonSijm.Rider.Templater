package ronsijm.templater.modules

import ronsijm.templater.parser.TemplateContext

/**
 * ConfigModule provides Templater runtime configuration
 * Implements tp.config.* properties
 */
class ConfigModule(private val context: TemplateContext) {
    
    /**
     * RunMode enum representing how Templater was launched
     */
    enum class RunMode {
        CREATE_NEW_FROM_TEMPLATE,
        APPEND_ACTIVE_FILE,
        OVERWRITE_FILE,
        OVERWRITE_ACTIVE_FILE,
        DYNAMIC_PROCESSOR,
        STARTUP_TEMPLATE
    }
    
    /**
     * Execute a config property access
     * @param property The property name
     * @return The property value
     */
    fun executeProperty(property: String): String? {
        return when (property) {
            "active_file" -> executeActiveFile()
            "run_mode" -> executeRunMode()
            "target_file" -> executeTargetFile()
            "template_file" -> executeTemplateFile()
            else -> null
        }
    }
    
    /**
     * tp.config.active_file
     * Returns the active file path when Templater was launched
     */
    private fun executeActiveFile(): String {
        return context.filePath ?: ""
    }
    
    /**
     * tp.config.run_mode
     * Returns how Templater was launched
     */
    private fun executeRunMode(): String {
        // For now, we always use DYNAMIC_PROCESSOR mode (template executed on current file)
        return RunMode.DYNAMIC_PROCESSOR.name
    }
    
    /**
     * tp.config.target_file
     * Returns the target file where template will be inserted
     */
    private fun executeTargetFile(): String {
        return context.filePath ?: ""
    }
    
    /**
     * tp.config.template_file
     * Returns the template file being executed
     * For inline templates, this is the same as the target file
     */
    private fun executeTemplateFile(): String {
        return context.filePath ?: ""
    }
}

