package ronsijm.templater.debug.visualization

data class VisualMetadata(
    val highlightedIndices: Set<Int> = emptySet(),
    val comparedIndices: Set<Int> = emptySet(),
    val swappedIndices: Pair<Int, Int>? = null,
    val annotations: Map<Int, String> = emptyMap()
)

