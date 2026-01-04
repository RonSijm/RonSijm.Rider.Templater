package ronsijm.templater.modules

import ronsijm.templater.TestContextFactory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ConfigModuleTest {

    @Test
    fun `test active_file returns file path`() {
        val context = TestContextFactory.create(filePath = "/path/to/myfile.md")
        val configModule = ConfigModule(context)

        val result = configModule.executeProperty("active_file")

        assertEquals("/path/to/myfile.md", result)
    }

    @Test
    fun `test run_mode returns DYNAMIC_PROCESSOR`() {
        val context = TestContextFactory.create()
        val configModule = ConfigModule(context)

        val result = configModule.executeProperty("run_mode")

        assertEquals("DYNAMIC_PROCESSOR", result)
    }

    @Test
    fun `test target_file returns file path`() {
        val context = TestContextFactory.create(filePath = "/path/to/target.md")
        val configModule = ConfigModule(context)

        val result = configModule.executeProperty("target_file")

        assertEquals("/path/to/target.md", result)
    }

    @Test
    fun `test template_file returns file path`() {
        val context = TestContextFactory.create(filePath = "/path/to/template.md")
        val configModule = ConfigModule(context)

        val result = configModule.executeProperty("template_file")

        assertEquals("/path/to/template.md", result)
    }

    @Test
    fun `test active_file with null path returns empty string`() {
        val context = TestContextFactory.create(filePath = "")
        val configModule = ConfigModule(context)

        val result = configModule.executeProperty("active_file")

        assertEquals("", result)
    }

    @Test
    fun `test unknown property returns null`() {
        val context = TestContextFactory.create()
        val configModule = ConfigModule(context)

        val result = configModule.executeProperty("unknown")

        assertNull(result)
    }

    @Test
    fun `test all properties return consistent values`() {
        val context = TestContextFactory.create(filePath = "/consistent/path.md")
        val configModule = ConfigModule(context)

        val activeFile = configModule.executeProperty("active_file")
        val targetFile = configModule.executeProperty("target_file")
        val templateFile = configModule.executeProperty("template_file")


        assertEquals(activeFile, targetFile)
        assertEquals(targetFile, templateFile)
    }
}
