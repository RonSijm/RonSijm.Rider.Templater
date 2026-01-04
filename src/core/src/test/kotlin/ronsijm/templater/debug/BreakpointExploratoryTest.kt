package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext


class BreakpointExploratoryTest {

    @Test
    fun `test breakpoint on variable declaration in bubble sort`() {
        val template = """
<%*
// Configuration
let size = 10;

// Generate random array
let arr = [];
for (let i = 0; i < size; i++) {
    arr[i] = Math.floor(Math.random() * 100);
}

let n = arr.length;
let swaps = 0;

// Bubble Sort Algorithm
for (let i = 0; i < n - 1; i++) {
    for (let j = 0; j < n - i - 1; j++) {
        if (arr[j] > arr[j + 1]) {
            let temp = arr[j];
            arr[j] = arr[j + 1];
            arr[j + 1] = temp;
            swaps++;
        }
    }
}

tR += "Swaps: " + swaps;
%>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        var breakpointHits = 0
        val hitLines = mutableListOf<Int>()



        parser.addBreakpoint(3)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                breakpointHits++
                bp.displayLineNumber?.let { hitLines.add(it) }
                println("? Breakpoint hit at line ${bp.displayLineNumber}: ${bp.step.description}")
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "bubble-sort.md"
        )
        val result = parser.parse(template, context)

        assertEquals(1, breakpointHits, "Breakpoint should hit exactly once")
        assertTrue(hitLines.contains(3), "Should hit at line 3")
    }

    @Test
    fun `test breakpoint inside nested loop`() {
        val template = """
<%*
let arr = [5, 2, 8, 1, 9];
let n = arr.length;
let swaps = 0;

for (let i = 0; i < n - 1; i++) {
    for (let j = 0; j < n - i - 1; j++) {
        if (arr[j] > arr[j + 1]) {
            let temp = arr[j];
            arr[j] = arr[j + 1];
            arr[j + 1] = temp;
            swaps++;
        }
    }
}

tR += "Done";
%>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        var breakpointHits = 0



        parser.addBreakpoint(12)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                breakpointHits++
                println("? Hit #$breakpointHits at line ${bp.displayLineNumber}")
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)


        assertTrue(breakpointHits > 0, "Breakpoint should hit at least once during sorting")
        println("Total breakpoint hits: $breakpointHits")
    }

    @Test
    fun `test breakpoint on comment line should resolve to next executable`() {
        val template = """
<%*
let x = 1;
// This is a comment
let y = 2;
let z = 3;
%>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        var hitLine: Int? = null



        parser.addBreakpoint(3)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                hitLine = bp.displayLineNumber
                println("? Breakpoint resolved to line ${bp.displayLineNumber}: ${bp.step.description}")
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)

        assertNotNull(hitLine, "Breakpoint should have hit")
        assertTrue(hitLine!! >= 3, "Should hit at or after the requested line")
    }

    @Test
    fun `test multiple breakpoints in same template`() {
        val template = """
<%*
let counter = 0;

for (let i = 0; i < 3; i++) {
    counter++;
}

let result = counter * 2;
tR += "Result: " + result;
%>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        val hitLines = mutableListOf<Int>()


        parser.addBreakpoint(2)
        parser.addBreakpoint(5)
        parser.addBreakpoint(8)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                bp.displayLineNumber?.let { hitLines.add(it) }
                println("? Breakpoint hit at line ${bp.displayLineNumber}")
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)

        assertTrue(hitLines.contains(2), "Should hit breakpoint at line 2")
        assertTrue(hitLines.contains(5), "Should hit breakpoint at line 5")
        assertTrue(hitLines.contains(8), "Should hit breakpoint at line 8")


        val line5Hits = hitLines.count { it == 5 }
        assertEquals(3, line5Hits, "Breakpoint inside loop should hit 3 times")
    }

    @Test
    fun `test breakpoint on conditional statement`() {
        val template = """
<%*
let numbers = [1, 2, 3, 4, 5];
let evens = 0;
let odds = 0;

for (let num of numbers) {
    if (num % 2 === 0) {
        evens++;
    } else {
        odds++;
    }
}

tR += "Evens: " + evens + ", Odds: " + odds;
%>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        var evenBranchHits = 0
        var oddBranchHits = 0


        parser.addBreakpoint(8)

        parser.addBreakpoint(10)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                when (bp.displayLineNumber) {
                    8 -> evenBranchHits++
                    10 -> oddBranchHits++
                }
                println("? Hit at line ${bp.displayLineNumber}")
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)

        assertEquals(2, evenBranchHits, "Should hit even branch 2 times (for 2 and 4)")
        assertEquals(3, oddBranchHits, "Should hit odd branch 3 times (for 1, 3, and 5)")
    }

    @Test
    fun `test breakpoint with step over functionality`() {
        val template = """
<%*
let a = 1;
let b = 2;
let c = 3;
let d = 4;
tR += "Sum: " + (a + b + c + d);
%>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        val executedLines = mutableListOf<Int>()
        var stepCount = 0


        parser.addBreakpoint(2)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                bp.displayLineNumber?.let { executedLines.add(it) }
                stepCount++
                println("? Step $stepCount at line ${bp.displayLineNumber}: ${bp.step.description}")


                if (stepCount < 4) {
                    DebugAction.STEP_OVER
                } else {
                    DebugAction.CONTINUE
                }
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)

        assertTrue(executedLines.size >= 4, "Should execute at least 4 steps")
        println("Executed lines: $executedLines")
    }

    @Test
    fun `test breakpoint on last line of template`() {
        val template = """
<%*
let x = 1;
let y = 2;
tR += "Result: " + (x + y);
%>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        var lastLineHit = false


        parser.addBreakpoint(4)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                if (bp.displayLineNumber == 4) {
                    lastLineHit = true
                    println("? Hit last line: ${bp.step.description}")
                }
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)

        assertTrue(lastLineHit, "Should hit breakpoint on last line")
    }
}

