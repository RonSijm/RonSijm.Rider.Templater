package ronsijm.templater.modules

import ronsijm.templater.parser.TemplateContext


class HooksModule(private val context: TemplateContext) {

    private val onAllTemplatesExecutedCallbacks = mutableListOf<() -> Unit>()


    fun executeCommand(function: String, args: List<Any?>): String {
        return when (function) {
            "on_all_templates_executed" -> {
                executeOnAllTemplatesExecuted(args)
                ""
            }
            else -> ""
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun executeOnAllTemplatesExecuted(args: List<Any?>) {
        if (args.isEmpty()) {
            return
        }


        val callback = args[0] as? (() -> Unit) ?: return
        onAllTemplatesExecutedCallbacks.add(callback)
    }


    fun executeAllCallbacks() {

        onAllTemplatesExecutedCallbacks.forEach { callback ->
            try {
                callback()
            } catch (e: Exception) {

            }
        }
        onAllTemplatesExecutedCallbacks.clear()
    }


    fun hasCallbacks(): Boolean {
        return onAllTemplatesExecutedCallbacks.isNotEmpty()
    }
}
