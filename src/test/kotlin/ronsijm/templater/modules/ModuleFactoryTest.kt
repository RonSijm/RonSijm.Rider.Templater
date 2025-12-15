package ronsijm.templater.modules

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.services.ServiceContainer

/**
 * Tests for ModuleFactory and HandlerRegistry
 *
 * Note: DateModule, FileModule, SystemModule, and WebModule have been eliminated.
 * Their functionality is now provided directly by HandlerRegistry.
 * ModuleFactory only creates non-command-based modules.
 */
class ModuleFactoryTest {

    @Test
    fun `test factory creates non-command modules`() {
        val context = TestContextFactory.create()
        val services = ServiceContainer.createForTesting()
        val factory = ModuleFactory(context, services)

        // Non-command-based modules are still created by factory
        assertNotNull(factory.getFrontmatterModule())
        assertNotNull(factory.getHooksModule())
        assertNotNull(factory.getConfigModule())
    }

    @Test
    fun `test factory returns same instance on multiple calls`() {
        val context = TestContextFactory.create()
        val services = ServiceContainer.createForTesting()
        val factory = ModuleFactory(context, services)

        val frontmatterModule1 = factory.getFrontmatterModule()
        val frontmatterModule2 = factory.getFrontmatterModule()
        assertSame(frontmatterModule1, frontmatterModule2, "FrontmatterModule should be singleton")

        val hooksModule1 = factory.getHooksModule()
        val hooksModule2 = factory.getHooksModule()
        assertSame(hooksModule1, hooksModule2, "HooksModule should be singleton")

        val configModule1 = factory.getConfigModule()
        val configModule2 = factory.getConfigModule()
        assertSame(configModule1, configModule2, "ConfigModule should be singleton")
    }

    @Test
    fun `test factory without project has no AppModule`() {
        val context = TestContextFactory.create()
        val services = ServiceContainer.createForTesting()
        val factory = ModuleFactory(context, services, project = null)

        assertFalse(factory.hasAppModule())
        assertNull(factory.getAppModule())
    }

    @Test
    fun `test HandlerRegistry has all command modules`() {
        // Command-based modules are now in HandlerRegistry
        assertTrue(HandlerRegistry.dateCommands.isNotEmpty(), "Date commands should exist")
        assertTrue(HandlerRegistry.fileCommands.isNotEmpty(), "File commands should exist")
        assertTrue(HandlerRegistry.systemCommands.isNotEmpty(), "System commands should exist")
        assertTrue(HandlerRegistry.webCommands.isNotEmpty(), "Web commands should exist")
    }

    @Test
    fun `test HandlerRegistry can execute commands`() {
        val context = TestContextFactory.create()

        // Test date module
        val dateResult = HandlerRegistry.executeCommand("date", "now", emptyList(), context)
        assertNotNull(dateResult)

        // Test file module
        val fileResult = HandlerRegistry.executeCommand("file", "name", emptyList(), context)
        assertNotNull(fileResult)
    }

    @Test
    fun `test non-command modules can execute`() {
        val context = TestContextFactory.create()
        val services = ServiceContainer.createForTesting()
        val factory = ModuleFactory(context, services)

        // Test FrontmatterModule
        val frontmatterResult = factory.getFrontmatterModule().getValue(emptyList())
        assertNull(frontmatterResult) // Empty frontmatter returns null

        // Test ConfigModule
        val configResult = factory.getConfigModule().executeProperty("run_mode")
        assertNotNull(configResult)
    }
}

