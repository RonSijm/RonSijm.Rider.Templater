package ronsijm.templater.modules

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.services.mock.NullAppModuleProvider


class ModuleFactoryTest {

    @Test
    fun `test factory creates non-command modules`() {
        val context = TestContextFactory.create()
        val factory = ModuleFactory(context)


        assertNotNull(factory.getFrontmatterModule())
        assertNotNull(factory.getHooksModule())
        assertNotNull(factory.getConfigModule())
    }

    @Test
    fun `test factory returns same instance on multiple calls`() {
        val context = TestContextFactory.create()
        val factory = ModuleFactory(context)

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
    fun `test factory without appModuleProvider has no AppModule`() {
        val context = TestContextFactory.create()
        val factory = ModuleFactory(context, appModuleProvider = NullAppModuleProvider)

        assertFalse(factory.hasAppModule())
        assertNull(factory.getAppModule())
    }

    @Test
    fun `test HandlerRegistry has all command modules`() {
        assertTrue(HandlerRegistry.allModules["date"]?.isNotEmpty() == true, "Date commands should exist")
        assertTrue(HandlerRegistry.allModules["file"]?.isNotEmpty() == true, "File commands should exist")
        assertTrue(HandlerRegistry.allModules["system"]?.isNotEmpty() == true, "System commands should exist")
        assertTrue(HandlerRegistry.allModules["web"]?.isNotEmpty() == true, "Web commands should exist")
    }

    @Test
    fun `test HandlerRegistry can execute commands`() {
        val context = TestContextFactory.create()


        val dateResult = HandlerRegistry.executeCommand("date", "now", emptyList(), context)
        assertNotNull(dateResult)


        val fileResult = HandlerRegistry.executeCommand("file", "name", emptyList(), context)
        assertNotNull(fileResult)
    }

    @Test
    fun `test non-command modules can execute`() {
        val context = TestContextFactory.create()
        val factory = ModuleFactory(context)


        val frontmatterResult = factory.getFrontmatterModule().getValue(emptyList())
        assertNull(frontmatterResult)


        val configResult = factory.getConfigModule().executeProperty("run_mode")
        assertNotNull(configResult)
    }
}
