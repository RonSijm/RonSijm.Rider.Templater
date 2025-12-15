package ronsijm.templater.modules.date

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.generated.HandlerRegistry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateCommandsTest {

    private fun getCommand(name: String): Command {
        return HandlerRegistry.commandsByModule["date"]?.get(name)
            ?: throw IllegalArgumentException("Command date.$name not found")
    }

    private fun createTodayCommand(): Command = getCommand("today")
    private fun createTomorrowCommand(): Command = getCommand("tomorrow")
    private fun createYesterdayCommand(): Command = getCommand("yesterday")
    private fun createWeekdayCommand(): Command = getCommand("weekday")

    @Test
    fun `test TodayCommand with default format`() {
        val command = createTodayCommand()
        val context = TestContextFactory.create()

        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        // Should match yyyy-MM-dd format
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        // Should be today's date
        val expected = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, result)
    }

    @Test
    fun `test TodayCommand with custom format`() {
        val command = createTodayCommand()
        val context = TestContextFactory.create()

        val result = command.execute(listOf("dd/MM/yyyy"), context)

        assertNotNull(result)
        // Should match dd/MM/yyyy format
        assertTrue(result!!.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
    }

    @Test
    fun `test TomorrowCommand with default format`() {
        val command = createTomorrowCommand()
        val context = TestContextFactory.create()

        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        // Should match yyyy-MM-dd format
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        // Should be tomorrow's date
        val expected = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, result)
    }

    @Test
    fun `test TomorrowCommand with custom format`() {
        val command = createTomorrowCommand()
        val context = TestContextFactory.create()

        val result = command.execute(listOf("MMMM dd, yyyy"), context)

        assertNotNull(result)
        // Should contain month name
        assertTrue(result!!.matches(Regex("[A-Za-z]+ \\d{2}, \\d{4}")))
    }

    @Test
    fun `test YesterdayCommand with default format`() {
        val command = createYesterdayCommand()
        val context = TestContextFactory.create()

        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        // Should match yyyy-MM-dd format
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

        // Should be yesterday's date
        val expected = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        assertEquals(expected, result)
    }

    @Test
    fun `test YesterdayCommand with custom format`() {
        val command = createYesterdayCommand()
        val context = TestContextFactory.create()

        val result = command.execute(listOf("yyyy/MM/dd"), context)

        assertNotNull(result)
        // Should match yyyy/MM/dd format
        assertTrue(result!!.matches(Regex("\\d{4}/\\d{2}/\\d{2}")))
    }

    @Test
    fun `test WeekdayCommand with default format`() {
        val command = createWeekdayCommand()
        val context = TestContextFactory.create()

        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        // Default format is yyyy-MM-dd, so should match that pattern
        assertTrue(result!!.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test WeekdayCommand with custom format`() {
        val command = createWeekdayCommand()
        val context = TestContextFactory.create()

        val result = command.execute(listOf("EEE"), context)

        assertNotNull(result)
        // Should be a 3-letter day abbreviation
        val validDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        assertTrue(validDays.contains(result))
    }

    @Test
    fun `test WeekdayCommand with offset`() {
        val command = createWeekdayCommand()
        val context = TestContextFactory.create()

        // Get weekday for tomorrow
        val result = command.execute(listOf("EEEE", "P1D"), context)

        assertNotNull(result)
        // Should be a day name
        val validDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        assertTrue(validDays.contains(result))
    }

    @Test
    fun `test NowCommand with YYYY format returns year only`() {
        val command = getCommand("now")
        val context = TestContextFactory.create()

        val result = command.execute(listOf("YYYY"), context)

        assertNotNull(result)
        // Should be just the year (4 digits)
        assertEquals(LocalDate.now().year.toString(), result)
    }

    @Test
    fun `test NowCommand with MM format returns month only`() {
        val command = getCommand("now")
        val context = TestContextFactory.create()

        val result = command.execute(listOf("MM"), context)

        assertNotNull(result)
        // Should be just the month (2 digits)
        val expected = String.format("%02d", LocalDate.now().monthValue)
        assertEquals(expected, result)
    }

    @Test
    fun `test NowCommand with DD format returns day only`() {
        val command = getCommand("now")
        val context = TestContextFactory.create()

        val result = command.execute(listOf("DD"), context)

        assertNotNull(result)
        // Should be just the day (2 digits)
        val expected = String.format("%02d", LocalDate.now().dayOfMonth)
        assertEquals(expected, result)
    }
}

