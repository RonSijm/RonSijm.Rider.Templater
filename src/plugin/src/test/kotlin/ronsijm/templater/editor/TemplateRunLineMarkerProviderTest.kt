package ronsijm.templater.editor

import org.junit.Test
import org.junit.Assert.*


class TemplateRunLineMarkerProviderTest {

    @Test
    fun testFindTemplateBlockAt() {

        val documentText = """
            # Test

            <% tp.date.now() %>

            Some text
        """.trimIndent()

        val result = TemplateRunLineMarkerProvider.findTemplateBlockAt(documentText, 10)

        assertNotNull("Should find template block", result)
        assertTrue("Template start should be non-negative", result!!.first >= 0)
        assertTrue("Template end should be after start", result.second > result.first)

        val templateContent = documentText.substring(result.first, result.second)
        assertTrue("Template should contain opening tag", templateContent.contains("<%"))
        assertTrue("Template should contain closing tag", templateContent.contains("%>"))
    }

    @Test
    fun testFindTemplateBlockAtWithMultipleTemplates() {

        val documentText = """
            <% tp.date.now() %>
            Some text
            <% tp.file.title %>
        """.trimIndent()


        val result1 = TemplateRunLineMarkerProvider.findTemplateBlockAt(documentText, 0)
        assertNotNull("Should find first template block", result1)


        val result2 = TemplateRunLineMarkerProvider.findTemplateBlockAt(documentText, result1!!.second + 5)
        assertNotNull("Should find second template block", result2)


        assertNotEquals("Should find different template blocks", result1.first, result2!!.first)
    }

    @Test
    fun testFindTemplateBlockAtNoTemplate() {

        val documentText = """
            # Regular Markdown

            No templates here
        """.trimIndent()

        val result = TemplateRunLineMarkerProvider.findTemplateBlockAt(documentText, 0)

        assertNull("Should not find template block in plain markdown", result)
    }

    @Test
    fun testFindTemplateBlockAtWithExecutionSyntax() {

        val documentText = """
            <%* tp.system.prompt("test") %>
        """.trimIndent()

        val result = TemplateRunLineMarkerProvider.findTemplateBlockAt(documentText, 0)

        assertNotNull("Should find execution template block", result)
        val templateContent = documentText.substring(result!!.first, result.second)
        assertTrue("Template should contain execution opening tag", templateContent.contains("<%*"))
    }

    @Test
    fun testFindTemplateBlockAtWithWhitespaceControl() {

        val documentText = """
            <%- tp.date.now() -%>
        """.trimIndent()

        val result = TemplateRunLineMarkerProvider.findTemplateBlockAt(documentText, 0)

        assertNotNull("Should find whitespace control template block", result)
        val templateContent = documentText.substring(result!!.first, result.second)
        assertTrue("Template should contain whitespace control opening tag", templateContent.contains("<%-"))
    }
}

