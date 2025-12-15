package ronsijm.templater.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FrontmatterParserTest {
    
    private val parser = FrontmatterParser()
    
    @Test
    fun `test parse valid frontmatter`() {
        val content = """
            ---
            title: My Document
            author: John Doe
            tags: [test, example]
            ---
            # Content
            This is the content.
        """.trimIndent()
        
        val result = parser.parse(content)
        
        assertTrue(result.hasFrontmatter)
        assertEquals("My Document", result.frontmatter["title"])
        assertEquals("John Doe", result.frontmatter["author"])
        assertTrue(result.content.contains("# Content"))
    }
    
    @Test
    fun `test parse content without frontmatter`() {
        val content = """
            # Document
            This is just content.
        """.trimIndent()
        
        val result = parser.parse(content)
        
        assertFalse(result.hasFrontmatter)
        assertTrue(result.frontmatter.isEmpty())
        assertEquals(content, result.content)
    }
    
    @Test
    fun `test parse frontmatter without closing delimiter`() {
        val content = """
            ---
            title: My Document
            author: John Doe
            # Content without closing ---
        """.trimIndent()
        
        val result = parser.parse(content)
        
        assertFalse(result.hasFrontmatter)
        assertEquals(content, result.content)
    }
    
    @Test
    fun `test parse empty frontmatter`() {
        val content = """
            ---
            ---
            # Content
        """.trimIndent()
        
        val result = parser.parse(content)
        
        assertTrue(result.hasFrontmatter)
        assertTrue(result.frontmatter.isEmpty())
        assertTrue(result.content.contains("# Content"))
    }
    
    @Test
    fun `test parse frontmatter with nested objects`() {
        val content = """
            ---
            metadata:
              author: John Doe
              date: 2024-01-01
            tags: [test]
            ---
            Content
        """.trimIndent()
        
        val result = parser.parse(content)
        
        assertTrue(result.hasFrontmatter)
        assertTrue(result.frontmatter.containsKey("metadata"))
        assertTrue(result.frontmatter.containsKey("tags"))
    }
    
    @Test
    fun `test parse frontmatter with special characters`() {
        val content = """
            ---
            title: "Document: With Colon"
            description: 'Single quotes work too'
            ---
            Content
        """.trimIndent()
        
        val result = parser.parse(content)
        
        assertTrue(result.hasFrontmatter)
        assertTrue(result.frontmatter.containsKey("title"))
        assertTrue(result.frontmatter.containsKey("description"))
    }
    
    @Test
    fun `test getNestedValue with simple path`() {
        val frontmatter = mapOf(
            "title" to "My Document",
            "author" to "John Doe"
        )
        
        val value = parser.getNestedValue(frontmatter, "title")
        
        assertEquals("My Document", value)
    }
    
    @Test
    fun `test getNestedValue with nested path`() {
        val frontmatter = mapOf(
            "metadata" to mapOf(
                "author" to "John Doe",
                "date" to "2024-01-01"
            )
        )
        
        val value = parser.getNestedValue(frontmatter, "metadata.author")
        
        assertEquals("John Doe", value)
    }
    
    @Test
    fun `test getNestedValue with deep nesting`() {
        val frontmatter = mapOf(
            "level1" to mapOf(
                "level2" to mapOf(
                    "level3" to "deep value"
                )
            )
        )
        
        val value = parser.getNestedValue(frontmatter, "level1.level2.level3")
        
        assertEquals("deep value", value)
    }
    
    @Test
    fun `test getNestedValue with non-existent path`() {
        val frontmatter = mapOf(
            "title" to "My Document"
        )
        
        val value = parser.getNestedValue(frontmatter, "nonexistent")
        
        assertNull(value)
    }
    
    @Test
    fun `test getNestedValue with invalid nested path`() {
        val frontmatter = mapOf(
            "title" to "My Document"
        )
        
        val value = parser.getNestedValue(frontmatter, "title.nested")
        
        assertNull(value)
    }
}

