package ronsijm.templater.debug


data class ControlFlowGraph(
    val nodes: List<FlowNode>,
    val edges: List<FlowEdge>,
    val parallelGroupExplanations: List<ParallelGroupExplanation> = emptyList(),
    val functionScopes: List<FunctionScope> = emptyList()
) {
    val isEmpty: Boolean get() = nodes.isEmpty()
}
