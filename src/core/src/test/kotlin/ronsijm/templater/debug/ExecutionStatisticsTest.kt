package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory

class ExecutionStatisticsTest {

    @Test
    fun `test statistics from empty trace`() {
        val trace = ExecutionTrace()
        val stats = trace.getStatistics()

        assertEquals(0, stats.totalSteps)
        assertEquals(0, stats.statementCount)
        assertEquals("O(1)", stats.estimatedBigO)
    }

    @Test
    fun `test statistics counts step types correctly`() {
        val trace = ExecutionTrace()

        trace.recordStep(ExecutionStep.StepType.TEMPLATE_START, "Start")
        trace.recordStep(ExecutionStep.StepType.STATEMENT, "let x = 1")
        trace.recordStep(ExecutionStep.StepType.VARIABLE_ASSIGN, "x = 2")
        trace.recordStep(ExecutionStep.StepType.FUNCTION_CALL, "console.log(x)")
        trace.recordStep(ExecutionStep.StepType.TEMPLATE_END, "End")

        val stats = trace.getStatistics()

        assertEquals(5, stats.totalSteps)
        assertEquals(1, stats.statementCount)
        assertEquals(1, stats.variableAssignmentCount)
        assertEquals(1, stats.functionCallCount)
    }

    @Test
    fun `test statistics tracks nesting depth`() {
        val trace = ExecutionTrace()

        trace.recordStep(ExecutionStep.StepType.TEMPLATE_START, "Start")
        trace.recordStep(ExecutionStep.StepType.BLOCK_START, "if (x > 0)")
        trace.recordStep(ExecutionStep.StepType.BLOCK_START, "for (let i = 0; i < 10; i++)")
        trace.recordStep(ExecutionStep.StepType.STATEMENT, "console.log(i)")
        trace.recordStep(ExecutionStep.StepType.BLOCK_END, "End for")
        trace.recordStep(ExecutionStep.StepType.BLOCK_END, "End if")
        trace.recordStep(ExecutionStep.StepType.TEMPLATE_END, "End")

        val stats = trace.getStatistics()

        assertEquals(2, stats.maxNestingDepth)
        assertEquals(1, stats.totalLoops)
        assertEquals(1, stats.totalConditionals)
    }

    @Test
    fun `test statistics estimates Big-O complexity`() {
        val trace = ExecutionTrace()


        trace.recordStep(ExecutionStep.StepType.STATEMENT, "x = 1")
        assertEquals("O(1)", trace.getStatistics().estimatedBigO)

        trace.clear()


        trace.recordStep(ExecutionStep.StepType.BLOCK_START, "for (let i = 0; i < n; i++)")
        trace.recordStep(ExecutionStep.StepType.LOOP_ITERATION, "Iteration 1")
        trace.recordStep(ExecutionStep.StepType.BLOCK_END, "End for")
        assertEquals("O(n)", trace.getStatistics().estimatedBigO)

        trace.clear()


        trace.recordStep(ExecutionStep.StepType.BLOCK_START, "for (let i = 0; i < n; i++)")
        trace.recordStep(ExecutionStep.StepType.BLOCK_START, "for (let j = 0; j < n; j++)")
        for (i in 1..12) {
            trace.recordStep(ExecutionStep.StepType.LOOP_ITERATION, "Iteration $i")
        }
        trace.recordStep(ExecutionStep.StepType.BLOCK_END, "End for")
        trace.recordStep(ExecutionStep.StepType.BLOCK_END, "End for")
        assertEquals("O(n²)", trace.getStatistics().estimatedBigO)
    }

    @Test
    fun `test statistics from real template execution`() {
        val debugParser = DebuggingTemplateParser(validateSyntax = false)

        val template = """<%*
let sum = 0;
for (let i = 0; i < 5; i++) {
    sum += i;
}
tR += sum;
%>""".trimIndent()

        debugParser.startDebugSession(
            onBreakpoint = { DebugAction.CONTINUE },
            startInStepMode = false
        )

        val result = debugParser.parse(template, TestContextFactory.create())
        val stats = result.trace.getStatistics()

        println("\n=== Execution Statistics ===")
        println("Total steps: ${stats.totalSteps}")
        println("Statements: ${stats.statementCount}")
        println("Loop iterations: ${stats.loopIterationCount}")
        println("Max nesting depth: ${stats.maxNestingDepth}")
        println("Total loops: ${stats.totalLoops}")
        println("Estimated Big-O: ${stats.estimatedBigO}")
        println("Max variables: ${stats.maxVariableCount}")
        println("============================\n")

        assertTrue(stats.totalSteps > 0, "Should have recorded steps")
        assertTrue(stats.loopIterationCount >= 5, "Should have at least 5 loop iterations")
        assertEquals(1, stats.totalLoops, "Should have 1 loop")
        assertEquals("O(n)", stats.estimatedBigO, "Single loop should be O(n)")
    }

    @Test
    fun `test statistics tracks variable count`() {
        val trace = ExecutionTrace()

        trace.recordStep(
            ExecutionStep.StepType.STATEMENT,
            "let x = 1",
            variables = mapOf("x" to 1)
        )

        trace.recordStep(
            ExecutionStep.StepType.STATEMENT,
            "let y = 2",
            variables = mapOf("x" to 1, "y" to 2)
        )

        trace.recordStep(
            ExecutionStep.StepType.STATEMENT,
            "let z = 3",
            variables = mapOf("x" to 1, "y" to 2, "z" to 3)
        )

        val stats = trace.getStatistics()

        assertEquals(3, stats.maxVariableCount, "Should track max variable count")
    }
}

