package ronsijm.templater.debug

import ronsijm.templater.ast.StatementNode
import ronsijm.templater.ast.TemplateAST
import ronsijm.templater.ast.TemplateASTBuilder
import ronsijm.templater.debug.visualization.DataStructureSnapshot
import ronsijm.templater.debug.visualization.StateChange
import ronsijm.templater.debug.visualization.StateChangeDetector
import ronsijm.templater.modules.AppModuleProvider
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.script.ExecutionAction
import ronsijm.templater.script.ScriptExecutionCallback
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.utils.CancellationChecker
import ronsijm.templater.utils.Logging
import ronsijm.templater.utils.NoCancellationChecker


class DebuggingTemplateParser(
    private val validateSyntax: Boolean = true,
    private val services: ServiceContainer = ServiceContainer()
) {
    companion object {
        private val LOG = Logging.getLogger<DebuggingTemplateParser>()
    }

    private val parser = TemplateParser(validateSyntax, services)
    private val astBuilder = TemplateASTBuilder()
    private val stateChangeDetector = StateChangeDetector()
    private var debugSession: DebugSession? = null
    private var onBlockProcessedCallback: ((String, String, String, Int) -> Unit)? = null
    private var templateAST: TemplateAST? = null
    private val previousSnapshots = mutableMapOf<String, DataStructureSnapshot>()
    private val pendingLineBreakpoints = mutableSetOf<Int>()


    private var sourceContent: String? = null
    private var sourceLines: List<String> = emptyList()
    private var documentLineOffset: Int = 0

    private val variableUpdaterWrapper = object : ronsijm.templater.script.MutableVariableUpdaterWrapper {
        @Volatile
        private var delegate: ronsijm.templater.script.VariableUpdater? = null

        override fun setDelegate(updater: ronsijm.templater.script.VariableUpdater?) {
            delegate = updater
        }

        override fun updateVariable(name: String, value: String): Boolean {
            return delegate?.updateVariable(name, value) ?: false
        }
    }

    fun getVariableUpdater(): ronsijm.templater.script.VariableUpdater {
        return variableUpdaterWrapper
    }

    fun startDebugSession(
        onBreakpoint: (DebugBreakpoint) -> DebugAction,
        onComplete: (ExecutionTrace) -> Unit = {},
        startInStepMode: Boolean = false,
        onBlockProcessed: ((String, String, String, Int) -> Unit)? = null
    ) {
        debugSession = DebugSession(onBreakpoint, onComplete, startInStepMode)
        onBlockProcessedCallback = onBlockProcessed
    }

    fun getDebugSession(): DebugSession? = debugSession

    fun getTemplateAST(): TemplateAST? = templateAST




    fun addBreakpoint(lineNumber: Int): String? {
        val ast = templateAST
        return if (ast != null) {

            addBreakpointOnLine(lineNumber)
        } else {

            pendingLineBreakpoints.add(lineNumber)
            null
        }
    }


    fun removeBreakpoint(lineNumber: Int) {

        pendingLineBreakpoints.remove(lineNumber)


        val ast = templateAST ?: return
        val nodesToRemove = ast.allStatements
            .filter { it.lineNumber == lineNumber }
        nodesToRemove.forEach { removeBreakpointOnNode(it) }
    }


    fun hasBreakpoint(lineNumber: Int): Boolean {
        val ast = templateAST ?: return false
        val session = debugSession ?: return false
        return ast.allStatements
            .filter { it.lineNumber == lineNumber }
            .any { session.hasBreakpoint(it) }
    }


    fun getBreakpoints(): Set<Int> {
        val resolvedBreakpoints = if (templateAST != null && debugSession != null) {
            val ast = templateAST!!
            val session = debugSession!!
            ast.allStatements
                .filter { it.lineNumber != null && session.hasBreakpoint(it) }
                .mapNotNull { it.lineNumber }
                .toSet()
        } else {
            emptySet()
        }


        return pendingLineBreakpoints + resolvedBreakpoints
    }


    private fun resolvePendingBreakpoints() {
        if (pendingLineBreakpoints.isEmpty()) return

        val linesToResolve = pendingLineBreakpoints.toList()
        pendingLineBreakpoints.clear()

        linesToResolve.forEach { lineNumber ->
            try {
                addBreakpointOnLine(lineNumber)
            } catch (e: BreakpointResolutionException) {
                LOG?.warn("Failed to resolve breakpoint at line $lineNumber: ${e.message}")

            }
        }
    }


    fun addBreakpointOnLine(lineNumber: Int): String {
        val ast = templateAST
            ?: throw BreakpointResolutionException.noAST(lineNumber)


        val candidates = ast.allStatements
            .filter { it.lineNumber != null && it.lineNumber >= lineNumber }
            .filter { isExecutableStatement(it) }

        LOG?.debug("Resolving breakpoint at line $lineNumber: found ${candidates.size} candidates")
        candidates.forEach {
            LOG?.debug("  Candidate: line ${it.lineNumber}, type ${it.type}, code: ${it.code.take(40)}")
        }

        val node = candidates.minByOrNull { it.lineNumber!! }

        if (node == null) {

            val sourceLineContent = getSourceLineContent(lineNumber)

            val nearestNode = ast.allStatements
                .filter { it.lineNumber != null && isExecutableStatement(it) }
                .minByOrNull { kotlin.math.abs(it.lineNumber!! - lineNumber) }

            throw BreakpointResolutionException.noExecutableStatement(
                lineNumber = lineNumber,
                sourceLineContent = sourceLineContent,
                nearestNode = nearestNode
            )
        }

        LOG?.debug("Selected node at line ${node.lineNumber}: ${node.code.take(40)}")


        validateNodeMatchesSource(node, lineNumber)

        val session = debugSession
            ?: throw BreakpointResolutionException.noDebugSession(lineNumber)

        session.addBreakpoint(node)
        LOG?.debug("Breakpoint added to session")
        return node.id
    }


    private fun getSourceLineContent(lineNumber: Int): String? {
        if (sourceLines.isEmpty()) return null
        val index = lineNumber - 1
        return if (index in sourceLines.indices) sourceLines[index] else null
    }


    private fun validateNodeMatchesSource(node: StatementNode, requestedLineNumber: Int) {
        val nodeLineNumber = node.lineNumber ?: return


        val sourceLineContent = getSourceLineContent(nodeLineNumber) ?: return


        val normalizedNodeCode = node.code.trim().lines().first().trim()
        val normalizedSourceLine = sourceLineContent.trim()





        val matches = normalizedSourceLine.contains(normalizedNodeCode) ||
            normalizedNodeCode.contains(normalizedSourceLine) ||

            normalizedNodeCode.take(20) in normalizedSourceLine ||
            normalizedSourceLine.take(20) in normalizedNodeCode

        if (!matches) {
            LOG?.warn(
                "AST node code mismatch at line $nodeLineNumber:\n" +
                "  Node code: '$normalizedNodeCode'\n" +
                "  Source line: '$normalizedSourceLine'"
            )
            throw BreakpointResolutionException.codeMismatch(
                lineNumber = requestedLineNumber,
                sourceLineContent = normalizedSourceLine,
                nodeCode = normalizedNodeCode,
                nodeLineNumber = nodeLineNumber
            )
        }

        LOG?.debug("Validated node matches source at line $nodeLineNumber")
    }


    fun removeBreakpointOnNode(node: StatementNode) {
        debugSession?.removeBreakpoint(node)
    }


    private fun isExecutableStatement(node: ronsijm.templater.ast.StatementNode): Boolean {
        return when (node.type) {
            ronsijm.templater.ast.StatementType.COMMENT -> false
            ronsijm.templater.ast.StatementType.BLOCK_START -> false
            ronsijm.templater.ast.StatementType.BLOCK_END -> false
            else -> true
        }
    }


    fun parse(
        content: String,
        context: TemplateContext,
        appModuleProvider: AppModuleProvider = NullAppModuleProvider,
        cancellationChecker: CancellationChecker = NoCancellationChecker,
        documentLineOffset: Int = 0
    ): DebugParseResult {

        this.sourceContent = content
        this.sourceLines = content.lines()
        this.documentLineOffset = documentLineOffset


        templateAST = astBuilder.build(content)


        resolvePendingBreakpoints()

        val session = debugSession ?: return DebugParseResult(
            result = parser.parse(content, context, appModuleProvider, cancellationChecker),
            trace = ExecutionTrace(),
            wasStopped = false
        )


        var action = session.recordStep(
            type = ExecutionStep.StepType.TEMPLATE_START,
            description = "Start template parsing",
            input = content.take(100) + if (content.length > 100) "..." else "",
            variables = context.frontmatter
        )

        if (action == DebugAction.STOP) {
            session.complete()
            return DebugParseResult(content, session.getTrace(), wasStopped = true)
        }


        var wasStopped = false
        val executionCallback = object : ScriptExecutionCallback {
            private var blockDepth = 0
            private var currentBlockLineNumber: Int = 1
            private var currentBlockContent: String = ""
            private var currentBlockLines: List<String> = emptyList()

            private var lastStatementDocumentLineNumber: Int? = null

            private var currentLoopHeaderLineNumber: Int? = null

            override fun setCurrentBlockLineNumber(lineNumber: Int) {
                currentBlockLineNumber = lineNumber
            }

            override fun setCurrentBlockContent(content: String) {
                currentBlockContent = content
                currentBlockLines = content.lines()
            }

            override fun getVariableUpdater(): ronsijm.templater.script.VariableUpdater {
                return variableUpdaterWrapper
            }

            override fun beforeStatement(
                node: ronsijm.templater.ast.StatementNode,
                variables: Map<String, Any?>
            ): ExecutionAction {



                val documentLineNumber = if (node.lineNumber != null) {
                    node.lineNumber + documentLineOffset
                } else {
                    null
                }


                lastStatementDocumentLineNumber = documentLineNumber

                val currentStepId = session.getTrace().size + 1
                val snapshots = DataStructureSnapshot.captureAll(currentStepId, variables)


                val stateChanges = mutableListOf<StateChange>()
                for (snapshot in snapshots) {
                    val previous = previousSnapshots[snapshot.variableName]
                    val changes = stateChangeDetector.detectChanges(previous, snapshot)
                    stateChanges.addAll(changes)
                }

                val comparisonChange = stateChangeDetector.detectComparisonFromStatement(node.code, "arr")
                if (comparisonChange != null) {
                    stateChanges.add(comparisonChange)
                }



                val debugAction = session.recordStep(
                    type = ExecutionStep.StepType.STATEMENT,
                    description = node.code.take(80) + if (node.code.length > 80) "..." else "",
                    input = node.code,
                    variables = variables,
                    statementNode = node,
                    dataSnapshots = snapshots,
                    stateChanges = stateChanges
                )

                return when (debugAction) {
                    DebugAction.STOP -> {
                        wasStopped = true
                        ExecutionAction.STOP
                    }
                    else -> ExecutionAction.CONTINUE
                }
            }

            override fun afterStatement(
                node: ronsijm.templater.ast.StatementNode,
                variables: Map<String, Any?>
            ) {
                val currentStepId = session.getTrace().size
                val snapshots = DataStructureSnapshot.captureAll(currentStepId, variables)

                for (snapshot in snapshots) {
                    previousSnapshots[snapshot.variableName] = snapshot
                }
            }

            override fun enterBlock(blockType: String, blockDescription: String) {
                blockDepth++

                session.recordStep(
                    type = ExecutionStep.StepType.BLOCK_START,
                    description = "$blockType: $blockDescription"
                )
            }

            override fun exitBlock(blockType: String) {
                blockDepth--

                session.recordStep(
                    type = ExecutionStep.StepType.BLOCK_END,
                    description = "End $blockType"
                )
            }

            override fun onLoopIteration(loopType: String, iterationNumber: Int, variables: Map<String, Any?>) {

                session.recordStep(
                    type = ExecutionStep.StepType.LOOP_ITERATION,
                    description = "Iteration $iterationNumber",
                    variables = variables
                )
            }

            override fun onBlockProcessed(
                originalBlock: String,
                replacement: String,
                currentDocument: String,
                lineNumber: Int
            ) {

                onBlockProcessedCallback?.invoke(originalBlock, replacement, currentDocument, lineNumber)
            }
        }



        val parsedResult = parser.parse(
            content,
            context,
            appModuleProvider,
            cancellationChecker,
            executionCallback,
            ast = templateAST
        )


        session.recordStep(
            type = ExecutionStep.StepType.TEMPLATE_END,
            description = "Template parsing complete",
            output = parsedResult.take(100) + if (parsedResult.length > 100) "..." else ""
        )

        session.complete()
        return DebugParseResult(parsedResult, session.getTrace(), wasStopped = wasStopped)
    }


    fun exportMermaidFlowchart(title: String = "Template Execution Flow"): String {
        val trace = debugSession?.getTrace() ?: return "flowchart TD\n    empty[No debug session]"
        return MermaidExporter().exportFlowchart(trace, title)
    }


    fun exportMermaidSequence(title: String = "Template Execution"): String {
        val trace = debugSession?.getTrace() ?: return "sequenceDiagram\n    Note over Parser: No debug session"
        return MermaidExporter().exportSequenceDiagram(trace, title)
    }
}


data class DebugParseResult(
    val result: String,
    val trace: ExecutionTrace,
    val wasStopped: Boolean
)
