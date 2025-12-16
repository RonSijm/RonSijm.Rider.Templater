package ronsijm.templater.handlers.system

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.services.MockSystemOperationsService
import ronsijm.templater.services.ServiceContainer

class SuggesterHandlerTest : SystemHandlerTestBase() {

    private fun createCommand() = getCommand("suggester")

    @Test
    fun `test with service`() {
        val service = MockSystemOperationsService(suggesterResponse = "Option 1")
        val services = ServiceContainer(systemOperationsService = service)
        val context = createContext(services)

        val command = createCommand()
        val result = command.execute(listOf(listOf("Option 1", "Option 2")), context)

        assertResultEquals("Option 1", result)
        assertEquals(1, service.suggesterCalls.size)
    }

    @Test
    fun `test with separate values`() {
        val service = MockSystemOperationsService(suggesterResponse = "value2")
        val services = ServiceContainer(systemOperationsService = service)
        val context = createContext(services)

        val command = createCommand()
        val result = command.execute(
            listOf(
                listOf("Display 1", "Display 2"),
                listOf("value1", "value2")
            ),
            context
        )

        assertResultEquals("value2", result)
        assertEquals(1, service.suggesterCalls.size)
        assertEquals(listOf("Display 1", "Display 2"), service.suggesterCalls[0].textItems)
        assertEquals(listOf("value1", "value2"), service.suggesterCalls[0].values)
    }
}

