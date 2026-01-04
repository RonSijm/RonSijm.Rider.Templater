package ronsijm.templater.modules

import ronsijm.templater.parser.TemplateContext

class ConfigModule(private val context: TemplateContext) {

    fun executeProperty(property: String): String? {
        return when (property) {
            "active_file" -> executeActiveFile()
            "run_mode" -> executeRunMode()
            "target_file" -> executeTargetFile()
            "template_file" -> executeTemplateFile()
            else -> null
        }
    }

    private fun executeActiveFile(): String {
        return context.filePath ?: ""
    }

    private fun executeRunMode(): String {

        return RunMode.DYNAMIC_PROCESSOR.name
    }

    private fun executeTargetFile(): String {
        return context.filePath ?: ""
    }

    private fun executeTemplateFile(): String {
        return context.filePath ?: ""
    }
}
