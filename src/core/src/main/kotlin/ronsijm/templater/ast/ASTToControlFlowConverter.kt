package ronsijm.templater.ast

import ronsijm.templater.debug.ControlFlowGraph
import ronsijm.templater.debug.FlowNode
import ronsijm.templater.debug.FlowEdge


class ASTToControlFlowConverter {


    fun convert(ast: TemplateAST): ControlFlowGraph {
        val flowNodes = ast.allStatements.map { statementToFlowNode(it) }
        val flowEdges = ast.controlFlowEdges.map { edgeToFlowEdge(it) }

        return ControlFlowGraph(
            nodes = flowNodes,
            edges = flowEdges,
            parallelGroupExplanations = emptyList(),
            functionScopes = emptyList()
        )
    }


    private fun statementToFlowNode(statement: StatementNode): FlowNode {
        val nodeType = when (statement.type) {
            StatementType.START -> FlowNode.NodeType.START
            StatementType.END -> FlowNode.NodeType.END
            StatementType.VARIABLE_DECLARATION -> FlowNode.NodeType.VARIABLE_ASSIGN
            StatementType.VARIABLE_ASSIGNMENT -> FlowNode.NodeType.VARIABLE_ASSIGN
            StatementType.FOR_LOOP -> FlowNode.NodeType.LOOP_START
            StatementType.WHILE_LOOP -> FlowNode.NodeType.LOOP_START
            StatementType.IF_STATEMENT -> FlowNode.NodeType.CONDITION
            StatementType.ELSE_IF_STATEMENT -> FlowNode.NodeType.CONDITION
            StatementType.ELSE_STATEMENT -> FlowNode.NodeType.EXECUTION
            StatementType.FUNCTION_DECLARATION -> FlowNode.NodeType.FUNCTION_DECL
            StatementType.FUNCTION_CALL -> FlowNode.NodeType.FUNCTION_CALL
            StatementType.RETURN_STATEMENT -> FlowNode.NodeType.RETURN
            StatementType.TEMPLATE_OUTPUT -> FlowNode.NodeType.EXECUTION
            StatementType.INTERPOLATION -> FlowNode.NodeType.INTERPOLATION
            StatementType.EXPRESSION -> FlowNode.NodeType.EXECUTION
            StatementType.BLOCK_START -> FlowNode.NodeType.EXECUTION
            StatementType.BLOCK_END -> FlowNode.NodeType.EXECUTION
            StatementType.COMMENT -> FlowNode.NodeType.EXECUTION
        }

        return FlowNode(
            id = statement.id,
            type = nodeType,
            label = truncateLabel(statement.code),
            code = statement.code,
            lineNumber = statement.lineNumber
        )
    }


    private fun edgeToFlowEdge(edge: ControlFlowEdge): FlowEdge {
        val edgeType = when (edge.edgeType) {
            ControlFlowEdge.EdgeType.NORMAL -> FlowEdge.EdgeType.NORMAL
            ControlFlowEdge.EdgeType.TRUE_BRANCH -> FlowEdge.EdgeType.TRUE_BRANCH
            ControlFlowEdge.EdgeType.FALSE_BRANCH -> FlowEdge.EdgeType.FALSE_BRANCH
            ControlFlowEdge.EdgeType.LOOP_BACK -> FlowEdge.EdgeType.LOOP_BACK
        }

        return FlowEdge(
            from = edge.fromNodeId,
            to = edge.toNodeId,
            type = edgeType
        )
    }


    private fun truncateLabel(code: String, maxLength: Int = 50): String {
        val singleLine = code.replace("\n", " ").trim()
        return if (singleLine.length > maxLength) {
            singleLine.take(maxLength - 3) + "..."
        } else {
            singleLine
        }
    }
}

