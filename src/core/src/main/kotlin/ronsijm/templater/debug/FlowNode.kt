package ronsijm.templater.debug

data class FlowNode(
    val id: String,
    val type: NodeType,
    val label: String,
    val code: String? = null,
    val lineNumber: Int? = null
) {
    enum class NodeType {
        START,
        END,
        INTERPOLATION,
        EXECUTION,
        CONDITION,
        LOOP_START,
        LOOP_END,
        FUNCTION_DECL,
        FUNCTION_CALL,
        VARIABLE_ASSIGN,
        RETURN,
        FORK,
        JOIN,
        ERROR
    }
}

