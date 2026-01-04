package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import java.util.concurrent.CopyOnWriteArrayList


class DebuggingTemplateParserIntegrationTest {

    private fun createContext(fileName: String = "test.md"): TemplateContext {
        return TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = fileName,
            filePath = "/$fileName"
        )
    }



    @Test
    fun `step mode - pauses at first statement when no breakpoints`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                println("PAUSED at: ${breakpoint.step.type} - ${breakpoint.step.description}")
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )


        val template = "<%* const hello = \"hello\" %>"
        val result = debugParser.parse(template, createContext())

        println("Paused at ${pausedSteps.size} steps:")
        pausedSteps.forEach { println("  - ${it.type}: ${it.description}") }


        assertTrue(pausedSteps.isNotEmpty(), "Should have paused at least once")

        val statementPauses = pausedSteps.filter { it.type == ExecutionStep.StepType.STATEMENT }
        assertTrue(statementPauses.isNotEmpty(), "Should have paused at a statement, but only paused at: ${pausedSteps.map { it.type }}")
    }

    @Test
    fun `step mode - can step through multiple statements`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()
        var stepCount = 0

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                stepCount++

                if (stepCount <= 5) DebugAction.STEP_INTO else DebugAction.CONTINUE
            },
            startInStepMode = true
        )

        val template = """
            <%* const x = 1 %>
            <%* const y = 2 %>
            <%* const z = x + y %>
            <% z %>
        """.trimIndent()

        val result = debugParser.parse(template, createContext())

        println("Stepped through ${pausedSteps.size} steps:")
        pausedSteps.forEach { step ->
            println("  - ${step.type}: ${step.description} (line ${step.displayLineNumber})")
        }


        val statementPauses = pausedSteps.filter { it.type == ExecutionStep.StepType.STATEMENT }
        assertTrue(statementPauses.size >= 3, "Should have paused at multiple statements, got ${statementPauses.size}")
    }

    @Test
    fun `step mode - stop action terminates execution`() {
        val debugParser = DebuggingTemplateParser()
        var pauseCount = 0

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pauseCount++
                if (pauseCount >= 2) DebugAction.STOP else DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        val template = """
            <%* const x = 1 %>
            <%* const y = 2 %>
            <%* const z = 3 %>
        """.trimIndent()

        val result = debugParser.parse(template, createContext())

        assertTrue(result.wasStopped, "Execution should have been stopped")
        println("Execution stopped after $pauseCount pauses")
    }



    @Test
    fun `breakpoint - pauses at correct line number`() {
        val debugParser = DebuggingTemplateParser()
        val pausedAtLines = mutableListOf<Int?>()
        val allStepsRecorded = mutableListOf<Pair<ExecutionStep.StepType, Int?>>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                println("BREAKPOINT HIT: line ${breakpoint.step.displayLineNumber}, type ${breakpoint.step.type}")
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )


        debugParser.addBreakpoint(2)


        println("Breakpoints in parser: ${debugParser.getBreakpoints()}")



        val template = """<%*
const first = "first"
const second = "second"
const third = "third"
%>"""

        println("Template:")
        template.lines().forEachIndexed { idx, line -> println("  ${idx + 1}: $line") }

        val result = debugParser.parse(template, createContext())

        println("All steps in trace:")
        result.trace.allSteps.forEach { step ->
            println("  [${step.type}] line=${step.displayLineNumber}: ${step.description}")
        }

        println("Paused at lines: $pausedAtLines")


        assertTrue(pausedAtLines.contains(2), "Should have paused at line 2, but paused at: $pausedAtLines")
    }

    @Test
    fun `breakpoint - regression test for UI scenario where breakpoints are set before debugging starts`() {








        val debugParser = DebuggingTemplateParser()
        val pausedAtLines = mutableListOf<Int?>()
        var pauseCount = 0


        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pauseCount++
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                println("PAUSE #$pauseCount: line ${breakpoint.step.displayLineNumber}, type ${breakpoint.step.type}, desc='${breakpoint.step.description}'")
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )



        debugParser.addBreakpoint(2)
        debugParser.addBreakpoint(4)

        val template = """<%*
const x = 1
const y = 2
const z = 3
const sum = x + y + z
%>"""

        println("\nTemplate with breakpoints at lines 2 and 4:")
        template.lines().forEachIndexed { idx, line ->
            val bp = if (idx + 1 in listOf(2, 4)) " <-- BREAKPOINT" else ""
            println("  ${idx + 1}: $line$bp")
        }


        val result = debugParser.parse(template, createContext())

        println("\nAll execution steps:")
        result.trace.allSteps.forEach { step ->
            val bp = if (step.displayLineNumber in listOf(2, 4)) " <-- BREAKPOINT" else ""
            println("  [${step.type}] line=${step.displayLineNumber}: ${step.description.take(50)}$bp")
        }

        println("\nPaused at lines: $pausedAtLines")
        println("Total pauses: $pauseCount")


        assertEquals(2, pauseCount, "Should have paused exactly 2 times at breakpoints")
        assertTrue(pausedAtLines.contains(2), "Should have paused at line 2")
        assertTrue(pausedAtLines.contains(4), "Should have paused at line 4")
        assertEquals(listOf(2, 4), pausedAtLines, "Should have paused at lines 2 and 4 in order")
    }

    @Test
    fun `breakpoint - regression test with startInStepMode based on breakpoints isEmpty`() {




        val debugParser = DebuggingTemplateParser()
        val pausedAtLines = mutableListOf<Int?>()
        var pauseCount = 0

        val breakpoints = setOf(2, 4)



        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pauseCount++
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                println("PAUSE #$pauseCount: line ${breakpoint.step.displayLineNumber}, type ${breakpoint.step.type}")
                DebugAction.CONTINUE
            },
            startInStepMode = breakpoints.isEmpty()
        )



        breakpoints.forEach { debugParser.addBreakpoint(it) }

        val template = """<%*
const x = 1
const y = 2
const z = 3
const sum = x + y + z
%>"""

        println("\nBreakpoints: $breakpoints")
        println("startInStepMode: ${breakpoints.isEmpty()}")


        val result = debugParser.parse(template, createContext())

        println("Paused at lines: $pausedAtLines")
        println("Total pauses: $pauseCount")


        assertEquals(2, pauseCount, "Should pause exactly 2 times (only at breakpoints, not every step)")
        assertEquals(listOf(2, 4), pausedAtLines, "Should pause at lines 2 and 4")
    }

    @Test
    fun `breakpoint - multiple breakpoints work`() {
        val debugParser = DebuggingTemplateParser()
        val pausedAtLines = mutableListOf<Int?>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )





        debugParser.addBreakpoint(1)
        debugParser.addBreakpoint(3)

        val template = """<%*
const first = "first"
const second = "second"
const third = "third"
%>"""

        val result = debugParser.parse(template, createContext())

        println("Paused at lines: $pausedAtLines")


        assertTrue(pausedAtLines.contains(1), "Should have paused at line 1")
        assertTrue(pausedAtLines.contains(3), "Should have paused at line 3")
        assertFalse(pausedAtLines.contains(2), "Should NOT have paused at line 2 (no breakpoint)")
    }

    @Test
    fun `breakpoint - no pause when no breakpoints and not in step mode`() {
        val debugParser = DebuggingTemplateParser()
        var pauseCount = 0

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pauseCount++
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )



        val template = "<% tp.date.now() %>"
        val result = debugParser.parse(template, createContext())

        assertEquals(0, pauseCount, "Should not have paused without breakpoints")
        assertFalse(result.wasStopped, "Should have completed normally")
    }



    @Test
    fun `frontmatter - line numbers are relative to content without frontmatter`() {
        val debugParser = DebuggingTemplateParser()
        val pausedAtLines = mutableListOf<Int?>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                println("Paused at line ${breakpoint.step.displayLineNumber}: ${breakpoint.step.description}")
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )




        debugParser.addBreakpoint(2)


        val contentWithoutFrontmatter = """<%*
const first = "first line after frontmatter"
const second = "second line"
%>"""

        val result = debugParser.parse(contentWithoutFrontmatter, createContext())

        println("Paused at lines: $pausedAtLines")
        assertTrue(pausedAtLines.contains(2), "Should pause at line 2 (first statement in content)")
    }

    @Test
    fun `frontmatter offset calculation - simulates DebugTemplateAction behavior`() {






        val frontmatterParser = FrontmatterParser()



        val fullDocument = """---
title: Test
date: 2024-01-01
---
<%*
const first = "first template block"
const second = "second template block"
%>"""

        val parseResult = frontmatterParser.parse(fullDocument)


        val frontmatterLineOffset = if (parseResult.hasFrontmatter) {
            parseResult.frontmatterRaw.count { it == '\n' } + 3
        } else {
            0
        }

        println("Full document:")
        fullDocument.lines().forEachIndexed { idx, line -> println("  ${idx + 1}: $line") }
        println()
        println("Frontmatter raw: '${parseResult.frontmatterRaw}'")
        println("Frontmatter line offset: $frontmatterLineOffset")
        println()
        println("Content without frontmatter:")
        parseResult.content.lines().forEachIndexed { idx, line -> println("  ${idx + 1}: $line") }





        val userBreakpointLine = 6
        val adjustedBreakpoint = userBreakpointLine - frontmatterLineOffset

        val astLineForFirstStatement = 1

        println()
        println("User breakpoint at line $userBreakpointLine in full document")
        println("Adjusted breakpoint: $adjustedBreakpoint in content")
        println("AST line for first statement: $astLineForFirstStatement")


        val debugParser = DebuggingTemplateParser()
        val pausedAtLines = mutableListOf<Int?>()
        val pausedDescriptions = mutableListOf<String>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                pausedDescriptions.add(breakpoint.step.description)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )


        debugParser.addBreakpoint(astLineForFirstStatement)

        val result = debugParser.parse(parseResult.content, createContext())

        println()
        println("Paused at lines: $pausedAtLines")
        println("Paused descriptions: $pausedDescriptions")


        assertTrue(pausedAtLines.contains(astLineForFirstStatement),
            "Should pause at AST line $astLineForFirstStatement, but paused at: $pausedAtLines")


        assertTrue(pausedDescriptions.any { it.contains("first") },
            "Should have paused at first statement")
    }

    @Test
    fun `frontmatter offset - ExecutionStep lineNumber should be document-relative when offset is provided`() {





        val frontmatterParser = FrontmatterParser()


        val fullDocument = """
            ---
            title: Debugger Showcase
            description: Test template for debugging
            ---
            # Debugger Showcase

            <%*
            // Set a breakpoint here to see variables being created
            const userName = "Developer";
            const projectName = "Templater";
            let counter = 0;

            // Step through to watch 'counter' change
            counter = counter + 1;
            counter = counter + 5;
            counter = counter * 2;

            tR += `Hello, ${"$"}{userName}! Welcome to ${"$"}{projectName}.\n`;
            tR += `Counter value: ${"$"}{counter}\n`;
            %>
        """.trimIndent()

        val parseResult = frontmatterParser.parse(fullDocument)


        val frontmatterLineOffset = if (parseResult.hasFrontmatter) {
            parseResult.frontmatterRaw.count { it == '\n' } + 3
        } else {
            0
        }


        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<Pair<Int?, String>>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step.displayLineNumber to breakpoint.step.description)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )


        debugParser.parse(parseResult.content, createContext(), documentLineOffset = frontmatterLineOffset)


        val helloStep = pausedSteps.find { it.second.contains("Hello") }
        assertNotNull(helloStep, "Should have a step containing 'Hello'")

        val documentLineNumber = helloStep!!.first
        assertNotNull(documentLineNumber, "Step should have a line number")




        assertTrue(documentLineNumber!! > frontmatterLineOffset,
            "Document line ($documentLineNumber) should be greater than frontmatter offset ($frontmatterLineOffset)")


        assertTrue(documentLineNumber in (frontmatterLineOffset + 1)..25,
            "Document line ($documentLineNumber) should be in a reasonable range")
    }



    @Test
    fun `variables - can inspect variables at breakpoint`() {
        val debugParser = DebuggingTemplateParser()
        val capturedVariables = mutableListOf<Map<String, Any?>>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                capturedVariables.add(breakpoint.step.variables.toMap())
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        val template = """
            <%* const myVar = "hello" %>
            <%* const myNum = 42 %>
            <% myVar + " " + myNum %>
        """.trimIndent()

        val result = debugParser.parse(template, createContext())

        println("Captured variables at each step:")
        capturedVariables.forEachIndexed { idx, vars ->
            println("  Step $idx: $vars")
        }


        assertTrue(capturedVariables.isNotEmpty(), "Should have captured variables")
    }



    @Test
    fun `incremental updates - onBlockProcessed callback is called`() {
        val debugParser = DebuggingTemplateParser()
        val blockUpdates = CopyOnWriteArrayList<BlockUpdate>()

        debugParser.startDebugSession(
            onBreakpoint = { DebugAction.CONTINUE },
            startInStepMode = false,
            onBlockProcessed = { originalBlock, replacement, currentDocument, lineNumber ->
                blockUpdates.add(BlockUpdate(originalBlock, replacement, currentDocument, lineNumber))
            }
        )

        val template = """
            First: <% "AAA" %>
            Second: <% "BBB" %>
            Third: <% "CCC" %>
        """.trimIndent()

        val result = debugParser.parse(template, createContext())

        println("Block updates received: ${blockUpdates.size}")
        blockUpdates.forEachIndexed { idx, update ->
            println("  Update $idx:")
            println("    Original: ${update.originalBlock}")
            println("    Replacement: ${update.replacement}")
            println("    Line: ${update.lineNumber}")
            println("    Current doc length: ${update.currentDocument.length}")
        }

        assertEquals(3, blockUpdates.size, "Should have 3 block updates")


        assertTrue(blockUpdates[0].currentDocument.contains("AAA"), "First update should contain AAA")
        assertTrue(blockUpdates[1].currentDocument.contains("AAA"), "Second update should still contain AAA")
        assertTrue(blockUpdates[1].currentDocument.contains("BBB"), "Second update should contain BBB")
        assertTrue(blockUpdates[2].currentDocument.contains("CCC"), "Third update should contain CCC")
    }

    @Test
    fun `incremental updates - document state is correct after each block`() {
        val debugParser = DebuggingTemplateParser()
        val documentStates = mutableListOf<String>()

        debugParser.startDebugSession(
            onBreakpoint = { DebugAction.CONTINUE },
            startInStepMode = false,
            onBlockProcessed = { _, _, currentDocument, _ ->
                documentStates.add(currentDocument)
            }
        )

        val template = """
            A: <% "1" %>
            B: <% "2" %>
            C: <% "3" %>
        """.trimIndent()

        val result = debugParser.parse(template, createContext())

        println("Document states after each block:")
        documentStates.forEachIndexed { idx, state ->
            println("  After block $idx:")
            println("    $state")
            println()
        }


        assertEquals(result.result, documentStates.last(), "Final document state should match result")
    }



    @Test
    fun `trace - captures complete execution history`() {
        val debugParser = DebuggingTemplateParser()

        debugParser.startDebugSession(
            onBreakpoint = { DebugAction.CONTINUE },
            startInStepMode = false
        )

        val template = """
            <%* const x = 1 %>
            <%* const y = 2 %>
            Result: <% x + y %>
        """.trimIndent()

        val result = debugParser.parse(template, createContext())

        val trace = result.trace
        val steps = trace.allSteps

        println("Execution trace (${steps.size} steps):")
        steps.forEach { step ->
            println("  [${step.type}] ${step.description} (line ${step.displayLineNumber})")
        }


        assertTrue(steps.any { it.type == ExecutionStep.StepType.TEMPLATE_START }, "Should have TEMPLATE_START")
        assertTrue(steps.any { it.type == ExecutionStep.StepType.TEMPLATE_END }, "Should have TEMPLATE_END")
        assertTrue(steps.any { it.type == ExecutionStep.StepType.STATEMENT }, "Should have STATEMENT steps")
    }



    @Test
    fun `edge case - empty template`() {
        val debugParser = DebuggingTemplateParser()
        var pauseCount = 0

        debugParser.startDebugSession(
            onBreakpoint = {
                pauseCount++
                DebugAction.CONTINUE
            },
            startInStepMode = true
        )

        val result = debugParser.parse("", createContext())

        println("Empty template - pause count: $pauseCount")
        println("Result: '${result.result}'")

        assertEquals("", result.result, "Empty template should produce empty result")
    }

    @Test
    fun `edge case - template with only text, no blocks`() {
        val debugParser = DebuggingTemplateParser()
        var pauseCount = 0

        debugParser.startDebugSession(
            onBreakpoint = {
                pauseCount++
                DebugAction.CONTINUE
            },
            startInStepMode = true
        )

        val template = "Just plain text, no template blocks"
        val result = debugParser.parse(template, createContext())

        println("Plain text template - pause count: $pauseCount")
        println("Result: '${result.result}'")

        assertEquals(template, result.result, "Plain text should pass through unchanged")
    }

    @Test
    fun `edge case - breakpoint on non-existent line`() {
        val debugParser = DebuggingTemplateParser()
        var pauseCount = 0

        debugParser.startDebugSession(
            onBreakpoint = {
                pauseCount++
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )


        debugParser.addBreakpoint(100)

        val template = "<% 'single line' %>"
        val result = debugParser.parse(template, createContext())

        println("Non-existent breakpoint - pause count: $pauseCount")

        assertEquals(0, pauseCount, "Should not pause at non-existent line")
    }



    @Test
    fun `handler calls - tp_date_now is debuggable`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )

        debugParser.addBreakpoint(1)


        val template = "<%* const date = tp.date.now() %>"
        val result = debugParser.parse(template, createContext())

        println("Handler call test - paused steps: ${pausedSteps.size}")
        pausedSteps.forEach { println("  - ${it.type}: ${it.description}") }


        assertTrue(pausedSteps.any { it.description.contains("tp.date.now") },
            "Should pause at tp.date.now() call")
    }

    @Test
    fun `handler calls - tp_file_title is debuggable`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )


        val template = "<%* const title = tp.file.title %>"
        val result = debugParser.parse(template, createContext("my-test-file.md"))

        println("File title test - paused steps: ${pausedSteps.size}")
        pausedSteps.forEach { println("  - ${it.type}: ${it.description}") }

        val statementSteps = pausedSteps.filter { it.type == ExecutionStep.StepType.STATEMENT }
        assertTrue(statementSteps.isNotEmpty(), "Should have statement steps for tp.file.title")
    }



    @Test
    fun `execution blocks - multi-statement block debugs each statement`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        val template = """<%*
const a = 1
const b = 2
const c = a + b
tR += c
%>""".trimIndent()

        val result = debugParser.parse(template, createContext())

        println("Multi-statement block - paused steps:")
        pausedSteps.forEach { println("  - ${it.type}: ${it.description} (line ${it.displayLineNumber})") }

        val statementSteps = pausedSteps.filter { it.type == ExecutionStep.StepType.STATEMENT }

        assertTrue(statementSteps.size >= 4,
            "Should have at least 4 statement pauses, got ${statementSteps.size}")
    }

    @Test
    fun `execution blocks - tR accumulator works during debugging`() {
        val debugParser = DebuggingTemplateParser()

        debugParser.startDebugSession(
            onBreakpoint = { DebugAction.CONTINUE },
            startInStepMode = false
        )

        val template = """<%*
tR += "Hello "
tR += "World"
%>"""

        val result = debugParser.parse(template, createContext())

        println("tR accumulator result: '${result.result}'")
        assertEquals("Hello World", result.result.trim(), "tR accumulator should work during debugging")
    }



    @Test
    fun `for loops - can step through loop iterations`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        val template = """<%*
for (let i = 0; i < 3; i++) {
    tR += i + " "
}
%>""".trimIndent()

        val result = debugParser.parse(template, createContext())

        println("For loop test - paused steps: ${pausedSteps.size}")
        pausedSteps.forEach { println("  - ${it.type}: ${it.description}") }


        val blockStarts = pausedSteps.filter { it.type == ExecutionStep.StepType.BLOCK_START }
        println("Block starts: ${blockStarts.size}")


        assertTrue(result.result.contains("0") && result.result.contains("1") && result.result.contains("2"),
            "Loop should execute 3 times, got: '${result.result}'")
    }



    @Test
    fun `if else - can debug conditional branches`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        val template = """<%*
const x = 10
if (x > 5) {
    tR += "big"
} else {
    tR += "small"
}
%>""".trimIndent()

        val result = debugParser.parse(template, createContext())

        println("If/else test - paused steps: ${pausedSteps.size}")
        pausedSteps.forEach { println("  - ${it.type}: ${it.description}") }

        assertEquals("big", result.result.trim(), "Should take the 'big' branch")
    }

    @Test
    fun `if else - BLOCK_START line numbers include frontmatter offset`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )


        val fullDocument = """---
title: Test
---
Line 1
<%*
const hour = 5
if (hour < 6) {
    tR += "early"
}
%>"""


        val frontmatterParser = FrontmatterParser()
        val frontmatterResult = frontmatterParser.parse(fullDocument)
        val contentWithoutFrontmatter = frontmatterResult.content
        val frontmatterOffset = if (frontmatterResult.hasFrontmatter) {
            frontmatterResult.frontmatterRaw.count { it == '\n' } + 3
        } else {
            0
        }

        println("Full document:")
        fullDocument.lines().forEachIndexed { i, line -> println("  ${i + 1}: $line") }
        println("\nFrontmatter offset: $frontmatterOffset")
        println("\nContent without frontmatter:")
        contentWithoutFrontmatter.lines().forEachIndexed { i, line -> println("  ${i + 1}: $line") }

        val result = debugParser.parse(
            content = contentWithoutFrontmatter,
            context = createContext(),
            documentLineOffset = frontmatterOffset
        )

        println("\nPaused steps with line numbers:")
        pausedSteps.forEach { step ->
            println("  ${step.type}: ${step.description} (line ${step.displayLineNumber})")
        }


        val blockStart = pausedSteps.find { it.type == ExecutionStep.StepType.BLOCK_START }
        assertNotNull(blockStart, "Should have a BLOCK_START step")









        assertEquals(7, blockStart!!.displayLineNumber,
            "BLOCK_START should be at document line 7 (if statement line)")


        val ifStatement = pausedSteps.find {
            it.type == ExecutionStep.StepType.STATEMENT && it.description.contains("if (hour < 6)")
        }
        assertNotNull(ifStatement, "Should have a STATEMENT step for the if")
        assertEquals(7, ifStatement!!.displayLineNumber,
            "if STATEMENT should be at document line 7")

        assertTrue(result.result.contains("early"), "Result should contain 'early'")
    }



    @Test
    fun `variable reference - can debug variable interpolation`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )



        val template = """<%*
const greeting = "Hello"
tR += greeting
%>"""

        val result = debugParser.parse(template, createContext())

        println("Variable reference test - paused steps:")
        pausedSteps.forEach { println("  - ${it.type}: ${it.description}") }


        val statementSteps = pausedSteps.filter { it.type == ExecutionStep.StepType.STATEMENT }
        assertTrue(statementSteps.size >= 2, "Should pause at const and tR assignment")

        assertTrue(result.result.contains("Hello"), "Should output the variable value")
    }



    @Test
    fun `mixed blocks - execution and interpolation blocks work together`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()
        val blockUpdates = mutableListOf<String>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true,
            onBlockProcessed = { _, replacement, _, _ ->
                blockUpdates.add(replacement)
            }
        )

        val template = """<%* const name = "World" %>
Hello, <% name %>!
<%* const count = 42 %>
Count: <% count %>"""

        val result = debugParser.parse(template, createContext())

        println("Mixed blocks test:")
        println("  Paused steps: ${pausedSteps.size}")
        println("  Block updates: $blockUpdates")
        println("  Result: ${result.result}")

        assertTrue(result.result.contains("Hello, World!"), "Should interpolate name")
        assertTrue(result.result.contains("Count: 42"), "Should interpolate count")
    }



    @Test
    fun `whitespace trimming - works during debugging`() {
        val debugParser = DebuggingTemplateParser()

        debugParser.startDebugSession(
            onBreakpoint = { DebugAction.CONTINUE },
            startInStepMode = false
        )

        val template = """Line1
<%_ "trimmed" _%>
Line2"""

        val result = debugParser.parse(template, createContext())

        println("Whitespace trimming result: '${result.result}'")


        assertTrue(result.result.contains("Line1trimmedLine2") ||
                   result.result.contains("Line1\ntrimmed\nLine2").not(),
            "Whitespace should be trimmed")
    }



    @Test
    fun `error handling - syntax error in template`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )

        debugParser.addBreakpoint(1)


        val template = "<% \"unclosed %>"
        val result = debugParser.parse(template, createContext())

        println("Error handling test - result: '${result.result}'")

        assertNotNull(result.result, "Should return a result even with syntax error")
    }

    @Test
    fun `error handling - undefined variable`() {
        val debugParser = DebuggingTemplateParser()

        debugParser.startDebugSession(
            onBreakpoint = { DebugAction.CONTINUE },
            startInStepMode = false
        )

        val template = "<% undefinedVariable %>"
        val result = debugParser.parse(template, createContext())

        println("Undefined variable test - result: '${result.result}'")

        assertNotNull(result.result, "Should return a result even with undefined variable")
    }



    @Test
    fun `complex template - realistic daily note template`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        val template = """# Daily Note
Date: <% tp.date.now("YYYY-MM-DD") %>

## Tasks
<%*
const tasks = ["Task 1", "Task 2", "Task 3"]
for (const task of tasks) {
    tR += "- [ ] " + task + "\n"
}
%>

## Notes
<% tp.file.title %>"""

        val result = debugParser.parse(template, createContext("2024-01-15.md"))

        println("Complex template test:")
        println("  Total paused steps: ${pausedSteps.size}")
        println("  Statement steps: ${pausedSteps.count { it.type == ExecutionStep.StepType.STATEMENT }}")
        println("  Result preview: ${result.result.take(200)}...")


        assertFalse(result.wasStopped, "Should complete without stopping")
        assertTrue(result.result.contains("Daily Note"), "Should contain header")
        assertTrue(result.result.contains("Task 1"), "Should contain tasks")
    }

    @Test
    fun `complex template - nested conditionals and loops`() {
        val debugParser = DebuggingTemplateParser()
        val pausedSteps = mutableListOf<ExecutionStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedSteps.add(breakpoint.step)
                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        val template = """<%*
const items = [1, 2, 3, 4, 5]
for (const item of items) {
    if (item % 2 === 0) {
        tR += item + " is even\n"
    } else {
        tR += item + " is odd\n"
    }
}
%>"""

        val result = debugParser.parse(template, createContext())

        println("Nested conditionals test:")
        println("  Paused steps: ${pausedSteps.size}")
        println("  Result:\n${result.result}")

        assertTrue(result.result.contains("1 is odd"), "Should identify 1 as odd")
        assertTrue(result.result.contains("2 is even"), "Should identify 2 as even")
        assertTrue(result.result.contains("5 is odd"), "Should identify 5 as odd")
    }



    @Test
    fun `breakpoint - on multi-line execution block`() {
        val debugParser = DebuggingTemplateParser()
        val pausedAtLines = mutableListOf<Int?>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )


        debugParser.addBreakpoint(3)

        val template = """Line 1
<%*
const x = 1
const y = 2
%>
Line 6"""

        val result = debugParser.parse(template, createContext())

        println("Multi-line block breakpoint test:")
        println("  Paused at lines: $pausedAtLines")


        assertTrue(pausedAtLines.isNotEmpty(), "Should pause at breakpoint in multi-line block")
    }

    @Test
    fun `breakpoint - continue after breakpoint processes remaining blocks`() {
        val debugParser = DebuggingTemplateParser()
        val pausedAtLines = mutableListOf<Int?>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                pausedAtLines.add(breakpoint.step.displayLineNumber)
                DebugAction.CONTINUE
            },
            startInStepMode = false
        )


        debugParser.addBreakpoint(1)


        val template = """<%*
tR += "first"
tR += "second"
tR += "third"
%>"""

        val result = debugParser.parse(template, createContext())

        println("Continue after breakpoint test:")
        println("  Paused at lines: $pausedAtLines")
        println("  Result: ${result.result}")


        assertEquals(1, pausedAtLines.size, "Should only pause once at line 1")
        assertTrue(result.result.contains("first"), "Should process first statement")
        assertTrue(result.result.contains("second"), "Should process second statement")
        assertTrue(result.result.contains("third"), "Should process third statement")
    }


    data class BlockUpdate(
        val originalBlock: String,
        val replacement: String,
        val currentDocument: String,
        val lineNumber: Int
    )
}

