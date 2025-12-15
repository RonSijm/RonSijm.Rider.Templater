package ronsijm.templater.parallel

/**
 * Schedules template blocks into execution phases based on dependencies
 * Blocks in the same phase can be executed in parallel
 */
class ParallelScheduler {
    
    private val analyzer = DependencyAnalyzer()
    
    /**
     * Create an execution plan from template blocks
     * Groups independent blocks into phases that can run in parallel
     */
    fun createExecutionPlan(blocks: List<TemplateBlock>): ExecutionPlan {
        if (blocks.isEmpty()) {
            return ExecutionPlan(emptyList(), 0, 0)
        }
        
        val analyses = analyzer.analyzeAll(blocks)
        val phases = mutableListOf<ExecutionPhase>()
        val scheduled = mutableSetOf<Int>()  // Block IDs that have been scheduled
        var phaseNumber = 0
        
        while (scheduled.size < analyses.size) {
            val currentPhase = mutableListOf<BlockAnalysis>()
            
            for (analysis in analyses) {
                if (analysis.block.id in scheduled) continue
                
                // Check if this block depends on any unscheduled block that comes before it
                val canSchedule = analyses
                    .filter { it.block.id < analysis.block.id && it.block.id !in scheduled }
                    .none { analysis.dependsOn(it) }
                
                // Also check if it depends on any block in the current phase
                val dependsOnCurrentPhase = currentPhase.any { analysis.dependsOn(it) }
                
                if (canSchedule && !dependsOnCurrentPhase) {
                    // Check if adding this block would create a dependency with existing phase blocks
                    val wouldCreateDependency = currentPhase.any { existing ->
                        existing.dependsOn(analysis)
                    }
                    
                    if (!wouldCreateDependency) {
                        currentPhase.add(analysis)
                        scheduled.add(analysis.block.id)
                    }
                }
            }
            
            if (currentPhase.isEmpty()) {
                // Safety: if we couldn't schedule anything, schedule the first unscheduled block
                val nextBlock = analyses.first { it.block.id !in scheduled }
                currentPhase.add(nextBlock)
                scheduled.add(nextBlock.block.id)
            }
            
            phases.add(ExecutionPhase(phaseNumber++, currentPhase))
        }
        
        // Count parallelizable blocks (blocks in phases with more than 1 block)
        val parallelizableBlocks = phases
            .filter { it.blocks.size > 1 }
            .sumOf { it.blocks.size }
        
        return ExecutionPlan(
            phases = phases,
            totalBlocks = blocks.size,
            parallelizableBlocks = parallelizableBlocks
        )
    }
    
    /**
     * Simple check if any parallelization is possible
     */
    fun canParallelize(blocks: List<TemplateBlock>): Boolean {
        val plan = createExecutionPlan(blocks)
        return plan.parallelizableBlocks > 0
    }
}

