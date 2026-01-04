package ronsijm.templater.debug

data class ParallelGroupExplanation(
    val forkNodeId: String,
    val blockCount: Int,
    val blockDescriptions: List<BlockDescription>,
    val reason: String
)

