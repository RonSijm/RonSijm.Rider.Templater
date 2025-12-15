package ronsijm.templater.handlers.system

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.MockSystemOperationsService

class MultiSuggesterHandlerTest {
    
    @Test
    fun `test handler with service returns list as string`() {
        val handler = MultiSuggesterHandler()
        
        val mockService = MockSystemOperationsService(multiSuggesterResponse = listOf("v1", "v2"))
        val services = ServiceContainer.createForTesting(systemOperationsService = mockService)
        val context = TestContextFactory.create(services = services)
        
        val request = MultiSuggesterRequest(
            textItems = listOf("Opt 1", "Opt 2"),
            items = listOf("v1", "v2"),
            throwOnCancel = false,
            placeholder = "",
            limit = null
        )
        
        val result = handler.handle(request, context)
        
        assertEquals("[v1, v2]", result)
        assertEquals(1, mockService.multiSuggesterCalls.size)
    }
}

