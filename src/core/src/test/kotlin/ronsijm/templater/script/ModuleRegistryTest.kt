package ronsijm.templater.script

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ModuleRegistryTest {

    @Test
    fun `test execute date module function`() {

        val registry = TestContextFactory.createRealModuleRegistry()

        val result = registry.executeFunction("tp.date.now", listOf("yyyy-MM-dd"))

        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test execute file module function`() {
        val registry = TestContextFactory.createRealModuleRegistry()

        val result = registry.executeFunction("tp.file.title", emptyList())

        assertNotNull(result)
        assertEquals("test", result)
    }

    @Test
    fun `test execute file path function`() {
        val registry = TestContextFactory.createRealModuleRegistry()

        val result = registry.executeFunction("tp.file.path", listOf(true))

        assertNotNull(result)
        assertEquals("/test/path/test.md", result)
    }

    @Test
    fun `test execute frontmatter access`() {
        val registry = TestContextFactory.createRealModuleRegistry(
            frontmatter = mapOf("title" to "My Document", "author" to "John Doe")
        )

        val result = registry.executeFunction("tp.frontmatter.title", emptyList())

        assertNotNull(result)
        assertEquals("My Document", result)
    }

    @Test
    fun `test execute nested frontmatter access`() {
        val registry = TestContextFactory.createRealModuleRegistry(
            frontmatter = mapOf("metadata" to mapOf("version" to "1.0"))
        )

        val result = registry.executeFunction("tp.frontmatter.metadata.version", emptyList())

        assertNotNull(result)
        assertEquals("1.0", result)
    }

    @Test
    fun `test execute system module function with callback`() {
        val mockService = ronsijm.templater.services.mock.MockSystemOperationsService(promptResponse = "User Response")
        val services = ronsijm.templater.services.ServiceContainer.createForTesting(systemOperationsService = mockService)
        val registry = TestContextFactory.createRealModuleRegistry(services = services)

        val result = registry.executeFunction("tp.system.prompt", listOf("Enter name"))

        assertNotNull(result)
        assertEquals("User Response", result)
    }

    @Test
    fun `test execute unknown module returns null`() {
        val registry = TestContextFactory.createRealModuleRegistry()

        val result = registry.executeFunction("tp.unknown.function", emptyList())


        assertNull(result)
    }

    @Test
    fun `test execute unknown function returns null`() {
        val registry = TestContextFactory.createRealModuleRegistry()

        val result = registry.executeFunction("tp.date.unknownFunction", emptyList())


        assertNull(result)
    }

    @Test
    fun `test execute date tomorrow function`() {
        val registry = TestContextFactory.createRealModuleRegistry()

        val result = registry.executeFunction("tp.date.tomorrow", listOf("yyyy-MM-dd"))

        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test execute date yesterday function`() {
        val registry = TestContextFactory.createRealModuleRegistry()

        val result = registry.executeFunction("tp.date.yesterday", listOf("yyyy-MM-dd"))

        assertNotNull(result)
        assertTrue(result is String)
        assertTrue((result as String).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `test execute file folder function`() {
        val registry = TestContextFactory.createRealModuleRegistry()


        val result = registry.executeFunction("tp.file.folder", emptyList())
        assertNotNull(result)
        assertEquals("path", result)


        val resultRelative = registry.executeFunction("tp.file.folder", listOf(true))
        assertNotNull(resultRelative)
        assertEquals("/test/path", resultRelative)
    }

    @Test
    fun `test frontmatter non-existent key returns null`() {
        val registry = TestContextFactory.createRealModuleRegistry(
            frontmatter = mapOf("title" to "Test")
        )

        val result = registry.executeFunction("tp.frontmatter.nonExistent", emptyList())

        assertNull(result)
    }
}
