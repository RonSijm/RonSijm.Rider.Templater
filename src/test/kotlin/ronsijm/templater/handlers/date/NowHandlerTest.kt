package ronsijm.templater.handlers.date

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.ResultAssertions.assertResultEquals
import java.time.LocalDate

class NowHandlerTest : DateHandlerTestBase() {

    private fun createCommand() = getCommand("now")

    @Test
    fun `test with YYYY format returns year only`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("YYYY"), context)

        result.shouldBeSuccess()
        assertResultEquals(LocalDate.now().year.toString(), result)
    }

    @Test
    fun `test with MM format returns month only`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("MM"), context)

        result.shouldBeSuccess()
        val expected = String.format("%02d", LocalDate.now().monthValue)
        assertResultEquals(expected, result)
    }

    @Test
    fun `test with DD format returns day only`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("DD"), context)

        result.shouldBeSuccess()
        val expected = String.format("%02d", LocalDate.now().dayOfMonth)
        assertResultEquals(expected, result)
    }
}

private fun ronsijm.templater.handlers.CommandResult.shouldBeSuccess() {
    assertTrue(this.isSuccess, "Expected successful result but got $this")
}

