package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.script.ScriptParser

class ControlFlowBuilderHtmlCommentTest {

    private val scriptParser = ScriptParser()
    private val builder = ControlFlowBuilder(scriptParser)

    @Test
    fun `should exclude template blocks inside HTML comments`() {
        val template = """
            <%* let x = 1 %>
            <!--
            <%* let y = 2 %>
            -->
            <% x %>
        """.trimIndent()

        val graph = builder.buildFromTemplate(template)



        assertEquals(4, graph.nodes.size, "Should have Start, let x=1, x, End nodes only")


        val nodeLabels = graph.nodes.map { it.label }
        assertTrue(nodeLabels.any { it.contains("let x = 1") })
        assertFalse(nodeLabels.any { it.contains("let y = 2") }, "Commented block should not appear in graph")
    }

    @Test
    fun `should handle multiple HTML comments`() {
        val template = """
            <%* let a = 1 %>
            <!-- <%* let b = 2 %> -->
            <%* let c = 3 %>
            <!-- <%* let d = 4 %> -->
            <% a + c %>
        """.trimIndent()

        val graph = builder.buildFromTemplate(template)

        val nodeLabels = graph.nodes.map { it.label }


        assertTrue(nodeLabels.any { it.contains("let a = 1") })
        assertTrue(nodeLabels.any { it.contains("let c = 3") })
        assertFalse(nodeLabels.any { it.contains("let b = 2") })
        assertFalse(nodeLabels.any { it.contains("let d = 4") })
    }

    @Test
    fun `should handle multi-line HTML comments`() {
        val template = """
            <%* let x = 5 %>
            <!--
            This is a comment
            <%* let y = x * 2 %>
            <%* let z = y + x %>
            More comment text
            -->
            <% x %>
        """.trimIndent()

        val graph = builder.buildFromTemplate(template)

        val nodeLabels = graph.nodes.map { it.label }

        assertTrue(nodeLabels.any { it.contains("let x = 5") })
        assertFalse(nodeLabels.any { it.contains("let y") })
        assertFalse(nodeLabels.any { it.contains("let z") })
    }

    @Test
    fun `should not affect parallel execution detection`() {
        val template = """
            <%* let a = 1 %>
            <%* let b = 2 %>
            <!--
            <%* let c = 3 %>
            -->
            <%* let d = a + b %>
        """.trimIndent()

        val graph = builder.buildFromTemplate(template)


        val parallelGroups = graph.parallelGroupExplanations


        assertTrue(parallelGroups.isNotEmpty(), "Should detect parallel execution")


        val firstGroup = parallelGroups.first()
        assertEquals(2, firstGroup.blockCount, "Should have 2 blocks in parallel group (not including commented block)")


        val nodeLabels = graph.nodes.map { it.label }
        assertFalse(nodeLabels.any { it.contains("let c") })
    }

    @Test
    fun `should handle unclosed HTML comment`() {
        val template = """
            <%* let x = 1 %>
            <!-- unclosed comment
            <%* let y = 2 %>
            <% y %>
        """.trimIndent()

        val graph = builder.buildFromTemplate(template)

        val nodeLabels = graph.nodes.map { it.label }


        assertTrue(nodeLabels.any { it.contains("let x = 1") })
        assertFalse(nodeLabels.any { it.contains("let y = 2") })
    }

    @Test
    fun `should handle template with no comments`() {
        val template = """
            <%* let x = 1 %>
            <%* let y = 2 %>
            <% x + y %>
        """.trimIndent()

        val graph = builder.buildFromTemplate(template)


        val nodeLabels = graph.nodes.map { it.label }
        assertTrue(nodeLabels.any { it.contains("let x = 1") })
        assertTrue(nodeLabels.any { it.contains("let y = 2") })
        assertTrue(nodeLabels.any { it.contains("x + y") })
    }

    @Test
    fun `should preserve line numbers after comment removal`() {
        val template = """
            Line 1: <%* let x = 1 %>
            Line 2: <!-- comment -->
            Line 3: <%* let y = 2 %>
        """.trimIndent()

        val graph = builder.buildFromTemplate(template)


        val xNode = graph.nodes.find { it.label.contains("let x = 1") }
        val yNode = graph.nodes.find { it.label.contains("let y = 2") }

        assertNotNull(xNode)
        assertNotNull(yNode)


        assertEquals(1, xNode!!.lineNumber)
        assertEquals(3, yNode!!.lineNumber)
    }
}

