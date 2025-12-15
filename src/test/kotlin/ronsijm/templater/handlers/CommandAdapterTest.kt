package ronsijm.templater.handlers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.services.MockFileOperationsService
import ronsijm.templater.services.ServiceContainer
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Test that CommandAdapter correctly bridges Handler pattern to Command pattern
 */
class CommandAdapterTest {

    private fun getCommand(module: String, name: String): Command {
        return HandlerRegistry.commandsByModule[module]?.get(name)
            ?: throw IllegalArgumentException("Command $module.$name not found")
    }

    @Test
    fun `test adapter with TodayHandler`() {
        val command = getCommand("date", "today")
        val context = TestContextFactory.create()

        // Call using old Command interface with List<Any?>
        val result = command.execute(listOf("yyyy-MM-dd"), context)

        assertNotNull(result)
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test adapter with NowHandler`() {
        val command = getCommand("date", "now")
        val context = TestContextFactory.create()

        val result = command.execute(listOf("yyyy-MM-dd HH:mm"), context)

        assertNotNull(result)
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }

    @Test
    fun `test adapter with CreationDateHandler`() {
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

        val command = getCommand("file", "creation_date")

        // Call using old Command interface
        val result = command.execute(listOf("yyyy-MM-dd HH:mm"), context)

        assertEquals("2025-01-15 14:30", result)
    }

    @Test
    fun `test adapter metadata conversion`() {
        val command = getCommand("date", "today")

        assertEquals("today", command.metadata.name)
        assertEquals("Returns today's date", command.metadata.description)
        assertEquals("today(\"YYYY-MM-DD\")", command.metadata.example)
        assertEquals("format?: string", command.metadata.parameters)
    }

    @Test
    fun `test adapter with empty args uses defaults`() {
        val command = getCommand("date", "today")
        val context = TestContextFactory.create()

        // Call with no arguments - should use defaults
        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test adapter with WeekdayHandler`() {
        val command = getCommand("date", "weekday")
        val context = TestContextFactory.create()

        val result = command.execute(listOf("yyyy-MM-dd", 0), context)

        assertNotNull(result)
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }
}

