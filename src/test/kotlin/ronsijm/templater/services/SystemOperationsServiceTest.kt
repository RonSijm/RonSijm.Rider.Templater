package ronsijm.templater.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SystemOperationsServiceTest {
    
    @Test
    fun `test MockSystemOperationsService prompt returns configured response`() {
        val service = MockSystemOperationsService(promptResponse = "test response")
        
        val result = service.prompt("Enter name", "default", false, false)
        
        assertEquals("test response", result)
        assertEquals(1, service.promptCalls.size)
        assertEquals("Enter name", service.promptCalls[0].promptText)
        assertEquals("default", service.promptCalls[0].defaultValue)
        assertFalse(service.promptCalls[0].multiLine)
        assertFalse(service.promptCalls[0].password)
    }
    
    @Test
    fun `test MockSystemOperationsService prompt with null response`() {
        val service = MockSystemOperationsService(promptResponse = null)
        
        val result = service.prompt("Enter name")
        
        assertNull(result)
        assertEquals(1, service.promptCalls.size)
    }
    
    @Test
    fun `test MockSystemOperationsService suggester returns configured response`() {
        val service = MockSystemOperationsService(suggesterResponse = "option2")
        
        val result = service.suggester(
            listOf("Option 1", "Option 2"),
            listOf("option1", "option2"),
            false,
            "Select one",
            null
        )
        
        assertEquals("option2", result)
        assertEquals(1, service.suggesterCalls.size)
        assertEquals(listOf("Option 1", "Option 2"), service.suggesterCalls[0].textItems)
        assertEquals(listOf("option1", "option2"), service.suggesterCalls[0].values)
        assertFalse(service.suggesterCalls[0].throwOnCancel)
        assertEquals("Select one", service.suggesterCalls[0].placeholder)
        assertNull(service.suggesterCalls[0].limit)
    }
    
    @Test
    fun `test MockSystemOperationsService multiSuggester returns configured response`() {
        val service = MockSystemOperationsService(
            multiSuggesterResponse = listOf("item1", "item3")
        )
        
        val result = service.multiSuggester(
            listOf("Item 1", "Item 2", "Item 3"),
            listOf("item1", "item2", "item3"),
            false,
            "Select multiple",
            5
        )
        
        assertEquals(listOf("item1", "item3"), result)
        assertEquals(1, service.multiSuggesterCalls.size)
        assertEquals(listOf("Item 1", "Item 2", "Item 3"), service.multiSuggesterCalls[0].textItems)
        assertEquals(listOf("item1", "item2", "item3"), service.multiSuggesterCalls[0].values)
        assertFalse(service.multiSuggesterCalls[0].throwOnCancel)
        assertEquals("Select multiple", service.multiSuggesterCalls[0].placeholder)
        assertEquals(5, service.multiSuggesterCalls[0].limit)
    }
    
    @Test
    fun `test MockSystemOperationsService tracks multiple calls`() {
        val service = MockSystemOperationsService()
        
        service.prompt("First prompt")
        service.prompt("Second prompt")
        service.suggester(listOf("A"), listOf("a"))
        
        assertEquals(2, service.promptCalls.size)
        assertEquals("First prompt", service.promptCalls[0].promptText)
        assertEquals("Second prompt", service.promptCalls[1].promptText)
        assertEquals(1, service.suggesterCalls.size)
    }
    
    @Test
    fun `test MockSystemOperationsService with custom responses`() {
        val service = MockSystemOperationsService(
            promptResponse = "custom prompt",
            suggesterResponse = 42,
            multiSuggesterResponse = listOf(1, 2, 3)
        )
        
        assertEquals("custom prompt", service.prompt("test"))
        assertEquals(42, service.suggester(listOf("a"), listOf(1)))
        assertEquals(listOf(1, 2, 3), service.multiSuggester(listOf("a"), listOf(1)))
    }
    
    @Test
    fun `test MockSystemOperationsService default responses`() {
        val service = MockSystemOperationsService()
        
        assertEquals("mock prompt response", service.prompt("test"))
        assertEquals("mock suggester response", service.suggester(listOf("a"), listOf("b")))
        assertEquals(listOf("mock", "multi", "suggester"), service.multiSuggester(listOf("a"), listOf("b")))
    }
}

