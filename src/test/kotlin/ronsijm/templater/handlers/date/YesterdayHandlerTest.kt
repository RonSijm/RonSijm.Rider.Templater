package ronsijm.templater.handlers.date

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class YesterdayHandlerTest : DateHandlerTestBase() {

    private fun createCommand() = getCommand("yesterday")

    @Test
    fun `test with default format`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context).toString()

        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        val expected = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, result)
    }

    @Test
    fun `test with custom format`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("yyyy/MM/dd"), context).toString()

        assertTrue(result.matches(Regex("\\d{4}/\\d{2}/\\d{2}")))
    }
}

