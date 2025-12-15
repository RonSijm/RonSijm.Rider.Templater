package ronsijm.templater.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TemplateExceptionTest {
    
    @Test
    fun `test TemplateValidationException with line number`() {
        val exception = TemplateValidationException(
            message = "Invalid syntax",
            lineNumber = 5
        )
        
        val message = exception.message
        assertNotNull(message)
        assertTrue(message!!.contains("Line 5"))
        assertTrue(message.contains("Invalid syntax"))
    }
    
    @Test
    fun `test TemplateValidationException with line and column`() {
        val exception = TemplateValidationException(
            message = "Invalid syntax",
            lineNumber = 5,
            columnNumber = 10
        )
        
        val message = exception.message
        assertNotNull(message)
        assertTrue(message!!.contains("Line 5"))
        assertTrue(message.contains("Column 10"))
        assertTrue(message.contains("Invalid syntax"))
    }
    
    @Test
    fun `test TemplateValidationException with suggestion`() {
        val exception = TemplateValidationException(
            message = "Invalid syntax",
            lineNumber = 5,
            suggestion = "Try using tp.date.now() instead"
        )
        
        val message = exception.message
        assertNotNull(message)
        assertTrue(message!!.contains("Suggestion"))
        assertTrue(message.contains("Try using tp.date.now() instead"))
    }
    
    @Test
    fun `test TemplateValidationException without location`() {
        val exception = TemplateValidationException(
            message = "Invalid syntax"
        )
        
        val message = exception.message
        assertNotNull(message)
        assertEquals("Invalid syntax", message)
    }
    
    @Test
    fun `test TemplateValidationException with cause`() {
        val cause = RuntimeException("Original error")
        val exception = TemplateValidationException(
            message = "Invalid syntax",
            lineNumber = 5,
            cause = cause
        )
        
        assertEquals(cause, exception.cause)
    }
    
    @Test
    fun `test TemplateExecutionException with command`() {
        val exception = TemplateExecutionException(
            message = "Function not found",
            command = "tp.date.invalid()"
        )
        
        val message = exception.message
        assertNotNull(message)
        assertTrue(message!!.contains("Error executing command"))
        assertTrue(message.contains("tp.date.invalid()"))
        assertTrue(message.contains("Function not found"))
    }
    
    @Test
    fun `test TemplateExecutionException with suggestion`() {
        val exception = TemplateExecutionException(
            message = "Unknown module",
            command = "tp.dat.now()",
            suggestion = "Did you mean 'tp.date'?"
        )
        
        val message = exception.message
        assertNotNull(message)
        assertTrue(message!!.contains("Suggestion"))
        assertTrue(message.contains("Did you mean 'tp.date'?"))
    }
    
    @Test
    fun `test TemplateExecutionException with cause`() {
        val cause = IllegalArgumentException("Invalid argument")
        val exception = TemplateExecutionException(
            message = "Execution failed",
            command = "tp.date.now()",
            cause = cause
        )
        
        assertEquals(cause, exception.cause)
    }
    
    @Test
    fun `test TemplateExecutionException command property`() {
        val exception = TemplateExecutionException(
            message = "Error",
            command = "tp.test.command()"
        )
        
        assertEquals("tp.test.command()", exception.command)
    }
    
    @Test
    fun `test TemplateExecutionException suggestion property`() {
        val exception = TemplateExecutionException(
            message = "Error",
            command = "tp.test.command()",
            suggestion = "Use this instead"
        )
        
        assertEquals("Use this instead", exception.suggestion)
    }
    
    @Test
    fun `test TemplateValidationException properties`() {
        val exception = TemplateValidationException(
            message = "Error",
            lineNumber = 10,
            columnNumber = 20,
            suggestion = "Fix this"
        )
        
        assertEquals(10, exception.lineNumber)
        assertEquals(20, exception.columnNumber)
        assertEquals("Fix this", exception.suggestion)
    }
}

