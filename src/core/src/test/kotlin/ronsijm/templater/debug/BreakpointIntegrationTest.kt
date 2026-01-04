package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.FrontmatterParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue


class BreakpointIntegrationTest {

    @Test
    fun `breakpoint set before parsing should trigger during execution`() {
        val template = """
<%*
let counter = 0;

for (let i = 0; i < 5; i++) {
    counter++;
}

tR += "Counter: " + counter;
%>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        var breakpointHit = false
        var hitLineNumber: Int? = null
        val allSteps = mutableListOf<String>()


        val nodeId = parser.addBreakpoint(5)
        println("Set breakpoint at line 5, resolved to node: $nodeId")


        parser.startDebugSession(
            onBreakpoint = { bp ->
                breakpointHit = true
                hitLineNumber = bp.displayLineNumber
                println(">>> BREAKPOINT HIT at line ${bp.displayLineNumber}: ${bp.step.description}")
                DebugAction.CONTINUE
            }
        )


        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        val result = parser.parse(template, context)

        println("\nAll execution steps:")
        result.trace.allSteps.forEach { step ->
            println("  Line ${step.displayLineNumber}: ${step.type} - ${step.description}")
            allSteps.add("Line ${step.displayLineNumber}: ${step.type}")
        }


        assertTrue(breakpointHit, "Breakpoint should have been hit. Steps: $allSteps")
        assertNotNull(hitLineNumber, "Hit line number should be recorded")
    }

    @Test
    fun `breakpoint in loop should trigger on first iteration`() {
        val template = """
            <%*
            let sum = 0;
            for (let i = 0; i < 5; i++) {
                sum += i;
            }
            %>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        var breakpointHitCount = 0


        parser.addBreakpoint(4)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                breakpointHitCount++
                println("Breakpoint hit #$breakpointHitCount at line ${bp.displayLineNumber}")
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)


        assertTrue(breakpointHitCount >= 1, "Breakpoint should hit at least once in the loop")
        println("Total breakpoint hits: $breakpointHitCount")
    }

    @Test
    fun `multiple breakpoints should all trigger`() {
        val template = """
            <%*
            let a = 1;
            let b = 2;
            let c = 3;
            let sum = a + b + c;
            %>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        val hitLines = mutableListOf<Int>()


        parser.addBreakpoint(2)
        parser.addBreakpoint(3)
        parser.addBreakpoint(4)
        parser.addBreakpoint(5)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                bp.displayLineNumber?.let { hitLines.add(it) }
                println("Breakpoint hit at line ${bp.displayLineNumber}")
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)


        assertTrue(hitLines.contains(2), "Breakpoint at line 2 should hit")
        assertTrue(hitLines.contains(3), "Breakpoint at line 3 should hit")
        assertTrue(hitLines.contains(4), "Breakpoint at line 4 should hit")
        assertTrue(hitLines.contains(5), "Breakpoint at line 5 should hit")
        assertEquals(4, hitLines.size, "Should hit exactly 4 breakpoints")
    }

    @Test
    fun `breakpoint on non-executable line should resolve to next executable line`() {
        val template = """
            <%*
            // This is a comment on line 2
            let x = 10;
            %>
        """.trimIndent()

        val parser = DebuggingTemplateParser()
        var hitLineNumber: Int? = null


        parser.addBreakpoint(2)

        parser.startDebugSession(
            onBreakpoint = { bp ->
                hitLineNumber = bp.displayLineNumber
                println("Breakpoint hit at line ${bp.displayLineNumber}")
                DebugAction.CONTINUE
            }
        )

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )
        parser.parse(template, context)


        assertNotNull(hitLineNumber, "Breakpoint should have been hit")
        assertEquals(3, hitLineNumber, "Breakpoint should resolve to line 3 (next executable line)")
    }
}

