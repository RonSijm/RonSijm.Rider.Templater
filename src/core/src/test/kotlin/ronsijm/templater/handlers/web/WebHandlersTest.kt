package ronsijm.templater.handlers.web

import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class WebHandlersTest {

    private fun executeWeb(command: String, args: List<Any?> = emptyList(), context: TemplateContext): String {
        return HandlerRegistry.executeCommand("web", command, args, context).toString()
    }

    @Test
    fun `test daily_quote returns formatted quote`() {
        val mockHttpService = ronsijm.templater.services.mock.MockHttpService()
        mockHttpService.addResponse(
            "https://api.quotable.io/random",
            """{"content": "Test quote", "author": "Test Author"}"""
        )
        val services = ServiceContainer.createForTesting(httpService = mockHttpService)
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("daily_quote", context = context)

        assertTrue(result.contains("> [!quote] Daily Quote"))
        assertTrue(result.contains("Test quote"))
        assertTrue(result.contains("Test Author"))
    }

    @Test
    fun `test random_picture with default size`() {
        val services = ServiceContainer.createForTesting()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("random_picture", context = context)

        assertTrue(result.startsWith("!["))
        assertTrue(result.contains("source.unsplash.com"))
    }

    @Test
    fun `test random_picture with custom size`() {
        val services = ServiceContainer.createForTesting()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("random_picture", listOf("200x200"), context)

        assertTrue(result.contains("200x200"))
    }

    @Test
    fun `test random_picture with query`() {
        val services = ServiceContainer.createForTesting()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("random_picture", listOf("200x200", "landscape,water"), context)

        assertTrue(result.contains("landscape,water"))
    }

    @Test
    fun `test random_picture with include_size`() {
        val services = ServiceContainer.createForTesting()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("random_picture", listOf("200x200", "", "true"), context)

        assertTrue(result.contains("|200x200"))
    }

    @Test
    fun `test request with valid URL`() {
        val context = TestContextFactory.create()


        val result = executeWeb("request", listOf("https://jsonplaceholder.typicode.com/todos/1"), context)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `test request with JSON path`() {
        val context = TestContextFactory.create()


        val result = executeWeb("request", listOf("https://jsonplaceholder.typicode.com/todos/1", "title"), context)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `test request with array JSON path`() {
        val context = TestContextFactory.create()


        val result = executeWeb("request", listOf("https://jsonplaceholder.typicode.com/todos", "0.title"), context)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `test request without URL returns error`() {
        val context = TestContextFactory.create()

        val result = executeWeb("request", emptyList(), context)


        assertTrue(result.contains("Error") && result.contains("URL required"))
    }

    @Test
    fun `test unknown command returns error`() {
        val context = TestContextFactory.create()

        val result = executeWeb("unknown", context = context)


        assertTrue(result.contains("Unknown function"))
    }
}
