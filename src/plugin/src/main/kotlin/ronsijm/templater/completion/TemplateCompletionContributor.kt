package ronsijm.templater.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.FrontmatterParser


class TemplateCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            TemplateCompletionProvider()
        )
    }
}


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

            text = text.replace("IntellijIdeaRulezzz", "")

            val elementOffset = element.textRange.startOffset
            val cursorOffset = parameters.offset
            val relativeOffset = cursorOffset - elementOffset

            val textBeforeCursor = text.substring(0, minOf(relativeOffset, text.length))


            when (val completionContext = CompletionContextAnalyzer.analyzeContext(textBeforeCursor)) {
                is CompletionContextAnalyzer.CompletionContext.NotInTemplate -> return
                is CompletionContextAnalyzer.CompletionContext.ModuleSuggestion -> {
                    addModuleSuggestions(resultSet, completionContext.prefix)
                }
                is CompletionContextAnalyzer.CompletionContext.FunctionSuggestion -> {
                    addFunctionSuggestions(resultSet, completionContext.module, completionContext.partial, parameters)
                }
            }
        } catch (e: Exception) {

        }
    }


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


    private fun addFunctionSuggestions(
        resultSet: CompletionResultSet,
        module: String,
        partial: String,
        parameters: CompletionParameters
    ) {
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

    private fun addDateCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = HandlerRegistry.allModules["date"]?.map { command ->
            FunctionSuggestion(
                name = command.metadata.name,
                insertText = command.metadata.example,
                description = command.metadata.description,
                params = command.metadata.parameters
            )
        } ?: emptyList()
        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }

    private fun addFileCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = HandlerRegistry.allModules["file"]?.map { command ->
            FunctionSuggestion(
                name = command.metadata.name,
                insertText = command.metadata.example,
                description = command.metadata.description,
                params = command.metadata.parameters
            )
        } ?: emptyList()
        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }


    private fun addSystemCompletions(resultSet: CompletionResultSet, partial: String) {
        val systemCommands = HandlerRegistry.allModules["system"] ?: emptyList()
        val functions = systemCommands.map { command ->
            FunctionSuggestion(
                name = command.metadata.name,
                insertText = command.metadata.example,
                description = command.metadata.description,
                params = command.metadata.parameters
            )
        }
        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }


    private fun addFrontmatterCompletions(
        resultSet: CompletionResultSet,
        partial: String,
        parameters: CompletionParameters
    ) {
        val file = parameters.originalFile
        val text = file.text

        try {
            val frontmatterParser = FrontmatterParser()
            val parseResult = frontmatterParser.parse(text)

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
            val commonKeys = listOf("title", "date", "tags", "author", "description")
            commonKeys.forEach { key ->
                if (key.startsWith(partial, ignoreCase = true)) {
                    resultSet.addElement(LookupElementBuilder.create(key))
                }
            }
        }
    }

    private fun addWebCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = HandlerRegistry.allModules["web"]?.map { command ->
            FunctionSuggestion(
                name = command.metadata.name,
                insertText = command.metadata.example,
                description = command.metadata.description,
                params = command.metadata.parameters
            )
        } ?: emptyList()
        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }


    private fun addHooksCompletions(resultSet: CompletionResultSet, partial: String) {
        val functions = listOf(
            FunctionSuggestion("on_all_templates_executed", "on_all_templates_executed(callback)", "Execute callback after all templates finish", "callback")
        )

        addFunctionSuggestionsToResult(resultSet, functions, partial)
    }


    private fun addConfigCompletions(resultSet: CompletionResultSet, partial: String) {
        val properties = listOf(
            FunctionSuggestion("active_file", "active_file", "Active file when Templater was launched", "property"),
            FunctionSuggestion("run_mode", "run_mode", "How Templater was launched", "property"),
            FunctionSuggestion("target_file", "target_file", "Target file where template will be inserted", "property"),
            FunctionSuggestion("template_file", "template_file", "Template file being executed", "property")
        )

        addFunctionSuggestionsToResult(resultSet, properties, partial)
    }


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
