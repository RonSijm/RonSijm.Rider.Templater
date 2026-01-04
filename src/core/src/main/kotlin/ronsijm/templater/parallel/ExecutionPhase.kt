package ronsijm.templater.parallel


data class ExecutionPhase(
    val phaseNumber: Int,
    val blocks: List<BlockAnalysis>
) {
    val canParallelize: Boolean
        get() = blocks.size > 1 && blocks.none { it.isBarrier }
}
