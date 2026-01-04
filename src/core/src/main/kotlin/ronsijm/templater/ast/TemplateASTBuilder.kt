package ronsijm.templater.ast

import ronsijm.templater.common.TemplateSyntax
import ronsijm.templater.script.ScriptParser
import ronsijm.templater.utils.TextUtils


class TemplateASTBuilder(
    private val scriptParser: ScriptParser = ScriptParser()
) {
    private val templateRegex = TemplateSyntax.TEMPLATE_BLOCK_REGEX
    private val edges = mutableListOf<ControlFlowEdge>()


    fun build(content: String): TemplateAST {
        edges.clear()


        val contentWithoutComments = TextUtils.removeHtmlComments(content)


        val matches = templateRegex.findAll(contentWithoutComments).toList()

        val blocks = matches.map { match ->
            parseTemplateBlock(match, content)
        }


        val allStatements = mutableListOf<StatementNode>()


        val startNode = StatementNode(
            type = StatementType.START,
            code = "Start",
            lineNumber = null
        )
        allStatements.add(startNode)


        var lastNodeId = startNode.id
        for (block in blocks) {
            if (block.statements.isNotEmpty()) {

                edges.add(ControlFlowEdge(lastNodeId, block.statements.first().id))


                allStatements.addAll(flattenStatements(block.statements))


                lastNodeId = findLastNode(block.statements).id
            }
        }


        val endNode = StatementNode(
            type = StatementType.END,
            code = "End",
            lineNumber = null
        )
        allStatements.add(endNode)
        edges.add(ControlFlowEdge(lastNodeId, endNode.id))

        return TemplateAST(blocks, allStatements, edges.toList())
    }


    private fun flattenStatements(statements: List<StatementNode>): List<StatementNode> {
        val result = mutableListOf<StatementNode>()
        for (stmt in statements) {
            result.add(stmt)
            if (stmt.children.isNotEmpty()) {
                result.addAll(flattenStatements(stmt.children))
            }
        }
        return result
    }


    private fun findLastNode(statements: List<StatementNode>): StatementNode {
        if (statements.isEmpty()) throw IllegalArgumentException("Cannot find last node in empty list")
        val lastStmt = statements.last()
        return if (lastStmt.children.isNotEmpty()) {
            findLastNode(lastStmt.children)
        } else {
            lastStmt
        }
    }


    private fun parseTemplateBlock(
        match: MatchResult,
        originalContent: String
    ): TemplateBlock {
        val isExecution = match.groupValues[2] == "*"
        val code = match.groupValues[3].trim()

        val startOffset = match.range.first
        val endOffset = match.range.last
        val startLine = TextUtils.calculateLineNumber(originalContent, startOffset)
        val endLine = TextUtils.calculateLineNumber(originalContent, endOffset)

        val statements = if (isExecution) {




            val codeStartOffset = originalContent.indexOf(code, startOffset).let {
                if (it >= 0) it else startOffset
            }
            val codeStartLine = TextUtils.calculateLineNumber(originalContent, codeStartOffset)
            parseExecutionBlock(code, blockStartLine = codeStartLine, blockStartOffset = codeStartOffset)
        } else {

            listOf(
                StatementNode(
                    type = StatementType.INTERPOLATION,
                    code = code,
                    lineNumber = startLine,
                    offset = startOffset
                )
            )
        }

        return TemplateBlock(
            isExecution = isExecution,
            statements = statements,
            startLine = startLine,
            endLine = endLine,
            startOffset = startOffset,
            endOffset = endOffset
        )
    }


    private fun findBlockEndPosition(code: String, startPosition: Int, headerStatement: String): Int {

        val headerIndex = code.indexOf(headerStatement, startPosition)
        if (headerIndex < 0) return startPosition

        val braceStart = code.indexOf('{', headerIndex)
        if (braceStart < 0) return startPosition


        var braceDepth = 1
        var pos = braceStart + 1
        while (pos < code.length && braceDepth > 0) {
            when (code[pos]) {
                '{' -> braceDepth++
                '}' -> braceDepth--
            }
            pos++
        }

        return pos
    }


    private fun parseExecutionBlock(
        code: String,
        blockStartLine: Int,
        blockStartOffset: Int
    ): List<StatementNode> {
        val statements = scriptParser.splitStatements(code)
        val nodes = mutableListOf<StatementNode>()



        var currentLine = blockStartLine
        var currentOffset = blockStartOffset
        var codePosition = 0

        var i = 0
        var previousNodeId: String? = null

        while (i < statements.size) {
            val stmt = statements[i].trim()

            if (stmt.isEmpty() || stmt == "{" || stmt == "}") {
                i++
                continue
            }


            val stmtIndex = code.indexOf(stmt, codePosition)
            if (stmtIndex >= 0) {

                val newlinesBetween = code.substring(codePosition, stmtIndex).count { it == '\n' }
                currentLine += newlinesBetween
                codePosition = stmtIndex + stmt.length
            }

            val node = when {
                stmt.startsWith("let ") || stmt.startsWith("const ") || stmt.startsWith("var ") -> {
                    val n = StatementNode(
                        type = StatementType.VARIABLE_DECLARATION,
                        code = stmt,
                        lineNumber = currentLine,
                        offset = currentOffset
                    )
                    i++
                    n
                }

                stmt.contains("=") && !stmt.startsWith("if") && !stmt.startsWith("for") && !stmt.startsWith("while") -> {
                    val n = StatementNode(
                        type = StatementType.VARIABLE_ASSIGNMENT,
                        code = stmt,
                        lineNumber = currentLine,
                        offset = currentOffset
                    )
                    i++
                    n
                }

                stmt.startsWith("for ") || stmt.startsWith("for(") -> {
                    val loopInfo = scriptParser.extractForLoop(statements, i)
                    if (loopInfo != null) {
                        val (header, body, nextIndex) = loopInfo
                        val bodyNodes = parseExecutionBlock(body.joinToString("\n"), currentLine + 1, currentOffset)
                        val loopNode = StatementNode(
                            type = StatementType.FOR_LOOP,
                            code = "for ($header)",
                            lineNumber = currentLine,
                            offset = currentOffset,
                            children = bodyNodes
                        )






                        if (bodyNodes.isNotEmpty()) {
                            edges.add(ControlFlowEdge(loopNode.id, bodyNodes.first().id, ControlFlowEdge.EdgeType.TRUE_BRANCH))
                            val lastBodyNode = findLastNode(bodyNodes)
                            edges.add(ControlFlowEdge(lastBodyNode.id, loopNode.id, ControlFlowEdge.EdgeType.LOOP_BACK))
                        }



                        codePosition = findBlockEndPosition(code, codePosition, stmt)

                        i = nextIndex
                        loopNode
                    } else {
                        val n = StatementNode(
                            type = StatementType.EXPRESSION,
                            code = stmt,
                            lineNumber = currentLine,
                            offset = currentOffset
                        )
                        i++
                        n
                    }
                }

                stmt.startsWith("if ") || stmt.startsWith("if(") -> {
                    val ifInfo = scriptParser.extractIfStatement(statements, i)
                    if (ifInfo != null) {
                        val (ifHeader, thenBody, elseIfBranches, nextIndex) = ifInfo
                        val thenNodes = parseExecutionBlock(thenBody.joinToString("\n"), currentLine + 1, currentOffset)


                        val parsedElseBranches = elseIfBranches.map { (elseCond, elseBody) ->
                            val elseNodes = parseExecutionBlock(elseBody.joinToString("\n"), currentLine + 1, currentOffset)
                            Pair(elseCond, elseNodes)
                        }

                        val ifNode = StatementNode(
                            type = StatementType.IF_STATEMENT,
                            code = ifHeader,
                            lineNumber = currentLine,
                            offset = currentOffset,
                            children = thenNodes,
                            elseBranches = parsedElseBranches
                        )




                        if (thenNodes.isNotEmpty()) {
                            edges.add(ControlFlowEdge(ifNode.id, thenNodes.first().id, ControlFlowEdge.EdgeType.TRUE_BRANCH))
                        }


                        if (parsedElseBranches.isNotEmpty()) {
                            val firstElseBranch = parsedElseBranches.first().second
                            if (firstElseBranch.isNotEmpty()) {
                                edges.add(ControlFlowEdge(ifNode.id, firstElseBranch.first().id, ControlFlowEdge.EdgeType.FALSE_BRANCH))
                            }
                        }


                        codePosition = findBlockEndPosition(code, codePosition, stmt)

                        i = nextIndex
                        ifNode
                    } else {
                        val n = StatementNode(
                            type = StatementType.EXPRESSION,
                            code = stmt,
                            lineNumber = currentLine,
                            offset = currentOffset
                        )
                        i++
                        n
                    }
                }

                else -> {
                    val n = StatementNode(
                        type = StatementType.EXPRESSION,
                        code = stmt,
                        lineNumber = currentLine,
                        offset = currentOffset
                    )
                    i++
                    n
                }
            }


            if (previousNodeId != null) {
                edges.add(ControlFlowEdge(previousNodeId, node.id))
            }

            nodes.add(node)
            previousNodeId = node.id

        }

        return nodes
    }
}

