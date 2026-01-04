package ronsijm.templater.parallel


data class ExecutionPlan(
    val phases: List<ExecutionPhase>,
    val totalBlocks: Int,
    val parallelizableBlocks: Int
) {
    val parallelizationRatio: Double
        get() = if (totalBlocks > 0) parallelizableBlocks.toDouble() / totalBlocks else 0.0
}
