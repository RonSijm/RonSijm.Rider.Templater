package ronsijm.templater.services

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import ronsijm.templater.debug.TracingTemplateParser
import ronsijm.templater.modules.IntelliJAppModuleProvider
import ronsijm.templater.parallel.ParallelTemplateParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.script.ScriptExecutor
import ronsijm.templater.settings.TemplaterSettings
import ronsijm.templater.utils.ProgressIndicatorCancellationChecker


data class TemplateParseResult(
    val content: String,
    val mermaidDiagram: String? = null
)


class TemplateExecutionService(
    private val project: Project,
    private val settings: TemplaterSettings,
    private val services: ServiceContainer
) {

    fun execute(
        content: String,
        context: TemplateContext,
        indicator: ProgressIndicator
    ): TemplateParseResult {
        configureProfiling()

        val cancellationChecker = ProgressIndicatorCancellationChecker(indicator)
        val appModuleProvider = IntelliJAppModuleProvider(context, project)


        if (settings.enableMermaidExport) {
            return executeWithTracing(content, context, appModuleProvider, cancellationChecker)
        }

        return executeStandard(content, context, appModuleProvider, cancellationChecker)
    }

    private fun configureProfiling() {
        ScriptExecutor.profilingEnabled = settings.enablePerformanceProfiling
        if (settings.enablePerformanceProfiling) {
            ScriptExecutor.resetProfiling()
        }
    }

    private fun executeWithTracing(
        content: String,
        context: TemplateContext,
        appModuleProvider: IntelliJAppModuleProvider,
        cancellationChecker: ProgressIndicatorCancellationChecker
    ): TemplateParseResult {
        val tracingParser = TracingTemplateParser(
            validateSyntax = settings.enableSyntaxValidation,
            services = services
        )
        val result = tracingParser.parse(content, context, appModuleProvider, cancellationChecker)
        val mermaid = tracingParser.exportMermaidFlowchart(
            title = context.fileName,
            includeExplanations = settings.includeMermaidExplanation,
            nodeStyles = settings.mermaidNodeStyles
        )
        return TemplateParseResult(result, mermaid)
    }

    private fun executeStandard(
        content: String,
        context: TemplateContext,
        appModuleProvider: IntelliJAppModuleProvider,
        cancellationChecker: ProgressIndicatorCancellationChecker
    ): TemplateParseResult {
        val result = if (settings.enableParallelExecution) {
            val parallelParser = ParallelTemplateParser(
                validateSyntax = settings.enableSyntaxValidation,
                services = services,
                enableParallel = true
            )
            parallelParser.parse(content, context, appModuleProvider, cancellationChecker)
        } else {
            val templateParser = TemplateParser(
                validateSyntax = settings.enableSyntaxValidation,
                services = services
            )
            templateParser.parse(content, context, appModuleProvider, cancellationChecker)
        }
        return TemplateParseResult(result)
    }
}

