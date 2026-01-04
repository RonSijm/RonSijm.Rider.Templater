package ronsijm.templater.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.common.TemplateSyntax

class TextUtilsTest {

    @Test
    fun `removeHtmlComments should remove single line comment`() {
        val input = "Before <!-- comment --> After"
        val result = TextUtils.removeHtmlComments(input)


        assertFalse(result.contains("comment"))
        assertTrue(result.contains("Before"))
        assertTrue(result.contains("After"))


        assertEquals(input.length, result.length)
    }

    @Test
    fun `removeHtmlComments should remove multi-line comment`() {
        val input = """
            Before
            <!--
            comment line 1
            comment line 2
            -->
            After
        """.trimIndent()

        val result = TextUtils.removeHtmlComments(input)


        assertFalse(result.contains("comment"))
        assertTrue(result.contains("Before"))
        assertTrue(result.contains("After"))


        assertEquals(input.count { it == '\n' }, result.count { it == '\n' })
    }

    @Test
    fun `removeHtmlComments should remove multiple comments`() {
        val input = "A <!-- c1 --> B <!-- c2 --> C"
        val result = TextUtils.removeHtmlComments(input)

        assertFalse(result.contains("c1"))
        assertFalse(result.contains("c2"))
        assertTrue(result.contains("A"))
        assertTrue(result.contains("B"))
        assertTrue(result.contains("C"))
    }

    @Test
    fun `removeHtmlComments should handle unclosed comment`() {
        val input = "Before <!-- unclosed comment"
        val result = TextUtils.removeHtmlComments(input)

        assertTrue(result.contains("Before"))
        assertFalse(result.contains("unclosed"))
        assertFalse(result.contains("comment"))
    }

    @Test
    fun `removeHtmlComments should handle nested comment markers`() {
        val input = "<!-- outer <!-- inner --> still in comment -->"
        val result = TextUtils.removeHtmlComments(input)



        assertTrue(result.contains("still in comment -->"))
    }

    @Test
    fun `removeHtmlComments should preserve line numbers`() {
        val input = """
            Line 1
            <!-- comment
            on multiple
            lines -->
            Line 5
        """.trimIndent()

        val result = TextUtils.removeHtmlComments(input)


        val inputLines = input.lines()
        val resultLines = result.lines()

        assertEquals(inputLines.size, resultLines.size)
        assertEquals("Line 1", resultLines[0])
        assertEquals("Line 5", resultLines[4])
    }

    @Test
    fun `removeHtmlComments should handle template blocks in comments`() {
        val input = """
            <%* let x = 1 %>
            <!--
            <%* let y = 2 %>
            <% y %>
            -->
            <% x %>
        """.trimIndent()

        val result = TextUtils.removeHtmlComments(input)


        assertTrue(result.contains("<%* let x = 1 %>"))
        assertTrue(result.contains("<% x %>"))


        assertFalse(result.contains("let y"))


        val matches = TemplateSyntax.TEMPLATE_BLOCK_REGEX.findAll(result).toList()

        assertEquals(2, matches.size, "Should find exactly 2 template blocks (not the commented ones)")
    }

    @Test
    fun `removeHtmlComments should handle empty comment`() {
        val input = "Before <!----> After"
        val result = TextUtils.removeHtmlComments(input)

        assertTrue(result.contains("Before"))
        assertTrue(result.contains("After"))
    }

    @Test
    fun `removeHtmlComments should handle no comments`() {
        val input = "No comments here"
        val result = TextUtils.removeHtmlComments(input)

        assertEquals(input, result)
    }

    @Test
    fun `removeHtmlComments should handle comment at start`() {
        val input = "<!-- comment -->After"
        val result = TextUtils.removeHtmlComments(input)

        assertFalse(result.contains("comment"))
        assertTrue(result.contains("After"))
    }

    @Test
    fun `removeHtmlComments should handle comment at end`() {
        val input = "Before<!-- comment -->"
        val result = TextUtils.removeHtmlComments(input)

        assertTrue(result.contains("Before"))
        assertFalse(result.contains("comment"))
    }
}

