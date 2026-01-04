package ronsijm.templater.handlers.date

import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.generated.HandlerRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate


class DateHandlersTest {

    private fun executeDate(command: String, args: List<Any?> = emptyList()): String {
        val context = TestContextFactory.create()
        return HandlerRegistry.executeCommand("date", command, args, context).toString()
    }

    @Test
    fun `test now with no arguments`() {
        val result = executeDate("now")

        println("now() = $result")

        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `test now with simple date format`() {
        val result = executeDate("now", listOf("yyyy-MM-dd"))

        println("now(\"yyyy-MM-dd\") = $result")
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test now with time format`() {
        val result = executeDate("now", listOf("HH:mm:ss"))

        println("now(\"HH:mm:ss\") = $result")
        assertTrue(result.matches(Regex("\\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `test today with no arguments`() {
        val result = executeDate("today")

        println("today() = $result")
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test today with custom format`() {
        val result = executeDate("today", listOf("MM/dd/yyyy"))

        println("today(\"MM/dd/yyyy\") = $result")
        assertTrue(result.matches(Regex("\\d{2}/\\d{2}/\\d{4}")))
    }

    @Test
    fun `test tomorrow with no arguments`() {
        val result = executeDate("tomorrow")

        println("tomorrow() = $result")
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))


        val tomorrow = LocalDate.now().plusDays(1).toString()
        assertEquals(tomorrow, result)
    }

    @Test
    fun `test yesterday with no arguments`() {
        val result = executeDate("yesterday")

        println("yesterday() = $result")
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))


        val yesterday = LocalDate.now().minusDays(1).toString()
        assertEquals(yesterday, result)
    }

    @Test
    fun `test weekday function`() {
        val result = executeDate("weekday", listOf("yyyy-MM-dd", "0"))

        println("weekday(\"yyyy-MM-dd\", 0) = $result")
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test Moment js YYYY format`() {
        val result = executeDate("now", listOf("YYYY"))

        println("now(\"YYYY\") = $result")
        assertTrue(result.matches(Regex("\\d{4}")))
    }

    @Test
    fun `test Moment js YYYY-MM-DD format`() {
        val result = executeDate("now", listOf("YYYY-MM-DD"))

        println("now(\"YYYY-MM-DD\") = $result")
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test Moment js full datetime format`() {
        val result = executeDate("now", listOf("YYYY-MM-DD HH:mm:ss"))

        println("now(\"YYYY-MM-DD HH:mm:ss\") = $result")
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
    }

    @Test
    fun `test Moment js 12-hour time with AM PM`() {
        val result = executeDate("now", listOf("h:mm:ss a"))

        println("now(\"h:mm:ss a\") = $result")
        assertTrue(result.matches(Regex("\\d{1,2}:\\d{2}:\\d{2} (AM|PM)")))
    }
}
