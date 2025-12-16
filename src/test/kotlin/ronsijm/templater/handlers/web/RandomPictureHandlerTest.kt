package ronsijm.templater.handlers.web

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class RandomPictureHandlerTest : WebHandlerTestBase() {

    private fun createCommand() = getCommand("random_picture")

    @Test
    fun `test with default parameters`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(emptyList(), context).toString()

        assertTrue(result.contains("https://source.unsplash.com/random/1600x900"))
        assertTrue(result.startsWith("!["))
    }

    @Test
    fun `test with custom size`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("800x600"), context).toString()

        assertTrue(result.contains("https://source.unsplash.com/random/800x600"))
    }

    @Test
    fun `test with query`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("1600x900", "nature"), context).toString()

        assertTrue(result.contains("nature"))
    }

    @Test
    fun `test with include size`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("800x600", "nature", "true"), context).toString()

        assertTrue(result.contains("|800x600"))
    }

    @Test
    fun `test with invalid size format uses default`() {
        val command = createCommand()
        val context = createContext()

        val result = command.execute(listOf("invalid"), context).toString()

        assertTrue(result.contains("https://source.unsplash.com/random/1600x900"))
    }
}

