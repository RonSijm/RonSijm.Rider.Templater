package ronsijm.templater.completion


object CompletionContextAnalyzer {


    sealed class CompletionContext {

        object NotInTemplate : CompletionContext()


        data class ModuleSuggestion(val prefix: String = "") : CompletionContext()


        data class FunctionSuggestion(val module: String, val partial: String) : CompletionContext()
    }


    fun analyzeContext(textBeforeCursor: String): CompletionContext {
        val lastOpenTag = textBeforeCursor.lastIndexOf("<%")
        val lastCloseTag = textBeforeCursor.lastIndexOf("%>")


        if (lastOpenTag == -1 || lastCloseTag > lastOpenTag) {
            return CompletionContext.NotInTemplate
        }


        val templateContent = textBeforeCursor.substring(lastOpenTag + 2).trim()


        val normalizedContent = when {
            templateContent.startsWith("*") -> templateContent.substring(1).trim()
            templateContent.startsWith("-") -> templateContent.substring(1).trim()
            else -> templateContent
        }

        return when {
            normalizedContent.isEmpty() || normalizedContent == "t" || normalizedContent == "tp" -> {
                CompletionContext.ModuleSuggestion(normalizedContent)
            }
            normalizedContent.startsWith("tp.") -> {
                val afterTp = normalizedContent.substring(3)
                when {
                    !afterTp.contains(".") -> {
                        CompletionContext.ModuleSuggestion(afterTp)
                    }
                    else -> {
                        val parts = afterTp.split(".")
                        val module = parts[0]
                        val partial = parts.getOrNull(1) ?: ""
                        CompletionContext.FunctionSuggestion(module, partial)
                    }
                }
            }
            else -> CompletionContext.NotInTemplate
        }
    }


    val availableModules = listOf(
        ModuleInfo("date", "Date and time functions", "tp.date."),
        ModuleInfo("file", "File operations and metadata", "tp.file."),
        ModuleInfo("system", "System interactions (prompts, dialogs)", "tp.system."),
        ModuleInfo("frontmatter", "Access frontmatter properties", "tp.frontmatter."),
        ModuleInfo("web", "Web requests and online content", "tp.web."),
        ModuleInfo("hooks", "Lifecycle hooks", "tp.hooks."),
        ModuleInfo("config", "Templater configuration", "tp.config."),
        ModuleInfo("app", "Rider/IDE integration", "tp.app.")
    )


    fun getMatchingModules(prefix: String): List<ModuleInfo> {
        return availableModules.filter { it.name.startsWith(prefix, ignoreCase = true) }
    }


    val hooksFunctions = listOf(
        FunctionInfo("on_all_templates_executed", "on_all_templates_executed(callback)", "Execute callback after all templates finish", "callback")
    )

    val configProperties = listOf(
        FunctionInfo("active_file", "active_file", "Active file when Templater was launched", "property"),
        FunctionInfo("run_mode", "run_mode", "How Templater was launched", "property"),
        FunctionInfo("target_file", "target_file", "Target file where template will be inserted", "property"),
        FunctionInfo("template_file", "template_file", "Template file being executed", "property")
    )

    val appApis = listOf(
        FunctionInfo("vault", "vault", "Access to project file system", "API"),
        FunctionInfo("fileManager", "fileManager", "File management operations", "API"),
        FunctionInfo("workspace", "workspace", "Workspace and editor operations", "API"),
        FunctionInfo("commands", "commands", "Execute IDE actions", "API")
    )


    fun getStaticFunctions(module: String, partial: String): List<FunctionInfo> {
        val functions = when (module) {
            "hooks" -> hooksFunctions
            "config" -> configProperties
            "app" -> appApis
            else -> emptyList()
        }
        return functions.filter { it.name.startsWith(partial, ignoreCase = true) }
    }

    data class ModuleInfo(val name: String, val description: String, val insertText: String)
    data class FunctionInfo(val name: String, val insertText: String, val description: String, val params: String)
}

