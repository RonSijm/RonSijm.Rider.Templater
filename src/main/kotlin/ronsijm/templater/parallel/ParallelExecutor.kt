package ronsijm.templater.parallel

import kotlinx.coroutines.*
import ronsijm.templater.utils.Logging
import java.util.concurrent.ConcurrentHashMap

/**
 * Executes template blocks in parallel where possible
 * Uses Kotlin coroutines for concurrent execution
 */
class ParallelExecutor {

    companion object {
        private val LOG = Logging.getLogger<ParallelExecutor>()
    }
    
    private val scheduler = ParallelScheduler()
    
    /**
     * Execute blocks according to the execution plan
     * @param blocks List of template blocks to execute
     * @param executeBlock Function to execute a single block and return its result
     * @return Map of block ID to execution result
     */
    suspend fun execute(
        blocks: List<TemplateBlock>,
        executeBlock: suspend (TemplateBlock) -> String
    ): Map<Int, String> {
        val plan = scheduler.createExecutionPlan(blocks)
        val results = ConcurrentHashMap<Int, String>()
        
        LOG?.info("Execution plan: ${plan.phases.size} phases, " +
                "${plan.parallelizableBlocks}/${plan.totalBlocks} blocks can be parallelized")
        
        for (phase in plan.phases) {
            LOG?.debug("Executing phase ${phase.phaseNumber} with ${phase.blocks.size} blocks")
            
            if (phase.canParallelize) {
                // Execute blocks in parallel
                coroutineScope {
                    phase.blocks.map { analysis ->
                        async(Dispatchers.Default) {
                            val result = executeBlock(analysis.block)
                            results[analysis.block.id] = result
                            LOG?.debug("Block ${analysis.block.id} completed: ${result.take(50)}...")
                        }
                    }.awaitAll()
                }
            } else {
                // Execute blocks sequentially (barrier or single block)
                for (analysis in phase.blocks) {
                    val result = executeBlock(analysis.block)
                    results[analysis.block.id] = result
                    LOG?.debug("Block ${analysis.block.id} completed (sequential): ${result.take(50)}...")
                }
            }
        }
        
        return results
    }
    
    /**
     * Execute blocks synchronously (blocking)
     * Convenience method for non-coroutine contexts
     */
    fun executeBlocking(
        blocks: List<TemplateBlock>,
        executeBlock: suspend (TemplateBlock) -> String
    ): Map<Int, String> {
        return runBlocking {
            execute(blocks, executeBlock)
        }
    }
    
    /**
     * Get execution plan for debugging/visualization
     */
    fun getExecutionPlan(blocks: List<TemplateBlock>): ExecutionPlan {
        return scheduler.createExecutionPlan(blocks)
    }
}

