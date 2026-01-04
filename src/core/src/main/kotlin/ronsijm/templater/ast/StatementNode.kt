package ronsijm.templater.ast

import java.util.UUID


data class StatementNode(

    val id: String = UUID.randomUUID().toString(),


    val type: StatementType,


    val code: String,


    val lineNumber: Int?,


    val columnNumber: Int? = null,


    val offset: Int? = null,


    val parent: StatementNode? = null,


    val children: List<StatementNode> = emptyList(),


    val elseBranches: List<Pair<String?, List<StatementNode>>> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StatementNode) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String = "StatementNode(id=$id, type=$type, code=${code.take(50)}...)"
}


enum class StatementType {

    VARIABLE_DECLARATION,
    VARIABLE_ASSIGNMENT,


    IF_STATEMENT,
    ELSE_IF_STATEMENT,
    ELSE_STATEMENT,
    FOR_LOOP,
    WHILE_LOOP,


    FUNCTION_DECLARATION,
    FUNCTION_CALL,
    RETURN_STATEMENT,


    TEMPLATE_OUTPUT,
    INTERPOLATION,


    EXPRESSION,
    BLOCK_START,
    BLOCK_END,
    COMMENT,


    START,
    END
}


data class TemplateBlock(

    val id: String = UUID.randomUUID().toString(),


    val isExecution: Boolean,


    val statements: List<StatementNode>,


    val startLine: Int,


    val endLine: Int,


    val startOffset: Int,


    val endOffset: Int
)


data class ControlFlowEdge(
    val fromNodeId: String,
    val toNodeId: String,
    val edgeType: EdgeType = EdgeType.NORMAL
) {
    enum class EdgeType {
        NORMAL,
        TRUE_BRANCH,
        FALSE_BRANCH,
        LOOP_BACK
    }
}


data class TemplateAST(

    val blocks: List<TemplateBlock>,


    val allStatements: List<StatementNode>,


    val controlFlowEdges: List<ControlFlowEdge>
) {

    fun findStatement(id: String): StatementNode? {
        return allStatements.find { it.id == id }
    }


    fun getOutgoingEdges(nodeId: String): List<ControlFlowEdge> {
        return controlFlowEdges.filter { it.fromNodeId == nodeId }
    }


    fun getIncomingEdges(nodeId: String): List<ControlFlowEdge> {
        return controlFlowEdges.filter { it.toNodeId == nodeId }
    }
}

