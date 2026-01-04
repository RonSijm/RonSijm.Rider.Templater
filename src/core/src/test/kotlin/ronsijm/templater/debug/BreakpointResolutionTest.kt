package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.FrontmatterParser

class BreakpointResolutionTest {

    @Test
    fun `test breakpoint resolution for nested loop`() {
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
        parser.startDebugSession(
            onBreakpoint = { bp ->
                breakpointHits++
                println("? Breakpoint hit #$breakpointHits at line ${bp.displayLineNumber}: ${bp.step.description}")
                DebugAction.CONTINUE
            }
        )


        println("Adding breakpoint at line 12 (before parsing)...")
        val nodeId = parser.addBreakpoint(12)
        println("  Breakpoint added, nodeId: $nodeId")
        println("  (null is expected because AST doesn't exist yet)")


        val astBefore = parser.getTemplateAST()
        println("\nAST before parsing: ${if (astBefore == null) "null" else "exists"}")


        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md"
        )

        println("\nParsing template...")
        parser.parse(template, context)


        val astAfter = parser.getTemplateAST()
        println("\nAST after parsing: ${if (astAfter == null) "null" else "exists with ${astAfter.allStatements.size} statements"}")
        astAfter?.allStatements?.filter { it.lineNumber != null }?.forEach {
            println("  Line ${it.lineNumber}: ${it.type} - ${it.code.take(40)}")
        }


        val session = parser.getDebugSession()
        val breakpoints = session?.getBreakpoints()
        println("\nBreakpoints in session after parsing: ${breakpoints?.size}")
        breakpoints?.forEach { node ->
            println("  Breakpoint on: Line ${node.lineNumber}, code: ${node.code.take(40)}")
        }

        println("\nTotal breakpoint hits: $breakpointHits")
        assertTrue(breakpointHits > 0, "Breakpoint should hit at least once")
    }
}

