package ronsijm.templater.debug

import ronsijm.templater.ast.ASTToControlFlowConverter
import ronsijm.templater.ast.TemplateASTBuilder
import ronsijm.templater.modules.AppModuleProvider
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.settings.MermaidNodeStyles
import ronsijm.templater.utils.CancellationChecker
import ronsijm.templater.utils.NoCancellationChecker


class TracingTemplateParser(
    private val validateSyntax: Boolean = true,
    private val services: ServiceContainer = ServiceContainer()
) {
    private val parser = TemplateParser(validateSyntax, services)
    private val astBuilder = TemplateASTBuilder()
    private val astToGraphConverter = ASTToControlFlowConverter()
    private val exporter = MermaidExporter()

    private var controlFlowGraph: ControlFlowGraph? = null


    fun getControlFlowGraph(): ControlFlowGraph? = controlFlowGraph


    fun parse(
        content: String,
        context: TemplateContext,
        appModuleProvider: AppModuleProvider = NullAppModuleProvider,
        cancellationChecker: CancellationChecker = NoCancellationChecker
    ): String {

        val ast = astBuilder.build(content)


        controlFlowGraph = astToGraphConverter.convert(ast)


        return parser.parse(content, context, appModuleProvider, cancellationChecker)
    }


    fun exportMermaidFlowchart(
        title: String = "Template Execution Flow",
        includeExplanations: Boolean = true,
        nodeStyles: MermaidNodeStyles? = null
    ): String {
        val graph = controlFlowGraph ?: return "flowchart TD\n    empty[No template blocks found]"
        return exporter.exportFlowchart(graph, title, includeExplanations, nodeStyles)
    }


    fun exportMermaidSequence(title: String = "Template Execution"): String {
        val graph = controlFlowGraph ?: return "sequenceDiagram\n    Note over Parser: No template blocks found"
        return exporter.exportSequenceDiagram(graph, title)
    }
}

