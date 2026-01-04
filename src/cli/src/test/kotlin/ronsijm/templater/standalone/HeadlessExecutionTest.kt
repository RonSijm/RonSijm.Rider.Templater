package ronsijm.templater.standalone

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.io.TempDir
import ronsijm.templater.standalone.services.TemplateExecutionService
import ronsijm.templater.standalone.settings.AppSettings
import java.io.File
import java.nio.file.Path


class HeadlessExecutionTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var originalBehavior: AppSettings.AfterRunningBehavior
    private lateinit var originalPostfix: String

    @BeforeEach
    fun setup() {

        originalBehavior = AppSettings.getAfterRunningBehavior()
        originalPostfix = AppSettings.getSideBySidePostfix()
    }

    @AfterEach
    fun tearDown() {

        AppSettings.setAfterRunningBehavior(originalBehavior)
        AppSettings.setSideBySidePostfix(originalPostfix)
    }

    @Test
    fun `TemplateExecutionService executes simple template`() {
        val content = "Hello World"
        val result = TemplateExecutionService.execute(content, null)

        assertTrue(result.success, "Execution should succeed")
        assertEquals("Hello World", result.output)
        assertNull(result.error)
    }

    @Test
    fun `TemplateExecutionService executes template with interpolation`() {
        val content = "Result: <% 1 + 1 %>"
        val result = TemplateExecutionService.execute(content, null)

        assertTrue(result.success, "Execution should succeed")
        assertEquals("Result: 2", result.output)
    }

    @Test
    fun `TemplateExecutionService executes template with file context`() {
        val templateFile = tempDir.resolve("test.md").toFile()
        templateFile.writeText("File: <% tp.file.title %>")

        val result = TemplateExecutionService.execute(templateFile.readText(), templateFile)

        assertTrue(result.success, "Execution should succeed")
        assertEquals("File: test", result.output)
    }

    @Test
    fun `TemplateExecutionService handles syntax errors gracefully`() {
        val content = "Invalid: <% if (true) { %>"
        val result = TemplateExecutionService.execute(content, null)



        assertNotNull(result)
    }

    @Test
    fun `TemplateExecutionService handles script errors`() {
        val content = "Error: <% undefinedVariable %>"
        val result = TemplateExecutionService.execute(content, null)


        assertNotNull(result)
    }

    @Test
    fun `headless execution with overwrite behavior`() {

        AppSettings.setAfterRunningBehavior(AppSettings.AfterRunningBehavior.OVERWRITE_AUTOMATICALLY)

        val templateFile = tempDir.resolve("overwrite_test.md").toFile()
        val originalContent = "Original: <% 2 + 2 %>"
        templateFile.writeText(originalContent)


        val result = TemplateExecutionService.execute(templateFile.readText(), templateFile)
        assertTrue(result.success)
        assertEquals("Original: 4", result.output)


        templateFile.writeText(result.output)


        assertEquals("Original: 4", templateFile.readText())
    }

    @Test
    fun `headless execution with side-by-side behavior`() {

        AppSettings.setAfterRunningBehavior(AppSettings.AfterRunningBehavior.SAVE_SIDE_BY_SIDE)
        AppSettings.setSideBySidePostfix("_output")

        val templateFile = tempDir.resolve("sidebyside_test.md").toFile()
        val originalContent = "Template: <% 3 * 3 %>"
        templateFile.writeText(originalContent)


        val result = TemplateExecutionService.execute(templateFile.readText(), templateFile)
        assertTrue(result.success)
        assertEquals("Template: 9", result.output)


        val postfix = AppSettings.getSideBySidePostfix()
        val outputFile = File(
            templateFile.parentFile,
            templateFile.nameWithoutExtension + postfix + "." + templateFile.extension
        )
        outputFile.writeText(result.output)


        assertEquals(originalContent, templateFile.readText())


        assertTrue(outputFile.exists())
        assertEquals("Template: 9", outputFile.readText())
    }

    @Test
    fun `template with frontmatter executes correctly`() {
        val content = """
            ---
            title: Test Document
            ---
            Title is: <% tp.frontmatter.title %>
        """.trimIndent()

        val result = TemplateExecutionService.execute(content, null)

        assertTrue(result.success, "Execution should succeed")
        assertTrue(result.output.contains("Title is: Test Document"))
    }

    @Test
    fun `template with date function executes`() {
        val content = "Year: <% tp.date.now('yyyy') %>"
        val result = TemplateExecutionService.execute(content, null)

        assertTrue(result.success, "Execution should succeed")

        assertTrue(result.output.matches(Regex("Year: \\d{4}")))
    }
}

