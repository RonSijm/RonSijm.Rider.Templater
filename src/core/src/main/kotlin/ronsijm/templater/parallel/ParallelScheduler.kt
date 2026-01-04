package ronsijm.templater.parallel


class ParallelScheduler {

    private val analyzer = DependencyAnalyzer()


    fun createExecutionPlan(blocks: List<TemplateBlock>): ExecutionPlan {
        if (blocks.isEmpty()) {
            return ExecutionPlan(emptyList(), 0, 0)
        }

        val analyses = analyzer.analyzeAll(blocks)
        val phases = mutableListOf<ExecutionPhase>()
        val scheduled = mutableSetOf<Int>()
        var phaseNumber = 0

        while (scheduled.size < analyses.size) {
            val currentPhase = mutableListOf<BlockAnalysis>()

            for (analysis in analyses) {
                if (analysis.block.id in scheduled) continue


                val canSchedule = analyses
                    .filter { it.block.id < analysis.block.id && it.block.id !in scheduled }
                    .none { analysis.dependsOn(it) }


                val dependsOnCurrentPhase = currentPhase.any { analysis.dependsOn(it) }

                if (canSchedule && !dependsOnCurrentPhase) {

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

                val nextBlock = analyses.first { it.block.id !in scheduled }
                currentPhase.add(nextBlock)
                scheduled.add(nextBlock.block.id)
            }

            phases.add(ExecutionPhase(phaseNumber++, currentPhase))
        }


        val parallelizableBlocks = phases
            .filter { it.blocks.size > 1 }
            .sumOf { it.blocks.size }

        return ExecutionPlan(
            phases = phases,
            totalBlocks = blocks.size,
            parallelizableBlocks = parallelizableBlocks
        )
    }


    fun canParallelize(blocks: List<TemplateBlock>): Boolean {
        val plan = createExecutionPlan(blocks)
        return plan.parallelizableBlocks > 0
    }
}
