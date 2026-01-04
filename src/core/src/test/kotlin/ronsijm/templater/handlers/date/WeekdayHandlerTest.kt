package ronsijm.templater.handlers.date

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class WeekdayHandlerTest : DateHandlerTestBase() {

    private fun createCommand() = getCommand("weekday")

    @Test
    fun `test with default format`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context).toString()

        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test with custom format`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("EEE"), context).toString()

        val validDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        assertTrue(validDays.contains(result))
    }

    @Test
    fun `test with offset`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("EEEE", "P1D"), context).toString()

        val validDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        assertTrue(validDays.contains(result))
    }
}
