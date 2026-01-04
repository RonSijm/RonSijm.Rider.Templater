package ronsijm.templater.debug

import ronsijm.templater.ast.ASTToControlFlowConverter
import ronsijm.templater.ast.TemplateASTBuilder
import ronsijm.templater.common.TemplateSyntax
import ronsijm.templater.parallel.DependencyAnalyzer
import ronsijm.templater.parallel.TemplateBlock
import ronsijm.templater.parallel.BlockAnalysis
import ronsijm.templater.script.ScriptParser
import ronsijm.templater.utils.TextUtils
import ronsijm.templater.script.parser.FunctionParser


class ControlFlowBuilder(private val scriptParser: ScriptParser) {

    private val astBuilder = TemplateASTBuilder(scriptParser)
    private val astConverter = ASTToControlFlowConverter()


    private var nodeIdCounter = 0
    private var scopeIdCounter = 0
    private val nodes = mutableListOf<FlowNode>()
    private val edges = mutableListOf<FlowEdge>()
    private val parallelGroupExplanations = mutableListOf<ParallelGroupExplanation>()
    private val functionScopes = mutableListOf<FunctionScope>()
    private val dependencyAnalyzer = DependencyAnalyzer()


    fun buildFromTemplate(
        content: String,
        @Suppress("UNUSED_PARAMETER") templateRegex: Regex = TemplateSyntax.TEMPLATE_BLOCK_REGEX
    ): ControlFlowGraph {

        val ast = astBuilder.build(content)


        return astConverter.convert(ast)
    }


    @Deprecated("Use buildFromTemplate instead, which uses the AST-based approach")
    fun buildFromTemplateLegacy(content: String, templateRegex: Regex = TemplateSyntax.TEMPLATE_BLOCK_REGEX): ControlFlowGraph {
        nodeIdCounter = 0
        scopeIdCounter = 0
        nodes.clear()
        edges.clear()
        parallelGroupExplanations.clear()
        functionScopes.clear()


        val startNode = createNode(FlowNode.NodeType.START, "Start")


        val contentWithoutComments = TextUtils.removeHtmlComments(content)


        val matches = templateRegex.findAll(contentWithoutComments).toList()
        val templateBlocks = matches.mapIndexed { index, match ->
            val isExecution = match.groupValues[2] == "*"
            val command = match.groupValues[3].trim()
            TemplateBlock(
                id = index,
                matchText = match.value,
                command = command,
                isExecution = isExecution,
                leftTrim = match.groupValues[1],
                rightTrim = match.groupValues[4],
                originalStart = match.range.first,
                originalEnd = match.range.last
            )
        }


        val analyses = dependencyAnalyzer.analyzeAll(templateBlocks)


        val lastNodeId = buildGraphWithParallelism(contentWithoutComments, matches, analyses, startNode.id)


        val endNode = createNode(FlowNode.NodeType.END, "End")
        addEdge(lastNodeId, endNode.id)

        return ControlFlowGraph(nodes.toList(), edges.toList(), parallelGroupExplanations.toList(), functionScopes.toList())
    }


    private fun buildGraphWithParallelism(
        content: String,
        matches: List<MatchResult>,
        analyses: List<BlockAnalysis>,
        startNodeId: String
    ): String {
        if (analyses.isEmpty()) return startNodeId


        val groups = groupByDependencies(analyses)

        var lastNodeId = startNodeId

        for (group in groups) {
            if (group.size == 1) {

                val analysis = group[0]
                val match = matches[analysis.block.id]
                val lineNumber = TextUtils.calculateLineNumber(content, match.range.first)

                lastNodeId = if (analysis.block.isExecution) {
                    buildExecutionBlockFlow(analysis.block.command, lastNodeId, lineNumber)
                } else {
                    val node = createNode(
                        FlowNode.NodeType.INTERPOLATION,
                        truncateLabel(analysis.block.command),
                        analysis.block.command,
                        lineNumber
                    )
                    addEdge(lastNodeId, node.id)
                    node.id
                }
            } else {

                lastNodeId = buildParallelGroup(content, matches, group, lastNodeId)
            }
        }

        return lastNodeId
    }


    private fun groupByDependencies(analyses: List<BlockAnalysis>): List<List<BlockAnalysis>> {
        if (analyses.isEmpty()) return emptyList()

        val groups = mutableListOf<List<BlockAnalysis>>()
        var currentGroup = mutableListOf<BlockAnalysis>()

        val currentGroupVariables = mutableSetOf<String>()

        for (analysis in analyses) {

            val dependsOnCurrentGroup = analysis.variablesRead.any { it in currentGroupVariables } ||
                    analysis.isBarrier ||
                    (analysis.hasTrWrite && currentGroup.any { it.hasTrWrite })

            if (dependsOnCurrentGroup && currentGroup.isNotEmpty()) {

                groups.add(currentGroup.toList())
                currentGroup = mutableListOf()
                currentGroupVariables.clear()
            }

            currentGroup.add(analysis)
            currentGroupVariables.addAll(analysis.variablesWritten)
        }

        if (currentGroup.isNotEmpty()) {
            groups.add(currentGroup.toList())
        }

        return groups
    }


    private fun buildParallelGroup(
        content: String,
        matches: List<MatchResult>,
        group: List<BlockAnalysis>,
        entryNodeId: String
    ): String {

        val forkNode = createNode(FlowNode.NodeType.FORK, "fork (${group.size} parallel)")
        addEdge(entryNodeId, forkNode.id)


        val joinNode = createNode(FlowNode.NodeType.JOIN, "join")


        val blockDescriptions = mutableListOf<BlockDescription>()


        for (analysis in group) {
            val match = matches[analysis.block.id]
            val lineNumber = TextUtils.calculateLineNumber(content, match.range.first)


            blockDescriptions.add(BlockDescription(
                label = truncateLabel(analysis.block.command),
                variablesRead = analysis.variablesRead,
                variablesWritten = analysis.variablesWritten,
                hasTrWrite = analysis.hasTrWrite
            ))

            if (analysis.block.isExecution) {


                val node = createNode(
                    FlowNode.NodeType.EXECUTION,
                    truncateLabel(analysis.block.command),
                    analysis.block.command,
                    lineNumber
                )
                addEdge(forkNode.id, node.id, null, FlowEdge.EdgeType.PARALLEL)
                addEdge(node.id, joinNode.id)
            } else {

                val node = createNode(
                    FlowNode.NodeType.INTERPOLATION,
                    truncateLabel(analysis.block.command),
                    analysis.block.command,
                    lineNumber
                )
                addEdge(forkNode.id, node.id, null, FlowEdge.EdgeType.PARALLEL)
                addEdge(node.id, joinNode.id)
            }
        }


        val explanation = generateParallelExplanation(forkNode.id, group, blockDescriptions)
        parallelGroupExplanations.add(explanation)

        return joinNode.id
    }


    private fun generateParallelExplanation(
        forkNodeId: String,
        group: List<BlockAnalysis>,
        blockDescriptions: List<BlockDescription>
    ): ParallelGroupExplanation {
        val allReads = group.flatMap { it.variablesRead }.toSet()
        val allWrites = group.flatMap { it.variablesWritten }.toSet()
        val hasTrWrites = group.any { it.hasTrWrite }

        val reasonParts = mutableListOf<String>()


        val hasNoDependencies = group.all { analysis ->
            analysis.variablesRead.none { it in allWrites }
        }

        if (hasNoDependencies && allWrites.isEmpty() && allReads.isEmpty()) {
            reasonParts.add("All ${group.size} blocks are independent - they don't read or write any shared variables")
        } else if (hasNoDependencies && allWrites.isEmpty()) {
            reasonParts.add("All ${group.size} blocks only read variables (no writes) - safe to run in parallel")
            if (allReads.isNotEmpty()) {
                reasonParts.add("Variables read: ${allReads.filter { it != "tR" }.joinToString(", ").ifEmpty { "(none)" }}")
            }
        } else if (hasNoDependencies) {
            reasonParts.add("${group.size} blocks can run in parallel - no block reads a variable written by another block in this group")
            if (allWrites.isNotEmpty()) {
                reasonParts.add("Variables written: ${allWrites.filter { it != "tR" }.joinToString(", ").ifEmpty { "(none)" }}")
            }
            if (allReads.isNotEmpty()) {
                val externalReads = allReads - allWrites
                if (externalReads.isNotEmpty()) {
                    reasonParts.add("Variables read (from previous groups): ${externalReads.filter { it != "tR" }.joinToString(", ")}")
                }
            }
        }

        if (!hasTrWrites) {
            reasonParts.add("No blocks write to template output (tR) - order doesn't matter")
        }

        return ParallelGroupExplanation(
            forkNodeId = forkNodeId,
            blockCount = group.size,
            blockDescriptions = blockDescriptions,
            reason = reasonParts.joinToString(". ")
        )
    }


    private fun buildExecutionBlockFlow(code: String, entryNodeId: String, lineNumber: Int): String {
        val statements = scriptParser.splitStatements(code)
        if (statements.isEmpty()) {
            return entryNodeId
        }

        var lastNodeId = entryNodeId
        var i = 0

        while (i < statements.size) {
            val stmt = statements[i].trim()
            if (stmt.isEmpty() || stmt == "{" || stmt == "}") {
                i++
                continue
            }

            when {
                stmt.startsWith("if ") || stmt.startsWith("if(") -> {
                    val ifInfo = scriptParser.extractIfStatement(statements, i)
                    if (ifInfo != null) {
                        lastNodeId = buildIfFlow(ifInfo.first, ifInfo.second, ifInfo.third, lastNodeId, lineNumber)
                        i = ifInfo.fourth
                    } else {
                        lastNodeId = addStatementNode(stmt, lastNodeId)
                        i++
                    }
                }
                stmt.startsWith("for ") || stmt.startsWith("for(") -> {
                    val loopInfo = scriptParser.extractForLoop(statements, i)
                    if (loopInfo != null) {
                        lastNodeId = buildForLoopFlow(loopInfo.first, loopInfo.second, lastNodeId, lineNumber)
                        i = loopInfo.third
                    } else {
                        lastNodeId = addStatementNode(stmt, lastNodeId)
                        i++
                    }
                }
                stmt.startsWith("while ") || stmt.startsWith("while(") -> {
                    val loopInfo = scriptParser.extractWhileLoop(statements, i)
                    if (loopInfo != null) {
                        lastNodeId = buildWhileLoopFlow(loopInfo.first, loopInfo.second, lastNodeId, lineNumber)
                        i = loopInfo.third
                    } else {
                        lastNodeId = addStatementNode(stmt, lastNodeId)
                        i++
                    }
                }
                stmt.startsWith("function ") -> {
                    val funcInfo = scriptParser.extractFunction(statements, i)
                    if (funcInfo != null) {
                        lastNodeId = buildFunctionFlow(funcInfo.first, lastNodeId)
                        i = funcInfo.second
                    } else {
                        lastNodeId = addFunctionDeclNode(stmt, lastNodeId)
                        i++
                    }
                }
                stmt.startsWith("return") -> {
                    lastNodeId = addReturnNode(stmt, lastNodeId)
                    i++
                }
                else -> {
                    lastNodeId = addStatementNode(stmt, lastNodeId)
                    i++
                }
            }
        }

        return lastNodeId
    }

    private fun buildIfFlow(
        condition: String,
        ifBody: List<String>,
        elseBranches: List<Pair<String?, List<String>>>,
        entryNodeId: String,
        lineNumber: Int?
    ): String {

        val condNode = createNode(FlowNode.NodeType.CONDITION, "if (${truncateLabel(condition)})", condition, lineNumber)
        addEdge(entryNodeId, condNode.id)


        val mergeNode = createNode(FlowNode.NodeType.EXECUTION, "endif", null)


        var trueLastId = condNode.id
        for (stmt in ifBody) {
            if (stmt.trim().isNotEmpty() && stmt.trim() != "{" && stmt.trim() != "}") {
                trueLastId = addStatementNode(stmt.trim(), trueLastId,
                    if (trueLastId == condNode.id) FlowEdge.EdgeType.TRUE_BRANCH else FlowEdge.EdgeType.NORMAL)
            }
        }
        addEdge(trueLastId, mergeNode.id)


        var falseEntryId = condNode.id
        for ((elseCondition, elseBody) in elseBranches) {
            if (elseCondition != null) {

                val elseIfNode = createNode(FlowNode.NodeType.CONDITION, "else if (${truncateLabel(elseCondition)})", elseCondition)
                addEdge(falseEntryId, elseIfNode.id, null, FlowEdge.EdgeType.FALSE_BRANCH)

                var elseIfLastId = elseIfNode.id
                for (stmt in elseBody) {
                    if (stmt.trim().isNotEmpty() && stmt.trim() != "{" && stmt.trim() != "}") {
                        elseIfLastId = addStatementNode(stmt.trim(), elseIfLastId,
                            if (elseIfLastId == elseIfNode.id) FlowEdge.EdgeType.TRUE_BRANCH else FlowEdge.EdgeType.NORMAL)
                    }
                }
                addEdge(elseIfLastId, mergeNode.id)
                falseEntryId = elseIfNode.id
            } else {

                var elseLastId = falseEntryId
                for (stmt in elseBody) {
                    if (stmt.trim().isNotEmpty() && stmt.trim() != "{" && stmt.trim() != "}") {
                        elseLastId = addStatementNode(stmt.trim(), elseLastId,
                            if (elseLastId == falseEntryId) FlowEdge.EdgeType.FALSE_BRANCH else FlowEdge.EdgeType.NORMAL)
                    }
                }
                addEdge(elseLastId, mergeNode.id)
                falseEntryId = ""
            }
        }


        if (elseBranches.isEmpty() || (elseBranches.isNotEmpty() && elseBranches.last().first != null)) {
            addEdge(falseEntryId, mergeNode.id, null, FlowEdge.EdgeType.FALSE_BRANCH)
        }

        return mergeNode.id
    }

    private fun buildForLoopFlow(header: String, body: List<String>, entryNodeId: String, lineNumber: Int?): String {

        val loopNode = createNode(FlowNode.NodeType.LOOP_START, "for (${truncateLabel(header)})", header, lineNumber)
        addEdge(entryNodeId, loopNode.id)


        val loopEndNode = createNode(FlowNode.NodeType.LOOP_END, "next iteration", null)


        var lastBodyId = loopNode.id
        for (stmt in body) {
            if (stmt.trim().isNotEmpty() && stmt.trim() != "{" && stmt.trim() != "}") {
                lastBodyId = addStatementNode(stmt.trim(), lastBodyId,
                    if (lastBodyId == loopNode.id) FlowEdge.EdgeType.TRUE_BRANCH else FlowEdge.EdgeType.NORMAL)
            }
        }


        addEdge(lastBodyId, loopEndNode.id)


        addEdge(loopEndNode.id, loopNode.id, "repeat", FlowEdge.EdgeType.LOOP_BACK)


        val exitNode = createNode(FlowNode.NodeType.EXECUTION, "loop done", null)
        addEdge(loopNode.id, exitNode.id, "done", FlowEdge.EdgeType.LOOP_EXIT)

        return exitNode.id
    }

    private fun buildWhileLoopFlow(condition: String, body: List<String>, entryNodeId: String, lineNumber: Int?): String {

        val loopNode = createNode(FlowNode.NodeType.LOOP_START, "while (${truncateLabel(condition)})", condition, lineNumber)
        addEdge(entryNodeId, loopNode.id)


        var lastBodyId = loopNode.id
        for (stmt in body) {
            if (stmt.trim().isNotEmpty() && stmt.trim() != "{" && stmt.trim() != "}") {
                lastBodyId = addStatementNode(stmt.trim(), lastBodyId,
                    if (lastBodyId == loopNode.id) FlowEdge.EdgeType.TRUE_BRANCH else FlowEdge.EdgeType.NORMAL)
            }
        }


        addEdge(lastBodyId, loopNode.id, "repeat", FlowEdge.EdgeType.LOOP_BACK)


        val exitNode = createNode(FlowNode.NodeType.EXECUTION, "loop done", null)
        addEdge(loopNode.id, exitNode.id, "false", FlowEdge.EdgeType.LOOP_EXIT)

        return exitNode.id
    }


    private fun buildFunctionFlow(funcInfo: FunctionParser.FunctionInfo, entryNodeId: String): String {

        val paramsStr = if (funcInfo.parameters.isEmpty()) "" else funcInfo.parameters.joinToString(", ")
        val funcDeclNode = createNode(
            FlowNode.NodeType.FUNCTION_DECL,
            "function ${funcInfo.name}($paramsStr)",
            funcInfo.fullDeclaration
        )
        addEdge(entryNodeId, funcDeclNode.id)


        if (funcInfo.body.isNotEmpty()) {
            val scopeId = "scope${scopeIdCounter++}"
            val bodyNodeIds = mutableListOf<String>()


            var lastBodyNodeId = funcDeclNode.id
            var i = 0
            while (i < funcInfo.body.size) {
                val stmt = funcInfo.body[i].trim()
                if (stmt.isEmpty() || stmt == "{" || stmt == "}") {
                    i++
                    continue
                }

                val nodesBefore = nodes.size
                when {
                    stmt.startsWith("if ") || stmt.startsWith("if(") -> {
                        val ifInfo = scriptParser.extractIfStatement(funcInfo.body, i)
                        if (ifInfo != null) {

                            lastBodyNodeId = buildIfFlow(ifInfo.first, ifInfo.second, ifInfo.third, lastBodyNodeId, null)
                            i = ifInfo.fourth
                        } else {
                            lastBodyNodeId = addStatementNode(stmt, lastBodyNodeId)
                            i++
                        }
                    }
                    stmt.startsWith("for ") || stmt.startsWith("for(") -> {
                        val loopInfo = scriptParser.extractForLoop(funcInfo.body, i)
                        if (loopInfo != null) {

                            lastBodyNodeId = buildForLoopFlow(loopInfo.first, loopInfo.second, lastBodyNodeId, null)
                            i = loopInfo.third
                        } else {
                            lastBodyNodeId = addStatementNode(stmt, lastBodyNodeId)
                            i++
                        }
                    }
                    stmt.startsWith("while ") || stmt.startsWith("while(") -> {
                        val loopInfo = scriptParser.extractWhileLoop(funcInfo.body, i)
                        if (loopInfo != null) {

                            lastBodyNodeId = buildWhileLoopFlow(loopInfo.first, loopInfo.second, lastBodyNodeId, null)
                            i = loopInfo.third
                        } else {
                            lastBodyNodeId = addStatementNode(stmt, lastBodyNodeId)
                            i++
                        }
                    }
                    stmt.startsWith("return") -> {
                        lastBodyNodeId = addReturnNode(stmt, lastBodyNodeId)
                        i++
                    }
                    else -> {
                        lastBodyNodeId = addStatementNode(stmt, lastBodyNodeId)
                        i++
                    }
                }


                for (j in nodesBefore until nodes.size) {
                    bodyNodeIds.add(nodes[j].id)
                }
            }


            functionScopes.add(FunctionScope(
                id = scopeId,
                name = funcInfo.name,
                nodeIds = bodyNodeIds
            ))
        }

        return funcDeclNode.id
    }

    private fun addStatementNode(stmt: String, prevNodeId: String, edgeType: FlowEdge.EdgeType = FlowEdge.EdgeType.NORMAL): String {
        val nodeType = classifyStatementType(stmt)
        val node = createNode(nodeType, truncateLabel(stmt), stmt)
        addEdge(prevNodeId, node.id, null, edgeType)
        return node.id
    }

    private fun addFunctionDeclNode(stmt: String, prevNodeId: String): String {
        val funcName = stmt.removePrefix("function ").substringBefore("(").trim()
        val node = createNode(FlowNode.NodeType.FUNCTION_DECL, "function $funcName()", stmt)
        addEdge(prevNodeId, node.id)
        return node.id
    }

    private fun addReturnNode(stmt: String, prevNodeId: String): String {
        val node = createNode(FlowNode.NodeType.RETURN, truncateLabel(stmt), stmt)
        addEdge(prevNodeId, node.id)
        return node.id
    }

    private fun classifyStatementType(stmt: String): FlowNode.NodeType {
        return when {
            stmt.contains("(") && !stmt.startsWith("let ") && !stmt.startsWith("const ") && !stmt.startsWith("var ") ->
                FlowNode.NodeType.FUNCTION_CALL
            stmt.contains("=") && !stmt.contains("==") && !stmt.contains("=>") ->
                FlowNode.NodeType.VARIABLE_ASSIGN
            else -> FlowNode.NodeType.EXECUTION
        }
    }

    private fun createNode(type: FlowNode.NodeType, label: String, code: String? = null, lineNumber: Int? = null): FlowNode {
        val node = FlowNode("n${nodeIdCounter++}", type, label, code, lineNumber)
        nodes.add(node)
        return node
    }

    private fun addEdge(from: String, to: String, label: String? = null, type: FlowEdge.EdgeType = FlowEdge.EdgeType.NORMAL) {
        if (from.isNotEmpty() && to.isNotEmpty()) {
            edges.add(FlowEdge(from, to, label, type))
        }
    }

    private fun truncateLabel(text: String): String {
        val cleaned = text.replace("\n", " ").replace("\r", "").trim()
        return if (cleaned.length > 40) cleaned.take(37) + "..." else cleaned
    }
}
