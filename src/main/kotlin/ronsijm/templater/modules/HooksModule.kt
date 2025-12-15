package ronsijm.templater.modules

import ronsijm.templater.parser.TemplateContext

/**
 * HooksModule provides lifecycle hooks for templates
 * Implements tp.hooks.* functions
 */
class HooksModule(private val context: TemplateContext) {
    
    private val onAllTemplatesExecutedCallbacks = mutableListOf<() -> Unit>()
    
    /**
     * Execute a hooks command
     * @param function The function name
     * @param args The function arguments (callback function)
     * @return Always returns empty string (hooks don't output)
     */
    fun executeCommand(function: String, args: List<Any?>): String {
        return when (function) {
            "on_all_templates_executed" -> {
                executeOnAllTemplatesExecuted(args)
                ""
            }
            else -> ""
        }
    }
    
    /**
     * tp.hooks.on_all_templates_executed(callback)
     * Register a callback to be executed after all templates finish
     */
    @Suppress("UNCHECKED_CAST")
    private fun executeOnAllTemplatesExecuted(args: List<Any?>) {
        if (args.isEmpty()) {
            return
        }

        // The callback is passed from the context
        val callback = args[0] as? (() -> Unit) ?: return
        onAllTemplatesExecutedCallbacks.add(callback)
    }
    
    /**
     * Execute all registered callbacks
     * This should be called by the template executor after all templates finish
     */
    fun executeAllCallbacks() {
        // Execute all callbacks in parallel (as per Templater spec)
        onAllTemplatesExecutedCallbacks.forEach { callback ->
            try {
                callback()
            } catch (e: Exception) {
                // Silently ignore callback errors
            }
        }
        onAllTemplatesExecutedCallbacks.clear()
    }
    
    /**
     * Check if there are any registered callbacks
     */
    fun hasCallbacks(): Boolean {
        return onAllTemplatesExecutedCallbacks.isNotEmpty()
    }
}

