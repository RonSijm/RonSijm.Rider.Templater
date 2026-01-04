package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.ast.ASTToControlFlowConverter


class DebuggerStartPositionTest {

    @Test
    fun `test debugger stops at first statement when breakpoint on opening tag`() {

        val template = """
<%*
const digitsText = 500;
if (digitsText == null) { tR += "Cancelled"; return; }

const digitsAfterDecimal = Math.max(1, Math.min(200000, parseInt(digitsText, 10) || 0));

const now = () =>
(typeof performance !== "undefined" && performance.now) ? performance.now() : Date.now();

function spy(totalDigits) {
const len = Math.floor(totalDigits * 10 / 3) + 1;
const a = new Array(len).fill(2);
return a;
}

function cs32(s) {
let h = 0;
for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0;
return h >>> 0;
}

const totalDigits = digitsAfterDecimal + 1;

spy(30);
%>
        """.trimIndent()

        val debugParser = DebuggingTemplateParser()
        val executionTrace = mutableListOf<String>()
        var firstBreakpointStep: ExecutionStep? = null
        var breakpointCount = 0

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                breakpointCount++


                if (firstBreakpointStep == null) {
                    firstBreakpointStep = breakpoint.step
                }


                executionTrace.add("Breakpoint #$breakpointCount: ${breakpoint.step.description} [Line ${breakpoint.step.displayLineNumber}]")


                if (breakpointCount == 1) DebugAction.CONTINUE else DebugAction.STEP_INTO
            },
            startInStepMode = false
        )


        debugParser.addBreakpoint(1)

        debugParser.parse(template, TestContextFactory.create())


        println("=== Execution Trace ===")
        executionTrace.forEach { println(it) }
        println("======================")


        assertNotNull(firstBreakpointStep, "First breakpoint should be hit")



        val firstStep = firstBreakpointStep!!
        println("First breakpoint step: ${firstStep.description} [Line ${firstStep.displayLineNumber}]")





        assertTrue(
            firstStep.description.contains("const digitsText = 500") ||
            firstStep.displayLineNumber == 2,
            "First breakpoint should be at the first statement (line 2: const digitsText = 500), " +
            "but was at: ${firstStep.description} [Line ${firstStep.displayLineNumber}]"
        )



        assertTrue(
            breakpointCount == 1 || executionTrace.size <= 2,
            "Expected minimal execution before first breakpoint, but found ${executionTrace.size} trace entries"
        )
    }

    @Test
    fun `test debugger stops when breakpoint set BEFORE starting debug session`() {

        val template = """
<%*
const digitsText = 500;
if (digitsText == null) { tR += "Cancelled"; return; }

const digitsAfterDecimal = Math.max(1, Math.min(200000, parseInt(digitsText, 10) || 0));

const now = () =>
(typeof performance !== "undefined" && performance.now) ? performance.now() : Date.now();

function spy(totalDigits) {
const len = Math.floor(totalDigits * 10 / 3) + 1;
const a = new Array(len).fill(2);
return a;
}

function cs32(s) {
let h = 0;
for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) >>> 0;
return h >>> 0;
}

const totalDigits = digitsAfterDecimal + 1;

spy(30);
%>
        """.trimIndent()

        val debugParser = DebuggingTemplateParser()
        val executionTrace = mutableListOf<String>()
        var firstBreakpointStep: ExecutionStep? = null
        var breakpointCount = 0


        debugParser.addBreakpoint(1)

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                breakpointCount++


                if (firstBreakpointStep == null) {
                    firstBreakpointStep = breakpoint.step
                }


                executionTrace.add("Breakpoint #$breakpointCount: ${breakpoint.step.description} [Line ${breakpoint.step.displayLineNumber}]")


                if (breakpointCount == 1) DebugAction.CONTINUE else DebugAction.STEP_INTO
            },
            startInStepMode = false
        )

        debugParser.parse(template, TestContextFactory.create())


        println("=== Execution Trace (Breakpoint Set BEFORE Debug Session) ===")
        executionTrace.forEach { println(it) }
        println("======================")


        assertNotNull(firstBreakpointStep, "First breakpoint should be hit")

        val firstStep = firstBreakpointStep!!
        println("First breakpoint step: ${firstStep.description} [Line ${firstStep.displayLineNumber}]")





        assertTrue(
            firstStep.description.contains("const digitsText = 500") ||
            firstStep.displayLineNumber == 2,
            "First breakpoint should be at the first statement (line 2: const digitsText = 500), " +
            "but was at: ${firstStep.description} [Line ${firstStep.displayLineNumber}]"
        )



        assertTrue(
            breakpointCount == 1 || executionTrace.size <= 2,
            "Expected minimal execution before first breakpoint, but found ${executionTrace.size} trace entries"
        )
    }

    @Test
    fun `test debugger execution trace when breakpoint on first line`() {

        val template = """
<%*
const a = 1;
const b = 2;
const c = 3;
%>
        """.trimIndent()

        val debugParser = DebuggingTemplateParser()
        val executedStatements = mutableListOf<String>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->

                if (executedStatements.isEmpty()) {
                    println("=== First Breakpoint Hit ===")
                    println("Current step: ${breakpoint.step.description} [Line ${breakpoint.step.displayLineNumber}]")
                    println("Step ID: ${breakpoint.step.id}")
                }

                executedStatements.add(breakpoint.step.description)
                DebugAction.STEP_INTO
            },
            startInStepMode = false
        )


        debugParser.addBreakpoint(1)

        debugParser.parse(template, TestContextFactory.create())

        println("=== All Executed Statements ===")
        executedStatements.forEachIndexed { index, stmt ->
            println("$index: $stmt")
        }



        assertTrue(
            executedStatements.isNotEmpty(),
            "Should have executed at least one statement"
        )

        val firstExecuted = executedStatements.first()
        assertTrue(
            firstExecuted.contains("const a = 1"),
            "First executed statement should be 'const a = 1', but was: $firstExecuted"
        )
    }

    @Test
    fun `test breakpoint triggers and visualizer can find matching blocks by statement ID`() {






        val template = """
<%*
const a = 1;
const b = 2;
if (a < b) {
    const c = 3;
}
const d = 4;
%>
        """.trimIndent()

        val debugParser = DebuggingTemplateParser()
        val capturedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                capturedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        debugParser.addBreakpoint(1)
        debugParser.parse(template, TestContextFactory.create())


        val ast = debugParser.getTemplateAST()
        assertNotNull(ast, "AST should be built")

        val converter = ASTToControlFlowConverter()
        val graph = converter.convert(ast!!)

        println("=== Captured Steps and Block Matching ===")


        val statementSteps = capturedSteps.filter { it.type == ExecutionStep.StepType.STATEMENT }

        assertTrue(statementSteps.isNotEmpty(), "Should have captured statement steps")


        val stepsWithoutStatementId = statementSteps.filter { it.statementNode?.id == null }
        if (stepsWithoutStatementId.isNotEmpty()) {
            println("ERROR: Steps without statement IDs:")
            stepsWithoutStatementId.forEach {
                println("  - Line ${it.displayLineNumber}: ${it.description}")
            }
        }
        assertTrue(
            stepsWithoutStatementId.isEmpty(),
            "All statement steps should have statement IDs. Found ${stepsWithoutStatementId.size} without IDs"
        )


        var matchedCount = 0
        var unmatchedCount = 0

        for (step in statementSteps) {
            val statementId = step.statementNode?.id
            val matchingNode = graph.nodes.find { it.id == statementId }

            if (matchingNode != null) {
                matchedCount++
                println("? Step '${step.description}' [ID: $statementId] -> Node '${matchingNode.code}' [ID: ${matchingNode.id}]")
            } else {
                unmatchedCount++
                println("? Step '${step.description}' [ID: $statementId] -> NO MATCHING NODE")
            }
        }

        println("======================")
        println("Matched: $matchedCount / ${statementSteps.size}")
        println("Unmatched: $unmatchedCount / ${statementSteps.size}")


        assertEquals(
            statementSteps.size,
            matchedCount,
            "All statement steps should have matching control flow nodes. " +
            "Expected ${statementSteps.size} matches, but got $matchedCount"
        )


        val firstStatementStep = statementSteps.first()
        assertTrue(
            firstStatementStep.description.contains("const a = 1"),
            "First statement should be 'const a = 1', but was: ${firstStatementStep.description}"
        )


        val firstNode = graph.nodes.find { it.id == firstStatementStep.statementNode?.id }
        assertNotNull(firstNode, "First step should have a matching control flow node")
        assertTrue(
            firstNode!!.code?.contains("const a = 1") == true,
            "First node should contain 'const a = 1', but was: ${firstNode.code}"
        )
    }

    @Test
    fun `test all expected statements are captured when stepping through with breakpoint on line 1`() {



        val template = """
<%*
const a = 1;
const b = 2;
const c = 3;
%>
        """.trimIndent()

        val debugParser = DebuggingTemplateParser()
        val capturedDescriptions = mutableListOf<String>()

        debugParser.addBreakpoint(1)

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                if (breakpoint.step.type == ExecutionStep.StepType.STATEMENT) {
                    capturedDescriptions.add(breakpoint.step.description)
                }
                DebugAction.STEP_INTO
            },
            startInStepMode = false
        )

        debugParser.parse(template, TestContextFactory.create())

        println("=== Captured Statements ===")
        capturedDescriptions.forEachIndexed { index, desc ->
            println("$index: $desc")
        }
        println("======================")


        assertTrue(
            capturedDescriptions.size >= 3,
            "Should capture at least 3 statements, but got ${capturedDescriptions.size}"
        )


        assertTrue(
            capturedDescriptions[0].contains("const a = 1"),
            "First statement should be 'const a = 1', but was: ${capturedDescriptions[0]}"
        )
        assertTrue(
            capturedDescriptions[1].contains("const b = 2"),
            "Second statement should be 'const b = 2', but was: ${capturedDescriptions[1]}"
        )
        assertTrue(
            capturedDescriptions[2].contains("const c = 3"),
            "Third statement should be 'const c = 3', but was: ${capturedDescriptions[2]}"
        )
    }
}

