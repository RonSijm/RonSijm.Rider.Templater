package ronsijm.templater.parallel

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ParallelSchedulerTest {

    private val scheduler = ParallelScheduler()

    private fun createBlock(id: Int, command: String, isExecution: Boolean = true) = TemplateBlock(
        id = id,
        matchText = if (isExecution) "<%* $command %>" else "<% $command %>",
        command = command,
        isExecution = isExecution,
        leftTrim = "",
        rightTrim = "",
        originalStart = id * 20,
        originalEnd = id * 20 + 15
    )

    @Test
    fun `test empty blocks returns empty plan`() {
        val plan = scheduler.createExecutionPlan(emptyList())

        assertEquals(0, plan.totalBlocks)
        assertEquals(0, plan.parallelizableBlocks)
        assertTrue(plan.phases.isEmpty())
    }

    @Test
    fun `test single block creates single phase`() {
        val blocks = listOf(createBlock(0, "let x = 5"))
        val plan = scheduler.createExecutionPlan(blocks)

        assertEquals(1, plan.phases.size)
        assertEquals(1, plan.totalBlocks)
        assertEquals(0, plan.parallelizableBlocks)
    }

    @Test
    fun `test independent blocks can be parallelized`() {
        val blocks = listOf(
            createBlock(0, "let x = 5"),
            createBlock(1, "let y = 10"),
            createBlock(2, "let z = 15")
        )
        val plan = scheduler.createExecutionPlan(blocks)


        assertEquals(1, plan.phases.size)
        assertEquals(3, plan.phases[0].blocks.size)
        assertEquals(3, plan.parallelizableBlocks)
    }

    @Test
    fun `test dependent blocks create multiple phases`() {
        val blocks = listOf(
            createBlock(0, "let x = 5"),
            createBlock(1, "let y = x * 2"),
            createBlock(2, "let z = y + x")
        )
        val plan = scheduler.createExecutionPlan(blocks)


        assertEquals(3, plan.phases.size)
        assertEquals(0, plan.parallelizableBlocks)
    }

    @Test
    fun `test mixed independent and dependent blocks`() {
        val blocks = listOf(
            createBlock(0, "let x = 5"),
            createBlock(1, "let y = 10"),
            createBlock(2, "let z = x + y")
        )
        val plan = scheduler.createExecutionPlan(blocks)



        assertEquals(2, plan.phases.size)
        assertEquals(2, plan.phases[0].blocks.size)
        assertEquals(1, plan.phases[1].blocks.size)
        assertEquals(2, plan.parallelizableBlocks)
    }

    @Test
    fun `test tR writes are sequential`() {
        val blocks = listOf(
            createBlock(0, "tR += \"A\""),
            createBlock(1, "tR += \"B\""),
            createBlock(2, "tR += \"C\"")
        )
        val plan = scheduler.createExecutionPlan(blocks)


        assertEquals(3, plan.phases.size)
        assertEquals(0, plan.parallelizableBlocks)
    }

    @Test
    fun `test barrier blocks force sequential execution`() {
        val blocks = listOf(
            createBlock(0, "let name = await tp.system.prompt(\"Name\")"),
            createBlock(1, "let x = 5"),
            createBlock(2, "let y = 10")
        )
        val plan = scheduler.createExecutionPlan(blocks)


        assertTrue(plan.phases.size >= 2)
    }

    @Test
    fun `test canParallelize returns true for independent blocks`() {
        val blocks = listOf(
            createBlock(0, "let x = 5"),
            createBlock(1, "let y = 10")
        )

        assertTrue(scheduler.canParallelize(blocks))
    }

    @Test
    fun `test canParallelize returns false for dependent blocks`() {
        val blocks = listOf(
            createBlock(0, "let x = 5"),
            createBlock(1, "let y = x * 2")
        )

        assertFalse(scheduler.canParallelize(blocks))
    }

    @Test
    fun `test parallelization ratio`() {
        val blocks = listOf(
            createBlock(0, "let x = 5"),
            createBlock(1, "let y = 10"),
            createBlock(2, "let z = x + y")
        )
        val plan = scheduler.createExecutionPlan(blocks)


        assertEquals(2.0 / 3.0, plan.parallelizationRatio, 0.01)
    }
}
