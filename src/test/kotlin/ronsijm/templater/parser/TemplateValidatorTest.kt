package ronsijm.templater.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TemplateValidatorTest {
    
    private val validator = TemplateValidator()
    
    @Test
    fun `test valid template has no errors`() {
        val template = """
            Hello <% tp.date.now("YYYY-MM-DD") %>
            <%* let x = 5 %>
            Value: <% x %>
        """.trimIndent()
        
        val errors = validator.validate(template)
        
        assertEquals(0, errors.size, "Valid template should have no errors")
    }
    
    @Test
    fun `test unclosed template tag`() {
        val template = "Hello <% tp.date.now()"
        
        val errors = validator.validate(template)
        
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("Unclosed template tag"))
        assertEquals(1, errors[0].lineNumber)
        assertNotNull(errors[0].suggestion)
    }
    
    @Test
    fun `test unopened closing tag`() {
        val template = "Hello tp.date.now() %>"
        
        val errors = validator.validate(template)
        
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("Closing tag"))
        assertEquals(1, errors[0].lineNumber)
    }
    
    @Test
    fun `test empty template command`() {
        val template = "Hello <% %> World"
        
        val errors = validator.validate(template)
        
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("Empty template command"))
        assertEquals(1, errors[0].lineNumber)
    }
    
    @Test
    fun `test empty execution command`() {
        val template = "Hello <%* %> World"
        
        val errors = validator.validate(template)
        
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("Empty template command"))
    }
    
    @Test
    fun `test typo in date module`() {
        val template = "<% tp.dat.now() %>"
        
        val errors = validator.validate(template)
        
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("tp.dat"))
        assertTrue(errors[0].suggestion?.contains("tp.date") == true)
    }
    
    @Test
    fun `test typo in file module`() {
        val template = "<% tp.fil.name %>"
        
        val errors = validator.validate(template)
        
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("tp.fil"))
        assertTrue(errors[0].suggestion?.contains("tp.file") == true)
    }
    
    @Test
    fun `test typo in frontmatter module`() {
        val template = "<% tp.front.title %>"
        
        val errors = validator.validate(template)
        
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("tp.front"))
        assertTrue(errors[0].suggestion?.contains("tp.frontmatter") == true)
    }
    
    @Test
    fun `test typo in system module`() {
        val template = "<% tp.sys.prompt() %>"
        
        val errors = validator.validate(template)
        
        assertEquals(1, errors.size)
        assertTrue(errors[0].message.contains("tp.sys"))
        assertTrue(errors[0].suggestion?.contains("tp.system") == true)
    }
    
    @Test
    fun `test multiple errors in template`() {
        val template = """
            <% tp.dat.now() %>
            <% tp.fil.name %>
            <% unclosed
        """.trimIndent()
        
        val errors = validator.validate(template)
        
        assertTrue(errors.size >= 2, "Should detect multiple errors")
    }
    
    @Test
    fun `test error line numbers are correct`() {
        val template = """
            Line 1
            Line 2 <% tp.dat.now() %>
            Line 3
            Line 4 <% tp.fil.name %>
        """.trimIndent()
        
        val errors = validator.validate(template)
        
        assertEquals(2, errors.size)
        assertEquals(2, errors[0].lineNumber)
        assertEquals(4, errors[1].lineNumber)
    }
    
    @Test
    fun `test validation error toString format`() {
        val error = ValidationError(
            message = "Test error",
            lineNumber = 5,
            columnNumber = 10,
            suggestion = "Try this instead"
        )
        
        val errorString = error.toString()
        
        assertTrue(errorString.contains("Line 5"))
        assertTrue(errorString.contains("Column 10"))
        assertTrue(errorString.contains("Test error"))
        assertTrue(errorString.contains("Try this instead"))
    }
}

