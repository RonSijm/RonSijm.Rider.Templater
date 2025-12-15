package ronsijm.templater.modules

import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Tests for web module commands using HandlerRegistry
 */
class WebModuleTest {

    private fun executeWeb(command: String, args: List<Any?> = emptyList(), context: TemplateContext): String? {
        return HandlerRegistry.executeCommand("web", command, args, context)
    }

    @Test
    fun `test daily_quote returns formatted quote`() {
        val services = ServiceContainer.createDefault()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("daily_quote", context = context)

        assertNotNull(result)
        assertTrue(result!!.contains("> [!quote] Daily Quote"))
    }

    @Test
    fun `test random_picture with default size`() {
        val services = ServiceContainer.createDefault()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("random_picture", context = context)

        assertNotNull(result)
        assertTrue(result!!.startsWith("!["))
        assertTrue(result.contains("source.unsplash.com"))
    }

    @Test
    fun `test random_picture with custom size`() {
        val services = ServiceContainer.createDefault()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("random_picture", listOf("200x200"), context)

        assertNotNull(result)
        assertTrue(result!!.contains("200x200"))
    }

    @Test
    fun `test random_picture with query`() {
        val services = ServiceContainer.createDefault()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("random_picture", listOf("200x200", "landscape,water"), context)

        assertNotNull(result)
        assertTrue(result!!.contains("landscape,water"))
    }

    @Test
    fun `test random_picture with include_size`() {
        val services = ServiceContainer.createDefault()
        val context = TestContextFactory.create(services = services)

        val result = executeWeb("random_picture", listOf("200x200", "", "true"), context)

        assertNotNull(result)
        assertTrue(result!!.contains("|200x200"))
    }

    @Test
    fun `test request with valid URL`() {
        val context = TestContextFactory.create()

        // Using a reliable test API
        val result = executeWeb("request", listOf("https://jsonplaceholder.typicode.com/todos/1"), context)

        assertNotNull(result)
        assertTrue(result!!.isNotEmpty())
    }

    @Test
    fun `test request with JSON path`() {
        val context = TestContextFactory.create()

        // Using a reliable test API with JSON path
        val result = executeWeb("request", listOf("https://jsonplaceholder.typicode.com/todos/1", "title"), context)

        assertNotNull(result)
        assertTrue(result!!.isNotEmpty())
    }

    @Test
    fun `test request with array JSON path`() {
        val context = TestContextFactory.create()

        // Using a reliable test API with array path
        val result = executeWeb("request", listOf("https://jsonplaceholder.typicode.com/todos", "0.title"), context)

        assertNotNull(result)
        assertTrue(result!!.isNotEmpty())
    }

    @Test
    fun `test request without URL returns error`() {
        val context = TestContextFactory.create()

        val result = executeWeb("request", emptyList(), context)

        assertNotNull(result)
        assertTrue(result!!.contains("HTTP request failed"))
    }

    @Test
    fun `test unknown command returns null`() {
        val context = TestContextFactory.create()

        val result = executeWeb("unknown", context = context)

        assertNull(result)
    }
}

