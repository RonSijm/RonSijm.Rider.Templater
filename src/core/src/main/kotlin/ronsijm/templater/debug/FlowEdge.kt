package ronsijm.templater.debug

data class FlowEdge(
    val from: String,
    val to: String,
    val label: String? = null,
    val type: EdgeType = EdgeType.NORMAL
) {
    enum class EdgeType {
        NORMAL,
        TRUE_BRANCH,
        FALSE_BRANCH,
        LOOP_BACK,
        LOOP_EXIT,
        PARALLEL
    }
}

