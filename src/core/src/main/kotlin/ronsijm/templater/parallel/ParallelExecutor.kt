package ronsijm.templater.parallel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import ronsijm.templater.utils.Logging
import java.util.concurrent.ConcurrentHashMap


class ParallelExecutor {

    companion object {
        private val LOG = Logging.getLogger<ParallelExecutor>()
    }

    private val scheduler = ParallelScheduler()


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

                for (analysis in phase.blocks) {
                    val result = executeBlock(analysis.block)
                    results[analysis.block.id] = result
                    LOG?.debug("Block ${analysis.block.id} completed (sequential): ${result.take(50)}...")
                }
            }
        }

        return results
    }


    fun executeBlocking(
        blocks: List<TemplateBlock>,
        executeBlock: suspend (TemplateBlock) -> String
    ): Map<Int, String> {
        return runBlocking {
            execute(blocks, executeBlock)
        }
    }


    fun getExecutionPlan(blocks: List<TemplateBlock>): ExecutionPlan {
        return scheduler.createExecutionPlan(blocks)
    }
}
