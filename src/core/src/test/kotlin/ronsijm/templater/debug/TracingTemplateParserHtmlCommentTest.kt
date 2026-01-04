package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer

class TracingTemplateParserHtmlCommentTest {

    private val frontmatterParser = FrontmatterParser()

    @Test
    fun `should exclude commented blocks from control flow graph`() {
        val template = """
            <%* let x = 1 %>
            <!--
            <%* let y = 2 %>
            -->
            <% x %>
        """.trimIndent()

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = frontmatterParser,
            fileName = "test.md",
            filePath = "/test.md",
            fileContent = template,
            services = ServiceContainer()
        )

        val parser = TracingTemplateParser(validateSyntax = false)
        parser.parse(template, context)

        val graph = parser.getControlFlowGraph()
        assertNotNull(graph)

        val nodeLabels = graph!!.nodes.map { it.label }


        assertTrue(nodeLabels.any { it.contains("let x = 1") })
        assertFalse(nodeLabels.any { it.contains("let y = 2") })
    }

    @Test
    fun `should generate correct Mermaid diagram without commented blocks`() {
        val template = """
            <%* let a = 1 %>
            <%* let b = 2 %>
            <!--
            <%* let c = 3 %>
            <%* let d = 4 %>
            -->
            <% a + b %>
        """.trimIndent()

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = frontmatterParser,
            fileName = "test.md",
            filePath = "/test.md",
            fileContent = template,
            services = ServiceContainer()
        )

        val parser = TracingTemplateParser(validateSyntax = false)
        parser.parse(template, context)

        val mermaid = parser.exportMermaidFlowchart("Test")


        assertFalse(mermaid.contains("let c"))
        assertFalse(mermaid.contains("let d"))


        assertTrue(mermaid.contains("let a"))
        assertTrue(mermaid.contains("let b"))
    }

    @Test
    fun `should handle parallel execution test case correctly`() {

        val template = """
            <%* let first = await tp.system.prompt("Enter FIRST value") %>
            <!--
            <%* let second = await tp.system.prompt("Enter SECOND value") %>

            You entered: <% first %> then <% second %>
            -->
        """.trimIndent()

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = frontmatterParser,
            fileName = "test.md",
            filePath = "/test.md",
            fileContent = template,
            services = ServiceContainer()
        )

        val parser = TracingTemplateParser(validateSyntax = false)
        parser.parse(template, context)

        val graph = parser.getControlFlowGraph()
        assertNotNull(graph)

        val nodeLabels = graph!!.nodes.map { it.label }


        assertTrue(nodeLabels.any { it.contains("first") || it.contains("FIRST") },
            "Should find first variable or FIRST in labels: $nodeLabels")
        assertFalse(nodeLabels.any { it.contains("SECOND") },
            "Should not find SECOND in labels: $nodeLabels")


        val interpolationNodes = graph.nodes.filter { it.type == FlowNode.NodeType.INTERPOLATION }

        assertTrue(interpolationNodes.none { it.label.contains("second") },
            "Should not have 'second' interpolation from comment")
    }

    @Test
    fun `should handle callout template with comments`() {
        val template = """
            <%* let calloutType = await tp.system.suggester(["Note"], "Type") %>
            <%* let title = await tp.system.prompt("Title") %>
            <!--
            <%* let extra = await tp.system.prompt("Extra") %>
            -->
            <%* let content = '> [!' + calloutType + '] ' + title %>
            <%* tR+=content %>
        """.trimIndent()

        val context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = frontmatterParser,
            fileName = "test.md",
            filePath = "/test.md",
            fileContent = template,
            services = ServiceContainer()
        )

        val parser = TracingTemplateParser(validateSyntax = false)
        parser.parse(template, context)

        val graph = parser.getControlFlowGraph()
        assertNotNull(graph)


        val parallelGroups = graph!!.parallelGroupExplanations

        if (parallelGroups.isNotEmpty()) {
            val firstGroup = parallelGroups.first()

            assertEquals(2, firstGroup.blockCount, "Should not include commented 'extra' prompt in parallel group")
        }
    }
}

