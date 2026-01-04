package ronsijm.templater.standalone.modules

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ronsijm.templater.cli.filesystem.JavaFileHandle
import ronsijm.templater.cli.filesystem.JavaFileSystem
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer
import java.io.File

class CliAppModuleTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var appModule: CliAppModule
    private lateinit var fileSystem: JavaFileSystem
    private lateinit var context: TemplateContext
    private var activeFile: JavaFileHandle? = null

    @BeforeEach
    fun setUp() {
        fileSystem = JavaFileSystem(tempDir)
        val services = ServiceContainer.createForTesting()
        context = TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test",
            filePath = tempDir.absolutePath,
            fileContent = "",
            services = services
        )
        appModule = CliAppModule(context, fileSystem) { activeFile }
    }

    @Test
    fun `vault getAllLoadedFiles returns empty for empty directory`() {
        val files = appModule.vault.getAllLoadedFiles()
        assertTrue(files.isEmpty())
    }

    @Test
    fun `vault getAllLoadedFiles returns files`() {
        File(tempDir, "test1.md").writeText("content1")
        File(tempDir, "test2.txt").writeText("content2")

        appModule.vault.invalidateCache()
        val files = appModule.vault.getAllLoadedFiles()

        assertEquals(2, files.size)
    }

    @Test
    fun `vault getMarkdownFiles filters by extension`() {
        File(tempDir, "test1.md").writeText("content1")
        File(tempDir, "test2.txt").writeText("content2")
        File(tempDir, "test3.md").writeText("content3")

        appModule.vault.invalidateCache()
        val mdFiles = appModule.vault.getMarkdownFiles()

        assertEquals(2, mdFiles.size)
        assertTrue(mdFiles.all { it.extension == "md" })
    }

    @Test
    fun `vault getAbstractFileByPath finds existing file`() {
        val testFile = File(tempDir, "test.md")
        testFile.writeText("content")

        val found = appModule.vault.getAbstractFileByPath("test.md")

        assertNotNull(found)
        assertEquals("test.md", found!!.name)
    }

    @Test
    fun `vault getAbstractFileByPath returns null for non-existing file`() {
        val found = appModule.vault.getAbstractFileByPath("nonexistent.md")
        assertNull(found)
    }

    @Test
    fun `vault create creates new file`() {
        val created = appModule.vault.create("new.md", "new content")

        assertNotNull(created)
        assertTrue(File(tempDir, "new.md").exists())
        assertEquals("new content", File(tempDir, "new.md").readText())
    }

    @Test
    fun `vault read returns file content`() {
        val testFile = File(tempDir, "test.md")
        testFile.writeText("test content")

        val handle = JavaFileHandle(testFile)
        val content = appModule.vault.read(handle)

        assertEquals("test content", content)
    }

    @Test
    fun `vault modify updates file content`() {
        val testFile = File(tempDir, "test.md")
        testFile.writeText("original")

        val handle = JavaFileHandle(testFile)
        appModule.vault.modify(handle, "modified")

        assertEquals("modified", testFile.readText())
    }

    @Test
    fun `vault delete removes file`() {
        val testFile = File(tempDir, "test.md")
        testFile.writeText("content")
        assertTrue(testFile.exists())

        val handle = JavaFileHandle(testFile)
        appModule.vault.delete(handle)

        assertFalse(testFile.exists())
    }

    @Test
    fun `workspace getActiveFile returns null when no active file`() {
        assertNull(appModule.workspace.getActiveFile())
    }

    @Test
    fun `workspace getActiveFile returns active file when set`() {
        val testFile = File(tempDir, "active.md")
        testFile.writeText("content")
        activeFile = JavaFileHandle(testFile)

        val result = appModule.workspace.getActiveFile()

        assertNotNull(result)
        assertEquals("active.md", result!!.name)
    }

    @Test
    fun `fileManager renameFile delegates to vault`() {
        val testFile = File(tempDir, "original.md")
        testFile.writeText("content")

        val handle = JavaFileHandle(testFile)
        val renamed = appModule.fileManager.renameFile(handle, "renamed.md")

        assertNotNull(renamed)
        assertTrue(File(tempDir, "renamed.md").exists())
    }

    @Test
    fun `commands executeCommandById does not throw`() {
        assertDoesNotThrow {
            appModule.commands.executeCommandById("some.command")
        }
    }
}

