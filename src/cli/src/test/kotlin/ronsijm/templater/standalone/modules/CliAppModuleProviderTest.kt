package ronsijm.templater.standalone.modules

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ronsijm.templater.cli.filesystem.JavaFileHandle
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer
import java.io.File

class CliAppModuleProviderTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var context: TemplateContext

    @BeforeEach
    fun setUp() {
        val services = ServiceContainer.createForTesting()
        context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test",
            filePath = tempDir.absolutePath,
            fileContent = "",
            services = services
        )
    }

    @Test
    fun `provider hasAppModule returns true`() {
        val provider = CliAppModuleProvider(context, tempDir)
        assertTrue(provider.hasAppModule())
    }

    @Test
    fun `provider getAppModule returns non-null`() {
        val provider = CliAppModuleProvider(context, tempDir)
        assertNotNull(provider.getAppModule())
    }

    @Test
    fun `provider getAppModule returns CliAppModule`() {
        val provider = CliAppModuleProvider(context, tempDir)
        assertTrue(provider.getAppModule() is CliAppModule)
    }

    @Test
    fun `provider getTypedAppModule returns same instance`() {
        val provider = CliAppModuleProvider(context, tempDir)
        val module1 = provider.getTypedAppModule()
        val module2 = provider.getTypedAppModule()
        assertSame(module1, module2)
    }

    @Test
    fun `fromPath creates provider from directory`() {
        val provider = CliAppModuleProvider.fromPath(context, tempDir.absolutePath)
        assertTrue(provider.hasAppModule())
    }

    @Test
    fun `fromPath creates provider from file path`() {
        val testFile = File(tempDir, "test.md")
        testFile.writeText("content")

        val provider = CliAppModuleProvider.fromPath(context, testFile.absolutePath)
        assertTrue(provider.hasAppModule())
    }

    @Test
    fun `withActiveFile creates provider with active file callback`() {
        val testFile = File(tempDir, "active.md")
        testFile.writeText("content")
        val handle = JavaFileHandle(testFile)

        val provider = CliAppModuleProvider.withActiveFile(context, tempDir) { handle }
        val module = provider.getTypedAppModule()

        val activeFile = module.workspace.getActiveFile()
        assertNotNull(activeFile)
        assertEquals("active.md", activeFile!!.name)
    }

    @Test
    fun `provider vault operations work`() {
        val provider = CliAppModuleProvider(context, tempDir)
        val module = provider.getTypedAppModule()


        val created = module.vault.create("test.md", "content")
        assertNotNull(created)


        val found = module.vault.getAbstractFileByPath("test.md")
        assertNotNull(found)


        val content = module.vault.read(found!!)
        assertEquals("content", content)
    }
}

