package ronsijm.templater.standalone.services

import ronsijm.templater.cli.CliFileOperationsService
import ronsijm.templater.debug.ControlFlowGraph
import ronsijm.templater.debug.TracingTemplateParser
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.services.DefaultHttpService
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.SystemClipboardService
import ronsijm.templater.services.SystemOperationsService
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.services.mock.NullFileOperationService
import java.io.File

object TemplateExecutionService {

    private val defaultSystemOperationsService: SystemOperationsService by lazy {
        SwingSystemOperationsService()
    }

    data class ContextResult(
        val context: TemplateContext,
        val frontmatterResult: FrontmatterParser.ParseResult,
        val services: ServiceContainer
    )

    data class ExecutionResult(
        val output: String,
        val success: Boolean,
        val error: Exception? = null
    )

    data class TracingResult(
        val graph: ControlFlowGraph?,
        val success: Boolean,
        val error: Exception? = null
    )

    fun createContext(
        content: String,
        file: File? = null,
        systemOperationsService: SystemOperationsService = defaultSystemOperationsService
    ): ContextResult {
        val frontmatterParser = FrontmatterParser()
        val frontmatterResult = frontmatterParser.parse(content)


        val fileOperationsService = if (file != null) {
            CliFileOperationsService(file, file.parentFile ?: File("."))
        } else {
            null
        }

        val services = ServiceContainer(
            clipboardService = SystemClipboardService(),
            httpService = DefaultHttpService(),
            fileOperationService = fileOperationsService ?: NullFileOperationService,
            systemOperationsService = systemOperationsService
        )

        val context = TemplateContext(
            frontmatter = frontmatterResult.frontmatter,
            frontmatterParser = frontmatterParser,
            fileName = file?.name ?: "template.md",
            filePath = file?.absolutePath ?: "",
            fileContent = content,
            services = services
        )

        return ContextResult(context, frontmatterResult, services)
    }

    fun execute(content: String, file: File? = null, validateSyntax: Boolean = true): ExecutionResult {
        return try {
            val contextResult = createContext(content, file)
            val parser = TemplateParser(validateSyntax = validateSyntax, services = contextResult.services)
            val output = parser.parse(
                content = contextResult.frontmatterResult.content,
                context = contextResult.context,
                appModuleProvider = NullAppModuleProvider
            )
            ExecutionResult(output, success = true)
        } catch (e: Exception) {
            ExecutionResult("", success = false, error = e)
        }
    }

    fun trace(content: String, file: File? = null): TracingResult {
        return try {
            val contextResult = createContext(content, file)
            val tracingParser = TracingTemplateParser(
                validateSyntax = false,
                services = contextResult.services
            )




            tracingParser.parse(content, contextResult.context, NullAppModuleProvider)
            val graph = tracingParser.getControlFlowGraph()
            TracingResult(graph, success = true)
        } catch (e: Exception) {
            TracingResult(null, success = false, error = e)
        }
    }

    fun getTracingParser(content: String, file: File? = null): Pair<TracingTemplateParser, ContextResult>? {
        return try {
            val contextResult = createContext(content, file)
            val tracingParser = TracingTemplateParser(
                validateSyntax = false,
                services = contextResult.services
            )


            tracingParser.parse(content, contextResult.context, NullAppModuleProvider)
            Pair(tracingParser, contextResult)
        } catch (e: Exception) {
            null
        }
    }

    fun calculateFrontmatterOffset(content: String): Int {
        val frontmatterParser = FrontmatterParser()
        val frontmatterResult = frontmatterParser.parse(content)

        return if (frontmatterResult.hasFrontmatter) {

            frontmatterResult.frontmatterRaw.count { it == '\n' } + 3
        } else {
            0
        }
    }
}

