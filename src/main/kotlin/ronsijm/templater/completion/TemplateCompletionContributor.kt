package ronsijm.templater.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.FrontmatterParser

/**
 * Provides code completion for Templater syntax in markdown files
 * Triggers on "<% tp." and provides suggestions for modules and functions
 */
class TemplateCompletionContributor : CompletionContributor() {

    init {
        // Trigger completion for any text in markdown files
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            TemplateCompletionProvider()
        )
    }
}

/**
 * Completion provider that handles template syntax completion
 */
class TemplateCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        try {
            val position = parameters.position
            val element = position.parent ?: position
            var text = element.text

            // Remove IntelliJ's completion placeholder if present
            text = text.replace("IntellijIdeaRulezzz", "")

            // Calculate offset relative to element start
            val elementOffset = element.textRange.startOffset
            val cursorOffset = parameters.offset
            val relativeOffset = cursorOffset - elementOffset

            // Get text before cursor
            val textBeforeCursor = text.substring(0, minOf(relativeOffset, text.length))

            // Check if we're inside a template tag
            val lastOpenTag = textBeforeCursor.lastIndexOf("<%")
            val lastCloseTag = textBeforeCursor.lastIndexOf("%>")

            // Only provide completions if we're inside a template tag
            if (lastOpenTag == -1 || lastCloseTag > lastOpenTag) {
                return
            }

            // Get the text after the opening tag
            val templateContent = textBeforeCursor.substring(lastOpenTag + 2).trim()

            // Determine what to complete based on context
            when {
                // User typed "tp" or "tp."
                templateContent.isEmpty() || templateContent == "t" || templateContent == "tp" -> {
                    addModuleSuggestions(resultSet)
                }
                templateContent.startsWith("tp.") -> {
                    val afterTp = templateContent.substring(3)
                    when {
                        // User is typing module name
                        !afterTp.contains(".") -> {
                            addModuleSuggestions(resultSet, afterTp)
                        }
                        // User is typing function/property name
                        else -> {
                            val parts = afterTp.split(".")
                            val module = parts[0]
                            val partial = parts.getOrNull(1) ?: ""
                            addFunctionSuggestions(resultSet, module, partial, parameters)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Silently fail - don't crash completion
        }
    }

    /**
     * Add module suggestions (tp.date, tp.file, tp.system, tp.frontmatter, tp.web, tp.hooks, tp.config, tp.app)
     */
    private fun addModuleSuggestions(resultSet: CompletionResultSet, prefix: String = "") {
        val modules = listOf(
            ModuleSuggestion("date", "Date and time functions", "tp.date."),
            ModuleSuggestion("file", "File operations and metadata", "tp.file."),
            ModuleSuggestion("system", "System interactions (prompts, dialogs)", "tp.system."),
            ModuleSuggestion("frontmatter", "Access frontmatter properties", "tp.frontmatter."),
            ModuleSuggestion("web", "Web requests and online content", "tp.web."),
            ModuleSuggestion("hooks", "Lifecycle hooks", "tp.hooks."),
            ModuleSuggestion("config", "Templater configuration", "tp.config."),
            ModuleSuggestion("app", "Rider/IDE integration", "tp.app.")
        )
        
        modules.forEach { module ->
            if (module.name.startsWith(prefix, ignoreCase = true)) {
                resultSet.addElement(
                    LookupElementBuilder.create(module.insertText)
                        .withPresentableText(module.name)
                        .withTypeText(module.description)
                        .bold()
                )
            }
        }
    }

    /**
     * Add function/property suggestions based on module
     */
    private fun addFunctionSuggestions(
        resultSet: CompletionResultSet,
        module: String,
        partial: String,
        parameters: CompletionParameters
    ) {
        // Use a custom prefix matcher to ensure IntelliJ doesn't filter our results
        val customResultSet = resultSet.withPrefixMatcher(partial)

        when (module) {
            "date" -> addDateCompletions(customResultSet, partial)
            "file" -> addFileCompletions(customResultSet, partial)
            "system" -> addSystemCompletions(customResultSet, partial)
            "frontmatter" -> addFrontmatterCompletions(customResultSet, partial, parameters)
            "web" -> addWebCompletions(customResultSet, partial)
            "hooks" -> addHooksCompletions(customResultSet, partial)
            "config" -> addConfigCompletions(customResultSet, partial)
            "app" -> addAppCompletions(customResultSet, partial)
        }
    }

    /**
     * Add date module completions
     * Auto-discovers commands from HandlerRegistry
     */
    private fun addDateCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = HandlerRegistry.dateCommands.map { command ->
            FunctionSuggestion(
                name = command.metadata.name,
                insertText = command.metadata.example,
                description = command.metadata.description,
                params = command.metadata.parameters
            )
        }
        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }

    /**
     * Add file module completions
     * Auto-discovers commands from HandlerRegistry
     */
    private fun addFileCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = HandlerRegistry.fileCommands.map { command ->
            FunctionSuggestion(
                name = command.metadata.name,
                insertText = command.metadata.example,
                description = command.metadata.description,
                params = command.metadata.parameters
            )
        }
        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }

    /**
     * Add system module completions
     * Auto-discovers commands from HandlerRegistry
     */
    private fun addSystemCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = HandlerRegistry.systemCommands.map { command ->
            FunctionSuggestion(
                name = command.metadata.name,
                insertText = command.metadata.example,
                description = command.metadata.description,
                params = command.metadata.parameters
            )
        }
        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }

    /**
     * Add frontmatter module completions
     * Dynamically suggests keys from the file's frontmatter
     */
    private fun addFrontmatterCompletions(
        resultSet: CompletionResultSet,
        partial: String,
        parameters: CompletionParameters
    ) {
        // Try to parse frontmatter from the current file
        val file = parameters.originalFile
        val text = file.text

        try {
            val frontmatterParser = FrontmatterParser()
            val parseResult = frontmatterParser.parse(text)

            // Add suggestions for each frontmatter key
            parseResult.frontmatter.keys.forEach { key ->
                if (key.startsWith(partial, ignoreCase = true)) {
                    val value = parseResult.frontmatter[key]
                    val typeText = when (value) {
                        is String -> "string"
                        is Number -> "number"
                        is Boolean -> "boolean"
                        is List<*> -> "array"
                        is Map<*, *> -> "object"
                        else -> "value"
                    }

                    resultSet.addElement(
                        LookupElementBuilder.create(key)
                            .withTailText(" = ${value.toString().take(30)}", true)
                    )
                }
            }
        } catch (e: Exception) {
            // If frontmatter parsing fails, provide generic suggestions
            val commonKeys = listOf("title", "date", "tags", "author", "description")
            commonKeys.forEach { key ->
                if (key.startsWith(partial, ignoreCase = true)) {
                    resultSet.addElement(LookupElementBuilder.create(key))
                }
            }
        }
    }

    /**
     * Add web module completions
     * Auto-discovers commands from HandlerRegistry
     */
    private fun addWebCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = HandlerRegistry.webCommands.map { command ->
            FunctionSuggestion(
                name = command.metadata.name,
                insertText = command.metadata.example,
                description = command.metadata.description,
                params = command.metadata.parameters
            )
        }
        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }

    /**
     * Add hooks module completions
     */
    private fun addHooksCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = listOf(
            FunctionSuggestion("on_all_templates_executed", "on_all_templates_executed(callback)", "Execute callback after all templates finish", "callback")
        )

        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }

    /**
     * Add config module completions
     */
    private fun addConfigCompletions(resultSet: CompletionResultSet, partial: String) {
        val properties = listOf(
            FunctionSuggestion("active_file", "active_file", "Active file when Templater was launched", "property"),
            FunctionSuggestion("run_mode", "run_mode", "How Templater was launched", "property"),
            FunctionSuggestion("target_file", "target_file", "Target file where template will be inserted", "property"),
            FunctionSuggestion("template_file", "template_file", "Template file being executed", "property")
        )

        addFunctionSuggestionsToResult(resultSet, properties, partial)
    }

    /**
     * Add app module completions
     */
    private fun addAppCompletions(resultSet: CompletionResultSet, partial: String) {
        val apis = listOf(
            FunctionSuggestion("vault", "vault", "Access to project file system", "API"),
            FunctionSuggestion("fileManager", "fileManager", "File management operations", "API"),
            FunctionSuggestion("workspace", "workspace", "Workspace and editor operations", "API"),
            FunctionSuggestion("commands", "commands", "Execute IDE actions", "API")
        )

        addFunctionSuggestionsToResult(resultSet, apis, partial)
    }

    private data class ModuleSuggestion(val name: String, val description: String, val insertText: String)
    private data class FunctionSuggestion(val name: String, val insertText: String, val description: String, val params: String)

    private fun addFunctionSuggestionsToResult(resultSet: CompletionResultSet, functions: List<FunctionSuggestion>, partial: String) {
        functions.forEach { func ->
            if (func.name.startsWith(partial, ignoreCase = true)) {
                resultSet.addElement(
                    LookupElementBuilder.create(func.insertText)
                        .withPresentableText(func.name)
                        .withTypeText(func.description)
                        .withTailText(" (${func.params})", true)
                )
            }
        }
    }
}

