package ronsijm.templater.parallel

/**
 * Represents a single template block (either interpolation or execution)
 */
data class TemplateBlock(
    val id: Int,
    val matchText: String,
    val command: String,
    val isExecution: Boolean,
    val leftTrim: String,
    val rightTrim: String,
    val originalStart: Int,
    val originalEnd: Int
)

/**
 * Analysis result for a template block
 * Contains information about variable reads, writes, and barriers
 */
data class BlockAnalysis(
    val block: TemplateBlock,
    val variablesRead: Set<String>,
    val variablesWritten: Set<String>,
    val isBarrier: Boolean,  // true if block requires user interaction or has side effects
    val hasTrWrite: Boolean  // true if block writes to tR
) {
    /**
     * Check if this block depends on another block
     * A depends on B if:
     * - A reads a variable that B writes
     * - A reads tR and B writes to tR
     * - B is a barrier (must complete before A starts)
     */
    fun dependsOn(other: BlockAnalysis): Boolean {
        // If other is a barrier, we depend on it
        if (other.isBarrier) return true
        
        // If we read any variable that other writes, we depend on it
        if (variablesRead.any { it in other.variablesWritten }) return true
        
        // If we read tR and other writes to tR, we depend on it
        if ("tR" in variablesRead && other.hasTrWrite) return true
        
        // If we write to a variable that other also writes, we depend on it (write-after-write)
        if (variablesWritten.any { it in other.variablesWritten }) return true
        
        // If we write to tR and other also writes to tR, we depend on it
        if (hasTrWrite && other.hasTrWrite) return true
        
        return false
    }
}

/**
 * Execution phase - a group of blocks that can be executed in parallel
 */
data class ExecutionPhase(
    val phaseNumber: Int,
    val blocks: List<BlockAnalysis>
) {
    val canParallelize: Boolean
        get() = blocks.size > 1 && blocks.none { it.isBarrier }
}

/**
 * Result of parallel scheduling
 */
data class ExecutionPlan(
    val phases: List<ExecutionPhase>,
    val totalBlocks: Int,
    val parallelizableBlocks: Int
) {
    val parallelizationRatio: Double
        get() = if (totalBlocks > 0) parallelizableBlocks.toDouble() / totalBlocks else 0.0
}

