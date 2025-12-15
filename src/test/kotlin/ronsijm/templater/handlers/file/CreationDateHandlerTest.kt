package ronsijm.templater.handlers.file

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.services.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer
import java.time.LocalDateTime
import java.time.ZoneId

class CreationDateHandlerTest {
    
    @Test
    fun `test handler with service returns exact date`() {
        val handler = CreationDateHandler()
        
        // Create a specific timestamp: 2025-01-15 14:30:00
        val testDate = LocalDateTime.of(2025, 1, 15, 14, 30, 0)
        val timestamp = testDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val mockFileService = MockFileOperationsService(
            currentFilePath = "test.md"
        )
        mockFileService.setCreationDate("test.md", timestamp)
        
        val services = ServiceContainer(
            fileOperationService = mockFileService
        )
        
        val context = TestContextFactory.create().copy(services = services)
        
        // Type-safe request object!
        val request = CreationDateRequest(
            format = "yyyy-MM-dd HH:mm",
            path = null
        )
        
        val result = handler.handle(request, context)
        
        assertEquals("2025-01-15 14:30", result)
    }
    
    @Test
    fun `test handler with custom format`() {
        val handler = CreationDateHandler()
        
        val testDate = LocalDateTime.of(2025, 1, 15, 14, 30, 0)
        val timestamp = testDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val mockFileService = MockFileOperationsService(
            currentFilePath = "test.md"
        )
        mockFileService.setCreationDate("test.md", timestamp)
        
        val services = ServiceContainer(
            fileOperationService = mockFileService
        )
        
        val context = TestContextFactory.create().copy(services = services)
        
        // Easy to create different requests for different test cases
        val request = CreationDateRequest(
            format = "yyyy-MM-dd",
            path = null
        )
        
        val result = handler.handle(request, context)
        
        assertEquals("2025-01-15", result)
    }
    
    @Test
    fun `test handler with specific file path`() {
        val handler = CreationDateHandler()
        
        val testDate = LocalDateTime.of(2025, 2, 10, 10, 15, 0)
        val timestamp = testDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val mockFileService = MockFileOperationsService()
        mockFileService.setCreationDate("other-file.md", timestamp)
        
        val services = ServiceContainer(
            fileOperationService = mockFileService
        )
        
        val context = TestContextFactory.create().copy(services = services)
        
        // Type-safe path parameter
        val request = CreationDateRequest(
            format = "yyyy-MM-dd HH:mm",
            path = "other-file.md"
        )
        
        val result = handler.handle(request, context)
        
        assertEquals("2025-02-10 10:15", result)
    }
    
    @Test
    fun `test handler without service falls back to current date`() {
        val handler = CreationDateHandler()
        val context = TestContextFactory.create() // No services
        
        val request = CreationDateRequest(
            format = "yyyy-MM-dd HH:mm",
            path = null
        )
        
        val result = handler.handle(request, context)
        
        // Should return a date string (we can't test exact value as it's current time)
        assertTrue(result.isNotEmpty())
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }
    
    @Test
    fun `test parser creates correct request`() {
        val parser = CreationDateRequestParser()
        
        val args = listOf("yyyy-MM-dd", "test.md")
        val request = parser.parse(args)
        
        assertEquals("yyyy-MM-dd", request.format)
        assertEquals("test.md", request.path)
    }
    
    @Test
    fun `test parser with default values`() {
        val parser = CreationDateRequestParser()
        
        val args = emptyList<Any?>()
        val request = parser.parse(args)
        
        assertEquals("yyyy-MM-dd HH:mm", request.format)
        assertNull(request.path)
    }
}



