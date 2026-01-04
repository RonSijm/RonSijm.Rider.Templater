package ronsijm.templater.handlers.system

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.ResultAssertions.assertResultEquals
import ronsijm.templater.ResultAssertions.assertCancelled
import ronsijm.templater.common.CancelledResult
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.services.mock.MockSystemOperationsService
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

    @Test
    fun `test cancellation returns CancelledResult`() {

        val service = MockSystemOperationsService(suggesterResponse = null)
        val services = ServiceContainer(systemOperationsService = service)
        val context = createContext(services)

        val command = createCommand()
        val result = command.execute(listOf(listOf("Option 1", "Option 2")), context)

        assertCancelled(result)
    }

    @Test
    fun `test multiple suggesters in sequence`() {

        val service = MockSystemOperationsService(
            suggesterResponses = listOf("note", "+", "")
        )
        val services = ServiceContainer(systemOperationsService = service)
        val context = createContext(services)

        val command = createCommand()


        val result1 = command.execute(
            listOf(
                listOf("Note", "Warning", "Info"),
                listOf("note", "warning", "info")
            ),
            context
        )
        assertResultEquals("note", result1)


        val result2 = command.execute(
            listOf(
                listOf("Not Foldable", "Default Expanded", "Default Collapsed"),
                listOf("", "+", "-")
            ),
            context
        )
        assertResultEquals("+", result2)


        val result3 = command.execute(
            listOf(
                listOf("Option A", "Option B"),
                listOf("a", "")
            ),
            context
        )
        assertResultEquals("", result3)

        assertEquals(3, service.suggesterCalls.size)
    }

    @Test
    fun `test suggester with placeholder and throwOnCancel`() {
        val service = MockSystemOperationsService(suggesterResponse = "selected")
        val services = ServiceContainer(systemOperationsService = service)
        val context = createContext(services)

        val command = createCommand()
        val result = command.execute(
            listOf(
                listOf("A", "B"),
                listOf("a", "b"),
                false,
                "Choose an option",
                10
            ),
            context
        )

        assertResultEquals("selected", result)
        assertEquals("Choose an option", service.suggesterCalls[0].placeholder)
        assertEquals(10, service.suggesterCalls[0].limit)
        assertEquals(false, service.suggesterCalls[0].throwOnCancel)
    }

    @Test
    fun `test suggester returns actual value not string representation`() {

        val service = MockSystemOperationsService(suggesterResponse = "abstract")
        val services = ServiceContainer(systemOperationsService = service)
        val context = createContext(services)

        val command = createCommand()
        val result = command.execute(
            listOf(
                listOf("Abstract", "Note"),
                listOf("abstract", "note")
            ),
            context
        )

        assertTrue(result is OkValueResult<*>)
        assertEquals("abstract", (result as OkValueResult<*>).value)
    }
}
