package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory


class DebugBreakpointTest {

    @Test
    fun `breakpoint on simple statement stops execution`() {
        val debugParser = DebuggingTemplateParser(validateSyntax = false)

        val template = """<%*
let x = 1;
let y = 2;
let z = 3;
tR += x + y + z;
%>"""

        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )






        debugParser.addBreakpoint(2)

        debugParser.parse(template, TestContextFactory.create())


        assertEquals(1, pausedSteps.size, "Should pause exactly once at the breakpoint")
        val pausedStep = pausedSteps.first()
        assertEquals(2, pausedStep.displayLineNumber, "Should pause at line 2")
        assertTrue(pausedStep.description.contains("let y = 2"), "Should pause at 'let y = 2' statement")
    }

    @Test
    fun `breakpoint in loop stops on each iteration`() {
        val debugParser = DebuggingTemplateParser(validateSyntax = false)

        val template = """<%*
let counter = 0;

for (let i = 0; i < 3; i++) {
    counter++;
}

tR += counter;
%>"""

        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )







        debugParser.addBreakpoint(3)

        debugParser.parse(template, TestContextFactory.create())





        assertEquals(5, pausedSteps.size, "Should pause 5 times (all STATEMENT steps on line 3)")


        pausedSteps.forEach { step ->
            assertEquals(3, step.displayLineNumber, "All pauses should be at line 3")
            assertEquals(ExecutionStep.StepType.STATEMENT, step.type, "Should only pause on STATEMENT steps")
        }


        val counterSteps = pausedSteps.filter { it.description.trim() == "counter++" }
        assertEquals(3, counterSteps.size, "Should pause 3 times at 'counter++' statement")


        val forSteps = pausedSteps.filter { it.description.contains("for (let i") }
        assertEquals(1, forSteps.size, "Should pause once at for loop header")


        val trSteps = pausedSteps.filter { it.description.contains("tR += counter") }
        assertEquals(1, trSteps.size, "Should pause once at 'tR += counter'")
    }

    @Test
    fun `multiple breakpoints work correctly`() {
        val debugParser = DebuggingTemplateParser(validateSyntax = false)

        val template = """<%*
let a = 1;
let b = 2;
let c = 3;
tR += a + b + c;
%>"""

        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )





        debugParser.addBreakpoint(1)
        debugParser.addBreakpoint(3)

        debugParser.parse(template, TestContextFactory.create())


        assertEquals(2, pausedSteps.size, "Should pause twice (once at each breakpoint)")


        assertEquals(1, pausedSteps[0].displayLineNumber, "First pause should be at line 1")
        assertTrue(pausedSteps[0].description.contains("let a = 1"), "First pause at 'let a = 1'")


        assertEquals(3, pausedSteps[1].displayLineNumber, "Second pause should be at line 3")
        assertTrue(pausedSteps[1].description.contains("let c = 3"), "Second pause at 'let c = 3'")
    }

    @Test
    fun `breakpoint on loop header stops before loop starts`() {
        val debugParser = DebuggingTemplateParser(validateSyntax = false)

        val template = """<%*
let counter = 0;

for (let i = 0; i < 3; i++) {
    counter++;
}

tR += counter;
%>"""

        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )





        debugParser.addBreakpoint(3)

        debugParser.parse(template, TestContextFactory.create())


        assertTrue(pausedSteps.isNotEmpty(), "Should pause at the for loop")
        val firstPause = pausedSteps.first()
        assertEquals(3, firstPause.displayLineNumber, "Should pause at line 3")
        assertTrue(firstPause.description.contains("for"), "Should pause at for loop statement")
    }
}

