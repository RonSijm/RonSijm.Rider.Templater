package ronsijm.templater.modules.web

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.services.ServiceContainer

class WebCommandsTest {

    private fun getCommand(name: String): Command {
        return HandlerRegistry.commandsByModule["web"]?.get(name)
            ?: throw IllegalArgumentException("Command web.$name not found")
    }

    private fun createRandomPictureCommand(): Command = getCommand("random_picture")
    private fun createRequestCommand(): Command = getCommand("request")

    @Test
    fun `test RandomPictureCommand with default parameters`() {
        val command = createRandomPictureCommand()
        val context = TestContextFactory.create()

        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        assertTrue(result!!.contains("https://source.unsplash.com/random/1600x900"))
        assertTrue(result.startsWith("!["))
    }

    @Test
    fun `test RandomPictureCommand with custom size`() {
        val command = createRandomPictureCommand()
        val context = TestContextFactory.create()

        val result = command.execute(listOf("800x600"), context)

        assertNotNull(result)
        assertTrue(result!!.contains("https://source.unsplash.com/random/800x600"))
    }

    @Test
    fun `test RandomPictureCommand with query`() {
        val command = createRandomPictureCommand()
        val context = TestContextFactory.create()

        val result = command.execute(listOf("1600x900", "nature"), context)

        assertNotNull(result)
        assertTrue(result!!.contains("nature"))
    }

    @Test
    fun `test RandomPictureCommand with include size`() {
        val command = createRandomPictureCommand()
        val context = TestContextFactory.create()

        val result = command.execute(listOf("800x600", "nature", "true"), context)

        assertNotNull(result)
        assertTrue(result!!.contains("|800x600"))
    }

    @Test
    fun `test RandomPictureCommand with invalid size format uses default`() {
        val command = createRandomPictureCommand()
        val context = TestContextFactory.create()

        val result = command.execute(listOf("invalid"), context)

        assertNotNull(result)
        // Should fall back to default 1600x900
        assertTrue(result!!.contains("https://source.unsplash.com/random/1600x900"))
    }

    @Test
    fun `test RequestCommand without URL returns error`() {
        val httpService = ronsijm.templater.services.DefaultHttpService()
        val services = ServiceContainer(httpService = httpService)
        val command = createRequestCommand()
        val context = TestContextFactory.create(services = services)

        val result = command.execute(emptyList(), context)

        assertNotNull(result)
        assertTrue(result!!.contains("HTTP request failed"))
        assertTrue(result.contains("URL required"))
    }

    @Test
    fun `test RequestCommand with null URL returns error`() {
        val httpService = ronsijm.templater.services.DefaultHttpService()
        val services = ServiceContainer(httpService = httpService)
        val command = createRequestCommand()
        val context = TestContextFactory.create(services = services)

        val result = command.execute(listOf(null), context)

        assertNotNull(result)
        assertTrue(result!!.contains("HTTP request failed"))
        assertTrue(result.contains("URL required"))
    }
}

